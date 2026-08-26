package com.example.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.SupportThread
import com.example.repository.AppRepository
import com.example.screens.support.LiveChatScreen
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen
import com.example.utils.Base64OrResourceImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLiveSupportScreen(
    repository: AppRepository,
    modifier: Modifier = Modifier
) {
    val supportThreads by repository.allSupportThreads.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var activeChatThread by remember { mutableStateOf<SupportThread?>(null) }

    if (activeChatThread != null) {
        val thread = activeChatThread!!
        LiveChatScreen(
            repository = repository,
            targetUserId = thread.userId,
            targetUserName = thread.userName.ifEmpty { "User (${thread.userId.takeLast(5)})" },
            targetUserAvatar = thread.userAvatar,
            targetUserEmail = thread.userEmail,
            targetUserPhone = thread.userPhone,
            isAdminView = true,
            onBackClick = { activeChatThread = null }
        )
        return
    }

    val filteredThreads = remember(supportThreads, searchQuery) {
        if (searchQuery.isBlank()) {
            supportThreads
        } else {
            val q = searchQuery.trim().lowercase()
            supportThreads.filter { thread ->
                thread.userName.lowercase().contains(q) ||
                thread.userEmail.lowercase().contains(q) ||
                thread.userPhone.lowercase().contains(q) ||
                thread.userId.lowercase().contains(q) ||
                thread.lastMessage.lowercase().contains(q)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Live Support Center",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${supportThreads.size} active user conversation threads",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = PurplePrimary.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HeadsetMic,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Realtime Support",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary
                    )
                }
            }
        }

        // Search Bar (Name, Phone, Email)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by Name, Phone number or Email...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = PurplePrimary)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("support_search_input"),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PurplePrimary
            )
        )

        if (filteredThreads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "No users matching '$searchQuery'" else "No support conversations yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "Try searching by another name, email, or phone number." else "When users send support messages, their conversation threads will appear here permanently.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredThreads, key = { it.userId }) { thread ->
                    SupportThreadCard(
                        thread = thread,
                        onClick = { activeChatThread = thread }
                    )
                }
            }
        }
    }
}

@Composable
fun SupportThreadCard(
    thread: SupportThread,
    onClick: () -> Unit
) {
    val formattedTime = remember(thread.lastTimestamp) {
        if (thread.lastTimestamp > 0) {
            val date = Date(thread.lastTimestamp)
            val sdf = SimpleDateFormat("hh:mm a", Locale.US)
            sdf.format(date)
        } else {
            ""
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("support_thread_${thread.userId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar
            Surface(
                shape = CircleShape,
                modifier = Modifier
                    .size(52.dp)
                    .border(2.dp, PurplePrimary, CircleShape),
                color = PurplePrimary.copy(alpha = 0.15f)
            ) {
                Base64OrResourceImage(
                    base64Str = thread.userAvatar,
                    placeholderRes = R.drawable.img_avatar_maruf_1787554123074,
                    contentDescription = thread.userName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = thread.userName.ifEmpty { "User (${thread.userId.takeLast(6)})" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (formattedTime.isNotEmpty()) {
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = PurpleNeon,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Contact details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (thread.userPhone.isNotEmpty()) {
                        Text(
                            text = thread.userPhone,
                            style = MaterialTheme.typography.bodySmall,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (thread.userEmail.isNotEmpty()) {
                        Text(
                            text = thread.userEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Last Message Preview
                Text(
                    text = thread.lastMessage.ifEmpty { "Tap to open conversation" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open Chat",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
