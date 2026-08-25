import re

with open("app/src/main/java/com/example/screens/admin/AdminScreens.kt", "r") as f:
    content = f.read()

old_card = """            if (isPending || isRunning) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = { showRejectDialog = true }) { 
                        Icon(Icons.Default.Close, contentDescription = "Reject", tint = ErrorRed) 
                    }
                    if (isPending) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { repository.updateCampaignStatus(id, "RUNNING") }) { 
                            Icon(Icons.Default.PlayArrow, contentDescription = "Start Running", tint = SuccessGreen) 
                        }
                    }
                }
            }"""

new_card = """            if (isPending || isRunning) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = { showRejectDialog = true }) { 
                        Icon(Icons.Default.Close, contentDescription = "Reject", tint = ErrorRed) 
                    }
                    if (isPending) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { repository.updateCampaignStatus(id, "RUNNING") }) { 
                            Icon(Icons.Default.PlayArrow, contentDescription = "Start Running", tint = SuccessGreen) 
                        }
                    }
                    if (isRunning) {
                        Spacer(modifier = Modifier.width(8.dp))
                        // Simulate adding views
                        Button(onClick = {
                            val currentViews = (campaign["completedViews"] as? Number)?.toInt() ?: 0
                            val targetViews = (campaign["targetViews"] as? Number)?.toInt() ?: 0
                            val newViews = currentViews + 100
                            if (newViews >= targetViews) {
                                repository.updateCampaignViews(id, targetViews, "COMPLETED")
                            } else {
                                repository.updateCampaignViews(id, newViews, "RUNNING")
                            }
                        }) {
                            Text("Add 100 Views")
                        }
                    }
                }
            }"""

content = content.replace(old_card, new_card)

with open("app/src/main/java/com/example/screens/admin/AdminScreens.kt", "w") as f:
    f.write(content)
