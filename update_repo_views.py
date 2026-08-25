with open("app/src/main/java/com/example/data/FirebaseRealtimeDbManager.kt", "r") as f:
    content = f.read()

new_admin_func = """    fun updateCampaignViews(campaignId: String, completedViews: Int, status: String) {
        campaignsRef.child(campaignId).updateChildren(mapOf(
            "completedViews" to completedViews,
            "status" to status
        ))
    }
"""

if "updateCampaignViews" not in content:
    content = content.replace("fun updateCampaignStatus", new_admin_func + "\n    fun updateCampaignStatus")

with open("app/src/main/java/com/example/data/FirebaseRealtimeDbManager.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/repository/AppRepository.kt", "r") as f:
    content2 = f.read()

new_repo_func = """    fun updateCampaignViews(campaignId: String, completedViews: Int, status: String) {
        com.example.data.FirebaseRealtimeDbManager.updateCampaignViews(campaignId, completedViews, status)
    }
"""

if "updateCampaignViews" not in content2:
    content2 = content2.replace("fun updateCampaignStatus", new_repo_func + "\n    fun updateCampaignStatus")

with open("app/src/main/java/com/example/repository/AppRepository.kt", "w") as f:
    f.write(content2)
