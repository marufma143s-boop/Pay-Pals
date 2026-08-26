package com.example.screens.deposit

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.SubScreenTopBar
import com.example.components.SuccessDialogView
import com.example.model.DepositMethodItem
import com.example.repository.AppRepository
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WalletGradientBrush
import com.example.utils.FormatUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DepositScreen(
    repository: AppRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val walletState by repository.walletState.collectAsState()
    val serviceSettings by repository.serviceControlSettings.collectAsState()
    val isDepositDisabled = serviceSettings.isServiceDisabled("deposit")
    val depositReason = serviceSettings.getServiceReason("deposit")
    
    val depositMethods by repository.depositMethods.collectAsState()
    var selectedMethodItem by remember { mutableStateOf<DepositMethodItem?>(null) }

    LaunchedEffect(depositMethods) {
        if (depositMethods.isNotEmpty() && (selectedMethodItem == null || depositMethods.none { it.name == selectedMethodItem?.name })) {
            selectedMethodItem = depositMethods.first()
        }
    }

    var amountText by remember { mutableStateOf("") }
    var senderNumber by remember { mutableStateOf("") }
    var transactionIdText by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var depositedAmount by remember { mutableStateOf(0.0) }
    val quickAmounts = listOf(500, 1000, 2000, 5000, 10000)
    val coroutineScope = rememberCoroutineScope()

    if (showSuccessDialog) {
        SuccessDialogView(
            title = "Deposit Request Submitted",
            message = "Your deposit request for ${FormatUtils.formatCredits(depositedAmount)} with Transaction ID: $transactionIdText has been submitted for admin verification.",
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
            .testTag("deposit_screen")
    ) {
        SubScreenTopBar(
            title = "Deposit",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isDepositDisabled) {
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
                                text = "ডিপোজিট সার্ভিস সাময়িকভাবে বন্ধ আছে",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ErrorRed
                            )
                            Text(
                                text = depositReason.ifBlank { "সাময়িক রক্ষণাবেক্ষণের কারণে ডিপোজিট বন্ধ আছে।" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Wallet Balance Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(WalletGradientBrush)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Current Balance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = FormatUtils.formatCredits(walletState.balance),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // 1. Select Payment Method
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "1. Select Payment Method",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    if (depositMethods.isEmpty()) {
                        Text(
                            text = "No deposit methods available right now. Please contact support.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        depositMethods.forEach { methodItem ->
                            val isSelected = selectedMethodItem?.name == methodItem.name
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedMethodItem = methodItem }
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) PurplePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .testTag("deposit_method_${methodItem.name.replace(" ", "_")}"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) PurplePrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) PurplePrimary else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Filled.AccountBalance,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = methodItem.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Filled.Check, contentDescription = null, tint = PurplePrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Deposit Number & Instructions Card
            selectedMethodItem?.let { currentMethod ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Payment Instructions",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = PurplePrimary
                            )
                        }

                        if (currentMethod.number.isNotBlank()) {
                            Text(
                                text = "Send money to this ${currentMethod.name} number:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = currentMethod.number,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Button(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(currentMethod.number))
                                            Toast.makeText(context, "Number copied to clipboard", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Please send money to the number above. After completing your payment, enter the deposit amount, your sender number, and transaction ID (TrxID) below.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 3. Deposit Amount Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "2. Enter Deposit Amount",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*\$"))) {
                                amountText = newValue
                                errorMessage = null
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deposit_amount_input"),
                        leadingIcon = {
                            Text(
                                text = "🪙",
                                modifier = Modifier.padding(start = 12.dp),
                                fontSize = 18.sp
                            )
                        },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleNeon,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Text(
                        text = "Quick Select",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickAmounts) { quickAmount ->
                            FilterChip(
                                selected = amountText == quickAmount.toString(),
                                onClick = {
                                    amountText = quickAmount.toString()
                                    errorMessage = null
                                },
                                label = {
                                    Text(
                                        text = FormatUtils.formatCredits(quickAmount.toDouble()),
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

            // 4. Payment Confirmation Details Card (Sender Number & Transaction ID)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "3. Payment Verification Details",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = senderNumber,
                        onValueChange = {
                            senderNumber = it
                            errorMessage = null
                        },
                        label = { Text("Sender Mobile Number (যে নম্বর থেকে পাঠিয়েছেন)") },
                        placeholder = { Text("017XXXXXXXX") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deposit_sender_number"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleNeon,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    OutlinedTextField(
                        value = transactionIdText,
                        onValueChange = {
                            transactionIdText = it.uppercase()
                            errorMessage = null
                        },
                        label = { Text("Transaction ID (TrxID)") },
                        placeholder = { Text("e.g. 9J87AKL2X") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deposit_transaction_id"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleNeon,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ErrorRed.copy(alpha = 0.12f),
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
                        errorMessage = "Please enter a valid deposit amount."
                        return@Button
                    }
                    if (selectedMethodItem == null) {
                        errorMessage = "Please select a payment method."
                        return@Button
                    }
                    if (senderNumber.isBlank()) {
                        errorMessage = "Please enter your sender mobile/account number."
                        return@Button
                    }
                    if (transactionIdText.isBlank()) {
                        errorMessage = "Please enter the payment Transaction ID (TrxID)."
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null
                    val currentMethod = selectedMethodItem!!

                    coroutineScope.launch {
                        delay(700)
                        val result = repository.deposit(
                            amount = amount,
                            method = currentMethod.name,
                            methodNumber = currentMethod.number,
                            senderNumber = senderNumber.trim(),
                            trxId = transactionIdText.trim()
                        )
                        isLoading = false
                        result.onSuccess {
                            depositedAmount = amount
                            showSuccessDialog = true
                        }.onFailure { ex ->
                            errorMessage = ex.message ?: "Deposit failed. Please try again."
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_deposit_button"),
                enabled = !isLoading && !isDepositDisabled,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDepositDisabled) Color.Gray else PurplePrimary,
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
                        text = "Submitting...",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = if (isDepositDisabled) "Deposit Closed" else "Submit Deposit Request",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
