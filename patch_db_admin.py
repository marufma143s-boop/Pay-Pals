with open("app/src/main/java/com/example/data/FirebaseRealtimeDbManager.kt", "r") as f:
    content = f.read()

new_admin_listeners = """
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
    
    fun updateCampaignStatus(campaignId: String, status: String, rejectReason: String? = null) {
        val updates = mutableMapOf<String, Any?>("status" to status)
        if (rejectReason != null) {
            updates["rejectReason"] = rejectReason
        }
        campaignsRef.child(campaignId).updateChildren(updates)
    }
"""

if "fun attachAdminUsersListener" not in content:
    content = content.replace("    // Admin Listeners\n", "    // Admin Listeners\n" + new_admin_listeners)

with open("app/src/main/java/com/example/data/FirebaseRealtimeDbManager.kt", "w") as f:
    f.write(content)
