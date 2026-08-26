package com.example.screens.admin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.PackageOrder
import com.example.model.PackageOrderStatus
import com.example.model.PaidPackage
import com.example.repository.AppRepository
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen
import com.example.utils.FormatUtils

@Composable
fun AdminUsersScreen(repository: AppRepository) {
    val users by repository.allUsers.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("User Management (${users.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(users) { user ->
                UserAdminCard(user = user, repository = repository)
            }
        }
    }
}

@Composable
fun UserAdminCard(user: Map<String, Any?>, repository: AppRepository) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    
    val userId = user["id"] as? String ?: return
    val isBlocked = user["isBlocked"] as? Boolean ?: false
    val name = user["fullName"] as? String ?: "Unknown"
    val email = user["email"] as? String ?: "No Email"
    val balance = (user["balance"] as? Number)?.toDouble() ?: 0.0
    val currentRole = user["role"] as? String ?: "USER"
    val isOwner = email == "d@gmail.com" || currentRole == "OWNER"

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete User Permanently?") },
            text = { Text("Are you sure you want to permanently delete $name ($email) from the database? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        repository.deleteUserAdmin(userId)
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showEditDialog) {
        var editName by remember { mutableStateOf(name) }
        var editEmail by remember { mutableStateOf(email) }
        var editBalance by remember { mutableStateOf(balance.toString()) }
        var editRole by remember { mutableStateOf(currentRole) }
        
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit User") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Name") })
                    OutlinedTextField(value = editEmail, onValueChange = { editEmail = it }, label = { Text("Email") })
                    OutlinedTextField(value = editBalance, onValueChange = { editBalance = it }, label = { Text("Balance") })
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Text("Grant Admin Access")
                        Switch(
                            checked = editRole == "ADMIN" || editRole == "OWNER",
                            onCheckedChange = { isAdmin ->
                                if (!isOwner) { // Protect OWNER role
                                    editRole = if (isAdmin) "ADMIN" else "USER"
                                }
                            },
                            enabled = !isOwner
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    repository.updateUserAdmin(userId, editName, editEmail, editBalance.toDoubleOrNull() ?: balance)
                    if (currentRole != editRole) {
                        repository.updateUserRole(userId, editRole)
                    }
                    showEditDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = FormatUtils.formatCredits(balance), color = SuccessGreen, fontWeight = FontWeight.Bold)
            }
            Text(text = "Email: $email", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Role: $currentRole", style = MaterialTheme.typography.bodySmall, color = PurplePrimary, fontWeight = FontWeight.SemiBold)
            Text(text = "ID: $userId", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(text = "Status: ${if(isBlocked) "BLOCKED" else "ACTIVE"}", style = MaterialTheme.typography.bodySmall, color = if(isBlocked) ErrorRed else SuccessGreen)
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (!isOwner) {
                    IconButton(onClick = { repository.updateUserBlockStatus(userId, !isBlocked) }) {
                        Icon(Icons.Default.Block, contentDescription = "Block/Unblock", tint = if (isBlocked) SuccessGreen else ErrorRed)
                    }
                }
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                if (!isOwner) {
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDepositScreen(repository: AppRepository) {
    val requests by repository.adminDepositRequests.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending", "Completed", "Rejected")
    
    val filteredRequests = requests.filter { it["status"] as? String == tabs[selectedTab].uppercase() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        if (filteredRequests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No ${tabs[selectedTab].lowercase()} deposits")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredRequests) { req ->
                    DepositRequestCard(
                        request = req,
                        isPending = selectedTab == 0,
                        onApprove = {
                            val id = req["id"] as? String ?: return@DepositRequestCard
                            val userId = req["userId"] as? String ?: return@DepositRequestCard
                            val amount = (req["amount"] as? Number)?.toDouble() ?: 0.0
                            repository.approveAdminDeposit(userId, id, amount)
                        },
                        onReject = {
                            val id = req["id"] as? String ?: return@DepositRequestCard
                            val userId = req["userId"] as? String ?: return@DepositRequestCard
                            repository.rejectAdminDeposit(userId, id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DepositRequestCard(request: Map<String, Any?>, isPending: Boolean, onApprove: () -> Unit, onReject: () -> Unit) {
    val trxId = (request["trxId"] as? String) ?: (request["transactionId"] as? String) ?: "N/A"
    val senderNo = (request["senderNumber"] as? String) ?: "N/A"
    val methodNo = (request["methodNumber"] as? String) ?: ""
    val method = (request["method"] as? String) ?: "N/A"

    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "${request["userName"]} (${request["userId"]})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = FormatUtils.formatCredits((request["amount"] as? Number)?.toDouble() ?: 0.0), color = SuccessGreen, fontWeight = FontWeight.Bold)
            }
            Text(text = "Method: $method ${if (methodNo.isNotBlank()) "($methodNo)" else ""}", style = MaterialTheme.typography.bodyMedium)
            
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Sender Mobile No: $senderNo",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "TrxID: $trxId",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(text = "Date: ${request["dateFormatted"]} ${request["timeFormatted"]}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Status: ${request["status"]}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            
            if (isPending) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onReject) { Icon(Icons.Default.Close, contentDescription = "Reject", tint = ErrorRed) }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onApprove) { Icon(Icons.Default.Check, contentDescription = "Approve", tint = SuccessGreen) }
                }
            }
        }
    }
}

@Composable
fun AdminWithdrawalScreen(repository: AppRepository) {
    val requests by repository.adminWithdrawalRequests.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending", "Completed", "Rejected")
    
    val filteredRequests = requests.filter { it["status"] as? String == tabs[selectedTab].uppercase() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        if (filteredRequests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No ${tabs[selectedTab].lowercase()} withdrawals")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredRequests) { req ->
                    WithdrawalRequestCard(
                        request = req,
                        isPending = selectedTab == 0,
                        onApprove = {
                            val id = req["id"] as? String ?: return@WithdrawalRequestCard
                            val userId = req["userId"] as? String ?: return@WithdrawalRequestCard
                            repository.approveAdminWithdrawal(userId, id)
                        },
                        onReject = {
                            val id = req["id"] as? String ?: return@WithdrawalRequestCard
                            val userId = req["userId"] as? String ?: return@WithdrawalRequestCard
                            repository.rejectAdminWithdrawal(userId, id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WithdrawalRequestCard(request: Map<String, Any?>, isPending: Boolean, onApprove: () -> Unit, onReject: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "${request["userName"]} (${request["userId"]})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = FormatUtils.formatCredits((request["amount"] as? Number)?.toDouble() ?: 0.0), color = ErrorRed, fontWeight = FontWeight.Bold)
            }
            Text(text = "Method: ${request["method"]}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Account: ${request["accountNumber"]}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(text = "Date: ${request["dateFormatted"]} ${request["timeFormatted"]}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Status: ${request["status"]}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            
            if (isPending) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onReject) { Icon(Icons.Default.Close, contentDescription = "Reject", tint = ErrorRed) }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onApprove) { Icon(Icons.Default.Check, contentDescription = "Approve", tint = SuccessGreen) }
                }
            }
        }
    }
}

@Composable
fun AdminCampaignsScreen(repository: AppRepository) {
    val campaigns by repository.allCampaigns.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending", "Running", "Completed", "Rejected")
    
    val filteredCampaigns = campaigns.filter { it["status"] as? String == tabs[selectedTab].uppercase() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 8.dp) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        if (filteredCampaigns.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No ${tabs[selectedTab].lowercase()} campaigns")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredCampaigns) { camp ->
                    AdminCampaignCard(
                        campaign = camp,
                        repository = repository,
                        isPending = selectedTab == 0,
                        isRunning = selectedTab == 1
                    )
                }
            }
        }
    }
}

