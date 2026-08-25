import re

with open("app/src/main/java/com/example/screens/admin/AdminDashboardScreen.kt", "r") as f:
    content = f.read()

# Make AdminDashboardMainContent take repository
content = content.replace("fun AdminDashboardMainContent(onNavigate: (String) -> Unit) {", "fun AdminDashboardMainContent(repository: com.example.repository.AppRepository, onNavigate: (String) -> Unit) {")

content = content.replace("                    \"dashboard\" -> AdminDashboardMainContent(onNavigate = { selectedScreen = it })", "                    \"dashboard\" -> AdminDashboardMainContent(repository = repository, onNavigate = { selectedScreen = it })")
content = content.replace("                \"users\" -> AdminUsersScreen()", "                \"users\" -> AdminUsersScreen(repository = repository)")
content = content.replace("                \"campaigns\" -> AdminCampaignsScreen()", "                \"campaigns\" -> AdminCampaignsScreen(repository = repository)")

new_content_body = """    val users by repository.allUsers.collectAsState()
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
        // Quick Stats Row
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
                color = Color(0xFFF59E0B) // WarningOrange
            )
            DashboardStatCard(
                modifier = Modifier.weight(1f),
                title = "With. Pending",
                value = "$pendingWithdrawals",
                icon = Icons.Default.MoneyOff,
                color = Color(0xFFEF4444) // ErrorRed
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
    }"""

old_content_regex = r"    Column\(\s*modifier = Modifier[\s\S]*?onClick = \{ onNavigate\(\"settings\"\) \}\s*\)\s*\}"
content = re.sub(old_content_regex, new_content_body, content)

content = content.replace("import androidx.compose.runtime.Composable", "import androidx.compose.runtime.Composable\nimport androidx.compose.runtime.collectAsState\nimport androidx.compose.runtime.getValue")

with open("app/src/main/java/com/example/screens/admin/AdminDashboardScreen.kt", "w") as f:
    f.write(content)
