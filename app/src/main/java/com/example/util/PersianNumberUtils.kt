package com.example.util

import java.text.DecimalFormat

object PersianNumberUtils {

    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    private val arabicDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

    /**
     * Converts Persian and Arabic digits in a string to standard ASCII English digits.
     */
    fun toEnglishDigits(input: String): String {
        var result = input
        for (i in 0..9) {
            result = result.replace(persianDigits[i], ('0' + i))
            // handle arabic variants as well
            result = result.replace(('آ'.code + i).toChar(), ('0' + i))
        }
        return result
    }

    /**
     * Converts standard ASCII English digits to Persian digits.
     */
    fun toPersianDigits(input: String): String {
        val builder = StringBuilder()
        for (ch in input) {
            if (ch in '0'..'9') {
                builder.append(persianDigits[ch - '0'])
            } else {
                builder.append(ch)
            }
        }
        return builder.toString()
    }

    /**
     * Parses a string containing numbers (with potential commas, Persian digits, spaces) into a Double.
     */
    fun parseAmount(input: String): Double {
        if (input.isBlank()) return 0.0
        val clean = toEnglishDigits(input).replace(",", "").replace(" ", "").trim()
        return clean.toDoubleOrNull() ?: 0.0
    }

    /**
     * Formats a raw number (or string input) into thousands separated string without suffix.
     */
    fun formatFormattedInput(input: String): String {
        val clean = toEnglishDigits(input).replace(",", "").replace(" ", "").trim()
        if (clean.isEmpty()) return ""
        val parts = clean.split(".")
        val integerPart = parts[0].toLongOrNull() ?: return clean
        val formatter = DecimalFormat("#,###")
        val formattedInt = formatter.format(integerPart)
        return if (parts.size > 1) {
            "$formattedInt.${parts[1]}"
        } else {
            formattedInt
        }
    }

    fun getCurrencyUnitLabel(isRial: Boolean): String = if (isRial) "ریال" else "تومان"

    /**
     * Converts user input amount string to Toman base amount for internal calculations.
     */
    fun parseAmountToToman(input: String, isRial: Boolean): Double {
        val parsed = parseAmount(input)
        return if (isRial) parsed / 10.0 else parsed
    }

    /**
     * Formats Double or Long amount to formatted string with dynamic Toman/Rial currency suffix.
     */
    fun formatCurrency(
        amount: Double,
        showSuffix: Boolean = true,
        usePersianDigits: Boolean = true,
        decimalPlaces: Int = 0,
        isRial: Boolean = false
    ): String {
        val displayAmount = if (isRial) amount * 10.0 else amount
        val suffix = getCurrencyUnitLabel(isRial)
        val pattern = if (decimalPlaces > 0) {
            "#,##0." + "0".repeat(decimalPlaces)
        } else {
            "#,##0"
        }
        val formatter = DecimalFormat(pattern)
        val formatted = formatter.format(displayAmount)
        val withDigits = if (usePersianDigits) toPersianDigits(formatted) else formatted
        return if (showSuffix) "$withDigits $suffix" else withDigits
    }

    /**
     * Formats a percentage nicely (e.g. 18.5%).
     */
    fun formatPercent(rate: Double, usePersianDigits: Boolean = true): String {
        val formatter = DecimalFormat("#,##0.##")
        val formatted = formatter.format(rate) + "%"
        return if (usePersianDigits) toPersianDigits(formatted) else formatted
    }

    private val ones = arrayOf("", "یک", "دو", "سه", "چهار", "پنج", "شش", "هفت", "هشت", "نه")
    private val teens = arrayOf("ده", "یازده", "دوازده", "سیزده", "چهارده", "پانزده", "شانزده", "هفده", "هجده", "نوزده")
    private val tens = arrayOf("", "", "بیست", "سی", "چهل", "پنجاه", "شصت", "هفتاد", "هشتاد", "نود")
    private val hundreds = arrayOf("", "صد", "دویست", "سیصد", "چهارصد", "پانصد", "ششصد", "هفتصد", "هشتصد", "نهصد")
    private val scaleNames = arrayOf("", "هزار", "میلیون", "میلیارد", "تریلیون")

    /**
     * Converts a Long number into Persian words (عدد به حروف فارسی).
     * e.g., 1000000 -> "یک میلیون"
     */
    fun numberToPersianWords(number: Long): String {
        if (number == 0L) return "صفر"
        if (number < 0L) return "منفی " + numberToPersianWords(-number)

        var num = number
        val parts = mutableListOf<String>()
        var scaleIndex = 0

        while (num > 0) {
            val chunk = (num % 1000).toInt()
            if (chunk > 0) {
                val chunkText = convertChunkToWords(chunk)
                val scale = scaleNames[scaleIndex]
                val fullChunk = if (scale.isNotEmpty()) "$chunkText $scale" else chunkText
                parts.add(0, fullChunk)
            }
            num /= 1000
            scaleIndex++
        }

        return parts.joinToString(" و ")
    }

    private fun convertChunkToWords(chunk: Int): String {
        val h = chunk / 100
        val remainder = chunk % 100
        val t = remainder / 10
        val o = remainder % 10

        val list = mutableListOf<String>()
        if (h > 0) list.add(hundreds[h])

        if (remainder in 10..19) {
            list.add(teens[remainder - 10])
        } else {
            if (t > 1) list.add(tens[t])
            if (o > 0) list.add(ones[o])
        }

        return list.joinToString(" و ")
    }
}
