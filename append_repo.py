with open('app/src/main/java/com/example/repository/AppRepository.kt', 'r') as f:
    lines = f.readlines()

for i in range(len(lines)-1, -1, -1):
    if lines[i].strip() == '}':
        lines.insert(i, """
    fun updateUserRole(userId: String, role: String) {
        com.example.data.FirebaseRealtimeDbManager.updateUserRole(userId, role)
    }
""")
        break

with open('app/src/main/java/com/example/repository/AppRepository.kt', 'w') as f:
    f.writelines(lines)
