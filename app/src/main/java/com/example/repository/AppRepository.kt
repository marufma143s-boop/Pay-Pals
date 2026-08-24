package com.example.repository

import android.content.Context
import com.example.data.DemoDataProvider
import com.example.model.Campaign
import com.example.model.CampaignPackage
import com.example.model.CampaignStatus
import com.example.model.PaymentMethod
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
import com.example.utils.FormatUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random
import java.util.UUID

class AppRepository private constructor(context: Context) {

    companion object {
        @Volatile
        private var instance: AppRepository? = null

        fun getInstance(context: Context): AppRepository {
            return instance ?: synchronized(this) {
                instance ?: AppRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val _adstraCount = MutableStateFlow(0)
    val adstraCount: StateFlow<Int> = _adstraCount.asStateFlow()
    val adstraLimit = 120

    private val _bloggerCount = MutableStateFlow(0)
    val bloggerCount: StateFlow<Int> = _bloggerCount.asStateFlow()
    val bloggerLimit = 80

    private val _monetagCount = MutableStateFlow(0)
    val monetagCount: StateFlow<Int> = _monetagCount.asStateFlow()
    val monetagLimit = 99

    fun incrementAdstraCount() {
        if (_adstraCount.value < adstraLimit) {
            _adstraCount.value += 1
        }
    }

    fun incrementBloggerCount() {
        if (_bloggerCount.value < bloggerLimit) {
            _bloggerCount.value += 1
        }
    }

    fun incrementMonetagCount() {
        if (_monetagCount.value < monetagLimit) {
            _monetagCount.value += 1
        }
    }

    private val _userProfile = MutableStateFlow(DemoDataProvider.getInitialUser())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _walletState = MutableStateFlow(DemoDataProvider.getInitialWallet())
    val walletState: StateFlow<WalletState> = _walletState.asStateFlow()

    private val _transactions = MutableStateFlow(DemoDataProvider.getInitialTransactions())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _campaigns = MutableStateFlow(DemoDataProvider.getInitialCampaigns())
    val campaigns: StateFlow<List<Campaign>> = _campaigns.asStateFlow()

    private val _referrals = MutableStateFlow(DemoDataProvider.getInitialReferrals())
    val referrals: StateFlow<List<ReferralUser>> = _referrals.asStateFlow()

    private val _tasks = MutableStateFlow(DemoDataProvider.getInitialTasks())
    val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()

    private val _withdrawals = MutableStateFlow(DemoDataProvider.getInitialWithdrawals())
    val withdrawals: StateFlow<List<WithdrawalRecord>> = _withdrawals.asStateFlow()

    private val _dailyTaskProgress = MutableStateFlow(12)
    val dailyTaskProgress: StateFlow<Int> = _dailyTaskProgress.asStateFlow()
    val dailyTaskLimit: Int = 50

    private val _totalCampaignCount = MutableStateFlow(24)
    val totalCampaignCount: StateFlow<Int> = _totalCampaignCount.asStateFlow()

    private val _totalReferralCount = MutableStateFlow(156)
    val totalReferralCount: StateFlow<Int> = _totalReferralCount.asStateFlow()

    private val _supportTickets = MutableStateFlow<List<SupportTicket>>(emptyList())
    val supportTickets: StateFlow<List<SupportTicket>> = _supportTickets.asStateFlow()

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", true))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.update { !it }
        prefs.edit().putBoolean("is_dark_mode", _isDarkMode.value).apply()
    }

    fun setDarkMode(dark: Boolean) {
        _isDarkMode.value = dark
        prefs.edit().putBoolean("is_dark_mode", dark).apply()
    }

    fun toggleBalanceVisibility() {
        _walletState.update { current ->
            current.copy(isBalanceVisible = !current.isBalanceVisible)
        }
    }

    private fun generateTxnId(): String {
        val randomNum = 100000 + Random().nextInt(900000)
        return "TXN-$randomNum"
    }

    private fun getCurrentTimeFormatted(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.US)
        return sdf.format(Date())
    }

    private fun getCurrentDateFormatted(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        return sdf.format(Date())
    }

    fun deposit(amount: Double, method: PaymentMethod): Result<Unit> {
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Amount must be greater than zero."))
        }

        val txnId = generateTxnId()
        val newTxn = Transaction(
            id = UUID.randomUUID().toString(),
            title = "Deposit",
            type = TransactionType.DEPOSIT,
            amount = amount,
            dateFormatted = "Today",
            timeFormatted = getCurrentTimeFormatted(),
            transactionId = txnId,
            status = TransactionStatus.COMPLETED,
            note = "${method.title} (${method.subtitle})"
        )

        _walletState.update { current ->
            current.copy(
                balance = current.balance + amount,
                totalDeposit = current.totalDeposit + amount
            )
        }

        _transactions.update { current ->
            listOf(newTxn) + current
        }

        return Result.success(Unit)
    }

