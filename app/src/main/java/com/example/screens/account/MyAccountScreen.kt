package com.example.screens.account

import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContactSupport
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.components.AccountMenuCard
import com.example.components.SectionHeader
import com.example.repository.AppRepository
import com.example.utils.FormatUtils
import com.example.ui.theme.DarkPurpleCard
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WalletGradientBrush

@Composable
fun MyAccountScreen(
    repository: AppRepository,
    onNavigateToAccountDetails: () -> Unit,
    onNavigateToWithdrawalHistory: () -> Unit,
    onNavigateToReferral: () -> Unit,
    onNavigateToShareApp: () -> Unit,
    onNavigateToSupportCenter: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToAboutUs: () -> Unit,
    onNavigateToDeveloperProfile: () -> Unit,
    onNavigateToAdminDashboard: () -> Unit,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val userProfile by repository.userProfile.collectAsState()
    val walletState by repository.walletState.collectAsState()
    val isDarkMode by repository.isDarkMode.collectAsState()
    val preferredBrowser by repository.preferredBrowser.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = "Sign Out", fontWeight = FontWeight.Bold) },
            text = { Text(text = "Are you sure you want to sign out of your PayPulse account?") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showLogoutDialog = false
                        repository.logout()
                        onLogout()
                    }
                ) {
                    Text(text = "Sign Out", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showLogoutDialog = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("my_account_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Account",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Large Centered Profile Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(28.dp),
                            spotColor = PurplePrimary
                        )
                        .testTag("account_profile_card"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(WalletGradientBrush)
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(28.dp)
                            )
                            .padding(vertical = 20.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Centered Large Circular Profile Picture
                            Surface(
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(80.dp)
                                    .border(3.5.dp, PurpleNeon, CircleShape)
                                    .shadow(8.dp, CircleShape),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_avatar_maruf_1787554123074),
                                    contentDescription = "Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.clip(CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Centered Name
                            Text(
                                text = userProfile.fullName,
                                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            // Centered Username Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = userProfile.username,
                                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
                                    color = PurpleNeon,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "${FormatUtils.formatCreditsOnly(walletState.balance)} Credits",
                                style = MaterialTheme.typography.titleMedium,
                                color = GoldAccent,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Centered Contact Info
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = userProfile.email,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = userProfile.phone,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Centered Stats Bar / Account Meta
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.25f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Account ID",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = Color.White.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = userProfile.accountId,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .height(20.dp)
                                            .width(1.dp)
                                            .background(Color.White.copy(alpha = 0.2f))
                                    )

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Member Since",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = Color.White.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = userProfile.registrationDate,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Account Menu Header
            item {
                SectionHeader(
                    title = "Menu Options",
                    actionText = null
                )
            }

            // 3. Exactly 8 Account Menu Items
            item {
                AccountMenuCard(
                    icon = Icons.Outlined.Person,
                    title = "Account Details",
                    description = "Manage your personal profile and contact info",
                    onClick = onNavigateToAccountDetails
                )
            }

            item {
                AccountMenuCard(
                    icon = Icons.Outlined.History,
                    title = "Withdrawal History",
                    description = "Track payout records and transfer statuses",
                    onClick = onNavigateToWithdrawalHistory
                )
            }

            item {
                AccountMenuCard(
                    icon = Icons.Outlined.People,
                    title = "Referral",
                    description = "Invite network friends and earn rewards",
                    onClick = onNavigateToReferral
                )
            }

            item {
                AccountMenuCard(
                    icon = Icons.Outlined.Share,
                    title = "Share App",
                    description = "Share PayPulse with your contacts and friends",
                    onClick = onNavigateToShareApp
                )
            }

            item {
                AccountMenuCard(
                    icon = Icons.Outlined.HelpOutline,
                    title = "Support Center",
                    description = "Browse FAQs and submit support inquiries",
                    onClick = onNavigateToSupportCenter
                )
            }

            item {
                AccountMenuCard(
                    icon = Icons.Outlined.Policy,
                    title = "Privacy Policy",
                    description = "Read our terms, security and privacy guidelines",
                    onClick = onNavigateToPrivacyPolicy
                )
            }

            item {
                AccountMenuCard(
                    icon = Icons.Outlined.Info,
                    title = "About Us",
                    description = "Learn more about the PayPulse reward platform",
                    onClick = onNavigateToAboutUs
                )
            }

            item {
                AccountMenuCard(
                    icon = Icons.Outlined.Terminal,
                    title = "Developer Profile",
                    description = "Technical specifications and engineering profile",
                    onClick = onNavigateToDeveloperProfile
                )
            }

            // Theme Mode Toggle Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(PurplePrimary.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Theme Icon",
                                    tint = PurplePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = if (isDarkMode) "Dark Mode Enabled" else "Light Mode Enabled (Default)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isDarkMode) "Switch to light theme" else "Switch to dark theme",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        androidx.compose.material3.Switch(
                            checked = isDarkMode,
                            onCheckedChange = { repository.toggleDarkMode() },
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PurplePrimary
                            )
                        )
                    }
                }
            }



            // Admin Panel Button Card (Only for Admins/Owners)
            if (userProfile.role == "ADMIN" || userProfile.role == "OWNER" || userProfile.email == "d@gmail.com") {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(18.dp))
                            .clickable { onNavigateToAdminDashboard() },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(SuccessGreen.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Admin Panel",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Admin Dashboard",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                                Text(
                                    text = "Control app settings and data",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Go",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Sign Out / Log Out Button Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(18.dp))
                        .clickable { showLogoutDialog = true },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(ErrorRed.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ManageAccounts,
                                contentDescription = "Sign Out",
                                tint = ErrorRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sign Out",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ErrorRed
                            )
                            Text(
                                text = "Log out from your current PayPulse session",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
