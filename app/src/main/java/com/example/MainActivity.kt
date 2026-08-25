package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.components.AppBottomNavigationBar
import com.example.navigation.Screen
import com.example.repository.AppRepository
import com.example.screens.auth.LoginScreen
import com.example.screens.auth.RegisterScreen
import com.example.screens.account.AboutUsScreen
import com.example.screens.account.AccountDetailsScreen
import com.example.screens.account.DeveloperProfileScreen
import com.example.screens.account.MyAccountScreen
import com.example.screens.account.PrivacyPolicyScreen
import com.example.screens.account.ShareAppScreen
import com.example.screens.account.SupportCenterScreen
import com.example.screens.account.WithdrawalHistoryScreen
import com.example.screens.campaign.CampaignDetailScreen
import com.example.screens.campaign.CampaignListScreen
import com.example.screens.campaign.CreateCampaignScreen
import com.example.screens.deposit.DepositScreen
import com.example.screens.home.HomeScreen
import com.example.screens.refer.ReferScreen
import com.example.screens.splash.SplashScreen
import com.example.screens.tasks.TasksScreen
import com.example.screens.tasks.VisitEarnScreen
import com.example.screens.tasks.AdstraEarningScreen
import com.example.screens.tasks.BloggerEarningScreen
import com.example.screens.tasks.MonetagEarningScreen
import com.example.screens.transactions.TransactionHistoryScreen
import com.example.screens.withdraw.WithdrawScreen
import com.example.ui.theme.PayPulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val repository = remember { AppRepository.getInstance(this) }
            val isDarkMode by repository.isDarkMode.collectAsState()

            PayPulseTheme(darkTheme = isDarkMode) {
                PayPulseApp(repository = repository)
            }
        }
    }
}

