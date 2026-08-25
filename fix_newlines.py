import re
with open("app/src/main/java/com/example/screens/admin/AdminScreens.kt", "r") as f:
    text = f.read()

text = text.replace("import", "\nimport").replace("@Composable", "\n@Composable\n").replace("fun ", "\nfun ").replace("val ", "\nval ").replace("var ", "\nvar ").replace("if ", "\nif ").replace("Card(", "\nCard(").replace("Column(", "\nColumn(").replace("Row(", "\nRow(").replace("Spacer(", "\nSpacer(").replace("Text(", "\nText(")
with open("app/src/main/java/com/example/screens/admin/AdminScreens.kt", "w") as f:
    f.write(text)
