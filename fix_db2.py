with open('app/src/main/java/com/example/data/FirebaseRealtimeDbManager.kt', 'r') as f:
    content = f.read()

content = content.replace('database.child("users")', 'database.reference.child("users")')

with open('app/src/main/java/com/example/data/FirebaseRealtimeDbManager.kt', 'w') as f:
    f.write(content)
