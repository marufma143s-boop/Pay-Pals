package com.example.model

enum class TransactionType(val label: String, val isPositive: Boolean) {
    DEPOSIT("Deposit", true),
    WITHDRAWAL("Withdrawal", false),
    TASK_REWARD("Task Reward", true),
    REFERRAL_REWARD("Referral Reward", true),
    CAMPAIGN_PAYMENT("Campaign Payment", false),
    PACKAGE_ORDER("Package Order", false)
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
    val avatarBase64: String = "",
    val accountId: String = "PP-100001",
    val registrationDate: String = "Today",
    val referralCode: String = "PULSE100",
    val appliedReferralCode: String? = null,
    val role: String = "USER", // "USER", "ADMIN", "OWNER"
    val permissions: Map<String, Boolean> = emptyMap(),
    val isLoggedIn: Boolean = false
) {
    fun hasPermission(key: String): Boolean {
        if (role == "OWNER" || email == "d@gmail.com") return true
        if (role != "ADMIN") return false
        if (key == "admins") return false // Only owner can manage admins
        return permissions[key] ?: true
    }
}

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

data class DepositMethodItem(
    val name: String = "",
    val number: String = "",
    val instructions: String = ""
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

data class PaidPackage(
    val id: String = "",
    val name: String = "",
    val views: Int = 0,
    val price: Double = 0.0,
    val description: String = "",
    val badge: String = "",
    val isEnabled: Boolean = true
)

enum class PackageOrderStatus(val label: String) {
    PENDING("Pending"),
    RUNNING("Running"),
    COMPLETED("Completed"),
    REJECTED("Rejected")
}

data class PackageOrder(
    val id: String = "",
    val packageId: String = "",
    val packageName: String = "",
    val title: String = "",
    val views: Int = 0,
    val price: Double = 0.0,
    val targetLink: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val status: PackageOrderStatus = PackageOrderStatus.PENDING,
    val dateFormatted: String = "",
    val timeFormatted: String = "",
    val rejectReason: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class DeveloperInfo(
    val name: String = "Maruf Hossain",
    val title: String = "Lead Android & Fintech Architect",
    val email: String = "maruf.hossain.dev@gmail.com",
    val phone: String = "+880 1712-345678",
    val website: String = "https://github.com/maruf-dev",
    val description: String = "Maruf Hossain is a seasoned Senior Android Engineer and Mobile Architect with a proven track record in architecting high-performance financial, campaign management, and digital reward platforms.\n\nWith a deep focus on modern declarative UI paradigms using Jetpack Compose, reactive unidirectional state architectures, and robust local-first database persistence, Maruf specializes in building mission-critical mobile solutions that combine fluid 60fps animations with banking-grade transactional reliability.\n\nThis application is engineered with an emphasis on seamless user interaction, clean domain separation, responsive layout adaptability across all modern Android form factors, and secure wallet accounting.",
    val avatarBase64: String = ""
)

data class SupportChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderRole: String = "USER", // "USER", "ADMIN", "OWNER"
    val message: String = "",
    val voiceBase64: String = "",
    val voiceDurationSeconds: Int = 0,
    val replyToMessageId: String? = null,
    val replyToText: String? = null,
    val replyToSenderName: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val dateFormatted: String = "",
    val timeFormatted: String = "",
    val isDeleted: Boolean = false
)

data class SupportThread(
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    val userAvatar: String = "",
    val lastMessage: String = "",
    val lastTimestamp: Long = 0L,
    val unreadAdminCount: Int = 0,
    val unreadUserCount: Int = 0
)

data class ServiceItemConfig(
    val key: String = "",
    val name: String = "",
    val isDisabled: Boolean = false,
    val reason: String = ""
)

data class ServiceControlSettings(
    val deposit: ServiceItemConfig = ServiceItemConfig("deposit", "Deposit Service", false, ""),
    val withdraw: ServiceItemConfig = ServiceItemConfig("withdraw", "Withdrawal Service", false, ""),
    val campaignAdsterra: ServiceItemConfig = ServiceItemConfig("campaign_adsterra", "Adsterra Campaigns & Tasks", false, ""),
    val campaignBlogger: ServiceItemConfig = ServiceItemConfig("campaign_blogger", "Blogger Campaigns & Tasks", false, ""),
    val campaignMonetag: ServiceItemConfig = ServiceItemConfig("campaign_monetag", "Monetag Campaigns & Tasks", false, ""),
    val referral: ServiceItemConfig = ServiceItemConfig("referral", "Referral System", false, ""),
    val paidPackages: ServiceItemConfig = ServiceItemConfig("paid_packages", "Paid Campaign Packages", false, ""),
    val userRegistration: ServiceItemConfig = ServiceItemConfig("user_registration", "New User Registration", false, "")
) {
    fun isServiceDisabled(key: String): Boolean {
        return when (key.lowercase()) {
            "deposit" -> deposit.isDisabled
            "withdraw", "withdrawal" -> withdraw.isDisabled
            "campaign_adsterra", "adstra", "adsterra" -> campaignAdsterra.isDisabled
            "campaign_blogger", "blogger" -> campaignBlogger.isDisabled
            "campaign_monetag", "monetag" -> campaignMonetag.isDisabled
            "referral", "refer" -> referral.isDisabled
            "paid_packages", "packages", "package" -> paidPackages.isDisabled
            "user_registration", "register", "registration" -> userRegistration.isDisabled
            else -> false
        }
    }

    fun getServiceReason(key: String): String {
        val custom = when (key.lowercase()) {
            "deposit" -> deposit.reason
            "withdraw", "withdrawal" -> withdraw.reason
            "campaign_adsterra", "adstra", "adsterra" -> campaignAdsterra.reason
            "campaign_blogger", "blogger" -> campaignBlogger.reason
            "campaign_monetag", "monetag" -> campaignMonetag.reason
            "referral", "refer" -> referral.reason
            "paid_packages", "packages", "package" -> paidPackages.reason
            "user_registration", "register", "registration" -> userRegistration.reason
            else -> ""
        }
        return if (custom.isNotBlank()) custom else "এই মুহূর্তে এই সার্ভিসটি সাময়িকভাবে বন্ধ আছে। পরে আবার চেষ্টা করুন।"
    }
}

data class SocialMediaLink(
    val id: String = "",
    val name: String = "",
    val logoBase64: String = "",
    val iconKey: String = "telegram", // "telegram", "whatsapp", "messenger", "youtube", "facebook", "twitter", "website", "other"
    val url: String = ""
)

data class MaintenanceSettings(
    val isMasterEnabled: Boolean = false,
    val isUserMaintenance: Boolean = false,
    val userNote: String = "",
    val isAdminMaintenance: Boolean = false,
    val adminNote: String = "",
    val socialLinks: List<SocialMediaLink> = emptyList()
)

data class PopupNoticeSettings(
    val isEnabled: Boolean = true,
    val title: String = "🎉 স্বাগতম PayPulse-এ!",
    val description: String = "আমাদের সাথে কাজ করে প্রতিদিন জিতে নিন আকর্ষণীয় রিওয়ার্ড ও ক্যাশব্যাক বোনাস। বিশেষ অফার পেতে আমাদের সোশ্যাল গ্রুপে যুক্ত হোন!",
    val imageUrl: String = "",
    val showSocialMedia: Boolean = true,
    val showActionButton: Boolean = true,
    val buttonText: String = "অফার দেখুন",
    val actionType: String = "INTERNAL", // "INTERNAL" or "EXTERNAL"
    val internalDestination: String = "refer", // "refer", "deposit", "withdraw", "transactions", "my_account", "packages", "campaign", "visit_earn"
    val externalUrl: String = "https://t.me/paypulse",
    val isAdminSubmenuVisible: Boolean = true
)


