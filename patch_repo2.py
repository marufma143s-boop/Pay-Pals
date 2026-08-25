with open("app/src/main/java/com/example/repository/AppRepository.kt", "r") as f:
    content = f.read()

# Add state flows
state_flows = """    private val _withdrawalMethods = MutableStateFlow(listOf("bKash", "Nagad", "Binance USDT"))
    val withdrawalMethods: StateFlow<List<String>> = _withdrawalMethods.asStateFlow()

    private val _adminDepositRequests = MutableStateFlow<List<Map<String, Any?>>>(emptyList())
    val adminDepositRequests: StateFlow<List<Map<String, Any?>>> = _adminDepositRequests.asStateFlow()
    
    private val _adminWithdrawalRequests = MutableStateFlow<List<Map<String, Any?>>>(emptyList())
    val adminWithdrawalRequests: StateFlow<List<Map<String, Any?>>> = _adminWithdrawalRequests.asStateFlow()"""

content = content.replace('    private val _withdrawalMethods = MutableStateFlow(listOf("bKash", "Nagad", "Binance USDT"))\n    val withdrawalMethods: StateFlow<List<String>> = _withdrawalMethods.asStateFlow()', state_flows)

# Add listeners
listeners = """            val withMethods = adminData["withdrawal_methods"] as? List<String>
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
    }"""

content = content.replace("""            val withMethods = adminData["withdrawal_methods"] as? List<String>
            if (withMethods != null) {
                _withdrawalMethods.value = withMethods
            }
        }
    }""", listeners)

# Add admin functions
admin_functions = """
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
"""

content = content.replace("    fun toggleBalanceVisibility() {", admin_functions + "\n    fun toggleBalanceVisibility() {")

with open("app/src/main/java/com/example/repository/AppRepository.kt", "w") as f:
    f.write(content)
