package com.example.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.model.MaintenanceSettings
import com.example.model.SocialMediaLink
import com.example.repository.AppRepository
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen
import java.util.UUID

@Composable
fun AdminMaintenanceScreen(repository: AppRepository) {
    val maintenanceSettings by repository.maintenanceSettings.collectAsState()
    val userProfile by repository.userProfile.collectAsState()

    val isOwner = userProfile.role == "OWNER" || userProfile.email == "d@gmail.com"

    var isMasterEnabled by remember(maintenanceSettings) { mutableStateOf(maintenanceSettings.isMasterEnabled) }
    var isUserMaintenance by remember(maintenanceSettings) { mutableStateOf(maintenanceSettings.isUserMaintenance) }
    var userNoteInput by remember(maintenanceSettings) { mutableStateOf(maintenanceSettings.userNote) }
    var isAdminMaintenance by remember(maintenanceSettings) { mutableStateOf(maintenanceSettings.isAdminMaintenance) }
    var adminNoteInput by remember(maintenanceSettings) { mutableStateOf(maintenanceSettings.adminNote) }

    var showAddSocialDialog by remember { mutableStateOf(false) }
    var socialToEdit by remember { mutableStateOf<SocialMediaLink?>(null) }
    var socialToDelete by remember { mutableStateOf<SocialMediaLink?>(null) }
    var showSuccessToast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(showSuccessToast) {
        if (showSuccessToast != null) {
            kotlinx.coroutines.delay(2500)
            showSuccessToast = null
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_maintenance_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Top Info Banner
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (isMasterEnabled) ErrorRed.copy(alpha = 0.15f)
                                else SuccessGreen.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Engineering,
                            contentDescription = null,
                            tint = if (isMasterEnabled) ErrorRed else SuccessGreen,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Maintenance Mode Controller",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "When active, blocked users see a full-screen maintenance message and your social support links. App Owner is always exempt.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Toast feedback
        if (showSuccessToast != null) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = SuccessGreen.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = showSuccessToast ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 1. MASTER TOGGLE CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMasterEnabled) ErrorRed.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Global Maintenance Switch",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = if (isMasterEnabled) "Maintenance Mode is currently ON" else "System running normally (OFF)",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isMasterEnabled) ErrorRed else SuccessGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Switch(
                        checked = isMasterEnabled,
                        onCheckedChange = { checked ->
                            isMasterEnabled = checked
                            val updated = maintenanceSettings.copy(isMasterEnabled = checked)
                            repository.updateMaintenanceSettings(updated)
                            showSuccessToast = if (checked) "Maintenance Mode Activated" else "Maintenance Mode Deactivated"
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ErrorRed,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("master_maintenance_switch")
                    )
                }
            }
        }

        // 2. SUB-TOGGLES (USER & ADMIN) - Only visible when Master is ON
        item {
            AnimatedVisibility(
                visible = isMasterEnabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // USER MAINTENANCE CARD
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Group,
                                        contentDescription = null,
                                        tint = PurplePrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "User Maintenance Mode",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "ইউজারদের জন্য রক্ষণাবেক্ষণ মোড",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Switch(
                                    checked = isUserMaintenance,
                                    onCheckedChange = { checked ->
                                        isUserMaintenance = checked
                                        val updated = maintenanceSettings.copy(isUserMaintenance = checked)
                                        repository.updateMaintenanceSettings(updated)
                                        showSuccessToast = "User maintenance updated."
                                    },
                                    modifier = Modifier.testTag("user_maintenance_switch")
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            Text(
                                text = "Notice / Reason to Display for Users:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = userNoteInput,
                                onValueChange = { userNoteInput = it },
                                placeholder = { Text("আমাদের সার্ভার আপগ্রেড চলছে। শীঘ্রই আমরা ফিরে আসব...") },
                                minLines = 2,
                                maxLines = 4,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("user_maintenance_note_input"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Button(
                                onClick = {
                                    val updated = maintenanceSettings.copy(
                                        isUserMaintenance = isUserMaintenance,
                                        userNote = userNoteInput.trim()
                                    )
                                    repository.updateMaintenanceSettings(updated)
                                    showSuccessToast = "User notice updated successfully."
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save User Notice")
                            }
                        }
                    }

                    // ADMIN MAINTENANCE CARD (Owner Only editable)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = if (isOwner) PurplePrimary else Color.Gray,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Admin Maintenance Mode",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isOwner) "এডমিনদের জন্য রক্ষণাবেক্ষণ (Owner Only)" else "Restricted (Owner Access Only)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isOwner) MaterialTheme.colorScheme.onSurfaceVariant else ErrorRed
                                        )
                                    }
                                }

                                Switch(
                                    checked = isAdminMaintenance,
                                    onCheckedChange = { checked ->
                                        if (isOwner) {
                                            isAdminMaintenance = checked
                                            val updated = maintenanceSettings.copy(isAdminMaintenance = checked)
                                            repository.updateMaintenanceSettings(updated)
                                            showSuccessToast = "Admin maintenance updated."
                                        }
                                    },
                                    enabled = isOwner,
                                    modifier = Modifier.testTag("admin_maintenance_switch")
                                )
                            }

                            if (!isOwner) {
                                Text(
                                    text = "Only the Master Owner can toggle or configure Admin Maintenance Mode.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                Text(
                                    text = "Notice / Reason to Display for Admins:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = adminNoteInput,
                                    onValueChange = { adminNoteInput = it },
                                    placeholder = { Text("Admin backend is temporarily undergoing database schema migration...") },
                                    minLines = 2,
                                    maxLines = 4,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("admin_maintenance_note_input"),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Button(
                                    onClick = {
                                        val updated = maintenanceSettings.copy(
                                            isAdminMaintenance = isAdminMaintenance,
                                            adminNote = adminNoteInput.trim()
                                        )
                                        repository.updateMaintenanceSettings(updated)
                                        showSuccessToast = "Admin notice saved."
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.align(Alignment.End),
                                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save Admin Notice")
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. SOCIAL MEDIA & CONTACT CHANNELS SECTION
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Support & Social Media Links",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "মেইনটেনেন্স চলাকালীন যোগাযোগ লিংক (${maintenanceSettings.socialLinks.size} channels)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isOwner) {
                            Button(
                                onClick = { showAddSocialDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("add_social_channel_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Link", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (!isOwner) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Only the Master Owner can add, edit or delete social media links.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    if (maintenanceSettings.socialLinks.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LinkOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "No social media links added yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (isOwner) {
                                    Text(
                                        text = "Click 'Add Link' above to add Telegram, WhatsApp, etc.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PurplePrimary
                                    )
                                }
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            maintenanceSettings.socialLinks.forEach { link ->
                                SocialLinkItemRow(
                                    link = link,
                                    isOwner = isOwner,
                                    onEdit = { socialToEdit = link },
                                    onDelete = { socialToDelete = link }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOG: ADD SOCIAL MEDIA
    if (showAddSocialDialog) {
        SocialMediaEditDialog(
            existing = null,
            onDismiss = { showAddSocialDialog = false },
            onSave = { newLink ->
                val updatedList = maintenanceSettings.socialLinks + newLink
                repository.updateMaintenanceSettings(maintenanceSettings.copy(socialLinks = updatedList))
                showSuccessToast = "Added ${newLink.name}"
                showAddSocialDialog = false
            }
        )
    }

    // DIALOG: EDIT SOCIAL MEDIA
    if (socialToEdit != null) {
        SocialMediaEditDialog(
            existing = socialToEdit,
            onDismiss = { socialToEdit = null },
            onSave = { updatedLink ->
                val updatedList = maintenanceSettings.socialLinks.map { if (it.id == updatedLink.id) updatedLink else it }
                repository.updateMaintenanceSettings(maintenanceSettings.copy(socialLinks = updatedList))
                showSuccessToast = "Updated ${updatedLink.name}"
                socialToEdit = null
            }
        )
    }

    // DIALOG: DELETE SOCIAL MEDIA
    if (socialToDelete != null) {
        val target = socialToDelete!!
        AlertDialog(
            onDismissRequest = { socialToDelete = null },
            title = { Text("Delete Social Link", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove ${target.name} from the maintenance links?") },
            confirmButton = {
                Button(
                    onClick = {
                        val updatedList = maintenanceSettings.socialLinks.filter { it.id != target.id }
                        repository.updateMaintenanceSettings(maintenanceSettings.copy(socialLinks = updatedList))
                        showSuccessToast = "Deleted ${target.name}"
                        socialToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { socialToDelete = null }, shape = RoundedCornerShape(8.dp)) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SocialLinkItemRow(
    link: SocialMediaLink,
    isOwner: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(getSocialIconColor(link.iconKey).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getSocialVectorIcon(link.iconKey),
                    contentDescription = null,
                    tint = getSocialIconColor(link.iconKey),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = link.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = link.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            if (isOwner) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PurplePrimary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun SocialMediaEditDialog(
    existing: SocialMediaLink?,
    onDismiss: () -> Unit,
    onSave: (SocialMediaLink) -> Unit
) {
    val isEdit = existing != null
    var name by remember { mutableStateOf(existing?.name ?: "Telegram") }
    var selectedIconKey by remember { mutableStateOf(existing?.iconKey ?: "telegram") }
    var url by remember { mutableStateOf(existing?.url ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val presetIcons = listOf(
        Triple("telegram", "Telegram", Color(0xFF229ED9)),
        Triple("whatsapp", "WhatsApp", Color(0xFF25D366)),
        Triple("messenger", "Messenger", Color(0xFF0084FF)),
        Triple("youtube", "YouTube", Color(0xFFFF0000)),
        Triple("facebook", "Facebook", Color(0xFF1877F2)),
        Triple("twitter", "Twitter/X", Color(0xFF1DA1F2)),
        Triple("website", "Website", Color(0xFF6C5CE7)),
        Triple("other", "Other", PurplePrimary)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEdit) "Edit Social Channel" else "Add Social Channel",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select Platform / Icon:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presetIcons) { (key, label, color) ->
                        val isSelected = selectedIconKey == key
                        Surface(
                            modifier = Modifier.clickable {
                                selectedIconKey = key
                                if (!isEdit || name.isBlank()) {
                                    name = label
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, color) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = getSocialVectorIcon(key),
                                    contentDescription = null,
                                    tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Display Name") },
                    placeholder = { Text("e.g. Telegram Support Group") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it; errorMessage = null },
                    label = { Text("Target Link / URL") },
                    placeholder = { Text("https://t.me/yourchannel or https://wa.me/...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isBlank()) {
                        errorMessage = "Please provide a name."
                        return@Button
                    }
                    if (url.trim().isBlank()) {
                        errorMessage = "Please enter a valid link."
                        return@Button
                    }
                    val finalLink = SocialMediaLink(
                        id = existing?.id ?: UUID.randomUUID().toString(),
                        name = name.trim(),
                        iconKey = selectedIconKey,
                        url = url.trim()
                    )
                    onSave(finalLink)
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                Text(if (isEdit) "Update" else "Add Channel", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("Cancel")
            }
        }
    )
}

fun getSocialVectorIcon(key: String): ImageVector {
    return when (key.lowercase()) {
        "telegram", "messenger" -> Icons.Default.Send
        "whatsapp" -> Icons.Default.Phone
        "youtube" -> Icons.Default.PlayCircleFilled
        "facebook" -> Icons.Default.ThumbUp
        "twitter" -> Icons.Default.Tag
        "website" -> Icons.Default.Language
        else -> Icons.Default.Share
    }
}

fun getSocialIconColor(key: String): Color {
    return when (key.lowercase()) {
        "telegram" -> Color(0xFF229ED9)
        "whatsapp" -> Color(0xFF25D366)
        "messenger" -> Color(0xFF0084FF)
        "youtube" -> Color(0xFFFF0000)
        "facebook" -> Color(0xFF1877F2)
        "twitter" -> Color(0xFF1DA1F2)
        "website" -> Color(0xFF6C5CE7)
        else -> PurplePrimary
    }
}
