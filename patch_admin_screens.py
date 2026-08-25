import os

content = """package com.example.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.repository.AppRepository
import com.example.ui.theme.ErrorRed
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
    
    val userId = user["id"] as? String ?: return
    val isBlocked = user["isBlocked"] as? Boolean ?: false
    val name = user["fullName"] as? String ?: "Unknown"
    val email = user["email"] as? String ?: "No Email"
    val balance = (user["balance"] as? Number)?.toDouble() ?: 0.0

    if (showEditDialog) {
        var editName by remember { mutableStateOf(name) }
        var editEmail by remember { mutableStateOf(email) }
        var editBalance by remember { mutableStateOf(balance.toString()) }
        
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit User") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Name") })
                    OutlinedTextField(value = editEmail, onValueChange = { editEmail = it }, label = { Text("Email") })
                    OutlinedTextField(value = editBalance, onValueChange = { editBalance = it }, label = { Text("Balance") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    repository.updateUserAdmin(userId, editName, editEmail, editBalance.toDoubleOrNull() ?: balance)
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
            Text(text = "ID: $userId", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(text = "Status: ${if(isBlocked) "BLOCKED" else "ACTIVE"}", style = MaterialTheme.typography.bodySmall, color = if(isBlocked) ErrorRed else SuccessGreen)
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = { repository.updateUserBlockStatus(userId, !isBlocked) }) {
                    Icon(Icons.Default.Block, contentDescription = "Block/Unblock", tint = if (isBlocked) SuccessGreen else ErrorRed)
                }
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { repository.deleteUserAdmin(userId) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
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
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "${request["userName"]} (${request["userId"]})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = FormatUtils.formatCredits((request["amount"] as? Number)?.toDouble() ?: 0.0), color = SuccessGreen, fontWeight = FontWeight.Bold)
            }
            Text(text = "Method: ${request["method"]}", style = MaterialTheme.typography.bodyMedium)
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
                }
            }
        }
    }
}
"""

with open("app/src/main/java/com/example/screens/admin/AdminScreens.kt", "w") as f:
    f.write(content)
