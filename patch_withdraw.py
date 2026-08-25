import re

with open("app/src/main/java/com/example/screens/withdraw/WithdrawScreen.kt", "r") as f:
    content = f.read()

# Replace PaymentMethod usages with String
content = content.replace("var selectedMethod by remember { mutableStateOf(\"Mobile Banking\") }", "val withdrawalMethods by repository.withdrawalMethods.collectAsState()\n    var selectedMethod by remember { mutableStateOf(\"bKash\") }\n    \n    LaunchedEffect(withdrawalMethods) {\n        if (withdrawalMethods.isNotEmpty() && !withdrawalMethods.contains(selectedMethod)) {\n            selectedMethod = withdrawalMethods.first()\n        }\n    }")

old_methods_block = """                    val methods = listOf(
                        Pair("Mobile Banking", Icons.Filled.PhoneAndroid),
                        Pair("Bank Transfer", Icons.Filled.AccountBalance)
                    )
                    methods.forEach { (method, icon) ->
                        val isSelected = selectedMethod == method
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedMethod = method }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) PurplePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .testTag("withdrawal_method_${method.replace(" ", "_").lowercase()}"),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) PurplePrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(icon, contentDescription=null, modifier=Modifier.size(20.dp), tint = Color.White)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = method,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = PurplePrimary)
                                }
                            }
                        }
                    }"""

new_methods_block = """                    withdrawalMethods.forEach { method ->
                        val isSelected = selectedMethod == method
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedMethod = method }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) PurplePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .testTag("withdrawal_method_${method}"),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) PurplePrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.AccountBalance, contentDescription=null, modifier=Modifier.size(20.dp), tint = Color.White)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = method,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = PurplePrimary)
                                }
                            }
                        }
                    }"""

content = content.replace(old_methods_block, new_methods_block)

# Add import for LaunchedEffect
content = content.replace("import com.example.components.SubScreenTopBar", "import androidx.compose.runtime.LaunchedEffect\nimport com.example.components.SubScreenTopBar")

with open("app/src/main/java/com/example/screens/withdraw/WithdrawScreen.kt", "w") as f:
    f.write(content)

