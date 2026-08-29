package com.example.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.repository.AppRepository
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    repository: AppRepository,
    initialSubMenu: String = "general"
) {
    var selectedSubMenu by remember { mutableStateOf(initialSubMenu) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val userProfile by repository.userProfile.collectAsState()

    val allSubMenuItems = listOf(
        Triple("general", "General Settings", Icons.Default.Tune),
        Triple("adward_settings", "Task & Break Timers (টাস্ক ও বিরতি)", Icons.Default.Timer),
        Triple("admin_links", "Sponsored Direct Links (স্পন্সর লিংক)", Icons.Default.Link),
        Triple("services", "Service Status (সার্ভিস বন্ধ)", Icons.Default.PowerSettingsNew),
        Triple("maintenance", "Maintenance Mode (মেইনটেনেন্স)", Icons.Default.Engineering),
        Triple("support", "Support Center", Icons.Default.HeadsetMic),
        Triple("developer", "Developer Profile", Icons.Default.Code),
        Triple("popup", "Popup Notice Settings", Icons.Default.Campaign)
    )

    val subMenuItems = remember(userProfile) {
        allSubMenuItems.filter { (key, _, _) ->
            when (key) {
                "general" -> userProfile.hasPermission("general_settings")
                "adward_settings" -> userProfile.hasPermission("adward_settings")
                "admin_links" -> userProfile.hasPermission("admin_links")
                "services" -> userProfile.hasPermission("service_control")
                "maintenance" -> userProfile.hasPermission("maintenance_mode")
                "support" -> userProfile.hasPermission("support_center")
                "developer" -> userProfile.hasPermission("developer_settings")
                "popup" -> userProfile.hasPermission("popup_settings")
                else -> true
            }
        }
    }

    LaunchedEffect(subMenuItems) {
        if (subMenuItems.isNotEmpty() && subMenuItems.none { it.first == selectedSubMenu }) {
            selectedSubMenu = subMenuItems.first().first
        }
    }

    val currentItem = subMenuItems.firstOrNull { it.first == selectedSubMenu } ?: subMenuItems.firstOrNull() ?: allSubMenuItems[0]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_settings_screen")
    ) {
        // Dropdown Sub-Menu Selector Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(
                    text = "Settings Category",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))

                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = currentItem.second,
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = {
                            Icon(
                                imageVector = currentItem.third,
                                contentDescription = null,
                                tint = PurplePrimary
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("settings_dropdown_selector"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        subMenuItems.forEach { (key, label, icon) ->
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (selectedSubMenu == key) PurplePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                text = {
                                    Text(
                                        text = label,
                                        fontWeight = if (selectedSubMenu == key) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedSubMenu == key) PurplePrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    selectedSubMenu = key
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Sub-Menu Content Display
        Box(modifier = Modifier.weight(1f)) {
            when (selectedSubMenu) {
                "general" -> AdminGeneralSettingsContent(repository = repository)
                "adward_settings" -> AdminAdwardSettingsSection(repository = repository)
                "admin_links" -> AdminDirectLinksSection(repository = repository)
                "services" -> AdminServiceControlScreen(repository = repository)
                "maintenance" -> AdminMaintenanceScreen(repository = repository)
                "support" -> AdminLiveSupportScreen(repository = repository)
                "developer" -> AdminDeveloperSettingsScreen(repository = repository)
                "popup" -> AdminPopupSettingsScreen(repository = repository)
            }
        }
    }
}

@Composable
private fun AdminGeneralSettingsContent(repository: AppRepository) {
    val preferredBrowser by repository.preferredBrowser.collectAsState()
    val campaignRates by repository.campaignRates.collectAsState()
    val onlineUsersMin by repository.onlineUsersMin.collectAsState()
    val onlineUsersMax by repository.onlineUsersMax.collectAsState()
    val ownerPin by repository.ownerPin.collectAsState()

    var minUsersInput by remember(onlineUsersMin) { mutableStateOf(onlineUsersMin.toString()) }
    var maxUsersInput by remember(onlineUsersMax) { mutableStateOf(onlineUsersMax.toString()) }
    var onlineRangeSuccessMessage by remember { mutableStateOf<String?>(null) }

    var newOwnerPinInput by remember(ownerPin) { mutableStateOf(ownerPin) }
    var isOwnerPinVisible by remember { mutableStateOf(false) }
    var ownerPinSuccessMessage by remember { mutableStateOf<String?>(null) }
    var ownerPinErrorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Owner Security PIN Section
        Text(
            text = "Owner Panel Security PIN (ওনার পিন)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Main Owner Access PIN",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "ওনার ও অ্যাডমিন প্যানেলে প্রবেশের মূল মাস্টার পিন কোড। ডিফল্ট: 1234",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    value = newOwnerPinInput,
                    onValueChange = {
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            newOwnerPinInput = it
                            ownerPinErrorMessage = null
                            ownerPinSuccessMessage = null
                        }
                    },
                    label = { Text("Enter New Owner PIN (4-6 digits)") },
                    singleLine = true,
                    isError = ownerPinErrorMessage != null,
                    supportingText = {
                        if (ownerPinErrorMessage != null) {
                            Text(ownerPinErrorMessage ?: "", color = com.example.ui.theme.ErrorRed)
                        } else if (ownerPinSuccessMessage != null) {
                            Text(ownerPinSuccessMessage ?: "", color = com.example.ui.theme.SuccessGreen)
                        } else {
                            Text("Current active PIN: $ownerPin", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = PurplePrimary)
                    },
                    trailingIcon = {
                        IconButton(onClick = { isOwnerPinVisible = !isOwnerPinVisible }) {
                            Icon(
                                imageVector = if (isOwnerPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle PIN Visibility",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    visualTransformation = if (isOwnerPinVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (newOwnerPinInput.length < 4) {
                            ownerPinErrorMessage = "PIN অবশ্যই ন্যূনতম ৪ সংখ্যার হতে হবে।"
                            ownerPinSuccessMessage = null
                            return@Button
                        }
                        repository.updateOwnerPin(newOwnerPinInput)
                        ownerPinSuccessMessage = "ওনার পিন সফলভাবে আপডেট করা হয়েছে! নতুন পিন: $newOwnerPinInput"
                        ownerPinErrorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Update Owner PIN (পিন পরিবর্তন করুন)")
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Campaign Rates Section
        Text(
            text = "Campaign Point Rates (per 100 views)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val networks = listOf("adsterra" to "Adsterra", "blogger" to "Blogger", "monetag" to "Monetag")
                networks.forEach { (id, name) ->
                    var currentVal by remember(campaignRates) { mutableStateOf(campaignRates[id]?.toString() ?: "1000") }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = name, style = MaterialTheme.typography.bodyMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = currentVal,
                                onValueChange = { currentVal = it },
                                modifier = Modifier.width(100.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { 
                                val newVal = currentVal.toIntOrNull()
                                if (newVal != null) {
                                    repository.updateAdminCampaignRates(id, newVal)
                                }
                            }) {
                                Text("Save")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Online Users Display Range Section
        Text(
            text = "Online Users Display Range",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Control the minimum and maximum simulated online users shown to all app users in real-time:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = minUsersInput,
                        onValueChange = { 
                            minUsersInput = it
                            onlineRangeSuccessMessage = null
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Min Users") },
                        placeholder = { Text("e.g. 50") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = maxUsersInput,
                        onValueChange = { 
                            maxUsersInput = it
                            onlineRangeSuccessMessage = null
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Max Users") },
                        placeholder = { Text("e.g. 1000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Live Active Range:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$onlineUsersMin – $onlineUsersMax Users",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PurplePrimary
                        )
                    }
                }

                if (onlineRangeSuccessMessage != null) {
                    Text(
                        text = onlineRangeSuccessMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = com.example.ui.theme.SuccessGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                        val minVal = minUsersInput.toIntOrNull() ?: 1
                        val maxVal = maxUsersInput.toIntOrNull() ?: minVal
                        val safeMin = if (minVal < 1) 1 else minVal
                        val safeMax = if (maxVal < safeMin) safeMin else maxVal
                        repository.updateAdminOnlineUsersRange(safeMin, safeMax)
                        onlineRangeSuccessMessage = "Saved successfully! Range is now $safeMin to $safeMax."
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Online Users Range")
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // In-App Browser Settings
        Text(
            text = "In-App Browser",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val isChromeSelected = preferredBrowser.equals("chrome", ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isChromeSelected) PurplePrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isChromeSelected) PurpleNeon else Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { repository.updateAdminPreferredBrowser("chrome") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Google Chrome", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        RadioButton(
                            selected = isChromeSelected,
                            onClick = { repository.updateAdminPreferredBrowser("chrome") },
                            colors = RadioButtonDefaults.colors(selectedColor = PurpleNeon)
                        )
                    }
                }

                val isFirefoxSelected = preferredBrowser.equals("firefox", ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isFirefoxSelected) PurplePrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isFirefoxSelected) PurpleNeon else Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { repository.updateAdminPreferredBrowser("firefox") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mozilla Firefox", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        RadioButton(
                            selected = isFirefoxSelected,
                            onClick = { repository.updateAdminPreferredBrowser("firefox") },
                            colors = RadioButtonDefaults.colors(selectedColor = PurpleNeon)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
