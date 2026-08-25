package com.example.data

import com.example.model.Campaign
import com.example.model.CampaignPackage
import com.example.model.CampaignStatus
import com.example.model.FAQItem
import com.example.model.ReferralStatus
import com.example.model.ReferralUser
import com.example.model.SupportCategory
import com.example.model.SupportTicket
import com.example.model.TaskItem
import com.example.model.Transaction
import com.example.model.TransactionStatus
import com.example.model.TransactionType
import com.example.model.UserProfile
import com.example.model.WalletState
import com.example.model.WithdrawalRecord

object DemoDataProvider {

    fun getInitialUser(): UserProfile = UserProfile(
        username = "@maruf123",
        fullName = "Maruf Hossain",
        email = "maruf@example.com",
        phone = "+8801712345678",
        accountId = "PP-982314",
        registrationDate = "Aug 15, 2026",
        referralCode = "MARUF123",
        appliedReferralCode = null
    )

    fun getInitialWallet(): WalletState = WalletState(
        balance = 12450.0,
        totalDeposit = 45500.0,
        totalWithdrawal = 21300.0,
        totalReferralEarnings = 3450.0,
        isBalanceVisible = true
    )

    val packages: List<CampaignPackage> = listOf(
        CampaignPackage(
            id = "pkg_30",
            price = 3000.0,
            targetViews = 500,
            description = "Ideal for rapid test traffic and micro campaigns.",
            isPopular = false
        ),
        CampaignPackage(
            id = "pkg_50",
            price = 5000.0,
            targetViews = 1000,
            description = "Best balance of audience reach and engagement.",
            isPopular = true
        ),
        CampaignPackage(
            id = "pkg_100",
            price = 10000.0,
            targetViews = 4000,
            description = "High engagement tier for high-converting landing pages.",
            isPopular = false
        ),
        CampaignPackage(
            id = "pkg_200",
            price = 20000.0,
            targetViews = 10000,
            description = "Massive viral distribution and verified clicks.",
            isPopular = false
        )
    )

    fun getInitialTransactions(): List<Transaction> = listOf(
        Transaction(
            id = "txn_1",
            title = "Deposit",
            type = TransactionType.DEPOSIT,
            amount = 100000.0,
            dateFormatted = "Today",
            timeFormatted = "10:30 AM",
            transactionId = "TXN-102934",
            status = TransactionStatus.COMPLETED,
            note = "Mobile Banking (bKash)"
        ),
        Transaction(
            id = "txn_2",
            title = "Withdrawal",
            type = TransactionType.WITHDRAWAL,
            amount = 50000.0,
            dateFormatted = "Yesterday",
            timeFormatted = "04:20 PM",
            transactionId = "TXN-102933",
            status = TransactionStatus.PENDING,
            note = "Bank Transfer Account **** 4812"
        ),
        Transaction(
            id = "txn_3",
            title = "Task Reward",
            type = TransactionType.TASK_REWARD,
            amount = 500.0,
            dateFormatted = "Aug 23, 2026",
            timeFormatted = "02:15 PM",
            transactionId = "TXN-102928",
            status = TransactionStatus.COMPLETED,
            note = "Visit Website campaign task"
        ),
        Transaction(
            id = "txn_4",
            title = "Referral Reward",
            type = TransactionType.REFERRAL_REWARD,
            amount = 5000.0,
            dateFormatted = "Aug 22, 2026",
            timeFormatted = "11:45 AM",
            transactionId = "TXN-102919",
            status = TransactionStatus.COMPLETED,
            note = "Invited new member @sakib_pro"
        ),
        Transaction(
            id = "txn_5",
            title = "Campaign Payment",
            type = TransactionType.CAMPAIGN_PAYMENT,
            amount = 5000.0,
            dateFormatted = "Aug 21, 2026",
            timeFormatted = "09:10 AM",
            transactionId = "TXN-102905",
            status = TransactionStatus.COMPLETED,
            note = "Standard Growth 1,000 Views package"
        ),
        Transaction(
            id = "txn_6",
            title = "Deposit",
            type = TransactionType.DEPOSIT,
            amount = 500000.0,
            dateFormatted = "Aug 20, 2026",
            timeFormatted = "06:30 PM",
            transactionId = "TXN-102890",
            status = TransactionStatus.COMPLETED,
            note = "Bank Transfer"
        ),
        Transaction(
            id = "txn_7",
            title = "Task Reward",
            type = TransactionType.TASK_REWARD,
            amount = 1000.0,
            dateFormatted = "Aug 19, 2026",
            timeFormatted = "03:40 PM",
            transactionId = "TXN-102875",
            status = TransactionStatus.COMPLETED,
            note = "Review Campaign submission"
        )
    )

