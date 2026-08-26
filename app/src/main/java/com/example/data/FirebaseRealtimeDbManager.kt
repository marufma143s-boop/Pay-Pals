package com.example.data

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object FirebaseRealtimeDbManager {
    private const val TAG = "FirebaseDbManager"
    private const val DB_URL = "https://pay-a9be1-default-rtdb.firebaseio.com"

    val database: FirebaseDatabase by lazy {
        try {
            FirebaseDatabase.getInstance(DB_URL).apply {
                try {
                    setPersistenceEnabled(true)
                } catch (e: Exception) {
                    Log.w(TAG, "Persistence already enabled or failed: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get FirebaseDatabase instance: ${e.message}")
            FirebaseDatabase.getInstance()
        }
    }

    private val usersRef get() = database.getReference("users")
    private val contactIndexRef get() = database.getReference("contact_index")
    private val referralIndexRef get() = database.getReference("referral_codes")
    private val campaignsRef get() = database.getReference("campaigns")
    private val withdrawalsRef get() = database.getReference("withdrawals")
    private val popupNoticeRef get() = database.getReference("popup_notice_settings")

    fun sanitizeKey(key: String): String {
        return key.trim().lowercase()
            .replace(".", "_dot_")
            .replace("@", "_at_")
            .replace("+", "_plus_")
            .replace("#", "_hash_")
            .replace("$", "_dollar_")
            .replace("[", "_lb_")
            .replace("]", "_rb_")
            .replace("/", "_slash_")
    }

    fun checkContactExists(contact: String, onResult: (Boolean, String?) -> Unit) {
        val sanitized = sanitizeKey(contact)
        contactIndexRef.child(sanitized).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val existingUserId = snapshot.getValue(String::class.java)
                    onResult(true, existingUserId)
                } else {
                    onResult(false, null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "checkContactExists error: ${error.message}")
                onResult(false, null)
            }
        })
    }

    fun indexReferralCode(referralCode: String, userId: String) {
        if (referralCode.isNotBlank() && userId.isNotBlank()) {
            referralIndexRef.child(referralCode.trim().uppercase()).setValue(userId)
        }
    }

    fun registerUserInDb(
        userId: String,
        userData: Map<String, Any?>,
        contactKey: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val sanitized = sanitizeKey(contactKey)
        val referralCode = (userData["referralCode"] as? String)?.trim()?.uppercase() ?: ""

        usersRef.child(userId).setValue(userData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                contactIndexRef.child(sanitized).setValue(userId)
                if (referralCode.isNotBlank()) {
                    referralIndexRef.child(referralCode).setValue(userId)
                }
                onComplete(true, null)
            } else {
                Log.e(TAG, "registerUserInDb failed: ${task.exception?.message}")
                onComplete(false, task.exception?.localizedMessage ?: "Registration failed")
            }
        }
    }

    fun loginUserInDb(
        contact: String,
        passwordAttempt: String,
        onComplete: (Boolean, Map<String, Any?>?, String?) -> Unit
    ) {
        val sanitized = sanitizeKey(contact)
        
        contactIndexRef.child(sanitized).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userId = snapshot.getValue(String::class.java)
                if (userId != null) {
                    usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnap: DataSnapshot) {
                            val data = userSnap.value as? Map<String, Any?>
                            if (data != null) {
                                val storedPassword = data["password"] as? String ?: ""
                                if (storedPassword == passwordAttempt) {
                                    val refCode = data["referralCode"] as? String
                                    if (!refCode.isNullOrBlank()) {
                                        referralIndexRef.child(refCode.trim().uppercase()).setValue(userId)
                                    }
                                    onComplete(true, data, null)
                                } else {
                                    onComplete(false, null, "Incorrect password. Please try again.")
                                }
                            } else {
                                onComplete(false, null, "User account data not found.")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            onComplete(false, null, error.message)
                        }
                    })
                } else {
                    usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(usersSnap: DataSnapshot) {
                            var matchedUser: Map<String, Any?>? = null
                            val cleanContact = contact.trim().lowercase()

                            for (child in usersSnap.children) {
                                val data = child.value as? Map<String, Any?>
                                if (data != null) {
                                    val email = (data["email"] as? String)?.trim()?.lowercase()
                                    val phone = (data["phone"] as? String)?.trim()?.lowercase()
                                    val contactVal = (data["contactValue"] as? String)?.trim()?.lowercase()
                                    
                                    if (cleanContact == email || cleanContact == phone || cleanContact == contactVal) {
                                        matchedUser = data
                                        break
                                    }
                                }
                            }

                            if (matchedUser != null) {
                                val storedPassword = matchedUser["password"] as? String ?: ""
                                if (storedPassword == passwordAttempt) {
                                    val uid = matchedUser["id"] as? String ?: ""
                                    if (uid.isNotBlank()) {
                                        contactIndexRef.child(sanitized).setValue(uid)
                                        val refCode = matchedUser["referralCode"] as? String
                                        if (!refCode.isNullOrBlank()) {
                                            referralIndexRef.child(refCode.trim().uppercase()).setValue(uid)
                                        }
                                    }
                                    onComplete(true, matchedUser, null)
                                } else {
                                    onComplete(false, null, "Incorrect password. Please try again.")
                                }
                            } else {
                                onComplete(false, null, "No account found with this phone or email.")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            onComplete(false, null, error.message)
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "loginUserInDb error: ${error.message}")
                onComplete(false, null, error.message)
            }
        })
    }

    private val adminSettingsRef get() = database.getReference("admin_settings")
    private val adminDepositsRef get() = database.getReference("admin_deposit_request")

    // Admin Settings Realtime Listener
    fun attachAdminSettingsListener(onDataChange: (Map<String, Any?>) -> Unit) {
        adminSettingsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.value as? Map<String, Any?>
                if (data != null) {
                    onDataChange(data)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "attachAdminSettingsListener onCancelled: ${error.message}")
            }
        })
    }

    fun updateAdminSetting(key: String, value: Any) {
        adminSettingsRef.child(key).setValue(value)
    }

    fun applyReferralBonusInCloud(
        refereeUserId: String,
        refereeName: String,
        cleanCode: String,
        bonusReward: Double,
        onComplete: (Boolean, String?, String?) -> Unit // (success, referrerName, errorMsg)
    ) {
        val upperCode = cleanCode.trim().uppercase()
        val timeFormatted = SimpleDateFormat("hh:mm a", Locale.US).format(Date())

        // 1. Check referral code owner
        referralIndexRef.child(upperCode).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val referrerUserId = snapshot.getValue(String::class.java)
                if (referrerUserId != null) {
                    if (referrerUserId == refereeUserId) {
                        onComplete(false, null, "You cannot apply your own referral code.")
                        return
                    }

                    // Reward Referrer in Cloud
                    usersRef.child(referrerUserId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(referrerSnap: DataSnapshot) {
                            val referrerData = referrerSnap.value as? Map<String, Any?>
                            val referrerName = referrerData?.get("name") as? String ?: "Inviter"
                            val curBal = (referrerData?.get("balance") as? Number)?.toDouble() ?: 0.0
                            val curRefEarn = (referrerData?.get("totalReferralEarnings") as? Number)?.toDouble() ?: 0.0

                            // Update Referrer
                            usersRef.child(referrerUserId).updateChildren(
                                mapOf(
                                    "balance" to (curBal + bonusReward),
                                    "totalReferralEarnings" to (curRefEarn + bonusReward)
                                )
                            )

                            // Add Transaction for Referrer
                            val refTxnId = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}"
                            val referrerTxn = mapOf(
                                "id" to UUID.randomUUID().toString(),
                                "title" to "Referral Bonus",
                                "type" to "REFERRAL_REWARD",
                                "amount" to bonusReward,
                                "dateFormatted" to "Today",
                                "timeFormatted" to timeFormatted,
                                "transactionId" to refTxnId,
                                "status" to "COMPLETED",
                                "note" to "Referral reward from $refereeName"
                            )
                            pushUserTransaction(referrerUserId, referrerTxn)

                            // Also record in Referrer's referrals list
                            val newRefRecord = mapOf(
                                "id" to UUID.randomUUID().toString(),
                                "friendName" to refereeName,
                                "friendAvatarUrl" to "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80",
                                "joinedDateFormatted" to "Today, $timeFormatted",
                                "rewardAmount" to bonusReward,
                                "status" to "COMPLETED"
                            )
                            usersRef.child(referrerUserId).child("referrals").push().setValue(newRefRecord)

                            // Update Referee
                            usersRef.child(refereeUserId).child("appliedReferralCode").setValue(upperCode)

                            onComplete(true, referrerName, null)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            onComplete(false, null, error.message)
                        }
                    })
                } else {
                    // Fallback: search all users to see if any user has this referralCode
                    usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(usersSnap: DataSnapshot) {
                            var foundReferrerId: String? = null
                            var foundReferrerName: String = "Inviter"
                            var foundBal = 0.0
                            var foundRefEarn = 0.0

                            for (child in usersSnap.children) {
                                val data = child.value as? Map<String, Any?>
                                if (data != null) {
                                    val rCode = (data["referralCode"] as? String)?.trim()?.uppercase()
                                    if (rCode == upperCode) {
                                        foundReferrerId = child.key ?: data["id"] as? String
                                        foundReferrerName = data["name"] as? String ?: "Inviter"
                                        foundBal = (data["balance"] as? Number)?.toDouble() ?: 0.0
                                        foundRefEarn = (data["totalReferralEarnings"] as? Number)?.toDouble() ?: 0.0
                                        break
                                    }
                                }
                            }

                            if (foundReferrerId != null) {
                                if (foundReferrerId == refereeUserId) {
                                    onComplete(false, null, "You cannot apply your own referral code.")
                                    return
                                }

                                // Index it for future lookups
                                referralIndexRef.child(upperCode).setValue(foundReferrerId)

                                // Reward Referrer
                                usersRef.child(foundReferrerId).updateChildren(
                                    mapOf(
                                        "balance" to (foundBal + bonusReward),
                                        "totalReferralEarnings" to (foundRefEarn + bonusReward)
                                    )
                                )

                                val refTxnId = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}"
                                val referrerTxn = mapOf(
                                    "id" to UUID.randomUUID().toString(),
                                    "title" to "Referral Bonus",
                                    "type" to "REFERRAL_REWARD",
                                    "amount" to bonusReward,
                                    "dateFormatted" to "Today",
                                    "timeFormatted" to timeFormatted,
                                    "transactionId" to refTxnId,
                                    "status" to "COMPLETED",
                                    "note" to "Referral reward from $refereeName"
                                )
                                pushUserTransaction(foundReferrerId, referrerTxn)

                                // Update Referee
                                usersRef.child(refereeUserId).child("appliedReferralCode").setValue(upperCode)

                                onComplete(true, foundReferrerName, null)
                            } else {
                                onComplete(false, null, "Invalid referral code. Please check and try again.")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            onComplete(false, null, error.message)
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(false, null, error.message)
            }
        })
    }

    fun syncUserWallet(userId: String, balance: Double, totalDeposit: Double, totalWithdrawal: Double, totalReferral: Double) {
        if (userId.isBlank()) return
        val map = mapOf(
            "balance" to balance,
            "totalDeposit" to totalDeposit,
            "totalWithdrawal" to totalWithdrawal,
            "totalReferralEarnings" to totalReferral
        )
        usersRef.child(userId).updateChildren(map)
    }

    fun pushUserTransaction(userId: String, transactionMap: Map<String, Any?>) {
        val txnId = transactionMap["id"] as? String ?: return
        if (userId.isNotBlank()) {
            usersRef.child(userId).child("transactions").child(txnId).setValue(transactionMap)
        }
    }

    fun pushCampaignToDb(userId: String, campaignMap: Map<String, Any?>) {
        val campaignId = campaignMap["id"] as? String ?: return
        campaignsRef.child(campaignId).setValue(campaignMap)
        if (userId.isNotBlank()) {
            usersRef.child(userId).child("campaigns").child(campaignId).setValue(campaignMap)
        }
    }

    fun pushCampaignToDb(campaignMap: Map<String, Any?>) {
        val campaignId = campaignMap["id"] as? String ?: return
        val userId = campaignMap["userId"] as? String ?: ""
        pushCampaignToDb(userId, campaignMap)
    }

    fun pushWithdrawalToDb(userId: String, withdrawalMap: Map<String, Any?>) {
        val id = withdrawalMap["id"] as? String ?: return
        adminDepositsRef.parent?.child("admin_withdrawal_request")?.child(id)?.setValue(withdrawalMap)
        withdrawalsRef.child(id).setValue(withdrawalMap)
        if (userId.isNotBlank()) {
            usersRef.child(userId).child("withdrawals").child(id).setValue(withdrawalMap)
            usersRef.child(userId).child("transactions").child(id).setValue(withdrawalMap)
        }
    }
    
    fun pushDepositToDb(userId: String, depositMap: Map<String, Any?>) {
        val id = depositMap["id"] as? String ?: return
        adminDepositsRef.child(id).setValue(depositMap)
        if (userId.isNotBlank()) {
            usersRef.child(userId).child("transactions").child(id).setValue(depositMap)
        }
    }

    fun attachUserRealtimeListener(userId: String, onDataChange: (Map<String, Any?>) -> Unit): ValueEventListener? {
        if (userId.isBlank()) return null
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.value as? Map<String, Any?>
                if (data != null) {
                    onDataChange(data)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "attachUserRealtimeListener onCancelled: ${error.message}")
            }
        }
        usersRef.child(userId).addValueEventListener(listener)
        return listener
    }

    fun attachUserCampaignsListener(userId: String, onDataChange: (List<Map<String, Any?>>) -> Unit): ValueEventListener? {
        if (userId.isBlank()) return null
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Map<String, Any?>>()
                for (child in snapshot.children) {
                    val map = child.value as? Map<String, Any?>
                    if (map != null) {
                        list.add(map)
                    }
                }
                onDataChange(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "attachUserCampaignsListener onCancelled: ${error.message}")
            }
        }
        usersRef.child(userId).child("campaigns").addValueEventListener(listener)
        return listener
    }

    fun attachUserReferralsListener(userId: String, onDataChange: (List<Map<String, Any?>>) -> Unit): ValueEventListener? {
        if (userId.isBlank()) return null
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Map<String, Any?>>()
                for (child in snapshot.children) {
                    val map = child.value as? Map<String, Any?>
                    if (map != null) {
                        list.add(map)
                    }
                }
                onDataChange(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "attachUserReferralsListener onCancelled: ${error.message}")
            }
        }
        usersRef.child(userId).child("referrals").addValueEventListener(listener)
        return listener
    }

    fun attachUserTransactionsListener(userId: String, onDataChange: (List<Map<String, Any?>>) -> Unit): ValueEventListener? {
        if (userId.isBlank()) return null
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Map<String, Any?>>()
                for (child in snapshot.children) {
                    val map = child.value as? Map<String, Any?>
                    if (map != null) {
                        list.add(map)
                    }
                }
                onDataChange(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "attachUserTransactionsListener onCancelled: ${error.message}")
            }
        }
        usersRef.child(userId).child("transactions").addValueEventListener(listener)
        return listener
    }

    fun detachUserListener(userId: String, listener: ValueEventListener?) {
        if (listener != null && userId.isNotBlank()) {
            usersRef.child(userId).removeEventListener(listener)
        }
    }

    // Admin Listeners

    fun attachAdminUsersListener(onDataChange: (Map<String, Any?>) -> Unit) {
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.value as? Map<String, Any?> ?: emptyMap()
                onDataChange(data)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun attachAdminCampaignsListener(onDataChange: (Map<String, Any?>) -> Unit) {
        campaignsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.value as? Map<String, Any?> ?: emptyMap()
                onDataChange(data)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    
    fun updateUserBlockStatus(userId: String, isBlocked: Boolean) {
        usersRef.child(userId).child("isBlocked").setValue(isBlocked)
    }

    fun updateUser(userId: String, name: String, email: String, balance: Double) {
        usersRef.child(userId).updateChildren(mapOf(
            "fullName" to name,
            "email" to email,
            "balance" to balance
        ))
    }

    fun deleteUser(userId: String) {
        usersRef.child(userId).removeValue()
    }
    
    fun updateCampaignViews(campaignId: String, completedViews: Int, status: String) {
        val updates = mapOf<String, Any?>(
            "completedViews" to completedViews,
            "status" to status
        )
        campaignsRef.child(campaignId).updateChildren(updates)
        campaignsRef.child(campaignId).child("userId").get().addOnSuccessListener { snap ->
            val userId = snap.getValue(String::class.java)
            if (!userId.isNullOrBlank()) {
                usersRef.child(userId).child("campaigns").child(campaignId).updateChildren(updates)
            }
        }
    }

    fun updateCampaignStatus(campaignId: String, status: String, rejectReason: String? = null) {
        val updates = mutableMapOf<String, Any?>("status" to status)
        if (rejectReason != null) {
            updates["rejectReason"] = rejectReason
        }
        campaignsRef.child(campaignId).updateChildren(updates)
        campaignsRef.child(campaignId).child("userId").get().addOnSuccessListener { snap ->
            val userId = snap.getValue(String::class.java)
            if (!userId.isNullOrBlank()) {
                usersRef.child(userId).child("campaigns").child(campaignId).updateChildren(updates)
            }
        }
    }
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

    private val paidPackagesRef get() = database.getReference("paid_packages")
    private val packageOrdersRef get() = database.getReference("package_orders")

    fun attachPaidPackagesListener(onDataChange: (List<Map<String, Any?>>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Map<String, Any?>>()
                for (child in snapshot.children) {
                    val map = child.value as? Map<String, Any?>
                    if (map != null) {
                        list.add(map)
                    }
                }
                onDataChange(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "attachPaidPackagesListener onCancelled: ${error.message}")
            }
        }
        paidPackagesRef.addValueEventListener(listener)
        return listener
    }

    fun savePaidPackage(packageMap: Map<String, Any?>) {
        val id = packageMap["id"] as? String ?: UUID.randomUUID().toString()
        paidPackagesRef.child(id).setValue(packageMap)
    }

    fun deletePaidPackage(packageId: String) {
        if (packageId.isNotBlank()) {
            paidPackagesRef.child(packageId).removeValue()
        }
    }

    fun pushPackageOrder(userId: String, orderMap: Map<String, Any?>) {
        val orderId = orderMap["id"] as? String ?: return
        packageOrdersRef.child(orderId).setValue(orderMap)
        if (userId.isNotBlank()) {
            usersRef.child(userId).child("package_orders").child(orderId).setValue(orderMap)
        }
    }

    fun attachUserPackageOrdersListener(userId: String, onDataChange: (List<Map<String, Any?>>) -> Unit): ValueEventListener? {
        if (userId.isBlank()) return null
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Map<String, Any?>>()
                for (child in snapshot.children) {
                    val map = child.value as? Map<String, Any?>
                    if (map != null) {
                        list.add(map)
                    }
                }
                onDataChange(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "attachUserPackageOrdersListener onCancelled: ${error.message}")
            }
        }
        usersRef.child(userId).child("package_orders").addValueEventListener(listener)
        return listener
    }

    fun attachAdminPackageOrdersListener(onDataChange: (Map<String, Any?>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.value as? Map<String, Any?> ?: emptyMap()
                onDataChange(data)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "attachAdminPackageOrdersListener onCancelled: ${error.message}")
            }
        }
        packageOrdersRef.addValueEventListener(listener)
        return listener
    }

    fun updatePackageOrderStatus(orderId: String, status: String, rejectReason: String? = null) {
        val updates = mutableMapOf<String, Any?>("status" to status)
        if (rejectReason != null) {
            updates["rejectReason"] = rejectReason
        }
        packageOrdersRef.child(orderId).updateChildren(updates)
        packageOrdersRef.child(orderId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orderData = snapshot.value as? Map<String, Any?>
                val userId = orderData?.get("userId") as? String
                if (!userId.isNullOrBlank()) {
                    usersRef.child(userId).child("package_orders").child(orderId).updateChildren(updates)
                    // If rejected, refund the price to user balance
                    if (status.equals("REJECTED", ignoreCase = true)) {
                        val price = (orderData["price"] as? Number)?.toDouble() ?: 0.0
                        if (price > 0.0) {
                            usersRef.child(userId).child("balance").get().addOnSuccessListener { balSnap ->
                                val curBal = balSnap.getValue(Double::class.java) ?: 0.0
                                usersRef.child(userId).child("balance").setValue(curBal + price)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private val developerInfoRef get() = database.getReference("app_settings/developer_info")
    private val supportThreadsRef get() = database.getReference("support_threads")
    private val supportMessagesRef get() = database.getReference("support_messages")

    fun saveDeveloperInfo(data: Map<String, Any?>) {
        developerInfoRef.setValue(data)
    }

    fun attachDeveloperInfoListener(onDataChange: (Map<String, Any?>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.value as? Map<String, Any?> ?: emptyMap()
                onDataChange(data)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "attachDeveloperInfoListener onCancelled: ${error.message}")
            }
        }
        developerInfoRef.addValueEventListener(listener)
        return listener
    }

    fun updateUserAvatar(userId: String, avatarBase64: String) {
        val updates = mapOf<String, Any?>("avatarBase64" to avatarBase64)
        usersRef.child(userId).updateChildren(updates)
    }

    fun sendSupportMessage(
        userId: String,
        messageId: String,
        messageData: Map<String, Any?>,
        threadSummary: Map<String, Any?>
    ) {
        supportMessagesRef.child(userId).child(messageId).setValue(messageData)
        supportThreadsRef.child(userId).updateChildren(threadSummary)
    }

    fun deleteSupportMessage(userId: String, messageId: String) {
        supportMessagesRef.child(userId).child(messageId).child("isDeleted").setValue(true)
    }

    fun attachUserSupportMessagesListener(
        userId: String,
        onDataChange: (Map<String, Any?>) -> Unit
    ): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.value as? Map<String, Any?> ?: emptyMap()
                onDataChange(data)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "attachUserSupportMessagesListener onCancelled: ${error.message}")
            }
        }
        supportMessagesRef.child(userId).addValueEventListener(listener)
        return listener
    }

    fun attachAllSupportThreadsListener(
        onDataChange: (Map<String, Any?>) -> Unit
    ): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.value as? Map<String, Any?> ?: emptyMap()
                onDataChange(data)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "attachAllSupportThreadsListener onCancelled: ${error.message}")
            }
        }
        supportThreadsRef.addValueEventListener(listener)
        return listener
    }

    fun updateUserRole(userId: String, role: String) {
        val updates = mapOf<String, Any?>(
            "role" to role
        )
        database.reference.child("users").child(userId).updateChildren(updates)
    }

    fun updateUserAdminPermissions(userId: String, permissions: Map<String, Boolean>) {
        database.reference.child("users").child(userId).child("permissions").setValue(permissions)
    }

    private val serviceControlRef get() = database.getReference("app_settings/service_control")
    private val maintenanceRef get() = database.getReference("app_settings/maintenance")

    fun saveServiceControlSettings(data: Map<String, Any?>) {
        serviceControlRef.setValue(data)
    }

    fun updateSingleService(serviceKey: String, isDisabled: Boolean, reason: String) {
        serviceControlRef.child(serviceKey).setValue(
            mapOf(
                "key" to serviceKey,
                "isDisabled" to isDisabled,
                "reason" to reason
            )
        )
    }

    fun attachServiceControlListener(onDataChange: (Map<String, Any?>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.value as? Map<String, Any?> ?: emptyMap()
                onDataChange(data)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "attachServiceControlListener onCancelled: ${error.message}")
            }
        }
        serviceControlRef.addValueEventListener(listener)
        return listener
    }

    fun saveMaintenanceSettings(data: Map<String, Any?>) {
        maintenanceRef.setValue(data)
    }

    fun attachMaintenanceListener(onDataChange: (Map<String, Any?>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.value as? Map<String, Any?> ?: emptyMap()
                onDataChange(data)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "attachMaintenanceListener onCancelled: ${error.message}")
            }
        }
        maintenanceRef.addValueEventListener(listener)
        return listener
    }

    fun savePopupNoticeSettings(data: Map<String, Any?>) {
        popupNoticeRef.setValue(data)
    }

    fun attachPopupNoticeListener(onDataChange: (Map<String, Any?>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.value as? Map<String, Any?> ?: emptyMap()
                onDataChange(data)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "attachPopupNoticeListener onCancelled: ${error.message}")
            }
        }
        popupNoticeRef.addValueEventListener(listener)
        return listener
    }
}
