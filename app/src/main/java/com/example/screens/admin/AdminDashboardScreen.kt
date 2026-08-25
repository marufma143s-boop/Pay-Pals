package com.example.screens.admin

import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.repository.AppRepository
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    repository: AppRepository,
    onBackClick: () -> Unit
) {
    var selectedScreen by remember { mutableStateOf("dashboard") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Admin Panel",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Divider()
                AdminDrawerItem(
                    title = "Dashboard",
                    icon = Icons.Default.Dashboard,
                    isSelected = selectedScreen == "dashboard",
                    onClick = { selectedScreen = "dashboard"; coroutineScope.launch { drawerState.close() } }
                )
                AdminDrawerItem(
                    title = "Users",
                    icon = Icons.Default.People,
                    isSelected = selectedScreen == "users",
                    onClick = { selectedScreen = "users"; coroutineScope.launch { drawerState.close() } }
                )
                AdminDrawerItem(
                    title = "Deposits",
                    icon = Icons.Default.AccountBalanceWallet,
                    isSelected = selectedScreen == "deposits",
                    onClick = { selectedScreen = "deposits"; coroutineScope.launch { drawerState.close() } }
                )
                AdminDrawerItem(
                    title = "Withdrawals",
                    icon = Icons.Default.MoneyOff,
                    isSelected = selectedScreen == "withdrawals",
                    onClick = { selectedScreen = "withdrawals"; coroutineScope.launch { drawerState.close() } }
                )
                AdminDrawerItem(
                    title = "Campaigns",
                    icon = Icons.Default.TrendingUp,
                    isSelected = selectedScreen == "campaigns",
                    onClick = { selectedScreen = "campaigns"; coroutineScope.launch { drawerState.close() } }
                )
                AdminDrawerItem(
                    title = "Settings",
                    icon = Icons.Default.Settings,
                    isSelected = selectedScreen == "settings",
                    onClick = { selectedScreen = "settings"; coroutineScope.launch { drawerState.close() } }
                )
                Spacer(modifier = Modifier.weight(1f))
                Divider()
                AdminDrawerItem(
                    title = "Exit Admin",
                    icon = Icons.Default.ExitToApp,
                    isSelected = false,
                    onClick = onBackClick
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(getTitleForScreen(selectedScreen), fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                when (selectedScreen) {
                    "dashboard" -> AdminDashboardMainContent(repository = repository, onNavigate = { selectedScreen = it })
                    "users" -> AdminUsersScreen(repository = repository)
                    "deposits" -> AdminDepositScreen(repository = repository)
                    "withdrawals" -> AdminWithdrawalScreen(repository = repository)
                    "campaigns" -> AdminCampaignsScreen(repository = repository)
                    "settings" -> AdminSettingsScreen(repository = repository)
                }
            }
        }
    }
}

private fun getTitleForScreen(route: String): String {
    return when (route) {
        "dashboard" -> "Admin Dashboard"
        "users" -> "Manage Users"
        "deposits" -> "Deposit Requests"
        "withdrawals" -> "Withdrawal Requests"
        "campaigns" -> "Campaigns"
        "settings" -> "Settings"
        else -> "Admin"
    }
}

@Composable
fun AdminDrawerItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) PurplePrimary.copy(alpha = 0.12f) else Color.Transparent
    val contentColor = if (isSelected) PurplePrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
fun AdminDashboardMainContent(repository: AppRepository, onNavigate: (String) -> Unit) {
    val users by repository.allUsers.collectAsState()
    val campaigns by repository.allCampaigns.collectAsState()
    val deposits by repository.adminDepositRequests.collectAsState()
    val withdrawals by repository.adminWithdrawalRequests.collectAsState()
    
    val totalUsers = users.size
    val activeCampaigns = campaigns.count { it["status"] == "RUNNING" }
    
    val pendingDeposits = deposits.count { it["status"] == "PENDING" }
    val totalDeposits = deposits.size
    
    val pendingWithdrawals = withdrawals.count { it["status"] == "PENDING" }
    val totalWithdrawals = withdrawals.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardStatCard(
                modifier = Modifier.weight(1f),
                title = "Total Users",
                value = "$totalUsers",
                icon = Icons.Default.People,
                color = PurplePrimary
            )
            DashboardStatCard(
                modifier = Modifier.weight(1f),
                title = "Active Campaigns",
                value = "$activeCampaigns",
                icon = Icons.Default.TrendingUp,
                color = SuccessGreen
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardStatCard(
                modifier = Modifier.weight(1f),
                title = "Dep. Pending",
                value = "$pendingDeposits",
                icon = Icons.Default.AccountBalanceWallet,
                color = Color(0xFFF59E0B)
            )
            DashboardStatCard(
                modifier = Modifier.weight(1f),
                title = "With. Pending",
                value = "$pendingWithdrawals",
                icon = Icons.Default.MoneyOff,
                color = Color(0xFFEF4444)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardStatCard(
                modifier = Modifier.weight(1f),
                title = "Total Deposits",
                value = "$totalDeposits",
                icon = Icons.Default.AccountBalanceWallet,
                color = PurplePrimary
            )
            DashboardStatCard(
                modifier = Modifier.weight(1f),
                title = "Total With.",
                value = "$totalWithdrawals",
                icon = Icons.Default.MoneyOff,
                color = PurplePrimary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        AdminModuleItem(
            icon = Icons.Default.AccountBalanceWallet,
            title = "Manage Deposits",
            subtitle = "Review $pendingDeposits pending requests",
            onClick = { onNavigate("deposits") }
        )
        AdminModuleItem(
            icon = Icons.Default.MoneyOff,
            title = "Manage Withdrawals",
            subtitle = "Review $pendingWithdrawals pending requests",
            onClick = { onNavigate("withdrawals") }
        )
        AdminModuleItem(
            icon = Icons.Default.People,
            title = "User Management",
            subtitle = "Manage $totalUsers users",
            onClick = { onNavigate("users") }
        )
        AdminModuleItem(
            icon = Icons.Default.TrendingUp,
            title = "Manage Campaigns",
            subtitle = "View active and pending campaigns",
            onClick = { onNavigate("campaigns") }
        )
        AdminModuleItem(
            icon = Icons.Default.Settings,
            title = "App Settings",
            subtitle = "Browser config, maintenance",
            onClick = { onNavigate("settings") }
        )
    }
}

@Composable
fun DashboardStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AdminModuleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(PurplePrimary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = PurplePrimary, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Go", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