    fun withdraw(amount: Double, method: String, accountNumber: String): Result<WithdrawalRecord> {
        val currentBalance = _walletState.value.balance
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Please enter a valid amount."))
        }
        if (amount > currentBalance) {
            return Result.failure(IllegalStateException("Insufficient balance. Available: ${FormatUtils.formatCredits(currentBalance)}"))
        }
        if (accountNumber.isBlank()) {
            return Result.failure(IllegalArgumentException("Please provide your account information."))
        }

        val txnId = generateTxnId()
        val newWithdrawal = WithdrawalRecord(
            id = UUID.randomUUID().toString(),
            amount = amount,
            method = method,
            accountNumber = accountNumber.trim(),
            dateFormatted = "Today",
            timeFormatted = getCurrentTimeFormatted(),
            transactionId = txnId,
            status = TransactionStatus.PENDING
        )

        val newTxn = Transaction(
            id = UUID.randomUUID().toString(),
            title = "Withdrawal",
            type = TransactionType.WITHDRAWAL,
            amount = amount,
            dateFormatted = "Today",
            timeFormatted = getCurrentTimeFormatted(),
            transactionId = txnId,
            status = TransactionStatus.PENDING,
            note = "$method ($accountNumber)"
        )

        _walletState.update { current ->
            current.copy(
                balance = current.balance - amount,
                totalWithdrawal = current.totalWithdrawal + amount
            )
        }

        _withdrawals.update { current ->
            listOf(newWithdrawal) + current
        }

        _transactions.update { current ->
            listOf(newTxn) + current
        }

