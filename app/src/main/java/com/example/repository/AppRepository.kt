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
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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

    fun getActiveCampaignForNetwork(networkType: String): Campaign? {
        val targetNetwork = networkType.lowercase().trim()
        return _campaigns.value.firstOrNull { cmp ->
            (cmp.networkType.lowercase() == targetNetwork || (targetNetwork == "adstra" && cmp.networkType.lowercase() == "adsterra")) &&
                    cmp.status == CampaignStatus.RUNNING &&
                    cmp.remainingViews > 0
        } ?: _campaigns.value.firstOrNull { cmp ->
            cmp.status == CampaignStatus.RUNNING && cmp.remainingViews > 0
        }
    }

    fun getTargetUrlForNetwork(networkType: String): String {
        val activeCampaign = getActiveCampaignForNetwork(networkType)
        if (activeCampaign != null && activeCampaign.targetLink.isNotBlank()) {
            return activeCampaign.targetLink
        }
        return when (networkType.lowercase()) {
            "adstra", "adsterra" -> "https://example.com/adsterra-sponsored-traffic"
            "blogger" -> "https://techpulse.blog/ai-trends-monetization"
            "monetag" -> "https://example.com/monetag-direct-smartlink"
            else -> "https://example.com"
        }
    }

    fun completeVisitEarn(networkType: String, rewardAmount: Double = 25.0, campaignId: String? = null): Result<Double> {
        val canIncrement = when (networkType.lowercase()) {
            "adstra", "adsterra" -> {
                if (_adstraCount.value < adstraLimit) {
                    _adstraCount.value += 1
                    true
                } else false
            }
            "blogger" -> {
                if (_bloggerCount.value < bloggerLimit) {
                    _bloggerCount.value += 1
                    true
                } else false
            }
            "monetag" -> {
                if (_monetagCount.value < monetagLimit) {
                    _monetagCount.value += 1
                    true
                } else false
            }
            else -> false
        }

        if (!canIncrement) {
            return Result.failure(IllegalStateException("Daily limit reached for this network."))
        }

        // If an active campaign was visited, increment its completedViews
        val targetCampaign = if (campaignId != null) {
            _campaigns.value.firstOrNull { it.id == campaignId }
        } else {
            getActiveCampaignForNetwork(networkType)
        }

        if (targetCampaign != null) {
            _campaigns.update { list ->
                list.map { cmp ->
                    if (cmp.id == targetCampaign.id) {
                        val newCompleted = cmp.completedViews + 1
                        val newStatus = if (newCompleted >= cmp.targetViews) CampaignStatus.COMPLETED else cmp.status
                        cmp.copy(completedViews = newCompleted, status = newStatus)
                    } else cmp
                }
            }
        }

        val txnId = generateTxnId()
        val newTxn = Transaction(
            id = UUID.randomUUID().toString(),
            title = "Visit Earn Reward",
            type = TransactionType.TASK_REWARD,
            amount = rewardAmount,
            dateFormatted = "Today",
            timeFormatted = getCurrentTimeFormatted(),
            transactionId = txnId,
            status = TransactionStatus.COMPLETED,
            note = "$networkType visit completed"
        )

        _walletState.update { current ->
            current.copy(
                balance = current.balance + rewardAmount
            )
        }

        _transactions.update { current ->
            listOf(newTxn) + current
        }

        return Result.success(rewardAmount)
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

    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Admin Settings State
    private val _campaignRates = MutableStateFlow(mapOf(
        "adsterra" to 1500,
        "blogger" to 1000,
        "monetag" to 2000
    ))
    val campaignRates: StateFlow<Map<String, Int>> = _campaignRates.asStateFlow()
    
    private val _depositMethods = MutableStateFlow(listOf("bKash", "Nagad", "Rocket", "Binance Pay"))
    val depositMethods: StateFlow<List<String>> = _depositMethods.asStateFlow()
    
    private val _withdrawalMethods = MutableStateFlow(listOf("bKash", "Nagad", "Binance USDT"))
    val withdrawalMethods: StateFlow<List<String>> = _withdrawalMethods.asStateFlow()

    private val _adminDepositRequests = MutableStateFlow<List<Map<String, Any?>>>(emptyList())
    val adminDepositRequests: StateFlow<List<Map<String, Any?>>> = _adminDepositRequests.asStateFlow()
    
    private val _adminWithdrawalRequests = MutableStateFlow<List<Map<String, Any?>>>(emptyList())
    val adminWithdrawalRequests: StateFlow<List<Map<String, Any?>>> = _adminWithdrawalRequests.asStateFlow()

    private val _allUsers = MutableStateFlow<List<Map<String, Any?>>>(emptyList())
    val allUsers: StateFlow<List<Map<String, Any?>>> = _allUsers.asStateFlow()

    private val _allCampaigns = MutableStateFlow<List<Map<String, Any?>>>(emptyList())
    val allCampaigns: StateFlow<List<Map<String, Any?>>> = _allCampaigns.asStateFlow()

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _preferredBrowser = MutableStateFlow(prefs.getString("preferred_browser", "chrome") ?: "chrome")
    val preferredBrowser: StateFlow<String> = _preferredBrowser.asStateFlow()

    init {
        loadSavedSession()
        listenToAdminSettings()
    }

    private fun listenToAdminSettings() {
        com.example.data.FirebaseRealtimeDbManager.attachAdminSettingsListener { adminData ->
            val browser = adminData["preferred_browser"] as? String
            if (browser != null) {
                val valid = if (browser.equals("firefox", ignoreCase = true)) "firefox" else "chrome"
                _preferredBrowser.value = valid
                prefs.edit().putString("preferred_browser", valid).apply()
            }
            
            val rates = adminData["campaign_rates"] as? Map<String, Any>
            if (rates != null) {
                _campaignRates.value = rates.mapValues { (it.value as? Number)?.toInt() ?: 1000 }
            }
            
            val depMethods = adminData["deposit_methods"] as? List<String>
            if (depMethods != null) {
                _depositMethods.value = depMethods
            }
            
            val withMethods = adminData["withdrawal_methods"] as? List<String>
            if (withMethods != null) {
                _withdrawalMethods.value = withMethods
            }
        }
        
        com.example.data.FirebaseRealtimeDbManager.attachAdminDepositsListener { requests ->
            _adminDepositRequests.value = requests.values.filterIsInstance<Map<String, Any?>>().toList()
        }
        
        com.example.data.FirebaseRealtimeDbManager.attachAdminWithdrawalsListener { requests ->
            _adminWithdrawalRequests.value = requests.values.filterIsInstance<Map<String, Any?>>().toList()
        }
        
        com.example.data.FirebaseRealtimeDbManager.attachAdminUsersListener { usersMap ->
            _allUsers.value = usersMap.values.filterIsInstance<Map<String, Any?>>().toList()
        }
        
        com.example.data.FirebaseRealtimeDbManager.attachAdminCampaignsListener { campaignsMap ->
            _allCampaigns.value = campaignsMap.values.filterIsInstance<Map<String, Any?>>().toList()
        }
    }

    private fun loadSavedSession() {
        val savedUserId = prefs.getString("user_id", null)
        val savedFullName = prefs.getString("user_full_name", null)
        val savedContact = prefs.getString("user_contact", null)
        val savedContactType = prefs.getString("user_contact_type", "phone") ?: "phone"
        val savedAccountId = prefs.getString("user_account_id", null)
        val savedReferral = prefs.getString("user_referral", null)
        val savedAppliedReferral = prefs.getString("user_applied_referral", null)
        val savedBalance = prefs.getFloat("user_balance", -1f)

        if (!savedUserId.isNullOrBlank() && !savedFullName.isNullOrBlank()) {
            val phone = if (savedContactType == "phone") savedContact ?: "" else ""
            val email = if (savedContactType == "email") savedContact ?: "" else ""

            _userProfile.value = UserProfile(
                id = savedUserId,
                username = "@${savedFullName.lowercase().replace(" ", "")}",
                fullName = savedFullName,
                contactType = savedContactType,
                contactValue = savedContact ?: "",
                email = email,
                phone = phone,
                accountId = savedAccountId ?: "PP-${savedUserId.takeLast(6)}",
                registrationDate = "Registered",
                referralCode = savedReferral ?: "PAY${savedUserId.takeLast(4)}",
                appliedReferralCode = savedAppliedReferral,
                isLoggedIn = true
            )
            _isLoggedIn.value = true

            if (savedBalance >= 0f) {
                _walletState.update { it.copy(balance = savedBalance.toDouble()) }
            }

            // Attach Realtime Firebase listener for this user
            com.example.data.FirebaseRealtimeDbManager.attachUserRealtimeListener(savedUserId) { data ->
                val bal = (data["balance"] as? Number)?.toDouble()
                val totalDep = (data["totalDeposit"] as? Number)?.toDouble()
                val totalWith = (data["totalWithdrawal"] as? Number)?.toDouble()
                val totalRef = (data["totalReferralEarnings"] as? Number)?.toDouble()
                val appliedRef = data["appliedReferralCode"] as? String

                if (appliedRef != null && _userProfile.value.appliedReferralCode == null) {
                    _userProfile.update { it.copy(appliedReferralCode = appliedRef) }
                    prefs.edit().putString("user_applied_referral", appliedRef).apply()
                }

                if (bal != null) {
                    _walletState.update { current ->
                        current.copy(
                            balance = bal,
                            totalDeposit = totalDep ?: current.totalDeposit,
                            totalWithdrawal = totalWith ?: current.totalWithdrawal,
                            totalReferralEarnings = totalRef ?: current.totalReferralEarnings
                        )
                    }
                }
            }
        }
    }

    fun register(
        fullName: String,
        contactType: String, // "phone" or "email"
        contactValue: String,
        password: String,
        referralCodeInput: String?,
        onResult: (Boolean, String?) -> Unit
    ) {
        val cleanName = fullName.trim()
        val cleanContact = contactValue.trim()
        val cleanPassword = password.trim()

        if (cleanName.length < 2) {
            onResult(false, "Please enter your full name.")
            return
        }
        if (contactType == "email" && !FormatUtils.isValidEmail(cleanContact)) {
            onResult(false, "Please enter a valid email address.")
            return
        }
        if (contactType == "phone" && (cleanContact.length < 10 || !cleanContact.all { it.isDigit() || it == '+' })) {
            onResult(false, "Please enter a valid phone number.")
            return
        }
        if (cleanPassword.length < 4) {
            onResult(false, "Password must be at least 4 characters.")
            return
        }

        val userId = "usr_" + System.currentTimeMillis() + "_" + (100..999).random()
        val accId = "PP-" + (100000..999999).random()
        val refCode = "PAY" + (1000..9999).random()
        val initialBonus = 50.0 // Welcome bonus

        val userData = hashMapOf<String, Any?>(
            "id" to userId,
            "fullName" to cleanName,
            "username" to "@" + cleanName.lowercase().replace(" ", ""),
            "contactType" to contactType,
            "contactValue" to cleanContact,
            "email" to (if (contactType == "email") cleanContact else ""),
            "phone" to (if (contactType == "phone") cleanContact else ""),
            "accountId" to accId,
            "password" to cleanPassword,
            "referralCode" to refCode,
            "appliedReferralCode" to (referralCodeInput?.takeIf { it.isNotBlank() }),
            "balance" to initialBonus,
            "totalDeposit" to 0.0,
            "totalWithdrawal" to 0.0,
            "totalReferralEarnings" to 0.0,
            "registrationDate" to getCurrentDateFormatted(),
            "createdAt" to System.currentTimeMillis()
        )

        com.example.data.FirebaseRealtimeDbManager.registerUserInDb(
            userId = userId,
            userData = userData,
            contactKey = cleanContact
        ) { success, errorMsg ->
            if (success) {
                // Save locally
                prefs.edit()
                    .putBoolean("is_logged_in", true)
                    .putString("user_id", userId)
                    .putString("user_full_name", cleanName)
                    .putString("user_contact", cleanContact)
                    .putString("user_contact_type", contactType)
                    .putString("user_account_id", accId)
                    .putString("user_referral", refCode)
                    .putFloat("user_balance", initialBonus.toFloat())
                    .apply()

                _userProfile.value = UserProfile(
                    id = userId,
                    username = "@${cleanName.lowercase().replace(" ", "")}",
                    fullName = cleanName,
                    contactType = contactType,
                    contactValue = cleanContact,
                    email = if (contactType == "email") cleanContact else "",
                    phone = if (contactType == "phone") cleanContact else "",
                    accountId = accId,
                    registrationDate = getCurrentDateFormatted(),
                    referralCode = refCode,
                    appliedReferralCode = referralCodeInput?.takeIf { it.isNotBlank() },
                    isLoggedIn = true
                )

                _walletState.value = WalletState(
                    balance = initialBonus,
                    totalDeposit = 0.0,
                    totalWithdrawal = 0.0,
                    totalReferralEarnings = 0.0,
                    isBalanceVisible = true
                )

                _isLoggedIn.value = true

                // Welcome transaction
                val welcomeTxn = Transaction(
                    id = UUID.randomUUID().toString(),
                    title = "Welcome Bonus",
                    type = TransactionType.TASK_REWARD,
                    amount = initialBonus,
                    dateFormatted = "Today",
                    timeFormatted = getCurrentTimeFormatted(),
                    transactionId = generateTxnId(),
                    status = TransactionStatus.COMPLETED,
                    note = "New user registration bonus"
                )
                _transactions.value = listOf(welcomeTxn)

                // Sync transaction to Firebase
                val txnMap = mapOf(
                    "id" to welcomeTxn.id,
                    "title" to welcomeTxn.title,
                    "amount" to welcomeTxn.amount,
                    "transactionId" to welcomeTxn.transactionId,
                    "status" to welcomeTxn.status.name,
                    "dateFormatted" to welcomeTxn.dateFormatted,
                    "timeFormatted" to welcomeTxn.timeFormatted,
                    "note" to welcomeTxn.note
                )
                com.example.data.FirebaseRealtimeDbManager.pushUserTransaction(userId, txnMap)

                onResult(true, null)
            } else {
                onResult(false, errorMsg ?: "Registration failed")
            }
        }
    }

    fun login(
        contact: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val cleanContact = contact.trim()
        val cleanPassword = password.trim()

        if (cleanContact.isBlank()) {
            onResult(false, "Please enter your email or phone number.")
            return
        }
        if (cleanPassword.isBlank()) {
            onResult(false, "Please enter your password.")
            return
        }

        com.example.data.FirebaseRealtimeDbManager.loginUserInDb(
            contact = cleanContact,
            passwordAttempt = cleanPassword
        ) { success, userData, errorMsg ->
            if (success && userData != null) {
                val userId = userData["id"] as? String ?: ("usr_" + System.currentTimeMillis())
                val fullName = userData["fullName"] as? String ?: "User"
                val contactType = userData["contactType"] as? String ?: (if (cleanContact.contains("@")) "email" else "phone")
                val contactVal = userData["contactValue"] as? String ?: cleanContact
                val email = userData["email"] as? String ?: (if (contactType == "email") cleanContact else "")
                val phone = userData["phone"] as? String ?: (if (contactType == "phone") cleanContact else "")
                val accountId = userData["accountId"] as? String ?: "PP-${userId.takeLast(6)}"
                val refCode = userData["referralCode"] as? String ?: "PAY100"
                val appliedRef = userData["appliedReferralCode"] as? String
                val balance = (userData["balance"] as? Number)?.toDouble() ?: 50.0
                val totalDep = (userData["totalDeposit"] as? Number)?.toDouble() ?: 0.0
                val totalWith = (userData["totalWithdrawal"] as? Number)?.toDouble() ?: 0.0
                val totalRef = (userData["totalReferralEarnings"] as? Number)?.toDouble() ?: 0.0

                prefs.edit()
                    .putBoolean("is_logged_in", true)
                    .putString("user_id", userId)
                    .putString("user_full_name", fullName)
                    .putString("user_contact", contactVal)
                    .putString("user_contact_type", contactType)
                    .putString("user_account_id", accountId)
                    .putString("user_referral", refCode)
                    .putString("user_applied_referral", appliedRef)
                    .putFloat("user_balance", balance.toFloat())
                    .apply()

                _userProfile.value = UserProfile(
                    id = userId,
                    username = "@${fullName.lowercase().replace(" ", "")}",
                    fullName = fullName,
                    contactType = contactType,
                    contactValue = contactVal,
                    email = email,
                    phone = phone,
                    accountId = accountId,
                    registrationDate = "Active",
                    referralCode = refCode,
                    appliedReferralCode = appliedRef,
                    isLoggedIn = true
                )

                _walletState.value = WalletState(
                    balance = balance,
                    totalDeposit = totalDep,
                    totalWithdrawal = totalWith,
                    totalReferralEarnings = totalRef,
                    isBalanceVisible = true
                )

                _isLoggedIn.value = true

                // Attach Firebase realtime sync
                com.example.data.FirebaseRealtimeDbManager.attachUserRealtimeListener(userId) { liveData ->
                    val liveBal = (liveData["balance"] as? Number)?.toDouble()
                    if (liveBal != null) {
                        _walletState.update { it.copy(balance = liveBal) }
                    }
                }

                onResult(true, null)
            } else {
                onResult(false, errorMsg ?: "Invalid credentials")
            }
        }
    }

    fun logout() {
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .remove("user_id")
            .remove("user_full_name")
            .remove("user_contact")
            .apply()

        _isLoggedIn.value = false
        _userProfile.value = DemoDataProvider.getInitialUser().copy(isLoggedIn = false)
    }

    fun toggleDarkMode() {
        _isDarkMode.update { !it }
        prefs.edit().putBoolean("is_dark_mode", _isDarkMode.value).apply()
    }

    fun setDarkMode(dark: Boolean) {
        _isDarkMode.value = dark
        prefs.edit().putBoolean("is_dark_mode", dark).apply()
    }

    fun setPreferredBrowser(browser: String) {
        val valid = if (browser.equals("firefox", ignoreCase = true)) "firefox" else "chrome"
        _preferredBrowser.value = valid
        prefs.edit().putString("preferred_browser", valid).apply()
    }

    fun updateAdminPreferredBrowser(browser: String) {
        val valid = if (browser.equals("firefox", ignoreCase = true)) "firefox" else "chrome"
        com.example.data.FirebaseRealtimeDbManager.updateAdminSetting("preferred_browser", valid)
    }

    fun updateAdminCampaignRates(networkId: String, pointsPer100Views: Int) {
        val current = _campaignRates.value.toMutableMap()
        current[networkId] = pointsPer100Views
        com.example.data.FirebaseRealtimeDbManager.updateAdminSetting("campaign_rates", current)
    }
    
    fun addAdminDepositMethod(method: String) {
        val current = _depositMethods.value.toMutableList()
        if (!current.contains(method)) {
            current.add(method)
            com.example.data.FirebaseRealtimeDbManager.updateAdminSetting("deposit_methods", current)
        }
    }
    
    fun removeAdminDepositMethod(method: String) {
        val current = _depositMethods.value.toMutableList()
        if (current.remove(method)) {
            com.example.data.FirebaseRealtimeDbManager.updateAdminSetting("deposit_methods", current)
        }
    }
    
    fun addAdminWithdrawalMethod(method: String) {
        val current = _withdrawalMethods.value.toMutableList()
        if (!current.contains(method)) {
            current.add(method)
            com.example.data.FirebaseRealtimeDbManager.updateAdminSetting("withdrawal_methods", current)
        }
    }
    
    fun removeAdminWithdrawalMethod(method: String) {
        val current = _withdrawalMethods.value.toMutableList()
        if (current.remove(method)) {
            com.example.data.FirebaseRealtimeDbManager.updateAdminSetting("withdrawal_methods", current)
        }
    }


    fun approveAdminDeposit(userId: String, depositId: String, amount: Double) {
        com.example.data.FirebaseRealtimeDbManager.approveDeposit(userId, depositId, amount)
    }

    fun rejectAdminDeposit(userId: String, depositId: String) {
        com.example.data.FirebaseRealtimeDbManager.rejectDeposit(userId, depositId)
    }
    
    fun approveAdminWithdrawal(userId: String, withdrawalId: String) {
        com.example.data.FirebaseRealtimeDbManager.approveWithdrawal(userId, withdrawalId)
    }

    fun rejectAdminWithdrawal(userId: String, withdrawalId: String) {
        com.example.data.FirebaseRealtimeDbManager.rejectWithdrawal(userId, withdrawalId)
    }

    fun updateUserBlockStatus(userId: String, isBlocked: Boolean) {
        com.example.data.FirebaseRealtimeDbManager.updateUserBlockStatus(userId, isBlocked)
    }

    fun updateUserAdmin(userId: String, name: String, email: String, balance: Double) {
        com.example.data.FirebaseRealtimeDbManager.updateUser(userId, name, email, balance)
    }

    fun deleteUserAdmin(userId: String) {
        com.example.data.FirebaseRealtimeDbManager.deleteUser(userId)
    }
    
        fun updateCampaignViews(campaignId: String, completedViews: Int, status: String) {
        com.example.data.FirebaseRealtimeDbManager.updateCampaignViews(campaignId, completedViews, status)
    }

    fun updateCampaignStatus(campaignId: String, status: String, rejectReason: String? = null) {
        com.example.data.FirebaseRealtimeDbManager.updateCampaignStatus(campaignId, status, rejectReason)
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

    fun deposit(amount: Double, method: String): Result<Unit> {
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Amount must be greater than zero."))
        }

        val txnId = generateTxnId()
        val id = UUID.randomUUID().toString()
        val newTxn = Transaction(
            id = id,
            title = "Deposit",
            type = TransactionType.DEPOSIT,
            amount = amount,
            dateFormatted = "Today",
            timeFormatted = getCurrentTimeFormatted(),
            transactionId = txnId,
            status = TransactionStatus.PENDING,
            note = "${method}"
        )

        _transactions.update { current ->
            listOf(newTxn) + current
        }
        
        val userId = _userProfile.value.id
        
        val requestMap = mapOf(
            "id" to id,
            "userId" to userId,
            "userName" to _userProfile.value.fullName,
            "amount" to amount,
            "method" to method,
            "dateFormatted" to "Today",
            "timeFormatted" to getCurrentTimeFormatted(),
            "transactionId" to txnId,
            "status" to "PENDING"
        )
        
        com.example.data.FirebaseRealtimeDbManager.pushDepositToDb(userId, requestMap)

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
        val id = UUID.randomUUID().toString()
        val newWithdrawal = WithdrawalRecord(
            id = id,
            amount = amount,
            method = method,
            accountNumber = accountNumber.trim(),
            dateFormatted = "Today",
            timeFormatted = getCurrentTimeFormatted(),
            transactionId = txnId,
            status = TransactionStatus.PENDING
        )

        val newTxn = Transaction(
            id = id,
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
                balance = current.balance - amount
            )
        }
        
        val userId = _userProfile.value.id

        _withdrawals.update { current ->
            listOf(newWithdrawal) + current
        }

        _transactions.update { current ->
            listOf(newTxn) + current
        }
        
        val requestMap = mapOf(
            "id" to id,
            "userId" to userId,
            "userName" to _userProfile.value.fullName,
            "amount" to amount,
            "method" to method,
            "accountNumber" to accountNumber.trim(),
            "dateFormatted" to "Today",
            "timeFormatted" to getCurrentTimeFormatted(),
            "transactionId" to txnId,
            "status" to "PENDING"
        )
        com.example.data.FirebaseRealtimeDbManager.pushWithdrawalToDb(userId, requestMap)

        return Result.success(newWithdrawal)
    }

    suspend fun applyReferralCode(code: String): Result<Double> {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter a referral code."))
        }

        val currentUser = _userProfile.value
        if (currentUser.appliedReferralCode != null) {
            return Result.failure(IllegalStateException("Offer already claimed! You have already applied a referral code."))
        }

        if (cleanCode == currentUser.referralCode.trim().uppercase()) {
            return Result.failure(IllegalArgumentException("You cannot apply your own referral code."))
        }

        if (cleanCode.length < 4) {
            return Result.failure(IllegalArgumentException("Invalid referral code. Please enter a valid code."))
        }

        val bonusReward = 50.0
        val refereeUserId = currentUser.id
        val refereeName = currentUser.fullName.ifBlank { "New Member" }

        return suspendCoroutine { cont ->
            if (refereeUserId.isNotBlank()) {
                com.example.data.FirebaseRealtimeDbManager.applyReferralBonusInCloud(
                    refereeUserId = refereeUserId,
                    refereeName = refereeName,
                    cleanCode = cleanCode,
                    bonusReward = bonusReward
                ) { success, referrerName, errorMsg ->
                    if (success) {
                        _userProfile.update { current ->
                            current.copy(appliedReferralCode = cleanCode)
                        }
                        prefs.edit().putString("user_applied_referral", cleanCode).apply()

                        val txnId = generateTxnId()
                        val newTxn = Transaction(
                            id = UUID.randomUUID().toString(),
                            title = "Referral Bonus",
                            type = TransactionType.REFERRAL_REWARD,
                            amount = bonusReward,
                            dateFormatted = "Today",
                            timeFormatted = getCurrentTimeFormatted(),
                            transactionId = txnId,
                            status = TransactionStatus.COMPLETED,
                            note = "Applied code $cleanCode bonus reward"
                        )

                        _walletState.update { current ->
                            val newBal = current.balance + bonusReward
                            val newRef = current.totalReferralEarnings + bonusReward
                            com.example.data.FirebaseRealtimeDbManager.syncUserWallet(
                                refereeUserId,
                                newBal,
                                current.totalDeposit,
                                current.totalWithdrawal,
                                newRef
                            )
                            current.copy(
                                balance = newBal,
                                totalReferralEarnings = newRef
                            )
                        }

                        _transactions.update { current ->
                            listOf(newTxn) + current
                        }

                        cont.resume(Result.success(bonusReward))
                    } else {
                        cont.resume(Result.failure(Exception(errorMsg ?: "Invalid referral code or offer already claimed.")))
                    }
                }
            } else {
                // Local state fallback
                _userProfile.update { current ->
                    current.copy(appliedReferralCode = cleanCode)
                }
                prefs.edit().putString("user_applied_referral", cleanCode).apply()

                val txnId = generateTxnId()
                val newTxn = Transaction(
                    id = UUID.randomUUID().toString(),
                    title = "Referral Bonus",
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

                cont.resume(Result.success(bonusReward))
            }
        }
    }

    fun createCampaign(
        title: String,
        networkType: String = "adstra",
        targetLink: String,
        pkg: CampaignPackage
    ): Result<Campaign> {
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

        val cleanNetwork = when (networkType.lowercase().trim()) {
            "adstra", "adsterra" -> "adstra"
            "blogger" -> "blogger"
            "monetag" -> "monetag"
            else -> "adstra"
        }

        val newCampaign = Campaign(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            networkType = cleanNetwork,
            targetLink = if (targetLink.startsWith("http://") || targetLink.startsWith("https://")) targetLink.trim() else "https://${targetLink.trim()}",
            packagePrice = pkg.price,
            targetViews = pkg.targetViews,
            completedViews = 0,
            status = CampaignStatus.PENDING,
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
            note = "${title.trim()} [${newCampaign.networkDisplayName}] (${FormatUtils.formatCount(pkg.targetViews)} Views)"
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
        
        // Push to Firebase
        val campaignMap = mapOf(
            "id" to newCampaign.id,
            "title" to newCampaign.title,
            "networkType" to newCampaign.networkType,
            "targetLink" to newCampaign.targetLink,
            "packagePrice" to newCampaign.packagePrice,
            "targetViews" to newCampaign.targetViews,
            "completedViews" to newCampaign.completedViews,
            "status" to newCampaign.status.name,
            "createdDate" to newCampaign.createdDate,
            "userId" to _userProfile.value.id
        )
        com.example.data.FirebaseRealtimeDbManager.pushCampaignToDb(campaignMap)
        
        val txnMap = mapOf(
            "id" to newTxn.id,
            "title" to newTxn.title,
            "type" to newTxn.type.name,
            "amount" to newTxn.amount,
            "dateFormatted" to newTxn.dateFormatted,
            "timeFormatted" to newTxn.timeFormatted,
            "transactionId" to newTxn.transactionId,
            "status" to newTxn.status.name,
            "note" to newTxn.note
        )
        com.example.data.FirebaseRealtimeDbManager.pushUserTransaction(_userProfile.value.id, txnMap)
        
        com.example.data.FirebaseRealtimeDbManager.syncUserWallet(
            _userProfile.value.id,
            _walletState.value.balance,
            _walletState.value.totalDeposit,
            _walletState.value.totalWithdrawal,
            _walletState.value.totalReferralEarnings
        )

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
