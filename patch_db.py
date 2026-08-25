with open("app/src/main/java/com/example/data/FirebaseRealtimeDbManager.kt", "r") as f:
    content = f.read()

admin_listeners = """
    // Admin Listeners
    fun attachAdminDepositsListener(onDataChange: (Map<String, Any?>) -> Unit) {
        adminDepositsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.value as? Map<String, Any?> ?: emptyMap()
                onDataChange(data)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    
    fun attachAdminWithdrawalsListener(onDataChange: (Map<String, Any?>) -> Unit) {
        adminDepositsRef.parent?.child("admin_withdrawal_request")?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.value as? Map<String, Any?> ?: emptyMap()
                onDataChange(data)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    
    fun approveDeposit(userId: String, depositId: String, amount: Double) {
        adminDepositsRef.child(depositId).child("status").setValue("COMPLETED")
        usersRef.child(userId).child("transactions").child(depositId).child("status").setValue("COMPLETED")
        
        usersRef.child(userId).child("balance").get().addOnSuccessListener { snapshot ->
            val currentBalance = snapshot.getValue(Double::class.java) ?: 0.0
            usersRef.child(userId).child("balance").setValue(currentBalance + amount)
        }
    }
    
    fun rejectDeposit(userId: String, depositId: String) {
        adminDepositsRef.child(depositId).child("status").setValue("REJECTED")
        usersRef.child(userId).child("transactions").child(depositId).child("status").setValue("REJECTED")
    }
    
    fun approveWithdrawal(userId: String, withdrawalId: String) {
        adminDepositsRef.parent?.child("admin_withdrawal_request")?.child(withdrawalId)?.child("status")?.setValue("COMPLETED")
        withdrawalsRef.child(withdrawalId).child("status").setValue("COMPLETED")
        usersRef.child(userId).child("transactions").child(withdrawalId).child("status").setValue("COMPLETED")
        usersRef.child(userId).child("withdrawals").child(withdrawalId).child("status").setValue("COMPLETED")
    }
    
    fun rejectWithdrawal(userId: String, withdrawalId: String) {
        adminDepositsRef.parent?.child("admin_withdrawal_request")?.child(withdrawalId)?.child("status")?.setValue("REJECTED")
        withdrawalsRef.child(withdrawalId).child("status").setValue("REJECTED")
        usersRef.child(userId).child("transactions").child(withdrawalId).child("status").setValue("REJECTED")
        usersRef.child(userId).child("withdrawals").child(withdrawalId).child("status").setValue("REJECTED")
        
        // Refund the amount
        usersRef.child(userId).child("withdrawals").child(withdrawalId).child("amount").get().addOnSuccessListener { snapshot ->
            val amount = snapshot.getValue(Double::class.java) ?: 0.0
            if(amount > 0) {
                usersRef.child(userId).child("balance").get().addOnSuccessListener { bSnap ->
                    val balance = bSnap.getValue(Double::class.java) ?: 0.0
                    usersRef.child(userId).child("balance").setValue(balance + amount)
                }
            }
        }
    }
"""

content = content.replace("    // Network Helpers", admin_listeners + "\n    // Network Helpers")

with open("app/src/main/java/com/example/data/FirebaseRealtimeDbManager.kt", "w") as f:
    f.write(content)
