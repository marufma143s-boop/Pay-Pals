package com.example.screens.support

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.model.SupportChatMessage
import com.example.repository.AppRepository
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen
import com.example.utils.AudioPlayerManager
import com.example.utils.AudioRecordingManager
import com.example.utils.Base64OrResourceImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveChatScreen(
    repository: AppRepository,
    targetUserId: String,
    targetUserName: String,
    targetUserAvatar: String? = null,
    targetUserEmail: String? = null,
    targetUserPhone: String? = null,
    isAdminView: Boolean = false,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by repository.userProfile.collectAsState()
    val chatMessages by repository.currentChatMessages.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var messageInput by remember { mutableStateOf("") }
    var replyingToMessage by remember { mutableStateOf<SupportChatMessage?>(null) }
    var messageToDelete by remember { mutableStateOf<SupportChatMessage?>(null) }

    // Audio Recording
    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableIntStateOf(0) }

    // Audio Playback
    var currentlyPlayingMsgId by remember { mutableStateOf<String?>(null) }
    var isAudioPlaying by remember { mutableStateOf(false) }
    var audioProgress by remember { mutableFloatStateOf(0f) }

    val effectiveUserId = remember(targetUserId, userProfile.id) {
        if (targetUserId.isNotBlank()) targetUserId else if (userProfile.id.isNotBlank()) userProfile.id else "user_default"
    }

    // Setup Chat listener
    LaunchedEffect(effectiveUserId) {
        if (effectiveUserId.isNotBlank()) {
            repository.openChatWithUser(effectiveUserId)
        }
    }

    DisposableEffect(effectiveUserId) {
        onDispose {
            repository.closeChatWithUser()
            AudioRecordingManager.cancelRecording()
            AudioPlayerManager.stop()
        }
    }

    // Auto-scroll on new message
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    // Recording timer
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingDuration = 0
            while (isRecording) {
                delay(1000)
                recordingDuration += 1
            }
        }
    }

    // Audio Playback progress tracking
    LaunchedEffect(currentlyPlayingMsgId, isAudioPlaying) {
        while (isAudioPlaying && currentlyPlayingMsgId != null) {
            val total = AudioPlayerManager.getDuration()
            val current = AudioPlayerManager.getCurrentPosition()
            if (total > 0) {
                audioProgress = (current.toFloat() / total.toFloat()).coerceIn(0f, 1f)
            }
            delay(150)
        }
    }

    // Mic permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val success = AudioRecordingManager.startRecording(context)
            if (success) {
                isRecording = true
            } else {
                Toast.makeText(context, "Failed to start microphone recording.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Microphone access is required for voice messages.", Toast.LENGTH_LONG).show()
        }
    }

    // Delete confirmation dialog
    if (messageToDelete != null) {
        AlertDialog(
            onDismissRequest = { messageToDelete = null },
            title = { Text("Delete Message?") },
            text = { Text("Are you sure you want to delete this message for everyone?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val msg = messageToDelete
                        if (msg != null) {
                            repository.deleteSupportChatMessage(effectiveUserId, msg.id)
                        }
                        messageToDelete = null
                    }
                ) {
                    Text("Delete", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { messageToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            modifier = Modifier
                                .size(40.dp)
                                .border(1.5.dp, PurplePrimary, CircleShape),
                            color = PurplePrimary.copy(alpha = 0.15f)
                        ) {
                            Base64OrResourceImage(
                                base64Str = targetUserAvatar,
                                placeholderRes = if (isAdminView) R.drawable.img_avatar_maruf_1787554123074 else R.drawable.ic_paypulse_logo_1787554101154,
                                contentDescription = targetUserName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = targetUserName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(SuccessGreen, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isAdminView) (targetUserPhone ?: targetUserEmail ?: "Active Now") else "24/7 Live Support",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isAdminView && !targetUserEmail.isNullOrBlank()) {
                        IconButton(onClick = {
                            Toast.makeText(context, "User ID: $targetUserId\nEmail: $targetUserEmail", Toast.LENGTH_LONG).show()
                        }) {
                            Icon(Icons.Default.Info, contentDescription = "User Info", tint = PurplePrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Reply bar preview
                    AnimatedVisibility(visible = replyingToMessage != null) {
                        val reply = replyingToMessage
                        if (reply != null) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = PurplePrimary.copy(alpha = 0.08f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(3.dp)
                                                .height(28.dp)
                                                .background(PurplePrimary, RoundedCornerShape(2.dp))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Replying to ${reply.senderName}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = PurplePrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = if (reply.voiceBase64.isNotBlank()) "🎤 Voice Note (${reply.voiceDurationSeconds}s)" else reply.message,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { replyingToMessage = null },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Cancel reply", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Recording Bar OR Text Input Bar
                    if (isRecording) {
                        // Active Recording Controller
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(ErrorRed, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Recording audio: ${recordingDuration}s",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ErrorRed
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Cancel Recording
                                IconButton(onClick = {
                                    AudioRecordingManager.cancelRecording()
                                    isRecording = false
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Cancel", tint = ErrorRed)
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // Send Recording
                                IconButton(
                                    onClick = {
                                        val result = AudioRecordingManager.stopRecording()
                                        isRecording = false
                                        if (result != null) {
                                            val (base64, durationSec) = result
                                            repository.sendSupportChatMessage(
                                                targetUserId = effectiveUserId,
                                                messageText = "",
                                                voiceBase64 = base64,
                                                voiceDurationSeconds = if (durationSec > 0) durationSec else recordingDuration.coerceAtLeast(1),
                                                replyToMessageId = replyingToMessage?.id,
                                                replyToText = replyingToMessage?.message,
                                                replyToSenderName = replyingToMessage?.senderName
                                            )
                                            replyingToMessage = null
                                        } else {
                                            Toast.makeText(context, "Voice message was empty.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(PurplePrimary, CircleShape)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Send Voice", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    } else {
                        // Standard Input Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mic Button
                            IconButton(
                                onClick = {
                                    val hasMicPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasMicPermission) {
                                        val started = AudioRecordingManager.startRecording(context)
                                        if (started) {
                                            isRecording = true
                                        } else {
                                            Toast.makeText(context, "Cannot start microphone.", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(PurplePrimary.copy(alpha = 0.12f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Record voice note",
                                    tint = PurplePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Message Input
                            OutlinedTextField(
                                value = messageInput,
                                onValueChange = { messageInput = it },
                                placeholder = { Text("Write a message...") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chat_message_input"),
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PurplePrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                maxLines = 4
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Send Button
                            IconButton(
                                onClick = {
                                    if (messageInput.isNotBlank()) {
                                        val textToSend = messageInput.trim()
                                        messageInput = ""
                                        repository.sendSupportChatMessage(
                                            targetUserId = effectiveUserId,
                                            messageText = textToSend,
                                            voiceBase64 = "",
                                            voiceDurationSeconds = 0,
                                            replyToMessageId = replyingToMessage?.id,
                                            replyToText = replyingToMessage?.message,
                                            replyToSenderName = replyingToMessage?.senderName
                                        )
                                        replyingToMessage = null
                                    }
                                },
                                enabled = messageInput.isNotBlank(),
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        if (messageInput.isNotBlank()) PurplePrimary else PurplePrimary.copy(alpha = 0.3f),
                                        CircleShape
                                    )
                                    .testTag("chat_send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (chatMessages.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PurplePrimary.copy(alpha = 0.12f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.HeadsetMic,
                                contentDescription = null,
                                tint = PurplePrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isAdminView) "Start Conversation with $targetUserName" else "Welcome to PayPulse Live Support",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isAdminView) "Send a text or recorded voice note to reply to this user. All messages are stored permanently." else "You can send text messages or voice recordings at any time. Our team will assist you promptly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chatMessages, key = { it.id }) { message ->
                        val isFromMe = message.senderId == userProfile.id || (!isAdminView && (message.senderRole == "USER")) || (isAdminView && (message.senderRole == "ADMIN" || message.senderRole == "OWNER"))

                        ChatMessageBubble(
                            message = message,
                            isFromMe = isFromMe,
                            isPlaying = currentlyPlayingMsgId == message.id && isAudioPlaying,
                            playbackProgress = if (currentlyPlayingMsgId == message.id) audioProgress else 0f,
                            onPlayVoice = {
                                if (currentlyPlayingMsgId == message.id && isAudioPlaying) {
                                    AudioPlayerManager.stop()
                                    isAudioPlaying = false
                                    currentlyPlayingMsgId = null
                                } else {
                                    AudioPlayerManager.stop()
                                    currentlyPlayingMsgId = message.id
                                    isAudioPlaying = true
                                    AudioPlayerManager.playBase64Audio(
                                        context = context,
                                        messageId = message.id,
                                        base64Audio = message.voiceBase64,
                                        onCompletion = {
                                            isAudioPlaying = false
                                            currentlyPlayingMsgId = null
                                            audioProgress = 0f
                                        },
                                        onError = {
                                            isAudioPlaying = false
                                            currentlyPlayingMsgId = null
                                            audioProgress = 0f
                                            Toast.makeText(context, "Failed to play voice note.", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            },
                            onReply = {
                                replyingToMessage = message
                            },
                            onDelete = {
                                messageToDelete = message
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: SupportChatMessage,
    isFromMe: Boolean,
    isPlaying: Boolean,
    playbackProgress: Float,
    onPlayVoice: () -> Unit,
    onReply: () -> Unit,
    onDelete: () -> Unit
) {
    val bubbleShape = if (isFromMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    val bubbleBg = if (isFromMe) {
        Brush.horizontalGradient(listOf(PurplePrimary, Color(0xFF7C3AED)))
    } else {
        Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface))
    }

    val contentColor = if (isFromMe) Color.White else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
    ) {
        // Sender label for group/support clarity
        if (!isFromMe) {
            Text(
                text = "${message.senderName} (${message.senderRole})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }

        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .shadow(2.dp, bubbleShape)
                .background(bubbleBg, bubbleShape)
                .border(
                    width = if (isFromMe) 0.dp else 1.dp,
                    color = if (isFromMe) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    shape = bubbleShape
                )
                .padding(12.dp)
        ) {
            Column {
                // Quoted reply banner if present
                if (!message.replyToText.isNullOrBlank() || !message.replyToSenderName.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = if (isFromMe) Color.Black.copy(alpha = 0.2f) else PurplePrimary.copy(alpha = 0.1f)
                    ) {
                        Row(modifier = Modifier.padding(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .width(2.5.dp)
                                    .height(24.dp)
                                    .background(if (isFromMe) GoldAccent else PurplePrimary, RoundedCornerShape(1.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = message.replyToSenderName ?: "Original Message",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFromMe) GoldAccent else PurplePrimary
                                )
                                Text(
                                    text = message.replyToText ?: "Voice Note",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (isFromMe) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Body content: Voice Note OR Text Message
                if (message.voiceBase64.isNotBlank()) {
                    // Voice Note Player
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onPlayVoice,
                            modifier = Modifier
                                .size(36.dp)
                                .background(if (isFromMe) Color.White.copy(alpha = 0.25f) else PurplePrimary.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = contentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            LinearProgressIndicator(
                                progress = { if (isPlaying) playbackProgress else 0f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = if (isFromMe) GoldAccent else PurplePrimary,
                                trackColor = if (isFromMe) Color.White.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "🎤 Voice Note (${message.voiceDurationSeconds}s)",
                                style = MaterialTheme.typography.labelSmall,
                                color = contentColor.copy(alpha = 0.85f)
                            )
                        }
                    }
                } else {
                    // Text Message
                    Text(
                        text = message.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Timestamp & Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.timeFormatted.ifEmpty { "Now" },
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = contentColor.copy(alpha = 0.7f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Reply action
                        Icon(
                            imageVector = Icons.Default.Reply,
                            contentDescription = "Reply",
                            tint = contentColor.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onReply() }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Delete action
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = contentColor.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onDelete() }
                        )
                    }
                }
            }
        }
    }
}
