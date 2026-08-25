import re

with open("app/src/main/java/com/example/repository/AppRepository.kt", "r") as f:
    content = f.read()

state_flows = """    private val _adminWithdrawalRequests = MutableStateFlow<List<Map<String, Any?>>>(emptyList())
    val adminWithdrawalRequests: StateFlow<List<Map<String, Any?>>> = _adminWithdrawalRequests.asStateFlow()

    private val _allUsers = MutableStateFlow<List<Map<String, Any?>>>(emptyList())
    val allUsers: StateFlow<List<Map<String, Any?>>> = _allUsers.asStateFlow()

    private val _allCampaigns = MutableStateFlow<List<Map<String, Any?>>>(emptyList())
    val allCampaigns: StateFlow<List<Map<String, Any?>>> = _allCampaigns.asStateFlow()"""

if "_allUsers" not in content:
    content = content.replace("    private val _adminWithdrawalRequests = MutableStateFlow<List<Map<String, Any?>>>(emptyList())\n    val adminWithdrawalRequests: StateFlow<List<Map<String, Any?>>> = _adminWithdrawalRequests.asStateFlow()", state_flows)

listeners = """        com.example.data.FirebaseRealtimeDbManager.attachAdminWithdrawalsListener { requests ->
            _adminWithdrawalRequests.value = requests.values.filterIsInstance<Map<String, Any?>>().toList()
        }
        
        com.example.data.FirebaseRealtimeDbManager.attachAdminUsersListener { usersMap ->
            _allUsers.value = usersMap.values.filterIsInstance<Map<String, Any?>>().toList()
        }
        
        com.example.data.FirebaseRealtimeDbManager.attachAdminCampaignsListener { campaignsMap ->
            _allCampaigns.value = campaignsMap.values.filterIsInstance<Map<String, Any?>>().toList()
        }"""

if "attachAdminUsersListener" not in content:
    content = content.replace("""        com.example.data.FirebaseRealtimeDbManager.attachAdminWithdrawalsListener { requests ->
            _adminWithdrawalRequests.value = requests.values.filterIsInstance<Map<String, Any?>>().toList()
        }""", listeners)

functions = """    fun rejectAdminWithdrawal(userId: String, withdrawalId: String) {
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
    
    fun updateCampaignStatus(campaignId: String, status: String, rejectReason: String? = null) {
        com.example.data.FirebaseRealtimeDbManager.updateCampaignStatus(campaignId, status, rejectReason)
    }"""

if "updateUserBlockStatus" not in content:
    content = content.replace("    fun rejectAdminWithdrawal(userId: String, withdrawalId: String) {\n        com.example.data.FirebaseRealtimeDbManager.rejectWithdrawal(userId, withdrawalId)\n    }", functions)

with open("app/src/main/java/com/example/repository/AppRepository.kt", "w") as f:
    f.write(content)
