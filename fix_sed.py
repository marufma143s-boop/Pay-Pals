with open("app/src/main/java/com/example/screens/admin/AdminDashboardScreen.kt", "r") as f:
    content = f.read()

content = content.replace("import kotlinx.coroutines.launch\nimport kotlinx.coroutines.GlobalScope\npackage com.example.screens.admin", "package com.example.screens.admin\n\nimport kotlinx.coroutines.launch\nimport kotlinx.coroutines.GlobalScope")

with open("app/src/main/java/com/example/screens/admin/AdminDashboardScreen.kt", "w") as f:
    f.write(content)
