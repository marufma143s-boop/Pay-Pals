package com.example.utils

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

object FormatUtils {
    private val decimalFormat = DecimalFormat("#,##0.00")
    private val integerFormat = DecimalFormat("#,##0")

    fun formatCredits(amount: Double): String {
        return "🪙 " + integerFormat.format(amount)
    }

    fun formatCreditsOnly(amount: Double): String {
        return integerFormat.format(amount)
    }

    fun formatEquivalentTaka(credits: Double): String {
        val taka = credits / 100.0
        return "= ৳" + decimalFormat.format(taka)
    }

    fun formatTaka(amount: Double, withDecimals: Boolean = true): String {
        return if (withDecimals) {
            "৳" + decimalFormat.format(amount)
        } else {
            "৳" + integerFormat.format(amount)
        }
    }

    fun formatCount(count: Int): String {
        return integerFormat.format(count)
    }

    fun isValidUrl(url: String): Boolean {
        if (url.isBlank()) return false
        val trimmed = url.trim()
        val urlRegex = Regex(
            "^(https?://)?([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(/.*)?$",
            RegexOption.IGNORE_CASE
        )
        return urlRegex.matches(trimmed)
    }

    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val emailRegex = Regex(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
        )
        return emailRegex.matches(email.trim())
    }

    fun isValidPhone(phone: String): Boolean {
        val cleaned = phone.replace(" ", "").replace("-", "")
        return cleaned.length in 10..15 && cleaned.all { it.isDigit() || it == '+' }
    }
}
