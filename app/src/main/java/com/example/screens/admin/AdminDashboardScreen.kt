package com.example.screens.admin

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.UserProfile
import com.example.repository.AppRepository
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    repository: AppRepository,
    onBackClick: () -> Unit
) {
    val userProfile by repository.userProfile.collectAsState()
    val isOwner = userProfile.email == "d@gmail.com" || userProfile.role == "OWNER"

    val canAdmins = isOwner
    val canDashboard = userProfile.hasPermission("dashboard")
    val canUsers = isOwner || userProfile.hasPermission("users")
    val canDeposits = userProfile.hasPermission("deposits")
    val canWithdrawals = userProfile.hasPermission("withdrawals")
    val canDepositMethods = userProfile.hasPermission("deposit_methods")
    val canWithdrawalMethods = userProfile.hasPermission("withdrawal_methods")
    val canCampaigns = userProfile.hasPermission("campaigns")
    val canPackages = userProfile.hasPermission("packages")
    val canPackageOrders = userProfile.hasPermission("package_orders")
    
    val canGeneralSettings = isOwner || userProfile.hasPermission("general_settings") || userProfile.hasPermission("settings")
    val canServiceControl = isOwner || userProfile.hasPermission("service_control")
    val canMaintenanceMode = isOwner || userProfile.hasPermission("maintenance_mode")
    val canSupportCenter = isOwner || userProfile.hasPermission("support_center")
    val canDeveloperSettings = isOwner || userProfile.hasPermission("developer_settings")
    val canSettings = canGeneralSettings || canServiceControl || canMaintenanceMode || canSupportCenter || canDeveloperSettings

    val initialScreen = remember(canDashboard, canUsers, canDeposits, canWithdrawals) {
        when {
            canDashboard -> "dashboard"
            canAdmins -> "admins"
            canUsers -> "users"
            canDeposits -> "deposits"
            canWithdrawals -> "withdrawals"
            canDepositMethods -> "deposit_methods"
            canWithdrawalMethods -> "withdrawal_methods"
            canCampaigns -> "campaigns"
            canPackages -> "packages"
            canPackageOrders -> "package_orders"
            canServiceControl -> "settings_services"
            canMaintenanceMode -> "settings_maintenance"
            canSupportCenter -> "settings_support"
            canDeveloperSettings -> "settings_developer"
            canGeneralSettings -> "settings_general"
            else -> "dashboard"
        }
    }

    var selectedScreen by remember { mutableStateOf(initialScreen) }
    var isSettingsDrawerExpanded by remember { mutableStateOf(true) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isOwner) Icons.Default.Stars else Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = PurplePrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isOwner) "Owner Panel" else "Admin Panel",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isOwner) "Full Control" else "Restricted Admin",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    if (canDashboard) {
                        AdminDrawerItem(
                            title = "Dashboard",
                            icon = Icons.Default.Dashboard,
                            isSelected = selectedScreen == "dashboard",
                            onClick = { selectedScreen = "dashboard"; coroutineScope.launch { drawerState.close() } }
                        )
                    }

                    if (canAdmins) {
                        AdminDrawerItem(
                            title = "Admin Management",
                            icon = Icons.Default.AdminPanelSettings,
                            isSelected = selectedScreen == "admins",
                            onClick = { selectedScreen = "admins"; coroutineScope.launch { drawerState.close() } }
                        )
                    }

                    if (canUsers) {
                        AdminDrawerItem(
                            title = "User Management",
                            icon = Icons.Default.People,
                            isSelected = selectedScreen == "users",
                            onClick = { selectedScreen = "users"; coroutineScope.launch { drawerState.close() } }
                        )
                    }

                    if (canDeposits) {
                        AdminDrawerItem(
                            title = "Deposit Requests",
                            icon = Icons.Default.AccountBalanceWallet,
                            isSelected = selectedScreen == "deposits",
                            onClick = { selectedScreen = "deposits"; coroutineScope.launch { drawerState.close() } }
                        )
                    }

                    if (canWithdrawals) {
                        AdminDrawerItem(
                            title = "Withdrawal Requests",
                            icon = Icons.Default.MoneyOff,
                            isSelected = selectedScreen == "withdrawals",
                            onClick = { selectedScreen = "withdrawals"; coroutineScope.launch { drawerState.close() } }
                        )
                    }

                    if (canDepositMethods) {
                        AdminDrawerItem(
                            title = "Deposit Methods",
                            icon = Icons.Default.Payments,
                            isSelected = selectedScreen == "deposit_methods",
                            onClick = { selectedScreen = "deposit_methods"; coroutineScope.launch { drawerState.close() } }
                        )
                    }

                    if (canWithdrawalMethods) {
                        AdminDrawerItem(
                            title = "Withdrawal Methods",
                            icon = Icons.Default.AccountBalance,
                            isSelected = selectedScreen == "withdrawal_methods",
                            onClick = { selectedScreen = "withdrawal_methods"; coroutineScope.launch { drawerState.close() } }
                        )
                    }

                    if (canCampaigns) {
                        AdminDrawerItem(
                            title = "Campaigns",
                            icon = Icons.Default.TrendingUp,
                            isSelected = selectedScreen == "campaigns",
                            onClick = { selectedScreen = "campaigns"; coroutineScope.launch { drawerState.close() } }
                        )
                    }

                    if (canPackages) {
                        AdminDrawerItem(
                            title = "Paid Packages",
                            icon = Icons.Default.CardGiftcard,
                            isSelected = selectedScreen == "packages",
                            onClick = { selectedScreen = "packages"; coroutineScope.launch { drawerState.close() } }
                        )
                    }

                    if (canPackageOrders) {
                        AdminDrawerItem(
                            title = "Package Orders",
                            icon = Icons.Default.ShoppingCart,
                            isSelected = selectedScreen == "package_orders",
                            onClick = { selectedScreen = "package_orders"; coroutineScope.launch { drawerState.close() } }
                        )
                    }

                    // Settings Section with Sub-Items
                    if (canSettings) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { isSettingsDrawerExpanded = !isSettingsDrawerExpanded },
                            color = if (selectedScreen.startsWith("settings")) PurplePrimary.copy(alpha = 0.08f) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = null,
                                        tint = if (selectedScreen.startsWith("settings")) PurplePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = "Settings",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedScreen.startsWith("settings")) PurplePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = if (isSettingsDrawerExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        AnimatedVisibility(visible = isSettingsDrawerExpanded) {
                            Column(modifier = Modifier.padding(start = 18.dp)) {
                                if (canGeneralSettings) {
                                    AdminDrawerItem(
                                        title = "General Settings",
                                        icon = Icons.Default.Tune,
                                        isSelected = selectedScreen == "settings_general" || selectedScreen == "settings",
                                        onClick = { selectedScreen = "settings_general"; coroutineScope.launch { drawerState.close() } }
                                    )
                                }

                                if (canServiceControl) {
                                    AdminDrawerItem(
                                        title = "Service Status",
                                        icon = Icons.Default.PowerSettingsNew,
                                        isSelected = selectedScreen == "settings_services",
                                        onClick = { selectedScreen = "settings_services"; coroutineScope.launch { drawerState.close() } }
                                    )
                                }

                                if (canMaintenanceMode) {
                                    AdminDrawerItem(
                                        title = "Maintenance Mode",
                                        icon = Icons.Default.Engineering,
                                        isSelected = selectedScreen == "settings_maintenance",
                                        onClick = { selectedScreen = "settings_maintenance"; coroutineScope.launch { drawerState.close() } }
                                    )
                                }

                                if (canSupportCenter) {
                                    AdminDrawerItem(
                                        title = "Support Center",
                                        icon = Icons.Default.HeadsetMic,
                                        isSelected = selectedScreen == "settings_support",
                                        onClick = { selectedScreen = "settings_support"; coroutineScope.launch { drawerState.close() } }
                                    )
                                }

                                if (canDeveloperSettings) {
                                    AdminDrawerItem(
                                        title = "Developer Profile",
                                        icon = Icons.Default.Code,
                                        isSelected = selectedScreen == "settings_developer",
                                        onClick = { selectedScreen = "settings_developer"; coroutineScope.launch { drawerState.close() } }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    AdminDrawerItem(
                        title = "Exit Admin",
                        icon = Icons.Default.ExitToApp,
                        isSelected = false,
                        onClick = onBackClick
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
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
                    "dashboard" -> AdminDashboardMainContent(
                        repository = repository,
                        userProfile = userProfile,
                        onNavigate = { selectedScreen = it }
                    )
                    "admins" -> AdminManagementScreen(repository = repository)
                    "users" -> AdminUsersScreen(repository = repository)
                    "deposits" -> AdminDepositScreen(repository = repository)
                    "withdrawals" -> AdminWithdrawalScreen(repository = repository)
                    "deposit_methods" -> AdminDepositMethodsScreen(repository = repository)
                    "withdrawal_methods" -> AdminWithdrawalMethodsScreen(repository = repository)
                    "campaigns" -> AdminCampaignsScreen(repository = repository)
                    "packages" -> AdminPaidPackagesScreen(repository = repository)
                    "package_orders" -> AdminPackageOrdersScreen(repository = repository)
                    "settings", "settings_general" -> AdminSettingsScreen(repository = repository, initialSubMenu = "general")
                    "settings_services" -> AdminSettingsScreen(repository = repository, initialSubMenu = "services")
                    "settings_maintenance" -> AdminSettingsScreen(repository = repository, initialSubMenu = "maintenance")
                    "settings_support" -> AdminSettingsScreen(repository = repository, initialSubMenu = "support")
                    "settings_developer" -> AdminSettingsScreen(repository = repository, initialSubMenu = "developer")
                }
            }
        }
    }
}

private fun getTitleForScreen(route: String): String {
    return when (route) {
        "dashboard" -> "Admin Dashboard"
        "admins" -> "Admin Management"
        "users" -> "Manage Users"
        "deposits" -> "Deposit Requests"
        "withdrawals" -> "Withdrawal Requests"
        "deposit_methods" -> "Deposit Methods"
        "withdrawal_methods" -> "Withdrawal Methods"
        "campaigns" -> "Campaigns"
        "packages" -> "Paid Packages"
        "package_orders" -> "Package Orders"
        "settings", "settings_general" -> "General Settings"
        "settings_services" -> "Service Status (সার্ভিস বন্ধ)"
        "settings_maintenance" -> "Maintenance Mode"
        "settings_support" -> "Support Center"
        "settings_developer" -> "Developer Profile"
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
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

data class DashboardStatItem(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun AdminDashboardMainContent(
    repository: AppRepository,
    userProfile: UserProfile,
    onNavigate: (String) -> Unit
) {
    val isOwner = userProfile.email == "d@gmail.com" || userProfile.role == "OWNER"
    val users by repository.allUsers.collectAsState()
    val campaigns by repository.allCampaigns.collectAsState()
    val deposits by repository.adminDepositRequests.collectAsState()
    val withdrawals by repository.adminWithdrawalRequests.collectAsState()
    val packages by repository.paidPackages.collectAsState()
    val packageOrders by repository.adminPackageOrders.collectAsState()
    val supportThreads by repository.allSupportThreads.collectAsState()
    
    val totalUsers = users.size
    val activeCampaigns = campaigns.count { it["status"] == "RUNNING" }
    
    val pendingDeposits = deposits.count { it["status"] == "PENDING" }
    val totalDeposits = deposits.size
    
    val pendingWithdrawals = withdrawals.count { it["status"] == "PENDING" }
    val totalWithdrawals = withdrawals.size

    val pendingPackageOrders = packageOrders.count { it.status.name == "PENDING" }
    val totalPackageOrders = packageOrders.size

    // Build stat cards dynamically based on user's granted permissions
    val statCards = remember(
        isOwner, userProfile.permissions,
        totalUsers, activeCampaigns,
        pendingDeposits, totalDeposits,
        pendingWithdrawals, totalWithdrawals,
        packages.size, pendingPackageOrders, totalPackageOrders,
        supportThreads.size
    ) {
        val list = mutableListOf<DashboardStatItem>()
        
        if (isOwner || userProfile.hasPermission("users")) {
            list.add(DashboardStatItem("Total Users", "$totalUsers", Icons.Default.People, PurplePrimary))
        }
        if (isOwner || userProfile.hasPermission("campaigns")) {
            list.add(DashboardStatItem("Active Campaigns", "$activeCampaigns", Icons.Default.TrendingUp, SuccessGreen))
        }
        if (isOwner || userProfile.hasPermission("deposits")) {
            list.add(DashboardStatItem("Dep. Pending", "$pendingDeposits", Icons.Default.AccountBalanceWallet, Color(0xFFF59E0B)))
            list.add(DashboardStatItem("Total Deposits", "$totalDeposits", Icons.Default.AccountBalanceWallet, PurplePrimary))
        }
        if (isOwner || userProfile.hasPermission("withdrawals")) {
            list.add(DashboardStatItem("With. Pending", "$pendingWithdrawals", Icons.Default.MoneyOff, Color(0xFFEF4444)))
            list.add(DashboardStatItem("Total With.", "$totalWithdrawals", Icons.Default.MoneyOff, PurplePrimary))
        }
        if (isOwner || userProfile.hasPermission("support_center")) {
            list.add(DashboardStatItem("Support Chats", "${supportThreads.size}", Icons.Default.HeadsetMic, Color(0xFF06B6D4)))
        }
        if (isOwner || userProfile.hasPermission("packages")) {
            list.add(DashboardStatItem("Active Packages", "${packages.size}", Icons.Default.CardGiftcard, Color(0xFF8B5CF6)))
        }
        if (isOwner || userProfile.hasPermission("package_orders")) {
            list.add(DashboardStatItem("Pending Orders", "$pendingPackageOrders", Icons.Default.ShoppingCart, if (pendingPackageOrders > 0) Color(0xFFF59E0B) else PurplePrimary))
            list.add(DashboardStatItem("Total Orders", "$totalPackageOrders", Icons.Default.ShoppingCart, PurplePrimary))
        }
        list
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (statCards.isNotEmpty()) {
            statCards.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { item ->
                        DashboardStatCard(
                            modifier = Modifier.weight(1f),
                            title = item.title,
                            value = item.value,
                            icon = item.icon,
                            color = item.color
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (isOwner) {
            AdminModuleItem(
                icon = Icons.Default.AdminPanelSettings,
                title = "Admin Management",
                subtitle = "Manage administrator accounts and granular permissions",
                onClick = { onNavigate("admins") }
            )
        }

        if (isOwner || userProfile.hasPermission("users")) {
            AdminModuleItem(
                icon = Icons.Default.People,
                title = "User Management",
                subtitle = "Manage $totalUsers users",
                onClick = { onNavigate("users") }
            )
        }

        if (isOwner || userProfile.hasPermission("deposits")) {
            AdminModuleItem(
                icon = Icons.Default.AccountBalanceWallet,
                title = "Deposit Requests",
                subtitle = "Review $pendingDeposits pending requests",
                onClick = { onNavigate("deposits") }
            )
        }

        if (isOwner || userProfile.hasPermission("withdrawals")) {
            AdminModuleItem(
                icon = Icons.Default.MoneyOff,
                title = "Withdrawal Requests",
                subtitle = "Review $pendingWithdrawals pending requests",
                onClick = { onNavigate("withdrawals") }
            )
        }

        if (isOwner || userProfile.hasPermission("deposit_methods")) {
            AdminModuleItem(
                icon = Icons.Default.Payments,
                title = "Deposit Payment Methods",
                subtitle = "Manage payment receiving accounts & numbers",
                onClick = { onNavigate("deposit_methods") }
            )
        }

        if (isOwner || userProfile.hasPermission("withdrawal_methods")) {
            AdminModuleItem(
                icon = Icons.Default.AccountBalance,
                title = "Withdrawal Payment Methods",
                subtitle = "Manage payout method options",
                onClick = { onNavigate("withdrawal_methods") }
            )
        }

        if (isOwner || userProfile.hasPermission("support_center")) {
            AdminModuleItem(
                icon = Icons.Default.HeadsetMic,
                title = "Support Center (Live Chats)",
                subtitle = "View and reply to ${supportThreads.size} user support threads",
                onClick = { onNavigate("settings_support") }
            )
        }

        if (isOwner || userProfile.hasPermission("packages")) {
            AdminModuleItem(
                icon = Icons.Default.CardGiftcard,
                title = "Paid Packages",
                subtitle = "Manage ${packages.size} unlimited traffic packages",
                onClick = { onNavigate("packages") }
            )
        }

        if (isOwner || userProfile.hasPermission("package_orders")) {
            AdminModuleItem(
                icon = Icons.Default.ShoppingCart,
                title = "Package Orders",
                subtitle = "Review $pendingPackageOrders pending / $totalPackageOrders total orders",
                onClick = { onNavigate("package_orders") }
            )
        }

        if (isOwner || userProfile.hasPermission("campaigns")) {
            AdminModuleItem(
                icon = Icons.Default.TrendingUp,
                title = "Campaign Management",
                subtitle = "View active and pending campaigns",
                onClick = { onNavigate("campaigns") }
            )
        }

        if (isOwner || userProfile.hasPermission("general_settings") || userProfile.hasPermission("settings")) {
            AdminModuleItem(
                icon = Icons.Default.Tune,
                title = "General Settings",
                subtitle = "Browser config, rate calculations, online users range",
                onClick = { onNavigate("settings_general") }
            )
        }

        if (isOwner || userProfile.hasPermission("developer_settings")) {
            AdminModuleItem(
                icon = Icons.Default.Code,
                title = "Developer Profile Settings",
                subtitle = "Update developer profile photo, bio and contact info",
                onClick = { onNavigate("settings_developer") }
            )
        }
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
