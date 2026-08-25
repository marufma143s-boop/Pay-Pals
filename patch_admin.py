with open("app/src/main/java/com/example/screens/admin/AdminDashboardScreen.kt", "r") as f:
    content = f.read()

content = content.replace("kotlinx.coroutines.GlobalScope.launch", "coroutineScope.launch")

with open("app/src/main/java/com/example/screens/admin/AdminDashboardScreen.kt", "w") as f:
    f.write(content)
