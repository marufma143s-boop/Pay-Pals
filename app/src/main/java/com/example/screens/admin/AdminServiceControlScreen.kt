package com.example.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.ServiceItemConfig
import com.example.repository.AppRepository
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen

data class ServiceDefinition(
    val key: String,
    val title: String,
    val titleBn: String,
    val description: String,
    val icon: ImageVector
)

val APP_SERVICE_DEFINITIONS = listOf(
    ServiceDefinition(
        key = "deposit",
        title = "Deposit Service",
        titleBn = "ডিপোজিট সার্ভিস",
        description = "Allow users to submit money deposit requests via bKash, Nagad, Bank, etc.",
        icon = Icons.Default.AccountBalanceWallet
    ),
    ServiceDefinition(
        key = "withdraw",
        title = "Withdrawal Service",
        titleBn = "উইথড্রয়াল সার্ভিস",
        description = "Allow users to withdraw their wallet earnings and request payouts.",
        icon = Icons.Default.MoneyOff
    ),
    ServiceDefinition(
        key = "campaign_adsterra",
        title = "Adsterra Campaigns & Tasks",
        titleBn = "এ ডেসট্রা ক্যাম্পেইন ও কাজ",
        description = "Create Adsterra campaigns and allow users to earn points by visiting links.",
        icon = Icons.Default.Public
    ),
    ServiceDefinition(
        key = "campaign_blogger",
        title = "Blogger Campaigns & Tasks",
        titleBn = "ব্লগার ক্যাম্পেইন ও কাজ",
        description = "Create Blogger campaigns and allow users to perform blog visit tasks.",
        icon = Icons.Default.Article
    ),
    ServiceDefinition(
        key = "campaign_monetag",
        title = "Monetag Campaigns & Tasks",
        titleBn = "মনিট্যাগ ক্যাম্পেইন ও কাজ",
        description = "Create Monetag campaigns and allow users to earn via Monetag links.",
        icon = Icons.Default.MonetizationOn
    ),
    ServiceDefinition(
        key = "campaign_run_adsterra",
        title = "Users Can Run Adsterra",
        titleBn = "ইউজার এ ডেসট্রা ক্যাম্পেইন",
        description = "Allow users to create and run Adsterra campaigns.",
        icon = Icons.Default.Campaign
    ),
    ServiceDefinition(
        key = "campaign_run_blogger",
        title = "Users Can Run Blogger",
        titleBn = "ইউজার ব্লগার ক্যাম্পেইন",
        description = "Allow users to create and run Blogger campaigns.",
        icon = Icons.Default.Campaign
    ),
    ServiceDefinition(
        key = "campaign_run_monetag",
        title = "Users Can Run Monetag",
        titleBn = "ইউজার মনিট্যাগ ক্যাম্পেইন",
        description = "Allow users to create and run Monetag campaigns.",
        icon = Icons.Default.Campaign
    ),
    ServiceDefinition(
        key = "referral",
        title = "Referral System",
        titleBn = "রেফারেল সিস্টেম",
        description = "Allow users to share invite links and earn commission from friends.",
        icon = Icons.Default.Share
    ),
    ServiceDefinition(
        key = "paid_packages",
        title = "Paid Campaign Packages",
        titleBn = "পেইড ক্যাম্পেইন প্যাকেজ",
        description = "Allow users to purchase pre-configured bulk traffic packages.",
        icon = Icons.Default.CardGiftcard
    ),
    ServiceDefinition(
        key = "user_registration",
        title = "New User Registration",
        titleBn = "নতুন ইউজার রেজিস্ট্রেশন",
        description = "Allow new accounts to register. Existing users can still log in when closed.",
        icon = Icons.Default.PersonAdd
    )
)

