package com.example.screens.campaign

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.example.components.CampaignCard
import com.example.components.EmptyStateView
import com.example.components.SubScreenTopBar
import com.example.model.CampaignStatus
import com.example.repository.AppRepository
import com.example.ui.theme.PurplePrimary

@Composable
fun CampaignListScreen(
    repository: AppRepository,
    onBackClick: () -> Unit,
    onCampaignClick: (String) -> Unit,
    onCreateCampaignClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val campaigns by repository.campaigns.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }

    val filterOptions = listOf("All", "Running", "Completed", "Pending", "Cancelled")

    val filteredCampaigns = campaigns.filter { cmp ->
        when (selectedFilter) {
            "All" -> true
            "Running" -> cmp.status == CampaignStatus.RUNNING
            "Completed" -> cmp.status == CampaignStatus.COMPLETED
            "Pending" -> cmp.status == CampaignStatus.PENDING
            "Cancelled" -> cmp.status == CampaignStatus.CANCELLED
            else -> true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("campaign_list_screen")
    ) {
        SubScreenTopBar(
            title = "My Campaigns",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Filter Tabs
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

            if (filteredCampaigns.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.Campaign,
                    title = "No Campaigns Yet",
                    message = "No campaigns found matching '$selectedFilter'. Create your first promotion campaign now!"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredCampaigns, key = { it.id }) { cmp ->
                        CampaignCard(
                            campaign = cmp,
                            onClick = { onCampaignClick(cmp.id) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}
