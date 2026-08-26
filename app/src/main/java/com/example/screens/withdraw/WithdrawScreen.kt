package com.example.screens.withdraw

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.components.SubScreenTopBar
import com.example.components.SuccessDialogView
import com.example.repository.AppRepository
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary
import com.example.utils.FormatUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WithdrawScreen(
    repository: AppRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val walletState by repository.walletState.collectAsState()
    val serviceSettings by repository.serviceControlSettings.collectAsState()
    val isWithdrawDisabled = serviceSettings.isServiceDisabled("withdraw")
    val withdrawReason = serviceSettings.getServiceReason("withdraw")

    var amountText by remember { mutableStateOf("") }
    val withdrawalMethods by repository.withdrawalMethods.collectAsState()
    var selectedMethod by remember { mutableStateOf("bKash") }
    
    LaunchedEffect(withdrawalMethods) {
        if (withdrawalMethods.isNotEmpty() && !withdrawalMethods.contains(selectedMethod)) {
            selectedMethod = withdrawalMethods.first()
        }
    }
    var accountNumber by remember { mutableStateOf("") }
    var accountHolderName by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var withdrawnAmount by remember { mutableStateOf(0.0) }

    val coroutineScope = rememberCoroutineScope()
    val percentages = listOf(25, 50, 75, 100)

    if (showSuccessDialog) {
        SuccessDialogView(
            title = "Withdrawal Submitted",
            message = "Your withdrawal request for ${FormatUtils.formatCredits(withdrawnAmount)} to $selectedMethod ($accountNumber) has been submitted for admin processing.",
            buttonText = "Done",
            onDismiss = {
                showSuccessDialog = false
                onBackClick()
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("withdraw_screen")
    ) {
        SubScreenTopBar(
            title = "Withdraw",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (isWithdrawDisabled) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ErrorRed.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "উইথড্রয়াল সার্ভিস সাময়িকভাবে বন্ধ আছে",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ErrorRed
                            )
                            Text(
                                text = withdrawReason.ifBlank { "সাময়িক রক্ষণাবেক্ষণের কারণে উইথড্রয়াল বন্ধ আছে।" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Available Balance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Available Balance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = FormatUtils.formatCredits(walletState.balance),
                            style = MaterialTheme.typography.headlineMedium,
                            color = PurplePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = PurplePrimary.copy(alpha = 0.15f),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "🪙",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
                }
            }

            // Withdrawal Method Card (Dynamic from Admin)
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
                        text = "1. Select Withdrawal Method",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (withdrawalMethods.isEmpty()) {
                        Text(
                            text = "No withdrawal methods configured yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        withdrawalMethods.forEach { method ->
                            val isSelected = selectedMethod == method
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { selectedMethod = method }
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) PurplePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .testTag("withdraw_method_${method.lowercase().replace(" ", "_")}"),
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) PurplePrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) PurplePrimary else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Filled.AccountBalance,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = method,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (isSelected) {
                                        Icon(Icons.Filled.Check, contentDescription = null, tint = PurplePrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Withdrawal Amount Card
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
                        text = "2. Enter Withdrawal Amount",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it.filter { ch -> ch.isDigit() || ch == '.' }
                            errorMessage = null
                        },
                        placeholder = { Text("0.00") },
                        prefix = {
                            Text(text = "🪙 ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        },
                        textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_amount_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleNeon,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        isError = errorMessage != null
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Percentage Selectors
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(percentages) { pct ->
                            val targetAmt = (walletState.balance * (pct / 100.0)).toInt()
                            FilterChip(
                                selected = amountText == targetAmt.toString(),
                                onClick = {
                                    amountText = targetAmt.toString()
                                    errorMessage = null
                                },
                                label = {
                                    Text(
                                        text = "$pct%",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PurplePrimary,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            // User Account Details Card
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
                        text = "3. Your Payout Account Information",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = {
                            accountNumber = it
                            errorMessage = null
                        },
                        label = { Text("Your $selectedMethod Number / Account") },
                        placeholder = { Text("017XXXXXXXX / Account No") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_account_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleNeon
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = accountHolderName,
                        onValueChange = {
                            accountHolderName = it
                            errorMessage = null
                        },
                        label = { Text("Account Holder Name (Optional)") },
                        placeholder = { Text("Your Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_holder_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleNeon
                        )
                    )
                }
            }

            if (errorMessage != null) {
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

            // Submit Button
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        errorMessage = "Please enter a valid withdrawal amount."
                        return@Button
                    }
                    if (amount > walletState.balance) {
                        errorMessage = "Insufficient balance. Maximum available: ${FormatUtils.formatCredits(walletState.balance)}"
                        return@Button
                    }
                    if (accountNumber.isBlank()) {
                        errorMessage = "Please enter your $selectedMethod account or phone number."
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null

                    coroutineScope.launch {
                        delay(700)
                        val result = repository.withdraw(amount, selectedMethod, accountNumber.trim())
                        isLoading = false
                        result.onSuccess {
                            withdrawnAmount = amount
                            showSuccessDialog = true
                        }.onFailure { ex ->
                            errorMessage = ex.message ?: "Withdrawal failed. Please try again."
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_withdraw_button"),
                enabled = !isLoading && !isWithdrawDisabled,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isWithdrawDisabled) Color.Gray else PurplePrimary,
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
                        text = "Processing...",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = if (isWithdrawDisabled) "Withdrawal Closed" else "Submit Withdrawal Request",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
