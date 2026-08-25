import re

# Fix CreateCampaignScreen
with open("app/src/main/java/com/example/screens/campaign/CreateCampaignScreen.kt", "r") as f:
    cc = f.read()
cc = cc.replace("name = \"Custom\",", "")
cc = cc.replace("isPopular = false", "description = \"Custom target\",\n                            isPopular = false")
with open("app/src/main/java/com/example/screens/campaign/CreateCampaignScreen.kt", "w") as f:
    f.write(cc)

# Fix DepositScreen
with open("app/src/main/java/com/example/screens/deposit/DepositScreen.kt", "r") as f:
    dc = f.read()

import re
old_methods_regex = r"val methods = listOf\([\s\S]*?}\n\s*}"
new_methods_block = """depositMethods.forEach { method ->
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
                                .testTag("payment_method_${method.replace(" ", "_")}"),
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
                                }
                                if (isSelected) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = PurplePrimary)
                                }
                            }
                        }
                    }"""
dc = re.sub(old_methods_regex, new_methods_block, dc)
with open("app/src/main/java/com/example/screens/deposit/DepositScreen.kt", "w") as f:
    f.write(dc)
