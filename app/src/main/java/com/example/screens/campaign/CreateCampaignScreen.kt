package com.example.screens.campaign

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.SubScreenTopBar
import com.example.components.SuccessDialogView
import com.example.model.CampaignPackage
import com.example.repository.AppRepository
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen
import com.example.utils.FormatUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class NetworkOption(
    val id: String,
    val name: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
fun CreateCampaignScreen(
    repository: AppRepository,
    onBackClick: (() -> Unit)? = null,
    onNavigateToCampaignList: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val walletState by repository.walletState.collectAsState()
    val campaignRates by repository.campaignRates.collectAsState()
    
    val networks = listOf(
        NetworkOption(
            id = "adsterra",
            name = "Adsterra",
            subtitle = "Direct Smartlink & CPA Ads",
            icon = Icons.Filled.AdsClick,
            accentColor = Color(0xFFF59E0B)
        ),
        NetworkOption(
            id = "blogger",
            name = "Blogger",
            subtitle = "Blog Articles & Web Pages",
            icon = Icons.Filled.Article,
            accentColor = Color(0xFF3B82F6)
        ),
        NetworkOption(
            id = "monetag",
            name = "Monetag",
            subtitle = "Direct Link & Pop Traffic",
            icon = Icons.Filled.MonetizationOn,
            accentColor = Color(0xFF10B981)
        )
    )

    var selectedNetwork by remember { mutableStateOf(networks.first().id) }
    var title by remember { mutableStateOf("") }
    var targetLink by remember { mutableStateOf("") }
    var targetViewsStr by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val currentNetworkObj = networks.first { it.id == selectedNetwork }
    val currentRatePer100 = campaignRates[selectedNetwork] ?: 1000
    
    val targetViewsInt = targetViewsStr.toIntOrNull() ?: 0
    val calculatedCost = (targetViewsInt / 100.0) * currentRatePer100

    if (showSuccessDialog) {
        SuccessDialogView(
            title = "Campaign Live!",
            message = "Your campaign '\$title' has been launched successfully.",
            onDismiss = {
                showSuccessDialog = false
                onNavigateToCampaignList?.invoke() ?: onBackClick?.invoke()
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SubScreenTopBar(
            title = "Run New Campaign",
            onBackClick = onBackClick ?: {}
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            item {
                Text(
                    text = "1. Select Advertising Network",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    networks.forEach { network ->
                        val isSelected = selectedNetwork == network.id
                        val rate = campaignRates[network.id] ?: 1000
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) network.accentColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) network.accentColor else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedNetwork = network.id }
                                .testTag("network_option_\${network.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(
                                            network.accentColor.copy(alpha = 0.15f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = network.icon,
                                        contentDescription = network.name,
                                        tint = network.accentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = network.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = network.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = network.accentColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "\${rate} pts/100v",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = network.accentColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "2. Campaign Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Campaign Title") },
                    placeholder = { Text("e.g. My Website Promotion") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Title, contentDescription = null, tint = PurpleNeon)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleNeon,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = targetLink,
                    onValueChange = { targetLink = it },
                    label = { Text("Target URL") },
                    placeholder = { Text("https://example.com") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Link, contentDescription = null, tint = PurpleNeon)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleNeon,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = targetViewsStr,
                    onValueChange = { targetViewsStr = it.filter { char -> char.isDigit() } },
                    label = { Text("Number of Views") },
                    placeholder = { Text("Enter views (e.g., 500)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Public, contentDescription = null, tint = PurpleNeon)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleNeon,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = currentNetworkObj.accentColor.copy(alpha = 0.05f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Campaign Summary",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Target Views:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${targetViewsInt} Views",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Price:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = FormatUtils.formatCredits(calculatedCost),
                                style = MaterialTheme.typography.titleMedium,
                                color = PurpleNeon,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Wallet Balance:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = FormatUtils.formatCredits(walletState.balance),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (walletState.balance >= calculatedCost) MaterialTheme.colorScheme.onSurfaceVariant else ErrorRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            if (errorMessage != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ErrorRed.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ErrorRed,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Submit Button
            item {
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            errorMessage = "Campaign title is required."
                            return@Button
                        }
                        if (targetLink.isBlank() || !FormatUtils.isValidUrl(targetLink)) {
                            errorMessage = "Please enter a valid target URL."
                            return@Button
                        }
                        if (targetViewsInt <= 0) {
                            errorMessage = "Please enter a valid number of views."
                            return@Button
                        }
                        if (walletState.balance < calculatedCost) {
                            errorMessage = "Insufficient balance. Please deposit funds."
                            return@Button
                        }

                        isLoading = true
                        errorMessage = null
                        
                        // Fake a CampaignPackage to pass down to old signature
                        val pkg = CampaignPackage(
                            id = "custom",
                            
                            targetViews = targetViewsInt,
                            price = calculatedCost,
                            description = "Custom target",
                            isPopular = false
                        )
                        
                        coroutineScope.launch {
                            delay(900)
                            val result = repository.createCampaign(
                                title = title,
                                networkType = selectedNetwork,
                                targetLink = targetLink,
                                pkg = pkg
                            )
                            isLoading = false
                            result.onSuccess {
                                showSuccessDialog = true
                            }.onFailure { ex ->
                                errorMessage = ex.message ?: "Failed to create campaign."
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("submit_campaign_button"),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurplePrimary,
                        contentColor = Color.White
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Launching Campaign...",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Campaign,
                            contentDescription = "Submit",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Submit Campaign",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