    fun getInitialCampaigns(): List<Campaign> = listOf(
        Campaign(
            id = "cmp_101",
            title = "Website Promotion",
            networkType = "adstra",
            targetLink = "https://example.com/promo",
            packagePrice = 5000.0,
            targetViews = 1000,
            completedViews = 450,
            status = CampaignStatus.RUNNING,
            createdDate = "Aug 22, 2026"
        ),
        Campaign(
            id = "cmp_102",
            title = "E-Commerce Summer Sale",
            networkType = "monetag",
            targetLink = "https://shopnow.com/summer-deals",
            packagePrice = 10000.0,
            targetViews = 4000,
            completedViews = 3200,
            status = CampaignStatus.RUNNING,
            createdDate = "Aug 20, 2026"
        ),
        Campaign(
            id = "cmp_103",
            title = "App Install Campaign",
            networkType = "adstra",
            targetLink = "https://play.google.com/store/apps/details?id=sample.app",
            packagePrice = 20000.0,
            targetViews = 10000,
            completedViews = 10000,
            status = CampaignStatus.COMPLETED,
            createdDate = "Aug 16, 2026"
        ),
        Campaign(
            id = "cmp_104",
            title = "Tech Blog Traffic",
            networkType = "blogger",
            targetLink = "https://techpulse.blog/ai-trends",
            packagePrice = 3000.0,
            targetViews = 500,
            completedViews = 180,
            status = CampaignStatus.RUNNING,
            createdDate = "Aug 23, 2026"
        ),
        Campaign(
            id = "cmp_105",
            title = "Fintech Product Launch",
            networkType = "blogger",
            targetLink = "https://finhub.io/signup",
            packagePrice = 5000.0,
            targetViews = 1000,
            completedViews = 0,
            status = CampaignStatus.PENDING,
            createdDate = "Aug 24, 2026"
        ),
        Campaign(
            id = "cmp_106",
            title = "Social Community Growth",
            networkType = "monetag",
            targetLink = "https://community.hub/join",
            packagePrice = 3000.0,
            targetViews = 500,
            completedViews = 500,
            status = CampaignStatus.COMPLETED,
            createdDate = "Aug 14, 2026"
        )
    )

    fun getInitialReferrals(): List<ReferralUser> = listOf(
        ReferralUser(
            id = "ref_1",
            username = "@john123",
            joinDate = "Aug 24, 2026",
            status = ReferralStatus.ACTIVE,
            reward = 100.0
        ),
        ReferralUser(
            id = "ref_2",
            username = "@sakib_pro",
            joinDate = "Aug 22, 2026",
            status = ReferralStatus.ACTIVE,
            reward = 100.0
        ),
        ReferralUser(
            id = "ref_3",
            username = "@hasan_khan",
            joinDate = "Aug 21, 2026",
            status = ReferralStatus.ACTIVE,
            reward = 100.0
        ),
        ReferralUser(
            id = "ref_4",
            username = "@tanvir_bd",
            joinDate = "Aug 19, 2026",
            status = ReferralStatus.PENDING,
            reward = 100.0
        ),
        ReferralUser(
            id = "ref_5",
            username = "@rakib_tech",
            joinDate = "Aug 18, 2026",
            status = ReferralStatus.ACTIVE,
            reward = 100.0
        ),
        ReferralUser(
            id = "ref_6",
            username = "@fahim_99",
            joinDate = "Aug 15, 2026",
            status = ReferralStatus.ACTIVE,
            reward = 100.0
        ),
        ReferralUser(
            id = "ref_7",
            username = "@arif_coder",
            joinDate = "Aug 12, 2026",
            status = ReferralStatus.ACTIVE,
            reward = 100.0
        )
    )

