package com.example.repository

import android.content.Context
import com.example.data.DemoDataProvider
import com.example.model.*
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

    private val _userProfile = MutableStateFlow(UserProfile(isLoggedIn = false))
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _walletState = MutableStateFlow(WalletState(balance = 0.0, totalDeposit = 0.0, totalWithdrawal = 0.0, totalReferralEarnings = 0.0, isBalanceVisible = true))
    val walletState: StateFlow<WalletState> = _walletState.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _campaigns = MutableStateFlow<List<Campaign>>(emptyList())
    val campaigns: StateFlow<List<Campaign>> = _campaigns.asStateFlow()

    private val _referrals = MutableStateFlow<List<ReferralUser>>(emptyList())
    val referrals: StateFlow<List<ReferralUser>> = _referrals.asStateFlow()

    private val _tasks = MutableStateFlow(DemoDataProvider.getInitialTasks())
    val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()

    private val _withdrawals = MutableStateFlow<List<WithdrawalRecord>>(emptyList())
    val withdrawals: StateFlow<List<WithdrawalRecord>> = _withdrawals.asStateFlow()

    private val _dailyTaskProgress = MutableStateFlow(0)
    val dailyTaskProgress: StateFlow<Int> = _dailyTaskProgress.asStateFlow()
    val dailyTaskLimit: Int = 50

    private val _totalCampaignCount = MutableStateFlow(0)
    val totalCampaignCount: StateFlow<Int> = _totalCampaignCount.asStateFlow()

    private val _totalReferralCount = MutableStateFlow(0)
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
    
    private val _depositMethods = MutableStateFlow<List<DepositMethodItem>>(
        listOf(
            DepositMethodItem(name = "bKash (Personal)", number = "01700000000", instructions = "Send Money to this personal number"),
            DepositMethodItem(name = "Nagad (Personal)", number = "01800000000", instructions = "Send Money to this personal number"),
            DepositMethodItem(name = "Rocket (Personal)", number = "01900000000", instructions = "Send Money to this personal number")
        )
    )
    val depositMethods: StateFlow<List<DepositMethodItem>> = _depositMethods.asStateFlow()
    
    private val _withdrawalMethods = MutableStateFlow(listOf("bKash", "Nagad", "Rocket", "Binance USDT"))
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

    private val _onlineUsersMin = MutableStateFlow(prefs.getInt("online_users_min", 50))
    val onlineUsersMin: StateFlow<Int> = _onlineUsersMin.asStateFlow()

    private val _onlineUsersMax = MutableStateFlow(prefs.getInt("online_users_max", 1000))
    val onlineUsersMax: StateFlow<Int> = _onlineUsersMax.asStateFlow()

    private val _paidPackages = MutableStateFlow<List<PaidPackage>>(getDefaultPaidPackages())
    val paidPackages: StateFlow<List<PaidPackage>> = _paidPackages.asStateFlow()

    private val _userPackageOrders = MutableStateFlow<List<PackageOrder>>(emptyList())
    val userPackageOrders: StateFlow<List<PackageOrder>> = _userPackageOrders.asStateFlow()

    private val _adminPackageOrders = MutableStateFlow<List<PackageOrder>>(emptyList())
    val adminPackageOrders: StateFlow<List<PackageOrder>> = _adminPackageOrders.asStateFlow()

    private val _developerInfo = MutableStateFlow(DeveloperInfo())
    val developerInfo: StateFlow<DeveloperInfo> = _developerInfo.asStateFlow()

    private val _allSupportThreads = MutableStateFlow<List<SupportThread>>(emptyList())
    val allSupportThreads: StateFlow<List<SupportThread>> = _allSupportThreads.asStateFlow()

    private val _currentChatMessages = MutableStateFlow<List<SupportChatMessage>>(emptyList())
    val currentChatMessages: StateFlow<List<SupportChatMessage>> = _currentChatMessages.asStateFlow()

    private val _serviceControlSettings = MutableStateFlow(ServiceControlSettings())
    val serviceControlSettings: StateFlow<ServiceControlSettings> = _serviceControlSettings.asStateFlow()

    private val _maintenanceSettings = MutableStateFlow(MaintenanceSettings())
    val maintenanceSettings: StateFlow<MaintenanceSettings> = _maintenanceSettings.asStateFlow()

    private var activeChatListener: com.google.firebase.database.ValueEventListener? = null
    private var activeChatUserId: String? = null

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
            
            val depMethodsRaw = adminData["deposit_methods"]
            if (depMethodsRaw is List<*>) {
                val parsedList = depMethodsRaw.mapNotNull { item ->
                    when (item) {
                        is Map<*, *> -> {
                            val name = item["name"] as? String ?: ""
                            val number = item["number"] as? String ?: ""
                            val instructions = item["instructions"] as? String ?: ""
                            if (name.isNotBlank()) DepositMethodItem(name, number, instructions) else null
                        }
                        is String -> {
                            if (item.isNotBlank()) DepositMethodItem(name = item, number = "") else null
                        }
                        else -> null
                    }
                }
                if (parsedList.isNotEmpty()) {
                    _depositMethods.value = parsedList
                }
            } else if (depMethodsRaw is Map<*, *>) {
                val parsedList = depMethodsRaw.values.mapNotNull { item ->
                    if (item is Map<*, *>) {
                        val name = item["name"] as? String ?: ""
                        val number = item["number"] as? String ?: ""
                        val instructions = item["instructions"] as? String ?: ""
                        if (name.isNotBlank()) DepositMethodItem(name, number, instructions) else null
                    } else null
                }
                if (parsedList.isNotEmpty()) {
                    _depositMethods.value = parsedList
                }
            }
            
            val withMethods = adminData["withdrawal_methods"] as? List<String>
            if (withMethods != null) {
                _withdrawalMethods.value = withMethods
            }

            val minUsers = (adminData["online_users_min"] as? Number)?.toInt()
            val maxUsers = (adminData["online_users_max"] as? Number)?.toInt()
            if (minUsers != null && minUsers > 0) {
                _onlineUsersMin.value = minUsers
                prefs.edit().putInt("online_users_min", minUsers).apply()
            }
            if (maxUsers != null && maxUsers > 0) {
                _onlineUsersMax.value = maxUsers
                prefs.edit().putInt("online_users_max", maxUsers).apply()
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

        com.example.data.FirebaseRealtimeDbManager.attachPaidPackagesListener { list ->
            val parsed = list.mapNotNull { parsePaidPackage(it) }
            if (parsed.isNotEmpty()) {
                _paidPackages.value = parsed
            } else {
                // If cloud database is empty, seed default packages to Firebase
                val defaults = getDefaultPaidPackages()
                _paidPackages.value = defaults
                defaults.forEach { pkg ->
                    val map = mapOf(
                        "id" to pkg.id,
                        "name" to pkg.name,
                        "views" to pkg.views,
                        "price" to pkg.price,
                        "description" to pkg.description,
                        "badge" to pkg.badge,
                        "isEnabled" to pkg.isEnabled
                    )
                    com.example.data.FirebaseRealtimeDbManager.savePaidPackage(map)
                }
            }
        }

        com.example.data.FirebaseRealtimeDbManager.attachAdminPackageOrdersListener { ordersMap ->
            val parsedList = ordersMap.values.filterIsInstance<Map<String, Any?>>().mapNotNull { parsePackageOrder(it) }.sortedByDescending { it.timestamp }
            _adminPackageOrders.value = parsedList
        }

        com.example.data.FirebaseRealtimeDbManager.attachDeveloperInfoListener { devMap ->
            if (devMap.isNotEmpty()) {
                val info = DeveloperInfo(
                    name = devMap["name"] as? String ?: "Maruf Hossain",
                    title = devMap["title"] as? String ?: "Lead Android & Fintech Architect",
                    email = devMap["email"] as? String ?: "maruf.hossain.dev@gmail.com",
                    phone = devMap["phone"] as? String ?: "+880 1712-345678",
                    website = devMap["website"] as? String ?: "https://github.com/maruf-dev",
                    description = devMap["description"] as? String ?: _developerInfo.value.description,
                    avatarBase64 = devMap["avatarBase64"] as? String ?: ""
                )
                _developerInfo.value = info
            }
        }

        com.example.data.FirebaseRealtimeDbManager.attachAllSupportThreadsListener { threadsMap ->
            val list = threadsMap.values.filterIsInstance<Map<String, Any?>>().mapNotNull { map ->
                val userId = map["userId"] as? String ?: return@mapNotNull null
                val userName = map["userName"] as? String ?: "User"
                val userEmail = map["userEmail"] as? String ?: ""
                val userPhone = map["userPhone"] as? String ?: ""
                val userAvatar = map["userAvatar"] as? String ?: ""
                val lastMessage = map["lastMessage"] as? String ?: ""
                val lastTimestamp = (map["lastTimestamp"] as? Number)?.toLong() ?: 0L
                val unreadAdmin = (map["unreadAdminCount"] as? Number)?.toInt() ?: 0
                val unreadUser = (map["unreadUserCount"] as? Number)?.toInt() ?: 0
                SupportThread(
                    userId = userId,
                    userName = userName,
                    userEmail = userEmail,
                    userPhone = userPhone,
                    userAvatar = userAvatar,
                    lastMessage = lastMessage,
                    lastTimestamp = lastTimestamp,
                    unreadAdminCount = unreadAdmin,
                    unreadUserCount = unreadUser
                )
            }.sortedByDescending { it.lastTimestamp }
            _allSupportThreads.value = list
        }

        com.example.data.FirebaseRealtimeDbManager.attachServiceControlListener { sData ->
            if (sData.isNotEmpty()) {
                val deposit = parseServiceItem(sData["deposit"] as? Map<*, *>, "deposit", "Deposit Service")
                val withdraw = parseServiceItem(sData["withdraw"] as? Map<*, *>, "withdraw", "Withdrawal Service")
                val adsterra = parseServiceItem(sData["campaign_adsterra"] as? Map<*, *>, "campaign_adsterra", "Adsterra Campaigns & Tasks")
                val blogger = parseServiceItem(sData["campaign_blogger"] as? Map<*, *>, "campaign_blogger", "Blogger Campaigns & Tasks")
                val monetag = parseServiceItem(sData["campaign_monetag"] as? Map<*, *>, "campaign_monetag", "Monetag Campaigns & Tasks")
                val referral = parseServiceItem(sData["referral"] as? Map<*, *>, "referral", "Referral System")
                val paidPackages = parseServiceItem(sData["paid_packages"] as? Map<*, *>, "paid_packages", "Paid Campaign Packages")
                val registration = parseServiceItem(sData["user_registration"] as? Map<*, *>, "user_registration", "New User Registration")

                _serviceControlSettings.value = ServiceControlSettings(
                    deposit = deposit,
                    withdraw = withdraw,
                    campaignAdsterra = adsterra,
                    campaignBlogger = blogger,
                    campaignMonetag = monetag,
                    referral = referral,
                    paidPackages = paidPackages,
                    userRegistration = registration
                )
            }
        }

        com.example.data.FirebaseRealtimeDbManager.attachMaintenanceListener { mData ->
            if (mData.isNotEmpty()) {
                val isMaster = mData["isMasterEnabled"] as? Boolean ?: false
                val isUser = mData["isUserMaintenance"] as? Boolean ?: false
                val userNote = mData["userNote"] as? String ?: ""
                val isAdmin = mData["isAdminMaintenance"] as? Boolean ?: false
                val adminNote = mData["adminNote"] as? String ?: ""

                val linksRaw = mData["socialLinks"]
                val linksList = mutableListOf<SocialMediaLink>()
                if (linksRaw is List<*>) {
                    linksRaw.forEach { item ->
                        if (item is Map<*, *>) {
                            parseSocialMediaLink(item)?.let { linksList.add(it) }
                        }
                    }
                } else if (linksRaw is Map<*, *>) {
                    linksRaw.values.forEach { item ->
                        if (item is Map<*, *>) {
                            parseSocialMediaLink(item)?.let { linksList.add(it) }
                        }
                    }
                }

                _maintenanceSettings.value = MaintenanceSettings(
                    isMasterEnabled = isMaster,
                    isUserMaintenance = isUser,
                    userNote = userNote,
                    isAdminMaintenance = isAdmin,
                    adminNote = adminNote,
                    socialLinks = linksList
                )
            }
        }
    }

    private fun parseServiceItem(map: Map<*, *>?, defaultKey: String, defaultName: String): ServiceItemConfig {
        if (map == null) return ServiceItemConfig(defaultKey, defaultName, false, "")
        val key = map["key"] as? String ?: defaultKey
        val name = map["name"] as? String ?: defaultName
        val isDisabled = map["isDisabled"] as? Boolean ?: false
        val reason = map["reason"] as? String ?: ""
        return ServiceItemConfig(key, name, isDisabled, reason)
    }

    private fun parseSocialMediaLink(map: Map<*, *>?): SocialMediaLink? {
        if (map == null) return null
        val id = map["id"] as? String ?: return null
        val name = map["name"] as? String ?: ""
        val logoBase64 = map["logoBase64"] as? String ?: ""
        val iconKey = map["iconKey"] as? String ?: "telegram"
        val url = map["url"] as? String ?: ""
        return SocialMediaLink(id, name, logoBase64, iconKey, url)
    }

    private fun getDefaultPaidPackages(): List<PaidPackage> = listOf(
        PaidPackage(
            id = "pkg_starter_1k",
            name = "Starter Package",
            views = 1000,
            price = 500.0,
            description = "High quality real visits, quick start & safe delivery.",
            badge = "Starter"
        ),
        PaidPackage(
            id = "pkg_growth_2k5",
            name = "Growth Package",
            views = 2500,
            price = 1200.0,
            description = "High retention visitors to boost organic engagement.",
            badge = "Popular"
        ),
        PaidPackage(
            id = "pkg_pro_5k",
            name = "Pro Traffic Package",
            views = 5000,
            price = 2300.0,
            description = "Premium targeted views, fast delivery & priority queue.",
            badge = "Best Value"
        ),
        PaidPackage(
            id = "pkg_ultra_10k",
            name = "Ultra VIP Package",
            views = 10000,
            price = 4500.0,
            description = "Maximum traffic volume with dedicated priority processing.",
            badge = "VIP"
        )
    )

    private fun parsePaidPackage(data: Map<String, Any?>): PaidPackage? {
        val id = data["id"] as? String ?: return null
        val name = data["name"] as? String ?: "Package"
        val views = (data["views"] as? Number)?.toInt() ?: 0
        val price = (data["price"] as? Number)?.toDouble() ?: 0.0
        val description = data["description"] as? String ?: ""
        val badge = data["badge"] as? String ?: ""
        val isEnabled = (data["isEnabled"] as? Boolean) ?: true
        return PaidPackage(
            id = id,
            name = name,
            views = views,
            price = price,
            description = description,
            badge = badge,
            isEnabled = isEnabled
        )
    }

    private fun parsePackageOrder(data: Map<String, Any?>): PackageOrder? {
        val id = data["id"] as? String ?: return null
        val packageId = data["packageId"] as? String ?: ""
        val packageName = data["packageName"] as? String ?: "Package"
        val title = data["title"] as? String ?: ""
        val views = (data["views"] as? Number)?.toInt() ?: 0
        val price = (data["price"] as? Number)?.toDouble() ?: 0.0
        val targetLink = data["targetLink"] as? String ?: ""
        val userId = data["userId"] as? String ?: ""
        val userName = data["userName"] as? String ?: ""
        val userEmail = data["userEmail"] as? String ?: ""
        val statusStr = data["status"] as? String ?: "PENDING"
        val status = try {
            PackageOrderStatus.valueOf(statusStr.uppercase())
        } catch (e: Exception) {
            when (statusStr.uppercase()) {
                "RUNNING" -> PackageOrderStatus.RUNNING
                "COMPLETED" -> PackageOrderStatus.COMPLETED
                "REJECTED" -> PackageOrderStatus.REJECTED
                else -> PackageOrderStatus.PENDING
            }
        }
        val dateFormatted = data["dateFormatted"] as? String ?: "Today"
        val timeFormatted = data["timeFormatted"] as? String ?: ""
        val rejectReason = data["rejectReason"] as? String
        val timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
        return PackageOrder(
            id = id,
            packageId = packageId,
            packageName = packageName,
            title = title,
            views = views,
            price = price,
            targetLink = targetLink,
            userId = userId,
            userName = userName,
            userEmail = userEmail,
            status = status,
            dateFormatted = dateFormatted,
            timeFormatted = timeFormatted,
            rejectReason = rejectReason,
            timestamp = timestamp
        )
    }

    private fun parseCampaign(data: Map<String, Any?>): Campaign? {
        val id = data["id"] as? String ?: return null
        val title = data["title"] as? String ?: "Campaign"
        val networkType = data["networkType"] as? String ?: "adstra"
        val targetLink = data["targetLink"] as? String ?: ""
        val packagePrice = (data["packagePrice"] as? Number)?.toDouble() ?: 0.0
        val targetViews = (data["targetViews"] as? Number)?.toInt() ?: 0
        val completedViews = (data["completedViews"] as? Number)?.toInt() ?: 0
        val statusStr = data["status"] as? String ?: "PENDING"
        val status = try {
            CampaignStatus.valueOf(statusStr.uppercase())
        } catch (e: Exception) {
            when (statusStr.uppercase()) {
                "RUNNING" -> CampaignStatus.RUNNING
                "COMPLETED" -> CampaignStatus.COMPLETED
                "CANCELLED" -> CampaignStatus.CANCELLED
                else -> CampaignStatus.PENDING
            }
        }
        val createdDate = data["createdDate"] as? String ?: "Today"
        return Campaign(
            id = id,
            title = title,
            networkType = networkType,
            targetLink = targetLink,
            packagePrice = packagePrice,
            targetViews = targetViews,
            completedViews = completedViews,
            status = status,
            createdDate = createdDate
        )
    }

    private fun parseReferral(data: Map<String, Any?>): ReferralUser? {
        val id = data["id"] as? String ?: return null
        val username = (data["username"] as? String) ?: (data["friendName"] as? String) ?: "Member"
        val joinDate = (data["joinDate"] as? String) ?: (data["joinedDateFormatted"] as? String) ?: "Today"
        val reward = (data["reward"] as? Number)?.toDouble() ?: (data["rewardAmount"] as? Number)?.toDouble() ?: 100.0
        val statusStr = (data["status"] as? String) ?: "ACTIVE"
        val status = if (statusStr.equals("PENDING", ignoreCase = true)) ReferralStatus.PENDING else ReferralStatus.ACTIVE
        return ReferralUser(
            id = id,
            username = if (username.startsWith("@")) username else "@$username",
            joinDate = joinDate,
            status = status,
            reward = reward
        )
    }

    private fun parseTransaction(data: Map<String, Any?>): Transaction? {
        val id = data["id"] as? String ?: return null
        val title = data["title"] as? String ?: "Transaction"
        val typeStr = data["type"] as? String ?: ""
        val type = try {
            TransactionType.valueOf(typeStr.uppercase())
        } catch (e: Exception) {
            when (typeStr.uppercase()) {
                "DEPOSIT" -> TransactionType.DEPOSIT
                "WITHDRAWAL" -> TransactionType.WITHDRAWAL
                "TASK_REWARD" -> TransactionType.TASK_REWARD
                "REFERRAL_REWARD" -> TransactionType.REFERRAL_REWARD
                "CAMPAIGN_PAYMENT" -> TransactionType.CAMPAIGN_PAYMENT
                else -> if (title.contains("Deposit", true)) TransactionType.DEPOSIT else if (title.contains("Withdraw", true)) TransactionType.WITHDRAWAL else TransactionType.TASK_REWARD
            }
        }
        val amount = (data["amount"] as? Number)?.toDouble() ?: 0.0
        val dateFormatted = data["dateFormatted"] as? String ?: "Today"
        val timeFormatted = data["timeFormatted"] as? String ?: ""
        val transactionId = (data["transactionId"] as? String) ?: (data["trxId"] as? String) ?: id
        val statusStr = data["status"] as? String ?: "COMPLETED"
        val status = try {
            TransactionStatus.valueOf(statusStr.uppercase())
        } catch (e: Exception) {
            if (statusStr.equals("PENDING", true)) TransactionStatus.PENDING else TransactionStatus.COMPLETED
        }
        val note = data["note"] as? String ?: ""
        return Transaction(
            id = id,
            title = title,
            type = type,
            amount = amount,
            dateFormatted = dateFormatted,
            timeFormatted = timeFormatted,
            transactionId = transactionId,
            status = status,
            note = note
        )
    }

    private fun attachUserListeners(userId: String) {
        if (userId.isBlank()) return

        com.example.data.FirebaseRealtimeDbManager.attachUserRealtimeListener(userId) { data ->
            val bal = (data["balance"] as? Number)?.toDouble()
            val totalDep = (data["totalDeposit"] as? Number)?.toDouble()
            val totalWith = (data["totalWithdrawal"] as? Number)?.toDouble()
            val totalRef = (data["totalReferralEarnings"] as? Number)?.toDouble()
            val appliedRef = data["appliedReferralCode"] as? String
            val roleFromDb = data["role"] as? String
            val permissionsMap = data["permissions"] as? Map<String, Any?>
            val parsedPermissions = permissionsMap?.mapValues { it.value == true || it.value.toString() == "true" } ?: emptyMap()

            if (appliedRef != null && _userProfile.value.appliedReferralCode == null) {
                _userProfile.update { it.copy(appliedReferralCode = appliedRef) }
                prefs.edit().putString("user_applied_referral", appliedRef).apply()
            }

            _userProfile.update { current ->
                val finalRole = if (current.email == "d@gmail.com") "OWNER" else (roleFromDb ?: current.role)
                current.copy(
                    role = finalRole,
                    permissions = parsedPermissions
                )
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

        com.example.data.FirebaseRealtimeDbManager.attachUserCampaignsListener(userId) { rawList ->
            val parsedList = rawList.mapNotNull { parseCampaign(it) }.reversed()
            _campaigns.value = parsedList
            _totalCampaignCount.value = parsedList.size
        }

        com.example.data.FirebaseRealtimeDbManager.attachUserReferralsListener(userId) { rawList ->
            val parsedList = rawList.mapNotNull { parseReferral(it) }.reversed()
            _referrals.value = parsedList
            _totalReferralCount.value = parsedList.size
        }

        com.example.data.FirebaseRealtimeDbManager.attachUserTransactionsListener(userId) { rawList ->
            val parsedList = rawList.mapNotNull { parseTransaction(it) }.reversed()
            if (parsedList.isNotEmpty()) {
                _transactions.value = parsedList
            }
        }

        com.example.data.FirebaseRealtimeDbManager.attachUserPackageOrdersListener(userId) { rawList ->
            val parsedList = rawList.mapNotNull { parsePackageOrder(it) }.sortedByDescending { it.timestamp }
            _userPackageOrders.value = parsedList
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
        val savedRole = prefs.getString("user_role", "USER") ?: "USER"

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
                role = if (email == "d@gmail.com") "OWNER" else savedRole,
                isLoggedIn = true
            )
            _isLoggedIn.value = true

            if (savedBalance >= 0f) {
                _walletState.update { it.copy(balance = savedBalance.toDouble()) }
            }

            attachUserListeners(savedUserId)
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
        if (_serviceControlSettings.value.userRegistration.isDisabled) {
            val reason = _serviceControlSettings.value.getServiceReason("user_registration")
            onResult(false, reason)
            return
        }

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
            "role" to (if (contactType == "email" && cleanContact == "d@gmail.com") "OWNER" else "USER"),
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
                    .putString("user_role", if (contactType == "email" && cleanContact == "d@gmail.com") "OWNER" else "USER")
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
                    role = if (contactType == "email" && cleanContact == "d@gmail.com") "OWNER" else "USER",
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

                if (!referralCodeInput.isNullOrBlank()) {
                    com.example.data.FirebaseRealtimeDbManager.applyReferralBonusInCloud(
                        refereeUserId = userId,
                        refereeName = cleanName,
                        cleanCode = referralCodeInput.trim().uppercase(),
                        bonusReward = 50.0
                    ) { _, _, _ -> }
                }

                attachUserListeners(userId)

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
                val roleFromDb = userData["role"] as? String ?: "USER"
                val finalRole = if (email == "d@gmail.com") "OWNER" else roleFromDb
                val permissionsMap = userData["permissions"] as? Map<String, Any?>
                val parsedPermissions = permissionsMap?.mapValues { it.value == true || it.value.toString() == "true" } ?: emptyMap()

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
                    .putString("user_role", finalRole)
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
                    role = finalRole,
                    permissions = parsedPermissions,
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

                attachUserListeners(userId)

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
            .remove("user_contact_type")
            .remove("user_account_id")
            .remove("user_referral")
            .remove("user_applied_referral")
            .remove("user_balance")
            .apply()

        _isLoggedIn.value = false
        _userProfile.value = UserProfile(isLoggedIn = false)
        _walletState.value = WalletState(balance = 0.0, totalDeposit = 0.0, totalWithdrawal = 0.0, totalReferralEarnings = 0.0)
        _campaigns.value = emptyList()
        _referrals.value = emptyList()
        _transactions.value = emptyList()
        _totalCampaignCount.value = 0
        _totalReferralCount.value = 0
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

    fun updateAdminOnlineUsersRange(min: Int, max: Int) {
        val safeMin = if (min < 1) 1 else min
        val safeMax = if (max < safeMin) safeMin else max
        _onlineUsersMin.value = safeMin
        _onlineUsersMax.value = safeMax
        prefs.edit().putInt("online_users_min", safeMin).putInt("online_users_max", safeMax).apply()
        com.example.data.FirebaseRealtimeDbManager.updateAdminSetting("online_users_min", safeMin)
        com.example.data.FirebaseRealtimeDbManager.updateAdminSetting("online_users_max", safeMax)
    }
    
    fun addAdminDepositMethod(name: String, number: String, instructions: String = "") {
        val current = _depositMethods.value.filter { it.name != name.trim() }.toMutableList()
        current.add(DepositMethodItem(name = name.trim(), number = number.trim(), instructions = instructions.trim()))
        _depositMethods.value = current
        val mappedForDb = current.map { mapOf("name" to it.name, "number" to it.number, "instructions" to it.instructions) }
        com.example.data.FirebaseRealtimeDbManager.updateAdminSetting("deposit_methods", mappedForDb)
    }
    
    fun removeAdminDepositMethod(name: String) {
        val current = _depositMethods.value.filter { it.name != name }.toMutableList()
        _depositMethods.value = current
        val mappedForDb = current.map { mapOf("name" to it.name, "number" to it.number, "instructions" to it.instructions) }
        com.example.data.FirebaseRealtimeDbManager.updateAdminSetting("deposit_methods", mappedForDb)
    }
    
    fun addAdminWithdrawalMethod(method: String) {
        val current = _withdrawalMethods.value.toMutableList()
        if (!current.contains(method.trim())) {
            current.add(method.trim())
            _withdrawalMethods.value = current
            com.example.data.FirebaseRealtimeDbManager.updateAdminSetting("withdrawal_methods", current)
        }
    }
    
    fun removeAdminWithdrawalMethod(method: String) {
        val current = _withdrawalMethods.value.toMutableList()
        if (current.remove(method.trim())) {
            _withdrawalMethods.value = current
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

    fun orderPackage(packageItem: PaidPackage, title: String, targetLink: String): Result<PackageOrder> {
        val user = _userProfile.value
        if (!user.isLoggedIn || user.id.isBlank()) {
            return Result.failure(IllegalStateException("Please log in to order a package."))
        }
        val cleanLink = targetLink.trim()
        val cleanTitle = title.trim()
        if (cleanLink.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter a valid target link."))
        }
        if (cleanTitle.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter a title for your order."))
        }
        val currentBalance = _walletState.value.balance
        if (currentBalance < packageItem.price) {
            return Result.failure(IllegalStateException("Insufficient balance! You need ${FormatUtils.formatCredits(packageItem.price)} but you have ${FormatUtils.formatCredits(currentBalance)}."))
        }

        val newBalance = currentBalance - packageItem.price
        _walletState.update { it.copy(balance = newBalance) }
        prefs.edit().putFloat("user_balance", newBalance.toFloat()).apply()

        val orderId = "PKG-${UUID.randomUUID().toString().take(8).uppercase()}"
        val dateFormatted = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())
        val timeFormatted = SimpleDateFormat("hh:mm a", Locale.US).format(Date())

        val order = PackageOrder(
            id = orderId,
            packageId = packageItem.id,
            packageName = packageItem.name,
            title = cleanTitle,
            views = packageItem.views,
            price = packageItem.price,
            targetLink = cleanLink,
            userId = user.id,
            userName = user.fullName,
            userEmail = if (user.email.isNotBlank()) user.email else user.contactValue,
            status = PackageOrderStatus.PENDING,
            dateFormatted = dateFormatted,
            timeFormatted = timeFormatted,
            timestamp = System.currentTimeMillis()
        )

        // Push order to Firebase
        val orderMap = mapOf(
            "id" to order.id,
            "packageId" to order.packageId,
            "packageName" to order.packageName,
            "title" to order.title,
            "views" to order.views,
            "price" to order.price,
            "targetLink" to order.targetLink,
            "userId" to order.userId,
            "userName" to order.userName,
            "userEmail" to order.userEmail,
            "status" to order.status.name,
            "dateFormatted" to order.dateFormatted,
            "timeFormatted" to order.timeFormatted,
            "timestamp" to order.timestamp
        )
        com.example.data.FirebaseRealtimeDbManager.pushPackageOrder(user.id, orderMap)

        // Add transaction record
        val txnId = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}"
        val txn = Transaction(
            id = UUID.randomUUID().toString(),
            title = "Package Order: ${packageItem.name}",
            type = TransactionType.PACKAGE_ORDER,
            amount = packageItem.price,
            dateFormatted = dateFormatted,
            timeFormatted = timeFormatted,
            transactionId = txnId,
            status = TransactionStatus.COMPLETED,
            note = "${packageItem.views} Views for $cleanLink"
        )
        _transactions.update { listOf(txn) + it }
        val txnMap = mapOf(
            "id" to txn.id,
            "title" to txn.title,
            "type" to txn.type.name,
            "amount" to txn.amount,
            "dateFormatted" to txn.dateFormatted,
            "timeFormatted" to txn.timeFormatted,
            "transactionId" to txn.transactionId,
            "status" to txn.status.name,
            "note" to txn.note
        )
        com.example.data.FirebaseRealtimeDbManager.pushUserTransaction(user.id, txnMap)
        com.example.data.FirebaseRealtimeDbManager.syncUserWallet(
            user.id,
            newBalance,
            _walletState.value.totalDeposit,
            _walletState.value.totalWithdrawal,
            _walletState.value.totalReferralEarnings
        )

        return Result.success(order)
    }

    fun saveAdminPaidPackage(pkg: PaidPackage) {
        val id = if (pkg.id.isBlank()) "pkg_${UUID.randomUUID().toString().take(8)}" else pkg.id
        val map = mapOf(
            "id" to id,
            "name" to pkg.name.trim(),
            "views" to pkg.views,
            "price" to pkg.price,
            "description" to pkg.description.trim(),
            "badge" to pkg.badge.trim(),
            "isEnabled" to pkg.isEnabled
        )
        com.example.data.FirebaseRealtimeDbManager.savePaidPackage(map)
    }

    fun deleteAdminPaidPackage(packageId: String) {
        com.example.data.FirebaseRealtimeDbManager.deletePaidPackage(packageId)
    }

    fun updateAdminPackageOrderStatus(orderId: String, status: String, rejectReason: String? = null) {
        com.example.data.FirebaseRealtimeDbManager.updatePackageOrderStatus(orderId, status, rejectReason)
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

    fun deposit(
        amount: Double,
        method: String,
        methodNumber: String = "",
        senderNumber: String = "",
        trxId: String = ""
    ): Result<Unit> {
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Amount must be greater than zero."))
        }

        val actualTxnId = if (trxId.isNotBlank()) trxId.trim().uppercase() else generateTxnId()
        val id = UUID.randomUUID().toString()
        val noteDetails = if (senderNumber.isNotBlank()) {
            "$method ($methodNumber) - Sender: $senderNumber"
        } else {
            "$method $methodNumber".trim()
        }

        val newTxn = Transaction(
            id = id,
            title = "Deposit",
            type = TransactionType.DEPOSIT,
            amount = amount,
            dateFormatted = "Today",
            timeFormatted = getCurrentTimeFormatted(),
            transactionId = actualTxnId,
            status = TransactionStatus.PENDING,
            note = noteDetails
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
            "methodNumber" to methodNumber,
            "senderNumber" to senderNumber.trim(),
            "trxId" to actualTxnId,
            "transactionId" to actualTxnId,
            "dateFormatted" to "Today",
            "timeFormatted" to getCurrentTimeFormatted(),
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
        com.example.data.FirebaseRealtimeDbManager.pushCampaignToDb(_userProfile.value.id, campaignMap)
        
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

    fun updateUserRole(userId: String, role: String) {
        com.example.data.FirebaseRealtimeDbManager.updateUserRole(userId, role)
    }

    fun updateUserAdminPermissions(userId: String, permissions: Map<String, Boolean>) {
        com.example.data.FirebaseRealtimeDbManager.updateUserAdminPermissions(userId, permissions)
    }

    fun removeAdminAccess(userId: String) {
        com.example.data.FirebaseRealtimeDbManager.updateUserRole(userId, "USER")
    }

    fun updateDeveloperInfo(info: DeveloperInfo) {
        val map = mapOf<String, Any?>(
            "name" to info.name,
            "title" to info.title,
            "email" to info.email,
            "phone" to info.phone,
            "website" to info.website,
            "description" to info.description,
            "avatarBase64" to info.avatarBase64
        )
        _developerInfo.value = info
        com.example.data.FirebaseRealtimeDbManager.saveDeveloperInfo(map)
    }

    fun updateUserAvatar(avatarBase64: String) {
        val uid = _userProfile.value.id
        if (uid.isNotBlank() && uid != "user_default") {
            _userProfile.update { it.copy(avatarBase64 = avatarBase64) }
            com.example.data.FirebaseRealtimeDbManager.updateUserAvatar(uid, avatarBase64)
        }
    }

    fun openChatWithUser(targetUserId: String) {
        activeChatUserId = targetUserId
        _currentChatMessages.value = emptyList()
        activeChatListener = com.example.data.FirebaseRealtimeDbManager.attachUserSupportMessagesListener(targetUserId) { messagesMap ->
            val parsed = messagesMap.values.filterIsInstance<Map<String, Any?>>().mapNotNull { map ->
                val id = map["id"] as? String ?: return@mapNotNull null
                val senderId = map["senderId"] as? String ?: ""
                val senderName = map["senderName"] as? String ?: "User"
                val senderRole = map["senderRole"] as? String ?: "USER"
                val message = map["message"] as? String ?: ""
                val voiceBase64 = map["voiceBase64"] as? String ?: ""
                val voiceDuration = (map["voiceDurationSeconds"] as? Number)?.toInt() ?: 0
                val replyToMessageId = map["replyToMessageId"] as? String
                val replyToText = map["replyToText"] as? String
                val replyToSenderName = map["replyToSenderName"] as? String
                val timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L
                val dateFormatted = map["dateFormatted"] as? String ?: ""
                val timeFormatted = map["timeFormatted"] as? String ?: ""
                val isDeleted = (map["isDeleted"] as? Boolean) ?: false
                SupportChatMessage(
                    id = id,
                    senderId = senderId,
                    senderName = senderName,
                    senderRole = senderRole,
                    message = message,
                    voiceBase64 = voiceBase64,
                    voiceDurationSeconds = voiceDuration,
                    replyToMessageId = replyToMessageId,
                    replyToText = replyToText,
                    replyToSenderName = replyToSenderName,
                    timestamp = timestamp,
                    dateFormatted = dateFormatted,
                    timeFormatted = timeFormatted,
                    isDeleted = isDeleted
                )
            }.sortedBy { it.timestamp }
            _currentChatMessages.value = parsed
        }
    }

    fun closeChatWithUser() {
        activeChatUserId = null
        _currentChatMessages.value = emptyList()
    }

    fun sendSupportChatMessage(
        targetUserId: String,
        messageText: String,
        voiceBase64: String = "",
        voiceDurationSeconds: Int = 0,
        replyToMessageId: String? = null,
        replyToText: String? = null,
        replyToSenderName: String? = null
    ) {
        val current = _userProfile.value
        val msgId = "msg_" + System.currentTimeMillis() + "_" + (100..999).random()
        val now = System.currentTimeMillis()
        val dateFormatted = getCurrentDateFormatted()
        val timeFormatted = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US).format(java.util.Date(now))

        val isSenderAdmin = current.role == "ADMIN" || current.role == "OWNER" || current.email == "d@gmail.com"
        val senderRole = if (isSenderAdmin) (if (current.email == "d@gmail.com" || current.role == "OWNER") "OWNER" else "ADMIN") else "USER"

        val msgData = hashMapOf<String, Any?>(
            "id" to msgId,
            "senderId" to current.id,
            "senderName" to current.fullName,
            "senderRole" to senderRole,
            "message" to messageText.trim(),
            "voiceBase64" to voiceBase64,
            "voiceDurationSeconds" to voiceDurationSeconds,
            "replyToMessageId" to replyToMessageId,
            "replyToText" to replyToText,
            "replyToSenderName" to replyToSenderName,
            "timestamp" to now,
            "dateFormatted" to dateFormatted,
            "timeFormatted" to timeFormatted,
            "isDeleted" to false
        )

        val lastSummary = if (voiceBase64.isNotBlank()) "🎤 Voice message (${voiceDurationSeconds}s)" else messageText.trim()

        val threadSummary = hashMapOf<String, Any?>(
            "userId" to targetUserId,
            "lastMessage" to lastSummary,
            "lastTimestamp" to now
        )

        if (!isSenderAdmin) {
            // When user sends, also update their contact details in the thread summary
            threadSummary["userName"] = current.fullName
            threadSummary["userEmail"] = current.email
            threadSummary["userPhone"] = current.phone
            threadSummary["userAvatar"] = current.avatarBase64
        }

        com.example.data.FirebaseRealtimeDbManager.sendSupportMessage(
            userId = targetUserId,
            messageId = msgId,
            messageData = msgData,
            threadSummary = threadSummary
        )
    }

    fun deleteSupportChatMessage(targetUserId: String, messageId: String) {
        com.example.data.FirebaseRealtimeDbManager.deleteSupportMessage(targetUserId, messageId)
    }

    fun updateServiceStatus(serviceKey: String, isDisabled: Boolean, reason: String) {
        com.example.data.FirebaseRealtimeDbManager.updateSingleService(serviceKey, isDisabled, reason)
    }

    fun updateMaintenanceSettings(settings: MaintenanceSettings) {
        val linksData = settings.socialLinks.map { link ->
            mapOf(
                "id" to link.id,
                "name" to link.name,
                "logoBase64" to link.logoBase64,
                "iconKey" to link.iconKey,
                "url" to link.url
            )
        }
        val data = mapOf<String, Any?>(
            "isMasterEnabled" to settings.isMasterEnabled,
            "isUserMaintenance" to settings.isUserMaintenance,
            "userNote" to settings.userNote,
            "isAdminMaintenance" to settings.isAdminMaintenance,
            "adminNote" to settings.adminNote,
            "socialLinks" to linksData
        )
        com.example.data.FirebaseRealtimeDbManager.saveMaintenanceSettings(data)
    }
}
