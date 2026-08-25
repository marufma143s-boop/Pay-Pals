import re

with open("app/src/main/java/com/example/repository/AppRepository.kt", "r") as f:
    content = f.read()

# Update Deposit
deposit_old = """    fun deposit(amount: Double, method: String): Result<Unit> {
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Amount must be greater than zero."))
        }

        val txnId = generateTxnId()
        val newTxn = Transaction(
            id = UUID.randomUUID().toString(),
            title = "Deposit",
            type = TransactionType.DEPOSIT,
            amount = amount,
            dateFormatted = "Today",
            timeFormatted = getCurrentTimeFormatted(),
            transactionId = txnId,
            status = TransactionStatus.COMPLETED,
            note = "${method}"
        )

        _walletState.update { current ->
            current.copy(
                balance = current.balance + amount,
                totalDeposit = current.totalDeposit + amount
            )
        }

        _transactions.update { current ->
            listOf(newTxn) + current
        }

        return Result.success(Unit)
    }"""

deposit_new = """    fun deposit(amount: Double, method: String): Result<Unit> {
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Amount must be greater than zero."))
        }

        val txnId = generateTxnId()
        val id = UUID.randomUUID().toString()
        val newTxn = Transaction(
            id = id,
            title = "Deposit",
            type = TransactionType.DEPOSIT,
            amount = amount,
            dateFormatted = "Today",
            timeFormatted = getCurrentTimeFormatted(),
            transactionId = txnId,
            status = TransactionStatus.PENDING,
            note = "${method}"
        )

        _transactions.update { current ->
            listOf(newTxn) + current
        }
        
        val userId = _userProfile.value.id
        
        val requestMap = mapOf(
            "id" to id,
            "userId" to userId,
            "userName" to _userProfile.value.name,
            "amount" to amount,
            "method" to method,
            "dateFormatted" to "Today",
            "timeFormatted" to getCurrentTimeFormatted(),
            "transactionId" to txnId,
            "status" to "PENDING"
        )
        
        com.example.data.FirebaseRealtimeDbManager.pushDepositToDb(userId, requestMap)

        return Result.success(Unit)
    }"""

content = content.replace(deposit_old, deposit_new)

# Update Withdrawal
withdrawal_old = """    fun withdraw(amount: Double, method: String, accountNumber: String): Result<WithdrawalRecord> {
        val currentBalance = _walletState.value.balance
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Please enter a valid amount."))
        }
        if (amount > currentBalance) {
            return Result.failure(IllegalStateException("Insufficient balance. Available: ${FormatUtils.formatCredits(currentBalance)}"))
        }
        if (accountNumber.isBlank()) {
            return Result.failure(IllegalArgumentException("Please provide your account information."))
        }

        val txnId = generateTxnId()
        val newWithdrawal = WithdrawalRecord(
            id = UUID.randomUUID().toString(),
            amount = amount,
            method = method,
            accountNumber = accountNumber.trim(),
            dateFormatted = "Today",
            timeFormatted = getCurrentTimeFormatted(),
            transactionId = txnId,
            status = TransactionStatus.PENDING
        )

        val newTxn = Transaction(
            id = UUID.randomUUID().toString(),
            title = "Withdrawal",
            type = TransactionType.WITHDRAWAL,
            amount = amount,
            dateFormatted = "Today",
            timeFormatted = getCurrentTimeFormatted(),
            transactionId = txnId,
            status = TransactionStatus.PENDING,
            note = "$method ($accountNumber)"
        )

        _walletState.update { current ->
            current.copy(
                balance = current.balance - amount,
                totalWithdrawal = current.totalWithdrawal + amount
            )
        }

        _withdrawals.update { current ->
            listOf(newWithdrawal) + current
        }

        _transactions.update { current ->
            listOf(newTxn) + current
        }

        return Result.success(newWithdrawal)
    }"""

withdrawal_new = """    fun withdraw(amount: Double, method: String, accountNumber: String): Result<WithdrawalRecord> {
        val currentBalance = _walletState.value.balance
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Please enter a valid amount."))
        }
        if (amount > currentBalance) {
            return Result.failure(IllegalStateException("Insufficient balance. Available: ${FormatUtils.formatCredits(currentBalance)}"))
        }
        if (accountNumber.isBlank()) {
            return Result.failure(IllegalArgumentException("Please provide your account information."))
        }

        val txnId = generateTxnId()
        val id = UUID.randomUUID().toString()
        val newWithdrawal = WithdrawalRecord(
            id = id,
            amount = amount,
            method = method,
            accountNumber = accountNumber.trim(),
            dateFormatted = "Today",
            timeFormatted = getCurrentTimeFormatted(),
            transactionId = txnId,
            status = TransactionStatus.PENDING
        )

        val newTxn = Transaction(
            id = id,
            title = "Withdrawal",
            type = TransactionType.WITHDRAWAL,
            amount = amount,
            dateFormatted = "Today",
            timeFormatted = getCurrentTimeFormatted(),
            transactionId = txnId,
            status = TransactionStatus.PENDING,
            note = "$method ($accountNumber)"
        )

        _walletState.update { current ->
            current.copy(
                balance = current.balance - amount
            )
        }
        
        val userId = _userProfile.value.id

        _withdrawals.update { current ->
            listOf(newWithdrawal) + current
        }

        _transactions.update { current ->
            listOf(newTxn) + current
        }
        
        val requestMap = mapOf(
            "id" to id,
            "userId" to userId,
            "userName" to _userProfile.value.name,
            "amount" to amount,
            "method" to method,
            "accountNumber" to accountNumber.trim(),
            "dateFormatted" to "Today",
            "timeFormatted" to getCurrentTimeFormatted(),
            "transactionId" to txnId,
            "status" to "PENDING"
        )
        com.example.data.FirebaseRealtimeDbManager.pushWithdrawalToDb(userId, requestMap)

        return Result.success(newWithdrawal)
    }"""

content = content.replace(withdrawal_old, withdrawal_new)

with open("app/src/main/java/com/example/repository/AppRepository.kt", "w") as f:
    f.write(content)