@Composable
fun AdminServiceControlScreen(repository: AppRepository) {
    val serviceSettings by repository.serviceControlSettings.collectAsState()

    var serviceToToggleOff by remember { mutableStateOf<ServiceDefinition?>(null) }
    var serviceToEditNote by remember { mutableStateOf<Pair<ServiceDefinition, String>?>(null) }
    var closeReasonInput by remember { mutableStateOf("") }
    var showSuccessToast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(showSuccessToast) {
        if (showSuccessToast != null) {
            kotlinx.coroutines.delay(2500)
            showSuccessToast = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_service_control_screen")
    ) {
        // Header info banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PurplePrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Service Status Control",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Toggle services ON/OFF. Closed services will be greyed out for users and display your custom note.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Toast feedback
        AnimatedVisibility(
            visible = showSuccessToast != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            showSuccessToast?.let { msg ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = SuccessGreen.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = msg, style = MaterialTheme.typography.bodyMedium, color = SuccessGreen, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(APP_SERVICE_DEFINITIONS, key = { it.key }) { def ->
                val isServiceDisabled = serviceSettings.isServiceDisabled(def.key)
                val currentReason = serviceSettings.getServiceReason(def.key)
                val rawConfig = when (def.key) {
                    "deposit" -> serviceSettings.deposit
                    "withdraw" -> serviceSettings.withdraw
                    "campaign_adsterra" -> serviceSettings.campaignAdsterra
                    "campaign_blogger" -> serviceSettings.campaignBlogger
                    "campaign_monetag" -> serviceSettings.campaignMonetag
                    "campaign_run_adsterra" -> serviceSettings.campaignRunAdsterra
                    "campaign_run_blogger" -> serviceSettings.campaignRunBlogger
                    "campaign_run_monetag" -> serviceSettings.campaignRunMonetag
                    "referral" -> serviceSettings.referral
                    "paid_packages" -> serviceSettings.paidPackages
                    "user_registration" -> serviceSettings.userRegistration
                    else -> ServiceItemConfig()
                }

                ServiceControlItemCard(
                    definition = def,
                    isDisabled = isServiceDisabled,
                    customReason = rawConfig.reason,
                    onToggle = { enable ->
                        if (enable) {
                            // Turn ON service
                            repository.updateServiceStatus(def.key, isDisabled = false, reason = "")
                            showSuccessToast = "${def.title} has been enabled."
                        } else {
                            // Open modal to specify reason
                            closeReasonInput = rawConfig.reason.ifBlank { "" }
                            serviceToToggleOff = def
                        }
                    },
                    onEditNote = {
                        closeReasonInput = rawConfig.reason
                        serviceToEditNote = Pair(def, rawConfig.reason)
                    }
                )
            }
        }
    }

    // Modal Dialog: Close Service with Note
    if (serviceToToggleOff != null) {
        val targetDef = serviceToToggleOff!!
        AlertDialog(
            onDismissRequest = { serviceToToggleOff = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PauseCircleFilled,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Close ${targetDef.title}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Why is this service being closed? Users who tap on this service will see this note (Optional):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = closeReasonInput,
                        onValueChange = { closeReasonInput = it },
                        placeholder = { Text("যেমন: সার্ভার আপগ্রেড চলছে অথবা সাময়িক রক্ষণাবেক্ষণ...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("service_close_note_input"),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Text(
                        text = "Note: You can leave this empty to show the default unavailable message.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.updateServiceStatus(
                            serviceKey = targetDef.key,
                            isDisabled = true,
                            reason = closeReasonInput.trim()
                        )
                        showSuccessToast = "${targetDef.title} is now CLOSED."
                        serviceToToggleOff = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Confirm & Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { serviceToToggleOff = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal Dialog: Edit Note of already closed service
    if (serviceToEditNote != null) {
        val (targetDef, _) = serviceToEditNote!!
        AlertDialog(
            onDismissRequest = { serviceToEditNote = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Edit Note - ${targetDef.title}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Update the custom reason shown to users for this closed service:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = closeReasonInput,
                        onValueChange = { closeReasonInput = it },
                        placeholder = { Text("Enter reason...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("service_edit_note_input"),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.updateServiceStatus(
                            serviceKey = targetDef.key,
                            isDisabled = true,
                            reason = closeReasonInput.trim()
                        )
                        showSuccessToast = "Note updated for ${targetDef.title}."
                        serviceToEditNote = null
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Save Note", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { serviceToEditNote = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ServiceControlItemCard(
    definition: ServiceDefinition,
    isDisabled: Boolean,
    customReason: String,
    onToggle: (Boolean) -> Unit,
    onEditNote: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isDisabled) 1.dp else 2.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDisabled) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Service Icon badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isDisabled) Color.Gray.copy(alpha = 0.2f)
                            else PurplePrimary.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = definition.icon,
                        contentDescription = null,
                        tint = if (isDisabled) Color.Gray else PurplePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Titles & status
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = definition.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDisabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = definition.titleBn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Switch Toggle
                Column(horizontalAlignment = Alignment.End) {
                    Switch(
                        checked = !isDisabled,
                        onCheckedChange = { isChecked ->
                            onToggle(isChecked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SuccessGreen,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = ErrorRed.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.testTag("switch_${definition.key}")
                    )
                    Text(
                        text = if (!isDisabled) "ACTIVE" else "CLOSED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (!isDisabled) SuccessGreen else ErrorRed
                    )
                }
            }

            // Description
            Text(
                text = definition.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
            )

            // If disabled, show note container & edit button
            if (isDisabled) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = ErrorRed.copy(alpha = 0.08f),
                    border = null
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Displayed Reason to Users:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ErrorRed
                            )
                            Text(
                                text = if (customReason.isNotBlank()) customReason else "(Default fallback message)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = onEditNote,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Note",
                                tint = PurplePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
