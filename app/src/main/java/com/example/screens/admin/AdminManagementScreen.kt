package com.example.screens.admin

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.repository.AppRepository
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen
import com.example.utils.FormatUtils

data class AdminPermissionDefinition(
    val key: String,
    val title: String,
    val description: String,
    val icon: ImageVector
)

val ALL_ADMIN_PERMISSIONS = listOf(
    AdminPermissionDefinition("dashboard", "Dashboard Overview", "View summary statistics & quick metrics", Icons.Default.Dashboard),
    AdminPermissionDefinition("users", "User Management", "View, edit, ban and delete regular users", Icons.Default.People),
    AdminPermissionDefinition("deposits", "Deposit Requests", "Approve or reject balance deposit requests", Icons.Default.AccountBalanceWallet),
    AdminPermissionDefinition("withdrawals", "Withdrawal Requests", "Approve or reject balance payout requests", Icons.Default.MoneyOff),
    AdminPermissionDefinition("deposit_methods", "Deposit Methods", "Add/delete deposit payment numbers", Icons.Default.Payments),
    AdminPermissionDefinition("withdrawal_methods", "Withdrawal Methods", "Add/delete payout options", Icons.Default.AccountBalance),
    AdminPermissionDefinition("campaigns", "Campaign Management", "Review, monitor & manage traffic campaigns", Icons.Default.TrendingUp),
    AdminPermissionDefinition("packages", "Paid Packages", "Create and edit fixed traffic packages", Icons.Default.CardGiftcard),
    AdminPermissionDefinition("package_orders", "Package Orders", "Manage package purchases & update status", Icons.Default.ShoppingCart),
    AdminPermissionDefinition("general_settings", "General Settings", "Configure point rates, browser & online users", Icons.Default.Tune),
    AdminPermissionDefinition("service_control", "Service Control", "Turn specific services (deposit, withdraw, campaign, refer) on/off with custom notes", Icons.Default.PowerSettingsNew),
    AdminPermissionDefinition("maintenance_mode", "Maintenance Mode", "Toggle user maintenance mode during system updates", Icons.Default.Engineering),
    AdminPermissionDefinition("support_center", "Live Support Center", "Manage user chat threads, reply & voice notes", Icons.Default.HeadsetMic),
    AdminPermissionDefinition("developer_settings", "Developer Profile", "Edit developer details, avatar photo & socials", Icons.Default.Code),
    AdminPermissionDefinition("popup_settings", "Popup Settings", "Manage the welcome popup configurations", Icons.Default.Campaign),
    AdminPermissionDefinition("adward_settings", "Task & Break Timers", "Configure task duration, rewards and break timers", Icons.Default.Timer),
    AdminPermissionDefinition("admin_links", "Admin Direct Links", "Add and manage admin sponsored direct links", Icons.Default.Link)
)

@Composable
fun AdminManagementScreen(repository: AppRepository) {
    val allUsers by repository.allUsers.collectAsState()
    
    // Filter users who are ADMIN or OWNER or main email
    val adminUsers = remember(allUsers) {
        allUsers.filter { user ->
            val role = user["role"] as? String ?: "USER"
            val email = user["email"] as? String ?: ""
            role == "ADMIN" || role == "OWNER" || email == "d@gmail.com"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Admin Management",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${adminUsers.size} authorized administrators",
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
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Owner Control",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = PurplePrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Click the dropdown arrow on any admin to customize their exact menu access. Deleting an admin here revokes admin privileges while keeping their user account safe.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (adminUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No administrators found. Grant admin access to users from User Management.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(adminUsers, key = { it["id"] as? String ?: it.hashCode().toString() }) { adminUser ->
                    AdminCardItem(user = adminUser, repository = repository)
                }
            }
        }
    }
}

