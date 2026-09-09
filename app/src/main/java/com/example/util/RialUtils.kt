package com.example.util

import com.example.data.local.BankAccountEntity
import com.example.data.local.MarketRateEntity
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * The portfolio module (Home / Gold&Dollar / Stock market / Add purchase) uses RIAL as its
 * base currency everywhere, per product requirement. The legacy ledger tables store Toman
 * (MarketRateEntity.priceToman), so this file is the single conversion boundary.
 */
val RIAL_PER_TOMAN = BigDecimal("10")

/** MarketRateEntity.priceToman converted to Rial — use this everywhere in the portfolio module. */
val MarketRateEntity.priceRial: BigDecimal get() = priceToman.multiply(RIAL_PER_TOMAN)

/** BankAccountEntity.currentBalance (Toman) converted to Rial — the single boundary for bank balances. */
val BankAccountEntity.currentBalanceRial: BigDecimal get() = currentBalance.multiply(RIAL_PER_TOMAN)

/** Formats an amount that is ALREADY in Rial (no unit conversion), with Persian digits + grouping. */
fun formatRial(
    amountRial: BigDecimal,
    showSuffix: Boolean = true,
    decimalPlaces: Int = 0,
    isRial: Boolean = false
): String {
    val displayAmount = if (isRial) amountRial else amountRial.divide(BigDecimal("10"), decimalPlaces, RoundingMode.HALF_UP)
    val suffix = if (isRial) "ریال" else "تومان"
    val pattern = if (decimalPlaces > 0) "#,##0." + "0".repeat(decimalPlaces) else "#,##0"
    val formatted = DecimalFormat(pattern).format(displayAmount)
    val persian = PersianNumberUtils.toPersianDigits(formatted)
    return if (showSuffix) "$persian $suffix" else persian
}

fun formatPercentSigned(percent: BigDecimal): String {
    val sign = if (percent >= BigDecimal.ZERO) "+" else ""
    val formatted = DecimalFormat("#,##0.##").format(percent)
    return PersianNumberUtils.toPersianDigits("$sign$formatted%")
}

fun formatUsd(amountUsd: BigDecimal, showSuffix: Boolean = true, decimalPlaces: Int = 2): String {
    val pattern = if (decimalPlaces > 0) "#,##0." + "0".repeat(decimalPlaces) else "#,##0"
    val formatted = DecimalFormat(pattern).format(amountUsd)
    val persian = PersianNumberUtils.toPersianDigits(formatted)
    return if (showSuffix) "$persian دلار" else persian
}
