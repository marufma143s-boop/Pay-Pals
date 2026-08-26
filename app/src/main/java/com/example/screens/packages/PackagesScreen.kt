package com.example.screens.packages

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PackageOrder
import com.example.model.PackageOrderStatus
import com.example.model.PaidPackage
import com.example.repository.AppRepository
import com.example.ui.theme.*
import com.example.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackagesScreen(
    repository: AppRepository,
    onBackClick: (() -> Unit)? = null,
    onNavigateToDeposit: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val paidPackages by repository.paidPackages.collectAsState()
    val userOrders by repository.userPackageOrders.collectAsState()
    val walletState by repository.walletState.collectAsState()
    val userProfile by repository.userProfile.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Packages, 1 = History
    var historyFilter by remember { mutableStateOf("ALL") }

    // Wizard State
    var orderStep by remember { mutableStateOf(1) } // 1: Select, 2: Form, 3: Summary, 4: Success
    var selectedPackageForOrder by remember { mutableStateOf<PaidPackage?>(null) }
    var inputTitle by remember { mutableStateOf("") }
    var inputLink by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = PurpleNeon,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Paid Packages",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = PurplePrimary.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PurpleNeon.copy(alpha = 0.4f)),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = PurpleNeon,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = FormatUtils.formatCredits(walletState.balance),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Only show Tabs if we are in Step 1 or History
            if (orderStep == 1) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        indicator = {},
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { 
                                focusManager.clearFocus()
                                selectedTab = 0 
                            },
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selectedTab == 0) PurplePrimary else Color.Transparent
                                )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    tint = if (selectedTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Packages (${paidPackages.size})",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Tab(
                            selected = selectedTab == 1,
                            onClick = { 
                                focusManager.clearFocus()
                                selectedTab = 1 
                            },
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selectedTab == 1) PurplePrimary else Color.Transparent
                                )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = if (selectedTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "History (${userOrders.size})",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                // Wizard UI
                if (orderStep in 1..3) {
                    PackageOrderStepper(currentStep = orderStep)
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (orderStep == 1) {
                        if (paidPackages.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Inventory2,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = "No packages available right now.",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .background(PurplePrimary.copy(alpha = 0.15f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.TrendingUp,
                                                    contentDescription = null,
                                                    tint = PurpleNeon,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "Boost Traffic & Views",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Select any package below to proceed with your order.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                items(paidPackages) { pkg ->
                                    val isSelected = selectedPackageForOrder?.id == pkg.id
                                    PaidPackageSelectableCard(
                                        pkg = pkg,
                                        isSelected = isSelected,
                                        onClick = { selectedPackageForOrder = pkg }
                                    )
                                }
                            }
                        }
                    } else if (orderStep == 2) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("Enter Order Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            
                            OutlinedTextField(
                                value = inputTitle,
                                onValueChange = { inputTitle = it; errorMessage = null },
                                label = { Text("Title (টাইটেল)") },
                                placeholder = { Text("e.g. My Website Boost") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) }
                            )

                            OutlinedTextField(
                                value = inputLink,
                                onValueChange = { inputLink = it; errorMessage = null },
                                label = { Text("Target Link / URL (টার্গেট লিংক)") },
                                placeholder = { Text("https://example.com/your-target") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) }
                            )

                            if (errorMessage != null) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = ErrorRed.copy(alpha = 0.1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = errorMessage!!,
                                            color = ErrorRed,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    } else if (orderStep == 3) {
                        val pkg = selectedPackageForOrder!!
                        val hasEnoughBalance = walletState.balance >= pkg.price

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("Order Summary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SummaryRow("Package Name", pkg.name, PurpleNeon)
                                    SummaryRow("Target Views", "${FormatUtils.formatNumber(pkg.views)} Views", MaterialTheme.colorScheme.onSurface)
                                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                                    SummaryRow("Title", inputTitle, MaterialTheme.colorScheme.onSurface)
                                    SummaryRow("Link", inputLink, MaterialTheme.colorScheme.onSurface)
                                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                                    SummaryRow("Current Balance", FormatUtils.formatCredits(walletState.balance), MaterialTheme.colorScheme.onSurface)
                                    SummaryRow("Package Cost", "- ${FormatUtils.formatCredits(pkg.price)}", ErrorRed)
                                    val remaining = walletState.balance - pkg.price
                                    SummaryRow("Remaining Balance", FormatUtils.formatCredits(remaining), if(remaining >= 0) SuccessGreen else ErrorRed)
                                }
                            }

                            if (!hasEnoughBalance) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = ErrorRed.copy(alpha = 0.1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Insufficient balance! Please deposit credits to order.",
                                            color = ErrorRed,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            if (errorMessage != null) {
                                Text(text = errorMessage!!, color = ErrorRed, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                    } else if (orderStep == 4) {
                        // Success Screen
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = SuccessGreen,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Order Successful!",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your package order has been placed and is waiting for admin approval.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    orderStep = 1
                                    selectedPackageForOrder = null
                                    inputTitle = ""
                                    inputLink = ""
                                    selectedTab = 1 // Go to history
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("View Order History")
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    orderStep = 1
                                    selectedPackageForOrder = null
                                    inputTitle = ""
                                    inputLink = ""
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Back to Packages")
                            }
                        }
                    }
                }

                // Fixed Bottom Bar for Next/Back buttons (Only for steps 1 to 3)
                if (orderStep in 1..3) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (orderStep > 1) {
                                OutlinedButton(
                                    onClick = { 
                                        focusManager.clearFocus()
                                        errorMessage = null
                                        orderStep -= 1 
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isSubmitting
                                ) {
                                    Text("Back")
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                            
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    errorMessage = null
                                    if (orderStep == 1) {
                                        if (selectedPackageForOrder != null) orderStep = 2
                                    } else if (orderStep == 2) {
                                        val cleanLink = inputLink.trim()
                                        val cleanTitle = inputTitle.trim()
                                        if (cleanTitle.isBlank()) {
                                            errorMessage = "Please enter a title."
                                        } else if (cleanLink.isBlank() || (!cleanLink.startsWith("http://") && !cleanLink.startsWith("https://"))) {
                                            errorMessage = "Please enter a valid URL starting with http:// or https://"
                                        } else {
                                            orderStep = 3
                                        }
                                    } else if (orderStep == 3) {
                                        val pkg = selectedPackageForOrder!!
                                        if (walletState.balance < pkg.price) {
                                            errorMessage = "Insufficient wallet balance."
                                            return@Button
                                        }
                                        isSubmitting = true
                                        val result = repository.orderPackage(pkg, inputTitle.trim(), inputLink.trim())
                                        isSubmitting = false
                                        if (result.isSuccess) {
                                            orderStep = 4 // Success
                                        } else {
                                            errorMessage = result.exceptionOrNull()?.message ?: "Failed to place order."
                                        }
                                    }
                                },
                                modifier = if (orderStep == 1) Modifier.fillMaxWidth() else Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                enabled = (orderStep == 1 && selectedPackageForOrder != null) || (orderStep > 1 && !isSubmitting)
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                                } else {
                                    Text(if (orderStep == 3) "Confirm" else "Next")
                                }
                            }
                        }
                    }
                }
            } else {
                // Tab 1: Order History
                Column(modifier = Modifier.fillMaxSize()) {
                    val filterOptions = listOf(
                        "ALL" to "All",
                        "PENDING" to "Pending",
                        "RUNNING" to "Running",
                        "COMPLETED" to "Completed",
                        "REJECTED" to "Rejected"
                    )

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filterOptions) { (key, label) ->
                            val isSelected = historyFilter == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { historyFilter = key },
                                label = {
                                    val count = if (key == "ALL") userOrders.size else userOrders.count { it.status.name == key }
                                    Text("$label ($count)")
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null
                            )
                        }
                    }

                    val filteredOrders = if (historyFilter == "ALL") {
                        userOrders
                    } else {
                        userOrders.filter { it.status.name == historyFilter }
                    }

                    if (filteredOrders.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.History,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = if (userOrders.isEmpty()) "You haven't ordered any packages yet." else "No orders found for this status.",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (userOrders.isEmpty()) {
                                    Button(
                                        onClick = { selectedTab = 0 },
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        Text("Browse Packages")
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredOrders) { order ->
                                UserPackageOrderCard(order = order)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = valueColor, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 16.dp))
    }
}

