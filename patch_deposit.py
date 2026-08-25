import re

with open("app/src/main/java/com/example/screens/deposit/DepositScreen.kt", "r") as f:
    content = f.read()

# Replace PaymentMethod usages with String
content = content.replace("var selectedMethod by remember { mutableStateOf(PaymentMethod.BKASH) }", "val depositMethods by repository.depositMethods.collectAsState()\n    var selectedMethod by remember { mutableStateOf(\"bKash\") }\n    \n    LaunchedEffect(depositMethods) {\n        if (depositMethods.isNotEmpty() && !depositMethods.contains(selectedMethod)) {\n            selectedMethod = depositMethods.first()\n        }\n    }")

old_methods_block = """                    val methods = listOf(
                        Triple(PaymentMethod.BKASH, Icons.Filled.PhoneAndroid, "Instant Mobile Banking"),
                        Triple(PaymentMethod.BANK, Icons.Filled.AccountBalance, "Direct Bank Wire"),
                        Triple(PaymentMethod.OTHER, Icons.Filled.CreditCard, "Cards & Digital Wallets")
                    )
                    methods.forEach { (method, icon, desc) ->
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
                                .testTag("payment_method_${method.id}"),
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
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = method.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${method.subtitle} • $desc",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                }
                            }
                        }
                    }"""

new_methods_block = """                    depositMethods.forEach { method ->
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
                                .testTag("payment_method_${method}"),
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = method,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Deposit securely via $method",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = PurplePrimary)
                                }
                            }
                        }
                    }"""

content = content.replace(old_methods_block, new_methods_block)

# Fix imports
content = content.replace("import com.example.model.PaymentMethod", "import androidx.compose.runtime.LaunchedEffect")

with open("app/src/main/java/com/example/screens/deposit/DepositScreen.kt", "w") as f:
    f.write(content)