@Composable
fun AdminCampaignCard(campaign: Map<String, Any?>, repository: AppRepository, isPending: Boolean, isRunning: Boolean) {
    val id = campaign["id"] as? String ?: return
    var showRejectDialog by remember { mutableStateOf(false) }

    if (showRejectDialog) {
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Reject Campaign") },
            text = {
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for Rejection") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    repository.updateCampaignStatus(id, "REJECTED", reason)
                    showRejectDialog = false
                }) { Text("Reject") }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "${campaign["title"]}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = "User ID: ${campaign["userId"]}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(text = "Link: ${campaign["targetLink"]}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Text(text = "Views: ${campaign["completedViews"]}/${campaign["targetViews"]}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(text = "Network: ${campaign["networkType"]}", style = MaterialTheme.typography.bodySmall)
            
            if (isPending || isRunning) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = { showRejectDialog = true }) { 
                        Icon(Icons.Default.Close, contentDescription = "Reject", tint = ErrorRed) 
                    }
                    if (isPending) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { repository.updateCampaignStatus(id, "RUNNING") }) { 
                            Icon(Icons.Default.PlayArrow, contentDescription = "Start Running", tint = SuccessGreen) 
                        }
                    }
                    if (isRunning) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            val currentViews = (campaign["completedViews"] as? Number)?.toInt() ?: 0
                            val targetViews = (campaign["targetViews"] as? Number)?.toInt() ?: 0
                            val newViews = currentViews + 100
                            if (newViews >= targetViews) {
                                repository.updateCampaignViews(id, targetViews, "COMPLETED")
                            } else {
                                repository.updateCampaignViews(id, newViews, "RUNNING")
                            }
                        }) {
                            Text("Add 100 Views")
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// ADMIN PAID PACKAGES SCREEN
// ----------------------------------------------------

@Composable
fun AdminPaidPackagesScreen(repository: AppRepository) {
    val packages by repository.paidPackages.collectAsState()
    var showAddEditDialog by remember { mutableStateOf(false) }
    var packageToEdit by remember { mutableStateOf<PaidPackage?>(null) }
    var packageToDelete by remember { mutableStateOf<PaidPackage?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Paid Packages (${packages.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Manage unlimited traffic packages",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Button(
                onClick = {
                    packageToEdit = null
                    showAddEditDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Package")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (packages.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No packages created yet. Click '+ Add Package' to create one.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(packages) { pkg ->
                    AdminPaidPackageCard(
                        pkg = pkg,
                        onEdit = {
                            packageToEdit = pkg
                            showAddEditDialog = true
                        },
                        onDelete = {
                            packageToDelete = pkg
                        }
                    )
                }
            }
        }
    }

    // Add / Edit Package Dialog
    if (showAddEditDialog) {
        var name by remember { mutableStateOf(packageToEdit?.name ?: "") }
        var viewsStr by remember { mutableStateOf(packageToEdit?.views?.toString() ?: "1000") }
        var priceStr by remember { mutableStateOf(packageToEdit?.price?.toString() ?: "500.0") }
        var badge by remember { mutableStateOf(packageToEdit?.badge ?: "") }
        var description by remember { mutableStateOf(packageToEdit?.description ?: "") }
        var isEnabled by remember { mutableStateOf(packageToEdit?.isEnabled ?: true) }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            title = {
                Text(
                    text = if (packageToEdit == null) "Create New Package" else "Edit Package",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; errorMsg = null },
                        label = { Text("Package Name (প্যাকেজের নাম)") },
                        placeholder = { Text("e.g. 5,000 Views Super Boost") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = viewsStr,
                        onValueChange = { viewsStr = it; errorMsg = null },
                        label = { Text("Target Views (কতগুলো ভিউজ পাবে)") },
                        placeholder = { Text("e.g. 5000") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it; errorMsg = null },
                        label = { Text("Price in Points/Credits (প্যাকেজের মূল্য কত পয়েন্ট)") },
                        placeholder = { Text("e.g. 2300") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = badge,
                        onValueChange = { badge = it },
                        label = { Text("Badge Tag (Optional: Popular, Best Value, VIP)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (Optional)") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active / Enabled for Users")
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it }
                        )
                    }

                    if (errorMsg != null) {
                        Text(errorMsg!!, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            errorMsg = "Package name cannot be empty."
                            return@Button
                        }
                        val views = viewsStr.toIntOrNull()
                        if (views == null || views <= 0) {
                            errorMsg = "Please enter valid number of views."
                            return@Button
                        }
                        val price = priceStr.toDoubleOrNull()
                        if (price == null || price < 0) {
                            errorMsg = "Please enter a valid price."
                            return@Button
                        }

                        val id = packageToEdit?.id ?: ""
                        val pkg = PaidPackage(
                            id = id,
                            name = name.trim(),
                            views = views,
                            price = price,
                            description = description.trim(),
                            badge = badge.trim(),
                            isEnabled = isEnabled
                        )
                        repository.saveAdminPaidPackage(pkg)
                        showAddEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Save Package")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (packageToDelete != null) {
        val pkg = packageToDelete!!
        AlertDialog(
            onDismissRequest = { packageToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed) },
            title = { Text("Delete Package?") },
            text = {
                Text("Are you sure you want to permanently delete '${pkg.name}' (${FormatUtils.formatNumber(pkg.views)} Views)? Users won't be able to order it anymore.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.deleteAdminPaidPackage(pkg.id)
                        packageToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { packageToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminPaidPackageCard(
    pkg: PaidPackage,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pkg.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (pkg.badge.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PurpleNeon.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = pkg.badge,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PurpleNeon
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (pkg.isEnabled) SuccessGreen.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (pkg.isEnabled) "ACTIVE" else "DISABLED",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (pkg.isEnabled) SuccessGreen else Color.Gray
                    )
                }
            }

            if (pkg.description.isNotBlank()) {
                Text(
                    text = pkg.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Views: ${FormatUtils.formatNumber(pkg.views)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PurplePrimary
                )
                Text(
                    text = "Price: ${FormatUtils.formatCredits(pkg.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
            }

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PurplePrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                }
            }
        }
    }
}

// ----------------------------------------------------
// ADMIN PACKAGE ORDERS SCREEN (Pending / Running / Completed / Rejected)
// ----------------------------------------------------

@Composable
fun AdminPackageOrdersScreen(repository: AppRepository) {
    val orders by repository.adminPackageOrders.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending", "Running", "Completed", "Rejected")

    val currentStatus = tabs[selectedTab].uppercase()
    val filteredOrders = orders.filter { it.status.name == currentStatus }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 8.dp) {
            tabs.forEachIndexed { index, title ->
                val count = orders.count { it.status.name == title.uppercase() }
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(title)
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedTab == index) PurplePrimary else Color.Gray.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "$count",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selectedTab == index) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No ${tabs[selectedTab].lowercase()} package orders",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredOrders) { order ->
                    AdminPackageOrderCard(
                        order = order,
                        repository = repository
                    )
                }
            }
        }
    }
}