@Composable
fun PackageOrderStepper(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepIndicator(step = 1, title = "Select", isActive = currentStep >= 1)
        Divider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = if(currentStep >= 2) PurplePrimary else Color.Gray.copy(alpha = 0.3f))
        StepIndicator(step = 2, title = "Details", isActive = currentStep >= 2)
        Divider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = if(currentStep >= 3) PurplePrimary else Color.Gray.copy(alpha = 0.3f))
        StepIndicator(step = 3, title = "Confirm", isActive = currentStep >= 3)
    }
}

@Composable
fun StepIndicator(step: Int, title: String, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(if (isActive) PurplePrimary else Color.Gray.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "$step", color = if (isActive) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = if (isActive) PurplePrimary else Color.Gray)
    }
}

@Composable
fun PaidPackageSelectableCard(
    pkg: PaidPackage,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isSelected) 8.dp else 2.dp, RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) PurplePrimary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PurplePrimary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Name & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pkg.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (pkg.badge.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (pkg.badge.lowercase()) {
                            "popular", "hot" -> Color(0xFFFF9800)
                            "best value", "vip" -> PurpleNeon
                            "starter" -> SuccessGreen
                            else -> PurplePrimary
                        }
                    ) {
                        Text(
                            text = pkg.badge,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            if (pkg.description.isNotBlank()) {
                Text(
                    text = pkg.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Key Metrics: Views & Price
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = PurpleNeon, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = FormatUtils.formatNumber(pkg.views),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text("Target Views", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }

                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .width(1.dp)
                            .background(Color.Gray.copy(alpha = 0.3f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = FormatUtils.formatCredits(pkg.price),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                        Text("Required Credits", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun UserPackageOrderCard(order: PackageOrder) {
    val context = LocalContext.current
    val statusColor = when (order.status) {
        PackageOrderStatus.PENDING -> Color(0xFFF59E0B)
        PackageOrderStatus.RUNNING -> Color(0xFF3B82F6)
        PackageOrderStatus.COMPLETED -> SuccessGreen
        PackageOrderStatus.REJECTED -> ErrorRed
    }

    val statusIcon = when (order.status) {
        PackageOrderStatus.PENDING -> Icons.Default.HourglassEmpty
        PackageOrderStatus.RUNNING -> Icons.Default.PlayArrow
        PackageOrderStatus.COMPLETED -> Icons.Default.CheckCircle
        PackageOrderStatus.REJECTED -> Icons.Default.Cancel
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.packageName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = order.status.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            Text(
                text = "Title: ${order.title}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Views: ${FormatUtils.formatNumber(order.views)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PurpleNeon
                )
                Text(
                    text = "Cost: ${FormatUtils.formatCredits(order.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SuccessGreen
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null, tint = PurpleNeon, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = order.targetLink,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Target Link", order.targetLink)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(order.targetLink))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = "Open", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            if (order.status == PackageOrderStatus.REJECTED && !order.rejectReason.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = ErrorRed.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reason: ${order.rejectReason} (Credits Refunded)",
                            style = MaterialTheme.typography.bodySmall,
                            color = ErrorRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ID: ${order.id}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Text(
                    text = "${order.dateFormatted} ${order.timeFormatted}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}
