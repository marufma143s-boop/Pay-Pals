package com.example.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.model.PopupNoticeSettings
import com.example.repository.AppRepository
import com.example.ui.theme.PurplePrimary
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import com.example.ui.theme.SuccessGreen

@Composable
fun AdminPopupSettingsScreen(repository: AppRepository) {
    val coroutineScope = rememberCoroutineScope()
    var popupSettings by remember { mutableStateOf(repository.maintenanceSettings.value.popupNotice ?: PopupNoticeSettings()) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    
    // We fetch current whenever we can, although we might just use the general settings one. 
    // Wait, popup settings are stored in AppRepository? Yes, they are part of MaintenanceSettings probably.
    // Or we should store them in `app_settings/popup_notice`. Let's store them in MaintenanceSettings.
    val maintenanceSettings by repository.maintenanceSettings.collectAsState()
    
    LaunchedEffect(maintenanceSettings) {
        popupSettings = maintenanceSettings.popupNotice ?: PopupNoticeSettings()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
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
                            .clip(RoundedCornerShape(22.dp))
                            .background(PurplePrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = PurplePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Popup Notice Configuration",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Configure the popup dialog that appears when users launch the app.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Popup Notice", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = popupSettings.isEnabled,
                            onCheckedChange = { popupSettings = popupSettings.copy(isEnabled = it) },
                            colors = SwitchDefaults.colors(checkedTrackColor = SuccessGreen)
                        )
                    }
                    
                    OutlinedTextField(
                        value = popupSettings.title,
                        onValueChange = { popupSettings = popupSettings.copy(title = it) },
                        label = { Text("Popup Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    
                    OutlinedTextField(
                        value = popupSettings.description,
                        onValueChange = { popupSettings = popupSettings.copy(description = it) },
                        label = { Text("Popup Description") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 3
                    )
                    
                    OutlinedTextField(
                        value = popupSettings.imageUrl,
                        onValueChange = { popupSettings = popupSettings.copy(imageUrl = it) },
                        label = { Text("Image URL (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Social Media Links", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = popupSettings.showSocialMedia,
                            onCheckedChange = { popupSettings = popupSettings.copy(showSocialMedia = it) },
                            colors = SwitchDefaults.colors(checkedTrackColor = SuccessGreen)
                        )
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Action Button", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = popupSettings.showActionButton,
                            onCheckedChange = { popupSettings = popupSettings.copy(showActionButton = it) },
                            colors = SwitchDefaults.colors(checkedTrackColor = SuccessGreen)
                        )
                    }
                    
                    if (popupSettings.showActionButton) {
                        OutlinedTextField(
                            value = popupSettings.buttonText,
                            onValueChange = { popupSettings = popupSettings.copy(buttonText = it) },
                            label = { Text("Button Text") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { popupSettings = popupSettings.copy(actionType = "INTERNAL") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = if (popupSettings.actionType == "INTERNAL") PurplePrimary.copy(alpha = 0.1f) else Color.Transparent)
                            ) { Text("Internal Route") }
                            OutlinedButton(
                                onClick = { popupSettings = popupSettings.copy(actionType = "EXTERNAL") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = if (popupSettings.actionType == "EXTERNAL") PurplePrimary.copy(alpha = 0.1f) else Color.Transparent)
                            ) { Text("External URL") }
                        }
                        
                        if (popupSettings.actionType == "INTERNAL") {
                            OutlinedTextField(
                                value = popupSettings.internalDestination,
                                onValueChange = { popupSettings = popupSettings.copy(internalDestination = it) },
                                label = { Text("Internal Destination Route (e.g. refer, deposit)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        } else {
                            OutlinedTextField(
                                value = popupSettings.externalUrl,
                                onValueChange = { popupSettings = popupSettings.copy(externalUrl = it) },
                                label = { Text("External Web URL") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                    
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                val newSettings = maintenanceSettings.copy(popupNotice = popupSettings)
                                repository.updateMaintenanceSettings(newSettings)
                                isLoading = false
                                showSuccessMessage = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = androidx.compose.ui.graphics.Color.White)
                        } else {
                            Text("Save Configuration", fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    if (showSuccessMessage) {
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(3000)
                            showSuccessMessage = false
                        }
                        Text(
                            text = "Settings saved successfully!",
                            color = SuccessGreen,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
