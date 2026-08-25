import os

content = """package com.example.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.repository.AppRepository
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary

@Composable
fun AdminSettingsScreen(repository: AppRepository) {
    val preferredBrowser by repository.preferredBrowser.collectAsState()
    val campaignRates by repository.campaignRates.collectAsState()
    val depositMethods by repository.depositMethods.collectAsState()
    val withdrawalMethods by repository.withdrawalMethods.collectAsState()

    var newDepMethod by remember { mutableStateOf("") }
    var newWithMethod by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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

        Spacer(modifier = Modifier.height(8.dp))

        // Deposit Methods Section
        Text(
            text = "Deposit Methods",
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
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newDepMethod,
                        onValueChange = { newDepMethod = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("e.g. bKash, PayPal") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { 
                        if (newDepMethod.isNotBlank()) {
                            repository.addAdminDepositMethod(newDepMethod)
                            newDepMethod = ""
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
                
                depositMethods.forEach { method ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = method, style = MaterialTheme.typography.bodyMedium)
                        IconButton(onClick = { repository.removeAdminDepositMethod(method) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = ErrorRed)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Withdrawal Methods Section
        Text(
            text = "Withdrawal Methods",
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
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newWithMethod,
                        onValueChange = { newWithMethod = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("e.g. bKash, Binance") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { 
                        if (newWithMethod.isNotBlank()) {
                            repository.addAdminWithdrawalMethod(newWithMethod)
                            newWithMethod = ""
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
                
                withdrawalMethods.forEach { method ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = method, style = MaterialTheme.typography.bodyMedium)
                        IconButton(onClick = { repository.removeAdminWithdrawalMethod(method) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = ErrorRed)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
    }
}
"""

with open("app/src/main/java/com/example/screens/admin/AdminSettingsScreen.kt", "w") as f:
    f.write(content)