        return Result.success(newWithdrawal)
    }

    fun applyReferralCode(code: String): Result<Double> {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter a referral code."))
        }

        val currentUser = _userProfile.value
        if (currentUser.appliedReferralCode != null) {
            return Result.failure(IllegalStateException("Referral code already applied."))
        }

        if (cleanCode == currentUser.referralCode) {
            return Result.failure(IllegalArgumentException("You cannot apply your own referral code."))
        }

        if (cleanCode.length < 4) {
            return Result.failure(IllegalArgumentException("Invalid referral code."))
        }

        val bonusReward = 50.0

        _userProfile.update { current ->
            current.copy(appliedReferralCode = cleanCode)
        }

        val txnId = generateTxnId()
        val newTxn = Transaction(
            id = UUID.randomUUID().toString(),
            title = "Referral Reward",
            type = TransactionType.REFERRAL_REWARD,
            amount = bonusReward,
            dateFormatted = "Today",
            timeFormatted = getCurrentTimeFormatted(),
            transactionId = txnId,
            status = TransactionStatus.COMPLETED,
            note = "Applied code $cleanCode bonus reward"
        )

        _walletState.update { current ->
            current.copy(
                balance = current.balance + bonusReward,
                totalReferralEarnings = current.totalReferralEarnings + bonusReward
            )
        }

        _transactions.update { current ->
            listOf(newTxn) + current
        }

        return Result.success(bonusReward)
    }

    fun createCampaign(title: String, targetLink: String, pkg: CampaignPackage): Result<Campaign> {
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Campaign title is required."))
        }
        if (targetLink.isBlank() || !FormatUtils.isValidUrl(targetLink)) {
            return Result.failure(IllegalArgumentException("Please enter a valid target URL."))
        }

        val currentBalance = _walletState.value.balance
        if (currentBalance < pkg.price) {
            return Result.failure(IllegalStateException("Insufficient balance. Package cost: ${FormatUtils.formatCredits(pkg.price)}"))
        }

        val newCampaign = Campaign(
            id = "cmp_" + (100 + _campaigns.value.size + 1),
            title = title.trim(),
            targetLink = if (targetLink.startsWith("http://") || targetLink.startsWith("https://")) targetLink.trim() else "https://${targetLink.trim()}",
            packagePrice = pkg.price,
            targetViews = pkg.targetViews,
            completedViews = 0,
            status = CampaignStatus.RUNNING,
            createdDate = "Today"
        )

        val txnId = generateTxnId()
        val newTxn = Transaction(
            id = UUID.randomUUID().toString(),
            title = "Campaign Payment",
            type = TransactionType.CAMPAIGN_PAYMENT,
            amount = pkg.price,
            dateFormatted = "Today",
            timeFormatted = getCurrentTimeFormatted(),
            transactionId = txnId,
            status = TransactionStatus.COMPLETED,
            note = "${title.trim()} (${FormatUtils.formatCount(pkg.targetViews)} Views)"
        )

        _walletState.update { current ->
            current.copy(balance = current.balance - pkg.price)
        }

        _campaigns.update { current ->
            listOf(newCampaign) + current
        }

        _totalCampaignCount.update { it + 1 }

        _transactions.update { current ->
            listOf(newTxn) + current
        }

        return Result.success(newCampaign)
    }

    fun completeTask(taskId: String): Result<Double> {
        if (_dailyTaskProgress.value >= dailyTaskLimit) {
            return Result.failure(IllegalStateException("Daily task limit reached (50/50)."))
        }

        val taskList = _tasks.value
        val taskIndex = taskList.indexOfFirst { it.id == taskId }
        if (taskIndex == -1) {
            return Result.failure(IllegalArgumentException("Task not found."))
        }

        val task = taskList[taskIndex]
        if (task.isCompleted) {
            return Result.failure(IllegalStateException("Task already completed."))
        }

        val updatedTask = task.copy(
            isCompleted = true,
            completedCount = (task.completedCount + 1).coerceAtMost(task.totalLimit)
        )
        val updatedList = taskList.toMutableList()
        updatedList[taskIndex] = updatedTask

        _tasks.value = updatedList
        _dailyTaskProgress.update { (it + 1).coerceAtMost(dailyTaskLimit) }

        val txnId = generateTxnId()
        val newTxn = Transaction(
            id = UUID.randomUUID().toString(),
            title = "Task Reward",
            type = TransactionType.TASK_REWARD,
            amount = task.reward,
            dateFormatted = "Today",
            timeFormatted = getCurrentTimeFormatted(),
            transactionId = txnId,
            status = TransactionStatus.COMPLETED,
            note = "${task.title} completed"
        )

        _walletState.update { current ->
            current.copy(balance = current.balance + task.reward)
        }

        _transactions.update { current ->
            listOf(newTxn) + current
        }

        return Result.success(task.reward)
    }

    fun updateProfile(fullName: String, email: String, phone: String): Result<Unit> {
        if (fullName.isBlank()) {
            return Result.failure(IllegalArgumentException("Full name cannot be empty."))
        }
        if (!FormatUtils.isValidEmail(email)) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (!FormatUtils.isValidPhone(phone)) {
            return Result.failure(IllegalArgumentException("Please enter a valid phone number."))
        }

        _userProfile.update { current ->
            current.copy(
                fullName = fullName.trim(),
                email = email.trim(),
                phone = phone.trim()
            )
        }
        return Result.success(Unit)
    }

    fun submitSupportTicket(name: String, email: String, category: SupportCategory, subject: String, message: String): Result<SupportTicket> {
        if (name.isBlank() || email.isBlank() || subject.isBlank() || message.isBlank()) {
            return Result.failure(IllegalArgumentException("Please fill out all required fields."))
        }
        if (!FormatUtils.isValidEmail(email)) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }

        val ticket = SupportTicket(
            id = "TKT-" + (1000 + _supportTickets.value.size + 1),
            name = name.trim(),
            email = email.trim(),
            category = category,
            subject = subject.trim(),
            message = message.trim(),
            dateFormatted = getCurrentDateFormatted(),
            status = "Under Review"
        )

        _supportTickets.update { current ->
            listOf(ticket) + current
        }
        return Result.success(ticket)
    }
}