@Composable
fun AdminCardItem(
    user: Map<String, Any?>,
    repository: AppRepository
) {
    val userId = user["id"] as? String ?: return
    val name = user["fullName"] as? String ?: "Admin User"
    val email = user["email"] as? String ?: "No Email"
    val balance = (user["balance"] as? Number)?.toDouble() ?: 0.0
    val isBlocked = user["isBlocked"] as? Boolean ?: false
    val role = user["role"] as? String ?: "ADMIN"
    val isOwner = email == "d@gmail.com" || role == "OWNER"
    val rawPin = (user["adminPin"] as? String) ?: (user["adminPin"] as? Number)?.toString() ?: ""
    val adminPin = if (rawPin.isBlank()) (if (isOwner) "1234" else "1234") else rawPin

    var isExpanded by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showRevokeDialog by remember { mutableStateOf(false) }
    var isPinVisible by remember { mutableStateOf(false) }

    // Permissions map from database
    val rawPerms = user["permissions"] as? Map<String, Any?>
    val permissions = remember(rawPerms) {
        val map = mutableMapOf<String, Boolean>()
        ALL_ADMIN_PERMISSIONS.forEach { def ->
            val v = rawPerms?.get(def.key)
            // If explicit boolean is set, use it; otherwise default to true
            map[def.key] = when (v) {
                is Boolean -> v
                "false" -> false
                "true" -> true
                else -> true
            }
        }
        map
    }

    if (showPinDialog) {
        var newPinInput by remember { mutableStateOf(rawPin.ifBlank { "1234" }) }
        var pinError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.VpnKey,
                    contentDescription = null,
                    tint = PurplePrimary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = if (isOwner) "Change Owner PIN" else "Set Admin Security PIN",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Set a 4-6 digit numeric PIN for $name to enter the admin/owner panel:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                newPinInput = it
                                pinError = null
                            }
                        },
                        label = { Text("Security PIN (4-6 digits)") },
                        singleLine = true,
                        isError = pinError != null,
                        supportingText = pinError?.let { { Text(it, color = ErrorRed) } },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = PurplePrimary)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinInput.length < 4) {
                            pinError = "PIN must be at least 4 digits."
                            return@Button
                        }
                        if (isOwner) {
                            repository.updateOwnerPin(newPinInput)
                        }
                        repository.updateUserAdminPin(userId, newPinInput)
                        showPinDialog = false
                    }
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditDialog) {
        var editName by remember { mutableStateOf(name) }
        var editEmail by remember { mutableStateOf(email) }
        var editBalance by remember { mutableStateOf(balance.toString()) }
        var editPin by remember { mutableStateOf(rawPin.ifBlank { "1234" }) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Administrator") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editBalance,
                        onValueChange = { editBalance = it },
                        label = { Text("Balance (Points)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editPin,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                editPin = it
                            }
                        },
                        label = { Text("Security PIN (পাসওয়ার্ড/পিন)") },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = PurplePrimary) },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    repository.updateUserAdmin(
                        userId = userId,
                        name = editName,
                        email = editEmail,
                        balance = editBalance.toDoubleOrNull() ?: balance,
                        pin = editPin
                    )
                    if (isOwner && editPin.isNotBlank()) {
                        repository.updateOwnerPin(editPin)
                    }
                    showEditDialog = false
                }) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRevokeDialog) {
        AlertDialog(
            onDismissRequest = { showRevokeDialog = false },
            title = { Text("Revoke Admin Access?") },
            text = {
                Text("Are you sure you want to remove admin access for $name? They will be converted back to a regular user account and will no longer have access to the Admin Panel. Their account will not be deleted from the database.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.removeAdminAccess(userId)
                        showRevokeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Revoke Admin", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevokeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Avatar + Name/Email + Role Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            if (isOwner) Color(0xFFFFD700).copy(alpha = 0.2f) else PurplePrimary.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isOwner) Icons.Default.Stars else Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = if (isOwner) Color(0xFFD97706) else PurplePrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isOwner) Color(0xFFFEF3C7) else PurplePrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isOwner) "MAIN OWNER" else "ADMIN",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isOwner) Color(0xFFB45309) else PurplePrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand Permissions",
                        tint = PurplePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Info Bar: Balance + Status + PIN + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Balance: ${FormatUtils.formatCredits(balance)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SuccessGreen
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Status: ${if (isBlocked) "BANNED" else "ACTIVE"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isBlocked) ErrorRed else SuccessGreen,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PurplePrimary.copy(alpha = 0.1f),
                            modifier = Modifier.clickable { isPinVisible = !isPinVisible }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VpnKey,
                                    contentDescription = null,
                                    tint = PurplePrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (isPinVisible) "PIN: $adminPin" else "PIN: ••••",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PurplePrimary
                                )
                            }
                        }
                    }
                }

                // Action Buttons: PIN, Ban, Edit, Delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showPinDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Set/Change PIN",
                            tint = PurplePrimary
                        )
                    }
                    if (!isOwner) {
                        IconButton(onClick = { repository.updateUserBlockStatus(userId, !isBlocked) }) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = if (isBlocked) "Unban" else "Ban",
                                tint = if (isBlocked) SuccessGreen else ErrorRed
                            )
                        }
                    }
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Admin",
                            tint = PurplePrimary
                        )
                    }
                    if (!isOwner) {
                        IconButton(onClick = { showRevokeDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Revoke Admin Access",
                                tint = ErrorRed
                            )
                        }
                    }
                }
            }

            // Expandable Granular Permissions Section
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))

                    Text(
                        text = "Menu & Feature Permissions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isOwner) "The Main Owner has full, unrestricted access to all admin and owner modules." else "Toggle on/off which sections this administrator can access in the Admin Panel:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (isOwner) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "All 10 modules & owner controls are permanently active.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ALL_ADMIN_PERMISSIONS.forEach { def ->
                                val isChecked = permissions[def.key] ?: true

                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = def.icon,
                                                contentDescription = null,
                                                tint = if (isChecked) PurplePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = def.title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = def.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Switch(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                val updated = permissions.toMutableMap()
                                                updated[def.key] = checked
                                                repository.updateUserAdminPermissions(userId, updated)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
