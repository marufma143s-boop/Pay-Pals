import re

with open("app/src/main/java/com/example/repository/AppRepository.kt", "r") as f:
    content = f.read()

old_campaign_regex = r"val newCampaign = Campaign\([\s\S]*?status = CampaignStatus.RUNNING,[\s\S]*?return Result.success\(newCampaign\)"

new_campaign = """val newCampaign = Campaign(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            networkType = cleanNetwork,
            targetLink = if (targetLink.startsWith("http://") || targetLink.startsWith("https://")) targetLink.trim() else "https://${targetLink.trim()}",
            packagePrice = pkg.price,
            targetViews = pkg.targetViews,
            completedViews = 0,
            status = CampaignStatus.PENDING,
            createdDate = "Today"
        )

        val txnId = generateTxnId()
        val newTxn = Transaction(
            id = UUID.randomUUID().toString(),
            title = "Campaign Payment",
            type = TransactionType.CAMPAIGN_PAYMENT,
            amount = pkg.price,
            dateFormatted = "Today",
            timeFormatted = getCurrentTimeFormatted(),
            transactionId = txnId,
            status = TransactionStatus.COMPLETED,
            note = "${title.trim()} [${newCampaign.networkDisplayName}] (${FormatUtils.formatCount(pkg.targetViews)} Views)"
        )

        _walletState.update { current ->
            current.copy(balance = current.balance - pkg.price)
        }

        _campaigns.update { current ->
            listOf(newCampaign) + current
        }

        _totalCampaignCount.update { it + 1 }

        _transactions.update { current ->
            listOf(newTxn) + current
        }
        
        // Push to Firebase
        val campaignMap = mapOf(
            "id" to newCampaign.id,
            "title" to newCampaign.title,
            "networkType" to newCampaign.networkType,
            "targetLink" to newCampaign.targetLink,
            "packagePrice" to newCampaign.packagePrice,
            "targetViews" to newCampaign.targetViews,
            "completedViews" to newCampaign.completedViews,
            "status" to newCampaign.status.name,
            "createdDate" to newCampaign.createdDate,
            "userId" to _userProfile.value.id
        )
        com.example.data.FirebaseRealtimeDbManager.pushCampaignToDb(campaignMap)
        
        val txnMap = mapOf(
            "id" to newTxn.id,
            "title" to newTxn.title,
            "type" to newTxn.type.name,
            "amount" to newTxn.amount,
            "dateFormatted" to newTxn.dateFormatted,
            "timeFormatted" to newTxn.timeFormatted,
            "transactionId" to newTxn.transactionId,
            "status" to newTxn.status.name,
            "note" to newTxn.note
        )
        com.example.data.FirebaseRealtimeDbManager.pushUserTransaction(_userProfile.value.id, txnMap)
        
        com.example.data.FirebaseRealtimeDbManager.syncUserWallet(
            _userProfile.value.id,
            _walletState.value.balance,
            _walletState.value.totalDeposit,
            _walletState.value.totalWithdrawal,
            _walletState.value.totalReferralEarnings
        )

        return Result.success(newCampaign)"""

content = re.sub(old_campaign_regex, new_campaign, content)
with open("app/src/main/java/com/example/repository/AppRepository.kt", "w") as f:
    f.write(content)
