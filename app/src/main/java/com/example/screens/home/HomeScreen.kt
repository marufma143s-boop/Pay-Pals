package com.example.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.People
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
import androidx.compose.ui.unit.dp
import com.example.components.AppStatCard
import com.example.components.CampaignCard
import com.example.components.MainAppHeader
import com.example.components.ReferralCampaignPromoBanner
import com.example.components.ReferralCard
import com.example.components.SectionHeader
import com.example.components.ServiceDisabledDialog
import com.example.components.TransactionCard
import com.example.components.WalletCard
import com.example.components.WalletQuickActionButtons
import com.example.components.OnlineUsersCard
import com.example.model.Campaign
import com.example.model.ReferralUser
import com.example.model.Transaction
import com.example.model.UserProfile
import com.example.model.WalletState
import com.example.repository.AppRepository
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange
import com.example.utils.FormatUtils

@Composable
fun HomeScreen(
    repository: AppRepository,
    onNavigateToDeposit: () -> Unit,
    onNavigateToWithdraw: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToCampaigns: () -> Unit,
    onNavigateToCampaignDetail: (String) -> Unit,
    onNavigateToRefer: () -> Unit,
    onNavigateToAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by repository.userProfile.collectAsState()
    val walletState by repository.walletState.collectAsState()
    val transactions by repository.transactions.collectAsState()
    val campaigns by repository.campaigns.collectAsState()
    val referrals by repository.referrals.collectAsState()
    val totalCampaignCount by repository.totalCampaignCount.collectAsState()
    val totalReferralCount by repository.totalReferralCount.collectAsState()
    val onlineUsersMin by repository.onlineUsersMin.collectAsState()
    val onlineUsersMax by repository.onlineUsersMax.collectAsState()
    val isDarkMode by repository.isDarkMode.collectAsState()
    val serviceSettings by repository.serviceControlSettings.collectAsState()
    val maintenanceSettings by repository.maintenanceSettings.collectAsState()

    var disabledServiceDialogInfo by remember { mutableStateOf<Pair<String, String>?>(null) }
    var hasShownPopup by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }

    if (!hasShownPopup && maintenanceSettings.popupNotice?.isEnabled == true) {
        com.example.components.PopupNoticeDialog(
            settings = maintenanceSettings.popupNotice!!,
            socialLinks = maintenanceSettings.socialLinks.associate { it.iconKey to it.url },
            onDismiss = { hasShownPopup = true },
            onNavigate = { route -> 
                hasShownPopup = true
                when (route) {
                    "deposit" -> onNavigateToDeposit()
                    "withdraw" -> onNavigateToWithdraw()
                    "transactions" -> onNavigateToTransactions()
                    "campaigns", "campaign" -> onNavigateToCampaigns()
                    "referral", "refer" -> onNavigateToRefer()
                    "my_account", "account" -> onNavigateToAccount()
                }
            }
        )
    }

    val isDepositDisabled = serviceSettings.isServiceDisabled("deposit")
    val isWithdrawDisabled = serviceSettings.isServiceDisabled("withdraw")
    val isReferralDisabled = serviceSettings.isServiceDisabled("referral")

    // 5 Recent Items
    val recentTransactions = transactions.take(5)
    val recentCampaigns = campaigns.take(5)
    val recentReferrals = referrals.take(5)

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen")
    ) {
        MainAppHeader(
            userProfile = userProfile,
            isDarkMode = isDarkMode,
            onToggleDarkMode = { repository.toggleDarkMode() },
            onProfileClick = onNavigateToAccount
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Larger Wallet Card
            item {
                WalletCard(
                    walletState = walletState,
                    onToggleVisibility = { repository.toggleBalanceVisibility() }
                )
            }
            
            // Online Users Card
            item {
                OnlineUsersCard(
                    minUsers = onlineUsersMin,
                    maxUsers = onlineUsersMax
                )
            }

            // 2. Separate Action Button Cards Below Wallet
            item {
                WalletQuickActionButtons(
                    onDepositClick = {
                        if (isDepositDisabled) {
                            disabledServiceDialogInfo = Pair("ডিপোজিট সার্ভিস (Deposit)", serviceSettings.getServiceReason("deposit"))
                        } else {
                            onNavigateToDeposit()
                        }
                    },
                    onWithdrawClick = {
                        if (isWithdrawDisabled) {
                            disabledServiceDialogInfo = Pair("উইথড্রয়াল সার্ভিস (Withdrawal)", serviceSettings.getServiceReason("withdraw"))
                        } else {
                            onNavigateToWithdraw()
                        }
                    },
                    onHistoryClick = onNavigateToTransactions,
                    isDepositDisabled = isDepositDisabled,
                    isWithdrawDisabled = isWithdrawDisabled
                )
            }

            // 3. Statistics Grid (4 Cards, 2 per row)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Total Campaign
                        AppStatCard(
                            title = "Total Campaign",
                            value = "$totalCampaignCount",
                            supportingText = "Active Promotions",
                            icon = Icons.Outlined.Campaign,
                            accentColor = PurpleNeon,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToCampaigns
                        )

                        // Total Referrals
                        AppStatCard(
                            title = "Total Referrals",
                            value = "$totalReferralCount",
                            supportingText = if (isReferralDisabled) "Closed" else "Network Members",
                            icon = Icons.Outlined.People,
                            accentColor = if (isReferralDisabled) Color.Gray else InfoBlue,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (isReferralDisabled) {
                                    disabledServiceDialogInfo = Pair("রেফারেল সিস্টেম (Referral)", serviceSettings.getServiceReason("referral"))
                                } else {
                                    onNavigateToRefer()
                                }
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Total Deposit
                        AppStatCard(
                            title = "Total Deposit",
                            value = FormatUtils.formatCredits(walletState.totalDeposit),
                            supportingText = if (isDepositDisabled) "Closed" else "All Time Deposits",
                            icon = Icons.Filled.ArrowDownward,
                            accentColor = if (isDepositDisabled) Color.Gray else SuccessGreen,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (isDepositDisabled) {
                                    disabledServiceDialogInfo = Pair("ডিপোজিট সার্ভিস (Deposit)", serviceSettings.getServiceReason("deposit"))
                                } else {
                                    onNavigateToDeposit()
                                }
                            }
                        )

                        // Total Withdrawal
                        AppStatCard(
                            title = "Total Withdrawal",
                            value = FormatUtils.formatCredits(walletState.totalWithdrawal),
                            supportingText = if (isWithdrawDisabled) "Closed" else "Processed Payouts",
                            icon = Icons.Filled.ArrowUpward,
                            accentColor = if (isWithdrawDisabled) Color.Gray else WarningOrange,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (isWithdrawDisabled) {
                                    disabledServiceDialogInfo = Pair("উইথড্রয়াল সার্ভিস (Withdrawal)", serviceSettings.getServiceReason("withdraw"))
                                } else {
                                    onNavigateToWithdraw()
                                }
                            }
                        )
                    }
                }
            }

            // 4. Referral & Campaign Promotion Banner Card
            item {
                ReferralCampaignPromoBanner(
                    onReferClick = {
                        if (isReferralDisabled) {
                            disabledServiceDialogInfo = Pair("রেফারেল সিস্টেম (Referral)", serviceSettings.getServiceReason("referral"))
                        } else {
                            onNavigateToRefer()
                        }
                    }
                )
            }

            // 5. Recent Transactions (Exactly 5)
            item {
                SectionHeader(
                    title = "Recent Transactions",
                    actionText = if (recentTransactions.isNotEmpty()) "View All" else null,
                    onActionClick = onNavigateToTransactions
                )
            }

            if (recentTransactions.isEmpty()) {
                item {
                    com.example.components.EmptyStateView(
                        icon = Icons.Filled.ArrowDownward,
                        title = "No Transactions",
                        message = "Your recent deposit, withdrawal, and reward history will appear here."
                    )
                }
            } else {
                items(recentTransactions, key = { it.id }) { txn ->
                    TransactionCard(
                        transaction = txn,
                        onClick = onNavigateToTransactions
                    )
                }
            }

            // 6. Recent Referrals (Exactly 5)
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader(
                    title = "Recent Referrals",
                    actionText = if (recentReferrals.isNotEmpty()) "View All" else null,
                    onActionClick = {
                        if (isReferralDisabled) {
                            disabledServiceDialogInfo = Pair("রেফারেল সিস্টেম (Referral)", serviceSettings.getServiceReason("referral"))
                        } else {
                            onNavigateToRefer()
                        }
                    }
                )
            }

            if (recentReferrals.isEmpty()) {
                item {
                    com.example.components.EmptyStateView(
                        icon = Icons.Outlined.People,
                        title = "No Referrals Yet",
                        message = "Invite friends with your referral code to earn bonuses together!"
                    )
                }
            } else {
                items(recentReferrals, key = { it.id }) { referral ->
                    ReferralCard(referral = referral)
                }
            }

            // Bottom padding for scroll above floating bar
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Modal dialog when user taps a disabled service
    if (disabledServiceDialogInfo != null) {
        val (title, reason) = disabledServiceDialogInfo!!
        ServiceDisabledDialog(
            serviceTitle = title,
            reason = reason,
            onDismiss = { disabledServiceDialogInfo = null }
        )
    }
}
