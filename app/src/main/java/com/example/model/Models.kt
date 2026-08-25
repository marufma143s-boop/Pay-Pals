package com.example.model

enum class TransactionType(val label: String, val isPositive: Boolean) {
    DEPOSIT("Deposit", true),
    WITHDRAWAL("Withdrawal", false),
    TASK_REWARD("Task Reward", true),
    REFERRAL_REWARD("Referral Reward", true),
    CAMPAIGN_PAYMENT("Campaign Payment", false)
}

enum class TransactionStatus(val label: String) {
    COMPLETED("Completed"),
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected")
}

enum class CampaignStatus(val label: String) {
    RUNNING("Running"),
    COMPLETED("Completed"),
    PENDING("Pending"),
    CANCELLED("Cancelled")
}

enum class ReferralStatus(val label: String) {
    ACTIVE("Active"),
    PENDING("Pending")
}

enum class PaymentMethod(val id: String, val title: String, val subtitle: String) {
    BKASH("bkash", "Mobile Banking", "bKash / Nagad / Rocket"),
    BANK("bank", "Bank Transfer", "Direct Bank Account"),
    OTHER("other", "Other Payment Method", "Cards & Digital Wallets")
}

enum class SupportCategory(val title: String) {
    PAYMENT("Payment Problem"),
    WITHDRAWAL("Withdrawal Problem"),
    DEPOSIT("Deposit Problem"),
    CAMPAIGN("Campaign Problem"),
    TASK("Task Problem"),
    ACCOUNT("Account Problem")
}

data class UserProfile(
    val id: String = "user_default",
    val username: String = "@user",
    val fullName: String = "New User",
    val contactType: String = "phone", // "phone" or "email"
    val contactValue: String = "",
    val email: String = "",
    val phone: String = "",
    val accountId: String = "PP-100001",
    val registrationDate: String = "Today",
    val referralCode: String = "PULSE100",
    val appliedReferralCode: String? = null,
    val isLoggedIn: Boolean = false
)

data class WalletState(
    val balance: Double = 12450.0,
    val totalDeposit: Double = 450000.0,
    val totalWithdrawal: Double = 210000.0,
    val totalReferralEarnings: Double = 1200.0,
    val isBalanceVisible: Boolean = true
)

data class Transaction(
    val id: String,
    val title: String,
    val type: TransactionType,
    val amount: Double,
    val dateFormatted: String,
    val timeFormatted: String,
    val transactionId: String,
    val status: TransactionStatus,
    val note: String = ""
)

data class CampaignPackage(
    val id: String,
    val price: Double,
    val targetViews: Int,
    val description: String,
    val isPopular: Boolean = false
)

data class Campaign(
    val id: String,
    val title: String,
    val networkType: String = "adstra", // "adstra", "blogger", "monetag"
    val targetLink: String,
    val packagePrice: Double,
    val targetViews: Int,
    val completedViews: Int,
    val status: CampaignStatus,
    val createdDate: String
) {
    val progressPercentage: Int
        get() = if (targetViews > 0) ((completedViews.toDouble() / targetViews) * 100).toInt().coerceIn(0, 100) else 0

    val remainingViews: Int
        get() = (targetViews - completedViews).coerceAtLeast(0)

    val networkDisplayName: String
        get() = when (networkType.lowercase()) {
            "adstra", "adsterra" -> "Adsterra"
            "blogger" -> "Blogger"
            "monetag" -> "Monetag"
            else -> "Adsterra"
        }
}

data class ReferralUser(
    val id: String,
    val username: String,
    val joinDate: String,
    val status: ReferralStatus,
    val reward: Double
)

data class TaskItem(
    val id: String,
    val title: String,
    val description: String,
    val instructions: String,
    val reward: Double,
    val estimatedTime: String,
    val isCompleted: Boolean = false,
    val category: String = "Promotion",
    val completedCount: Int = 38,
    val totalLimit: Int = 50
) {
    val progressPercentage: Int
        get() = if (totalLimit > 0) ((completedCount.toDouble() / totalLimit) * 100).toInt().coerceIn(0, 100) else 0
}

data class WithdrawalRecord(
    val id: String,
    val amount: Double,
    val method: String,
    val accountNumber: String,
    val dateFormatted: String,
    val timeFormatted: String,
    val transactionId: String,
    val status: TransactionStatus
)

data class SupportTicket(
    val id: String,
    val name: String,
    val email: String,
    val category: SupportCategory,
    val subject: String,
    val message: String,
    val dateFormatted: String,
    val status: String = "Under Review"
)

data class FAQItem(
    val id: String,
    val question: String,
    val answer: String,
    val category: String
)
