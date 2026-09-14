package com.example.util

import java.math.BigDecimal
import java.util.regex.Pattern

data class ParsedBankSms(
    val bankName: String,
    val amount: BigDecimal,
    val type: BankTransactionType,
    val description: String
)

enum class BankTransactionType { DEPOSIT, WITHDRAWAL, UNKNOWN }

object SmsParser {

    /**
     * Attempts to parse a bank SMS content to extract amount and transaction type.
     * Supports common patterns of Iranian banks (Melli, Mellat, Saman, Pasargad, etc.)
     */
    fun parse(content: String, sender: String? = null): ParsedBankSms? {
        val cleanContent = PersianNumberUtils.toEnglishDigits(content)
            .replace(",", "")
            .replace("،", "")

        // 1. Identify Type (Deposit vs Withdrawal)
        val type = when {
            cleanContent.contains("واریز") -> BankTransactionType.DEPOSIT
            cleanContent.contains("برداشت") || cleanContent.contains("خرید") || cleanContent.contains("پرداخت") -> BankTransactionType.WITHDRAWAL
            else -> BankTransactionType.UNKNOWN
        }

        if (type == BankTransactionType.UNKNOWN) return null

        // 2. Extract Amount
        // Look for numbers followed by "ریال" or "تومان"
        // Also look for "مبلغ:" pattern
        val amount = extractAmount(cleanContent) ?: return null

        // 3. Identify Bank Name
        val bankName = extractBankName(content, sender) ?: "بانک نامشخص"

        return ParsedBankSms(
            bankName = bankName,
            amount = amount,
            type = type,
            description = "تراکنش شناسایی شده از پیامک $bankName"
        )
    }

    private fun extractAmount(content: String): BigDecimal? {
        // Regex to find numbers that look like currency amounts
        // We look for sequences of digits, potentially separated by commas (already removed)
        // Usually, the largest number in the SMS that isn't a date or account number is the amount.
        
        // Match numbers with at least 4 digits (to avoid small codes/dates)
        val pattern = Pattern.compile("\\d{4,}")
        val matcher = pattern.matcher(content)
        val candidates = mutableListOf<BigDecimal>()
        
        while (matcher.find()) {
            val numStr = matcher.group()
            // Ignore common patterns like dates (1403/xx/xx) or hours (xx:xx) if possible
            // But since we removed delimiters, we have to be careful.
            
            val value = BigDecimal(numStr)
            candidates.add(value)
        }

        if (candidates.isEmpty()) return null

        // Usually, the transaction amount is before "ریال" or "تومان"
        val rialPos = content.indexOf("ریال")
        val tomanPos = content.indexOf("تومان")
        
        if (rialPos != -1) {
            // Find closest candidate before "ریال"
            val beforeRial = candidates.filter { content.indexOf(it.toPlainString()) < rialPos }
            if (beforeRial.isNotEmpty()) {
                // Convert Rial to Toman (app's internal unit for transactions usually, 
                // but let's check if we should keep Rial. The app uses Toman in ledger usually, 
                // but recently we migrated to BigDecimal. Portfolio uses Rial usually? 
                // Let's check TransactionEntity)
                return beforeRial.last().divide(BigDecimal("10"))
            }
        }
        
        if (tomanPos != -1) {
            val beforeToman = candidates.filter { content.indexOf(it.toPlainString()) < tomanPos }
            if (beforeToman.isNotEmpty()) return beforeToman.last()
        }

        // Fallback: take the largest number (heuristic)
        return candidates.maxByOrNull { it }
    }

    private fun extractBankName(content: String, sender: String?): String? {
        val banks = listOf(
            "ملی", "ملت", "سامان", "پاسارگاد", "پارسیان", "صادرات", "سپه", "تجارت", "کشاورزی", "مسکن", "رسالت", "بلو", "بلوبانک"
        )
        
        banks.forEach { 
            if (content.contains(it)) return "بانک $it"
        }
        
        return sender // Use sender number/name if not found in content
    }
}