@Composable
fun AdminPackageOrderCard(
    order: PackageOrder,
    repository: AppRepository
) {
    val context = LocalContext.current
    var showRejectDialog by remember { mutableStateOf(false) }
    var showStatusDropdown by remember { mutableStateOf(false) }

    val statusColor = when (order.status) {
        PackageOrderStatus.PENDING -> Color(0xFFF59E0B)
        PackageOrderStatus.RUNNING -> Color(0xFF3B82F6)
        PackageOrderStatus.COMPLETED -> SuccessGreen
        PackageOrderStatus.REJECTED -> ErrorRed
    }

    if (showRejectDialog) {
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            icon = { Icon(Icons.Default.Cancel, contentDescription = null, tint = ErrorRed) },
            title = { Text("Reject Package Order") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Rejecting this order will automatically refund ${FormatUtils.formatCredits(order.price)} back to user ${order.userName}'s wallet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Rejection Reason (Optional)") },
                        placeholder = { Text("e.g. Invalid link, violated guidelines") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.updateAdminPackageOrderStatus(order.id, "REJECTED", reason.ifBlank { "Rejected by admin" })
                        showRejectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Confirm Reject & Refund")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Package Name, Status & Price
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
                Text(
                    text = FormatUtils.formatCredits(order.price),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
            }

            // User Info
            Text(
                text = "User: ${order.userName} (${order.userEmail.ifBlank { order.userId }})",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            // Title Info
            Text(
                text = "Title: ${order.title}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Views
            Text(
                text = "Views: ${FormatUtils.formatNumber(order.views)} Views Target",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = PurpleNeon
            )

            // Target Link
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
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
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Target Link", order.targetLink)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
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
                Text(
                    text = "Reject Reason: ${order.rejectReason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ErrorRed
                )
            }

            // Date & ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "ID: ${order.id}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(text = "${order.dateFormatted} ${order.timeFormatted}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            // Action Buttons & Status Changer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Status: ${order.status.name}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                // Quick Status Actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Change Dropdown Menu
                    Box {
                        OutlinedButton(
                            onClick = { showStatusDropdown = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Change Status", style = MaterialTheme.typography.labelSmall)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }

                        DropdownMenu(
                            expanded = showStatusDropdown,
                            onDismissRequest = { showStatusDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Mark as Pending") },
                                onClick = {
                                    repository.updateAdminPackageOrderStatus(order.id, "PENDING")
                                    showStatusDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Mark as Running") },
                                onClick = {
                                    repository.updateAdminPackageOrderStatus(order.id, "RUNNING")
                                    showStatusDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Mark as Completed") },
                                onClick = {
                                    repository.updateAdminPackageOrderStatus(order.id, "COMPLETED")
                                    showStatusDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Mark as Rejected (Refund)", color = ErrorRed) },
                                onClick = {
                                    showStatusDropdown = false
                                    showRejectDialog = true
                                }
                            )
                        }
                    }

                    if (order.status == PackageOrderStatus.PENDING) {
                        IconButton(onClick = { showRejectDialog = true }) {
                            Icon(Icons.Default.Close, contentDescription = "Reject", tint = ErrorRed)
                        }
                        IconButton(onClick = { repository.updateAdminPackageOrderStatus(order.id, "RUNNING") }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Start Running", tint = SuccessGreen)
                        }
                    } else if (order.status == PackageOrderStatus.RUNNING) {
                        IconButton(onClick = { repository.updateAdminPackageOrderStatus(order.id, "COMPLETED") }) {
                            Icon(Icons.Default.Check, contentDescription = "Complete", tint = SuccessGreen)
                        }
                    }
                }
            }
        }
    }
}

