package com.example.screens.campaign

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Link
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.components.PackageCard
import com.example.components.SubScreenTopBar
import com.example.components.SuccessDialogView
import com.example.data.DemoDataProvider
import com.example.model.CampaignPackage
import com.example.repository.AppRepository
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary
import com.example.utils.FormatUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CreateCampaignScreen(
    repository: AppRepository,
    onBackClick: (() -> Unit)? = null,
    onNavigateToCampaignList: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val walletState by repository.walletState.collectAsState()
    val packages = DemoDataProvider.packages

    var title by remember { mutableStateOf("") }
    var targetLink by remember { mutableStateOf("") }
    var selectedPackage by remember { mutableStateOf(packages[1]) } // default 5,000 Credits / 1,000 Views

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    if (showSuccessDialog) {
        SuccessDialogView(
            title = "Campaign Created Successfully",
            message = "Your campaign '${title}' targeting ${FormatUtils.formatCount(selectedPackage.targetViews)} views is now active and delivering traffic.",
            buttonText = "View Campaigns",
            onDismiss = {
                showSuccessDialog = false
                title = ""
                targetLink = ""
                onNavigateToCampaignList?.invoke()
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("create_campaign_screen")
    ) {
        if (onBackClick != null) {
            SubScreenTopBar(
                title = "Create Campaign",
                onBackClick = onBackClick
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Create Campaign",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Campaign Title Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Campaign Title",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                errorMessage = null
                            },
                            placeholder = { Text("Campaign Name") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Title,
                                    contentDescription = "Title",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("campaign_title_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PurplePrimary
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            // Target Link Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Target Link",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = targetLink,
                            onValueChange = {
                                targetLink = it
                                errorMessage = null
                            },
                            placeholder = { Text("https://example.com") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Link,
                                    contentDescription = "Target Link",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("campaign_link_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PurplePrimary
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            // Select Package Heading
            item {
                Text(
                    text = "Select Package",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }

            // 4 Package Options
            items(packages) { pkg ->
                PackageCard(
                    pkg = pkg,
                    isSelected = selectedPackage.id == pkg.id,
                    onSelect = {
                        selectedPackage = pkg
                        errorMessage = null
                    }
                )
            }

            // Selected Package Summary & Submission
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Selected Package Summary",
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
                                text = "Price:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = FormatUtils.formatCredits(selectedPackage.price),
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
                                text = "Target Views:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${FormatUtils.formatCount(selectedPackage.targetViews)} Views",
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
                                text = "Wallet Balance:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = FormatUtils.formatCredits(walletState.balance),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        if (walletState.balance < selectedPackage.price) {
                            errorMessage = "Insufficient balance. Please deposit funds or choose a smaller package."
                            return@Button
                        }

                        isLoading = true
                        errorMessage = null

                        coroutineScope.launch {
                            delay(900)
                            val result = repository.createCampaign(title, targetLink, selectedPackage)
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
