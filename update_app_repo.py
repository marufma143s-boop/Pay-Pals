import re

with open('app/src/main/java/com/example/repository/AppRepository.kt', 'r') as f:
    content = f.read()

# loadSavedSession changes
content = content.replace('val savedBalance = prefs.getFloat("user_balance", -1f)', 
'''val savedBalance = prefs.getFloat("user_balance", -1f)
        val savedRole = prefs.getString("user_role", "USER") ?: "USER"''')

content = content.replace('appliedReferralCode = savedAppliedReferral,\n                isLoggedIn = true',
'''appliedReferralCode = savedAppliedReferral,
                role = if (email == "d@gmail.com") "OWNER" else savedRole,
                isLoggedIn = true''')

# register changes
content = content.replace('"balance" to initialBonus,\n            "totalDeposit" to 0.0',
'''"role" to (if (contactType == "email" && cleanContact == "d@gmail.com") "OWNER" else "USER"),
            "balance" to initialBonus,
            "totalDeposit" to 0.0''')

content = content.replace('.putFloat("user_balance", initialBonus.toFloat())\n                    .apply()',
'''.putFloat("user_balance", initialBonus.toFloat())
                    .putString("user_role", if (contactType == "email" && cleanContact == "d@gmail.com") "OWNER" else "USER")
                    .apply()''')

content = content.replace('appliedReferralCode = referralCodeInput?.takeIf { it.isNotBlank() },\n                    isLoggedIn = true',
'''appliedReferralCode = referralCodeInput?.takeIf { it.isNotBlank() },
                    role = if (contactType == "email" && cleanContact == "d@gmail.com") "OWNER" else "USER",
                    isLoggedIn = true''')

# login changes
content = content.replace('val totalRef = (userData["totalReferralEarnings"] as? Number)?.toDouble() ?: 0.0',
'''val totalRef = (userData["totalReferralEarnings"] as? Number)?.toDouble() ?: 0.0
                val roleFromDb = userData["role"] as? String ?: "USER"
                val finalRole = if (email == "d@gmail.com") "OWNER" else roleFromDb''')

content = content.replace('.putFloat("user_balance", balance.toFloat())\n                    .apply()',
'''.putFloat("user_balance", balance.toFloat())
                    .putString("user_role", finalRole)
                    .apply()''')

content = content.replace('appliedReferralCode = appliedRef,\n                    isLoggedIn = true',
'''appliedReferralCode = appliedRef,
                    role = finalRole,
                    isLoggedIn = true''')

with open('app/src/main/java/com/example/repository/AppRepository.kt', 'w') as f:
    f.write(content)
