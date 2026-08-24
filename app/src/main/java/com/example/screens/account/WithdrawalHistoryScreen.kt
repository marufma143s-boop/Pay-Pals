package com.example.screens.account

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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
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
import com.example.model.TransactionStatus
import com.example.model.TransactionType
import com.example.repository.AppRepository
import com.example.ui.theme.PurplePrimary

@Composable
fun WithdrawalHistoryScreen(
    repository: AppRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transactions by repository.transactions.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }

    val withdrawalTxns = transactions.filter { it.type == TransactionType.WITHDRAWAL }
    val filterOptions = listOf("All", "Completed", "Pending", "Rejected")

    val filteredList = withdrawalTxns.filter { txn ->
        when (selectedFilter) {
            "All" -> true
            "Completed" -> txn.status == TransactionStatus.COMPLETED
            "Pending" -> txn.status == TransactionStatus.PENDING
            "Rejected" -> txn.status == TransactionStatus.REJECTED
            else -> true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("withdrawal_history_screen")
    ) {
        SubScreenTopBar(
            title = "Withdrawal History",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Filter Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
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

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredList.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Filled.History,
                    title = "No Withdrawal Records",
                    message = "No withdrawal requests found under '$selectedFilter'."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { txn ->
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
