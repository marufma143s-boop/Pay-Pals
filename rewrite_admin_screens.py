import os

content = """package com.example.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun AdminUsersScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("User Management Content", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun AdminDepositScreen(repository: AppRepository) {
    val requests by repository.adminDepositRequests.collectAsState()
    val pendingRequests = requests.filter { it["status"] as? String == "PENDING" }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Pending Deposits (${pendingRequests.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        if (pendingRequests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No pending deposits")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(pendingRequests) { req ->
                    DepositRequestCard(
                        request = req,
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
fun DepositRequestCard(request: Map<String, Any?>, onApprove: () -> Unit, onReject: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "${request["userName"]} (${request["userId"]})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = FormatUtils.formatCredits((request["amount"] as? Number)?.toDouble() ?: 0.0),
                    style = MaterialTheme.typography.titleMedium,
                    color = SuccessGreen,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Method: ${request["method"]}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Date: ${request["dateFormatted"]} ${request["timeFormatted"]}", style = MaterialTheme.typography.bodySmall)
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onReject) {
                    Icon(Icons.Default.Close, contentDescription = "Reject", tint = ErrorRed)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onApprove) {
                    Icon(Icons.Default.Check, contentDescription = "Approve", tint = SuccessGreen)
                }
            }
        }
    }
}

@Composable
fun AdminWithdrawalScreen(repository: AppRepository) {
    val requests by repository.adminWithdrawalRequests.collectAsState()
    val pendingRequests = requests.filter { it["status"] as? String == "PENDING" }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Pending Withdrawals (${pendingRequests.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        if (pendingRequests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No pending withdrawals")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(pendingRequests) { req ->
                    WithdrawalRequestCard(
                        request = req,
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
fun WithdrawalRequestCard(request: Map<String, Any?>, onApprove: () -> Unit, onReject: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "${request["userName"]} (${request["userId"]})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = FormatUtils.formatCredits((request["amount"] as? Number)?.toDouble() ?: 0.0),
                    style = MaterialTheme.typography.titleMedium,
                    color = ErrorRed,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Method: ${request["method"]}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Account: ${request["accountNumber"]}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(text = "Date: ${request["dateFormatted"]} ${request["timeFormatted"]}", style = MaterialTheme.typography.bodySmall)
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onReject) {
                    Icon(Icons.Default.Close, contentDescription = "Reject", tint = ErrorRed)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onApprove) {
                    Icon(Icons.Default.Check, contentDescription = "Approve", tint = SuccessGreen)
                }
            }
        }
    }
}

@Composable
fun AdminCampaignsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Manage Campaigns Content", style = MaterialTheme.typography.titleLarge)
    }
}
"""

with open("app/src/main/java/com/example/screens/admin/AdminScreens.kt", "w") as f:
    f.write(content)
