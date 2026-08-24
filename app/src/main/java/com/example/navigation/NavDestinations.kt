package com.example.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    
    // Bottom Nav Tabs
    object Home : Screen("home")
    object Refer : Screen("refer")
    object CreateCampaign : Screen("create_campaign")
    object Tasks : Screen("tasks")
    object MyAccount : Screen("account")

    // Sub Screens
    object Deposit : Screen("deposit")
    object Withdraw : Screen("withdraw")
    object TransactionHistory : Screen("transaction_history")
    object MyCampaigns : Screen("my_campaigns")
    object CampaignDetails : Screen("campaign_details/{campaignId}") {
        fun createRoute(campaignId: String) = "campaign_details/$campaignId"
    }
    object AccountDetails : Screen("account_details")
    object WithdrawalHistory : Screen("withdrawal_history")
    object ShareApp : Screen("share_app")
    object SupportCenter : Screen("support_center")
    object PrivacyPolicy : Screen("privacy_policy")
    object AboutUs : Screen("about_us")
    object DeveloperProfile : Screen("developer_profile")
    object Adstra : Screen("adstra")
    object Blogger : Screen("blogger")
    object VisitEarn : Screen("visit_earn")
    object Monetag : Screen("monetag")
}
