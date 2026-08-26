package com.example.screens.account

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.SectionHeader
import com.example.components.SubScreenTopBar
import com.example.components.SuccessDialogView
import com.example.repository.AppRepository
import com.example.screens.support.LiveChatScreen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WalletGradientBrush
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportCenterScreen(
    repository: AppRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by repository.userProfile.collectAsState()
    var isLiveChatOpen by remember { mutableStateOf(false) }

    val categories = listOf("Wallet & Payment", "Campaign Promotion", "Task & Rewards", "Referral System", "Account Security", "Other")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    var expandedFaqIndex by remember { mutableIntStateOf(-1) }
    val coroutineScope = rememberCoroutineScope()

    val faqs = listOf(
        Pair("How do I deposit funds into my wallet?", "Navigate to Home > Deposit. Enter the desired amount in Credits, choose Mobile Banking (bKash/Nagad) or Bank Wire, and confirm the transaction."),
        Pair("What are the withdrawal payout times?", "Withdrawals are reviewed and processed within 24 hours. The minimum withdrawal is 10,000 Credits with zero platform handling fees."),
        Pair("How does the daily 50 tasks limit work?", "Each user can complete up to 50 verified tasks every day. Rewards are instantly credited to your wallet in Credits. The task counter resets at midnight."),
        Pair("When do I get my referral bonus?", "As soon as your invited friend registers or applies your unique referral code, both of you instantly receive 100 Credits in your wallet balance.")
    )

    if (showSuccessDialog) {
        SuccessDialogView(
            title = "Ticket Submitted",
            message = "Your support request has been received. Our team will review and reply to ${userProfile.email} within 2-4 hours.",
            buttonText = "Done",
            onDismiss = {
                showSuccessDialog = false
                subject = ""
                message = ""
            }
        )
    }

    if (isLiveChatOpen) {
        LiveChatScreen(
            repository = repository,
            targetUserId = userProfile.id,
            targetUserName = "PayPulse 24/7 Support",
            isAdminView = false,
            onBackClick = { isLiveChatOpen = false }
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .testTag("support_center_screen")
        ) {
            SubScreenTopBar(
                title = "Support Center",
                onBackClick = onBackClick
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero: Live Support Chat Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = PurplePrimary)
                        .clickable { isLiveChatOpen = true }
                        .testTag("open_live_support_banner"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(WalletGradientBrush)
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.25f),
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.HeadsetMic,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "24/7 Live Support Chat",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Instant Text & Voice messaging",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = PurpleNeon,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White
                                ) {
                                    Text(
                                        text = "LIVE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PurplePrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Speak directly with our technical support team in real-time. Send text messages or audio voice notes with permanent chat history.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f),
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { isLiveChatOpen = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = PurplePrimary
                                )
                            ) {
                                Icon(Icons.Outlined.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open Live Chat & Voice", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 1. Direct Contact Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = PurplePrimary.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Email, contentDescription = "Email", tint = PurplePrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Email Support", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("support@paypulse.com", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = InfoBlue.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Mic, contentDescription = "Voice Support", tint = InfoBlue, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Voice Support", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Voice Notes Active", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // 2. FAQ Section
            item {
                SectionHeader(title = "Frequently Asked Questions", actionText = null)
            }

            items(faqs.size) { index ->
                val (question, answer) = faqs[index]
                val isExpanded = expandedFaqIndex == index

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedFaqIndex = if (isExpanded) -1 else index
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = question,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = "Toggle",
                                tint = PurplePrimary
                            )
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Column {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = answer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 3. Submit Support Ticket Form
            item {
                Spacer(modifier = Modifier.height(6.dp))
                SectionHeader(title = "Submit a Support Ticket", actionText = null)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Category Dropdown
                        ExposedDropdownMenuBox(
                            expanded = isCategoryDropdownExpanded,
                            onExpandedChange = { isCategoryDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedCategory,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Issue Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurplePrimary)
                            )
                            ExposedDropdownMenu(
                                expanded = isCategoryDropdownExpanded,
                                onDismissRequest = { isCategoryDropdownExpanded = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            selectedCategory = cat
                                            isCategoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Subject
                        OutlinedTextField(
                            value = subject,
                            onValueChange = {
                                subject = it
                                errorMessage = null
                            },
                            label = { Text("Subject") },
                            placeholder = { Text("Brief issue description") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("support_subject_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        // Message
                        OutlinedTextField(
                            value = message,
                            onValueChange = {
                                message = it
                                errorMessage = null
                            },
                            label = { Text("Message Details") },
                            placeholder = { Text("Explain your problem or inquiry in detail...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("support_message_input"),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 5
                        )

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = ErrorRed
                            )
                        }

                        Button(
                            onClick = {
                                if (subject.isBlank()) {
                                    errorMessage = "Please enter a subject."
                                    return@Button
                                }
                                if (message.isBlank()) {
                                    errorMessage = "Please enter your message."
                                    return@Button
                                }

                                isLoading = true
                                errorMessage = null

                                coroutineScope.launch {
                                    delay(800)
                                    isLoading = false
                                    showSuccessDialog = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_ticket_button"),
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Filled.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Send Ticket", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
}
