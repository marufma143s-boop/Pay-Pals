package com.example.screens.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.components.EmptyStateView
import com.example.components.SubScreenTopBar
import com.example.components.TransactionCard
import com.example.model.TransactionType
import com.example.repository.AppRepository
import com.example.ui.theme.PurplePrimary

@Composable
fun TransactionHistoryScreen(
    repository: AppRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transactions by repository.transactions.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val filterOptions = listOf(
        "All",
        "Deposit",
        "Withdrawal",
        "Task Reward",
        "Referral Reward",
        "Campaign Payment"
    )

    val filteredTransactions = transactions.filter { txn ->
        val matchesFilter = when (selectedFilter) {
            "All" -> true
            "Deposit" -> txn.type == TransactionType.DEPOSIT
            "Withdrawal" -> txn.type == TransactionType.WITHDRAWAL
            "Task Reward" -> txn.type == TransactionType.TASK_REWARD
            "Referral Reward" -> txn.type == TransactionType.REFERRAL_REWARD
            "Campaign Payment" -> txn.type == TransactionType.CAMPAIGN_PAYMENT
            else -> true
        }

        val matchesSearch = if (searchQuery.isBlank()) {
            true
        } else {
            txn.title.contains(searchQuery, ignoreCase = true) ||
            txn.transactionId.contains(searchQuery, ignoreCase = true) ||
            txn.note.contains(searchQuery, ignoreCase = true)
        }

        matchesFilter && matchesSearch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("transaction_history_screen")
    ) {
        SubScreenTopBar(
            title = "Transaction History",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by TXN ID or title") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("txn_search_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurplePrimary
                ),
                singleLine = true
            )

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                items(filterOptions) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = filter,
                                fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal
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

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredTransactions.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Filled.History,
                    title = "No Transactions Yet",
                    message = "No transactions found matching your selected criteria."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTransactions, key = { it.id }) { txn ->
                        TransactionCard(transaction = txn)
                    }

                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}