    fun getInitialTasks(): List<TaskItem> = listOf(
        TaskItem(
            id = "task_1",
            title = "Visit Website",
            description = "Visit the assigned website and complete the required engagement action.",
            instructions = "1. Click Complete Task to visit the sponsor link.\n2. Stay on the web page for at least 15 seconds.\n3. Scroll to the bottom to verify complete load.",
            reward = 500.0,
            estimatedTime = "30s",
            isCompleted = false,
            category = "Website",
            completedCount = 38,
            totalLimit = 50
        ),
        TaskItem(
            id = "task_2",
            title = "View Campaign",
            description = "Explore featured advertiser promotion and review campaign highlights.",
            instructions = "1. Open the sponsored banner.\n2. Read through the featured promotional offer.\n3. Return to claim your reward instantly.",
            reward = 500.0,
            estimatedTime = "45s",
            isCompleted = false,
            category = "Campaign",
            completedCount = 44,
            totalLimit = 50
        ),
        TaskItem(
            id = "task_3",
            title = "Open Promotion",
            description = "Check daily brand campaign partner deals and promotional announcements.",
            instructions = "1. Click Open Promotion to view exclusive discounts.\n2. Inspect the deal catalog.\n3. Receive instant 800 Credits.",
            reward = 800.0,
            estimatedTime = "1m",
            isCompleted = false,
            category = "Deals",
            completedCount = 29,
            totalLimit = 50
        ),
        TaskItem(
            id = "task_4",
            title = "Check Content",
            description = "Browse the sponsored tech news article and verify readability.",
            instructions = "1. Open the article link.\n2. Read the headline and summary points.\n3. Confirm review to add 500 Credits to your wallet.",
            reward = 500.0,
            estimatedTime = "30s",
            isCompleted = false,
            category = "Content",
            completedCount = 47,
            totalLimit = 50
        ),
        TaskItem(
            id = "task_5",
            title = "Complete Promotion",
            description = "Follow official social channel link and check the weekly event banner.",
            instructions = "1. Open the partner community link.\n2. View the pinned announcement.\n3. Get rewarded 1000 Credits directly into your balance.",
            reward = 1000.0,
            estimatedTime = "1m",
            isCompleted = false,
            category = "Community",
            completedCount = 18,
            totalLimit = 50
        ),
        TaskItem(
            id = "task_6",
            title = "Visit Sponsored Page",
            description = "Check newly published partner e-commerce store catalog.",
            instructions = "1. Visit the store landing page.\n2. Browse 2 product categories.\n3. Collect your instant reward.",
            reward = 500.0,
            estimatedTime = "40s",
            isCompleted = false,
            category = "E-Commerce",
            completedCount = 35,
            totalLimit = 50
        ),
        TaskItem(
            id = "task_7",
            title = "Review Campaign",
            description = "Verify campaign quality and advertiser landing page performance.",
            instructions = "1. Launch the verification preview.\n2. Confirm page loaded cleanly without errors.\n3. Submit confirmation to credit 600 Credits.",
            reward = 600.0,
            estimatedTime = "30s",
            isCompleted = false,
            category = "Review",
            completedCount = 49,
            totalLimit = 50
        ),
        TaskItem(
            id = "task_8",
            title = "Explore Fintech Tool",
            description = "Try the interactive savings calculator demo page.",
            instructions = "1. Open the calculator tool.\n2. Test a sample calculation.\n3. Claim 700 Credits instant reward.",
            reward = 700.0,
            estimatedTime = "50s",
            isCompleted = false,
            category = "Tool",
            completedCount = 22,
            totalLimit = 50
        )
    )

    fun getInitialWithdrawals(): List<WithdrawalRecord> = listOf(
        WithdrawalRecord(
            id = "wdr_1",
            amount = 50000.0,
            method = "Mobile Banking",
            accountNumber = "+8801712345678",
            dateFormatted = "Yesterday",
            timeFormatted = "04:20 PM",
            transactionId = "TXN-102933",
            status = TransactionStatus.PENDING
        ),
        WithdrawalRecord(
            id = "wdr_2",
            amount = 200000.0,
            method = "Bank Transfer",
            accountNumber = "Account **** 4812",
            dateFormatted = "Aug 18, 2026",
            timeFormatted = "11:15 AM",
            transactionId = "TXN-102850",
            status = TransactionStatus.COMPLETED
        ),
        WithdrawalRecord(
            id = "wdr_3",
            amount = 150000.0,
            method = "Mobile Banking",
            accountNumber = "+8801819876543",
            dateFormatted = "Aug 10, 2026",
            timeFormatted = "02:45 PM",
            transactionId = "TXN-102710",
            status = TransactionStatus.COMPLETED
        ),
        WithdrawalRecord(
            id = "wdr_4",
            amount = 500000.0,
            method = "Bank Transfer",
            accountNumber = "Account **** 4812",
            dateFormatted = "Aug 02, 2026",
            timeFormatted = "09:30 AM",
            transactionId = "TXN-102550",
            status = TransactionStatus.COMPLETED
        )
    )

    fun getFaqList(): List<FAQItem> = listOf(
        FAQItem(
            id = "faq_1",
            category = "Wallet & Payments",
            question = "How quickly are deposits credited to my wallet?",
            answer = "Mobile banking and bank transfer deposits are automatically processed within 1 to 5 minutes after payment verification."
        ),
        FAQItem(
            id = "faq_2",
            category = "Withdrawals",
            question = "What is the minimum withdrawal limit?",
            answer = "The minimum withdrawal amount is 10,000 Credits. Withdrawals are processed to your verified Mobile Banking or Bank Account within 24 hours."
        ),
        FAQItem(
            id = "faq_3",
            category = "Campaigns",
            question = "How do campaign views and tracking work?",
            answer = "Every campaign is delivered to authentic users within our network. Our anti-fraud verification system ensures 100% genuine traffic."
        ),
        FAQItem(
            id = "faq_4",
            category = "Tasks & Limits",
            question = "What is the daily task limit and when does it reset?",
            answer = "Users can complete up to 50 tasks per calendar day. The limit resets every midnight at 12:00 AM (GMT+6)."
        ),
        FAQItem(
            id = "faq_5",
            category = "Referrals",
            question = "How much do I earn per successful referral?",
            answer = "You earn 100 Credits for every friend who joins using your referral code and completes their first task."
        )
    )
}
