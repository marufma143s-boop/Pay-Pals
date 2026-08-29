package com.example.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NetworkTaskConfig
import com.example.repository.AppRepository
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen

@Composable
fun AdminAdwardSettingsSection(repository: AppRepository) {
    val adwardSettings by repository.adwardSettings.collectAsState()

    var selectedNetworkTab by remember { mutableStateOf("adstra") }
    var showSavedSnackbar by remember { mutableStateOf(false) }

    // Editable state for selected network
    val currentConfig = when (selectedNetworkTab) {
        "adstra" -> adwardSettings.adstraConfig
        "blogger" -> adwardSettings.bloggerConfig
        "monetag" -> adwardSettings.monetagConfig
        else -> adwardSettings.adstraConfig
    }

    var visitDurationInput by remember(selectedNetworkTab, currentConfig) {
        mutableStateOf(currentConfig.visitDurationSeconds.toString())
    }
    var rewardPointsInput by remember(selectedNetworkTab, currentConfig) {
        mutableStateOf(currentConfig.rewardPoints.toInt().toString())
    }
    var breakFreqInput by remember(selectedNetworkTab, currentConfig) {
        mutableStateOf(currentConfig.breakFrequency.toString())
    }
    var breakDurationInput by remember(selectedNetworkTab, currentConfig) {
        mutableStateOf(currentConfig.breakDurationMinutes.toString())
    }
    var dailyLimitInput by remember(selectedNetworkTab, currentConfig) {
        mutableStateOf(currentConfig.dailyLimit.toString())
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showSavedSnackbar) {
        if (showSavedSnackbar) {
            snackbarHostState.showSnackbar("Network settings saved successfully!")
            showSavedSnackbar = false
        }
    }

    fun saveConfig() {
        val duration = visitDurationInput.toIntOrNull() ?: 15
        val reward = rewardPointsInput.toDoubleOrNull() ?: 25.0
        val breakFreq = breakFreqInput.toIntOrNull() ?: 10
        val breakDur = breakDurationInput.toIntOrNull() ?: 5
        val limit = dailyLimitInput.toIntOrNull() ?: 50

        repository.updateNetworkTaskConfig(
            networkId = selectedNetworkTab,
            visitDurationSeconds = duration.coerceAtLeast(3),
            rewardPoints = reward.coerceAtLeast(1.0),
            breakFrequency = breakFreq.coerceAtLeast(0),
            breakDurationMinutes = breakDur.coerceAtLeast(0),
            dailyLimit = limit.coerceAtLeast(1)
        )
        showSavedSnackbar = true
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, PurpleNeon.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PurplePrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = PurpleNeon,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Adward & Task Configurations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "প্রতিটি নেটওয়ার্কের ভিজিট টাইম, পয়েন্ট, বিরতির সময় ও লিমিট নির্ধারণ করুন",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Network Tabs (Adsterra, Blogger, Monetag)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabs = listOf(
                    Triple("adstra", "Adsterra", Icons.Default.AdsClick),
                    Triple("blogger", "Blogger", Icons.Default.MenuBook),
                    Triple("monetag", "Monetag", Icons.Default.MonetizationOn)
                )

                tabs.forEach { (id, label, icon) ->
                    val isSelected = selectedNetworkTab == id
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PurplePrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        onClick = { selectedNetworkTab = id }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Configuration Form Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "${currentConfig.displayName} Task Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 1. Visit Duration Seconds
                    OutlinedTextField(
                        value = visitDurationInput,
                        onValueChange = { visitDurationInput = it },
                        label = { Text("ভিজিট টাইম (Seconds)") },
                        supportingText = { Text("কত সেকেন্ড সাইটে থাকতে হবে (যেমন: 15, 20, 30s)") },
                        leadingIcon = { Icon(Icons.Default.HourglassBottom, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 2. Reward Points
                    OutlinedTextField(
                        value = rewardPointsInput,
                        onValueChange = { rewardPointsInput = it },
                        label = { Text("পয়েন্ট/ক্রেডিট রিওয়ার্ড (🪙 Credits)") },
                        supportingText = { Text("প্রতিটি কাজ সম্পন্ন করলে কত পয়েন্ট পাবে (যেমন: 25)") },
                        leadingIcon = { Icon(Icons.Default.Paid, contentDescription = null, tint = GoldAccent) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 3. Break Frequency
                    OutlinedTextField(
                        value = breakFreqInput,
                        onValueChange = { breakFreqInput = it },
                        label = { Text("বিরতি ফ্রিকোয়েন্সি (কয়টি কাজের পর)") },
                        supportingText = { Text("প্রতি কতটি ভিজিটের পর ইউজারকে বিরতি দেওয়া হবে (যেমন: 10 বা 20 টি কাজ)") },
                        leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 4. Break Duration Minutes
                    OutlinedTextField(
                        value = breakDurationInput,
                        onValueChange = { breakDurationInput = it },
                        label = { Text("বিরতির সময় (Minutes)") },
                        supportingText = { Text("ইউজার কত মিনিট বিরতিতে থাকবে (যেমন: 5, 10, 15 মিনিট)") },
                        leadingIcon = { Icon(Icons.Default.Bedtime, contentDescription = null, tint = PurpleNeon) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 5. Daily Task Limit
                    OutlinedTextField(
                        value = dailyLimitInput,
                        onValueChange = { dailyLimitInput = it },
                        label = { Text("দৈনিক কাজের লিমিট (Daily Limit)") },
                        supportingText = { Text("প্রতিদিন সর্বোচ্চ কয়টি কাজ করতে পারবে (যেমন: 50, 80, 120)") },
                        leadingIcon = { Icon(Icons.Default.FormatListNumbered, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Save Button
                    Button(
                        onClick = { saveConfig() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_adward_config_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save ${currentConfig.displayName} Settings",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
