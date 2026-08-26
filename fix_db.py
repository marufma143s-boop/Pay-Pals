with open('app/src/main/java/com/example/data/FirebaseRealtimeDbManager.kt', 'r') as f:
    lines = f.readlines()

# find the start of the messed up `    fun updateUserRole`
idx = -1
for i in range(len(lines)-1, -1, -1):
    if 'fun updateUserRole' in lines[i]:
        idx = i
        break

# remove the messed up block
del lines[idx:]

# delete the last `}`
for i in range(len(lines)-1, -1, -1):
    if '}' in lines[i]:
        del lines[i]
        break

lines.append("""
    fun updateUserRole(userId: String, role: String) {
        val updates = mapOf(
            "role" to role
        )
        database.child("users").child(userId).updateChildren(updates)
    }
}
""")

with open('app/src/main/java/com/example/data/FirebaseRealtimeDbManager.kt', 'w') as f:
    f.writelines(lines)