@Composable
fun PayPulseApp(repository: AppRepository) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = setOf(
        Screen.Home.route,
        Screen.VisitEarn.route,
        Screen.CreateCampaign.route,
        Screen.Tasks.route,
        Screen.MyAccount.route
    )

    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                AppBottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { targetRoute ->
                        navController.navigate(targetRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier.fillMaxSize()
            ) {
                // 0. Splash Screen
                composable(Screen.Splash.route) {
                    SplashScreen(
                        onSplashFinished = {
                            val target = if (repository.isLoggedIn.value) Screen.Home.route else Screen.Login.route
                            navController.navigate(target) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                // Auth Screens
                composable(Screen.Login.route) {
                    LoginScreen(
                        repository = repository,
                        onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Register.route) {
                    RegisterScreen(
                        repository = repository,
                        onNavigateToLogin = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        },
                        onRegistrationSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        }
                    )
                }

                // 1. Home Screen
                composable(Screen.Home.route) {
                    HomeScreen(
                        repository = repository,
                        onNavigateToDeposit = { navController.navigate(Screen.Deposit.route) },
                        onNavigateToWithdraw = { navController.navigate(Screen.Withdraw.route) },
                        onNavigateToTransactions = { navController.navigate(Screen.TransactionHistory.route) },
                        onNavigateToCampaigns = { navController.navigate(Screen.MyCampaigns.route) },
                        onNavigateToCampaignDetail = { campaignId ->
                            navController.navigate(Screen.CampaignDetails.createRoute(campaignId))
                        },
                        onNavigateToRefer = { navController.navigate(Screen.Refer.route) },
                        onNavigateToAccount = { navController.navigate(Screen.MyAccount.route) }
                    )
                }

                // 2. Visit Earn Screen
                composable(Screen.VisitEarn.route) {
                    VisitEarnScreen(
                        repository = repository,
                        onNavigateToAdstra = { navController.navigate(Screen.Adstra.route) },
                        onNavigateToBlogger = { navController.navigate(Screen.Blogger.route) },
                        onNavigateToMonetag = { navController.navigate(Screen.Monetag.route) },
                        onBackClick = null
                    )
                }

                // Refer Screen (still accessible from elsewhere)
                composable(Screen.Refer.route) {
                    ReferScreen(
                        repository = repository,
                        onBackClick = null
                    )
                }

                // 3. Create Campaign Screen (Plus)
                composable(Screen.CreateCampaign.route) {
                    CreateCampaignScreen(
                        repository = repository,
                        onBackClick = null,
                        onNavigateToCampaignList = { navController.navigate(Screen.MyCampaigns.route) }
                    )
                }

                // 4. Tasks Screen
                composable(Screen.Tasks.route) {
                    TasksScreen(
                        repository = repository,
                        onBackClick = null
                    )
                }

                // 5. My Account Screen
                composable(Screen.MyAccount.route) {
                    MyAccountScreen(
                        repository = repository,
                        onNavigateToAccountDetails = { navController.navigate(Screen.AccountDetails.route) },
                        onNavigateToWithdrawalHistory = { navController.navigate(Screen.WithdrawalHistory.route) },
                        onNavigateToReferral = { navController.navigate(Screen.Refer.route) },
                        onNavigateToShareApp = { navController.navigate(Screen.ShareApp.route) },
                        onNavigateToSupportCenter = { navController.navigate(Screen.SupportCenter.route) },
                        onNavigateToPrivacyPolicy = { navController.navigate(Screen.PrivacyPolicy.route) },
                        onNavigateToAboutUs = { navController.navigate(Screen.AboutUs.route) },
                        onNavigateToDeveloperProfile = { navController.navigate(Screen.DeveloperProfile.route) },
                        onNavigateToAdminDashboard = { navController.navigate("admin_dashboard") },
                        onLogout = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                composable("admin_dashboard") {
                    com.example.screens.admin.AdminDashboardScreen(
                        repository = repository,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // Sub-screens
                // 6. Deposit
                composable(Screen.Deposit.route) {
                    DepositScreen(
                        repository = repository,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // 7. Withdraw
                composable(Screen.Withdraw.route) {
                    WithdrawScreen(
                        repository = repository,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // 8. Transaction History
                composable(Screen.TransactionHistory.route) {
                    TransactionHistoryScreen(
                        repository = repository,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // 9. Campaign List
                composable(Screen.MyCampaigns.route) {
                    CampaignListScreen(
                        repository = repository,
                        onBackClick = { navController.popBackStack() },
                        onCampaignClick = { campaignId ->
                            navController.navigate(Screen.CampaignDetails.createRoute(campaignId))
                        },
                        onCreateCampaignClick = { navController.navigate(Screen.CreateCampaign.route) }
                    )
                }

                // 10. Campaign Detail
                composable(
                    route = Screen.CampaignDetails.route,
                    arguments = listOf(navArgument("campaignId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val campaignId = backStackEntry.arguments?.getString("campaignId") ?: ""
                    CampaignDetailScreen(
                        campaignId = campaignId,
                        repository = repository,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // 11. Account Details
                composable(Screen.AccountDetails.route) {
                    AccountDetailsScreen(
                        repository = repository,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // 12. Withdrawal History
                composable(Screen.WithdrawalHistory.route) {
                    WithdrawalHistoryScreen(
                        repository = repository,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // 13. Share App
                composable(Screen.ShareApp.route) {
                    ShareAppScreen(
                        repository = repository,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // 14. Support Center
                composable(Screen.SupportCenter.route) {
                    SupportCenterScreen(
                        repository = repository,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // 15. Privacy Policy
                composable(Screen.PrivacyPolicy.route) {
                    PrivacyPolicyScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // 16. About Us
                composable(Screen.AboutUs.route) {
                    AboutUsScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // 17. Developer Profile
                composable(Screen.DeveloperProfile.route) {
                    DeveloperProfileScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // 18. Adstra Earning
                composable(Screen.Adstra.route) {
                    AdstraEarningScreen(
                        repository = repository,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // 19. Blogger Earning
                composable(Screen.Blogger.route) {
                    BloggerEarningScreen(
                        repository = repository,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // 20. Monetag Earning
                composable(Screen.Monetag.route) {
                    MonetagEarningScreen(
                        repository = repository,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
