package com.example.util

import com.example.data.local.MarketRateEntity
import java.text.DecimalFormat

/**
 * The portfolio module (Home / Gold&Dollar / Stock market / Add purchase) uses RIAL as its
 * base currency everywhere, per product requirement. The legacy ledger tables store Toman
 * (MarketRateEntity.priceToman), so this file is the single conversion boundary.
 */
const val RIAL_PER_TOMAN = 10.0

/** MarketRateEntity.priceToman converted to Rial — use this everywhere in the portfolio module. */
val MarketRateEntity.priceRial: Double get() = priceToman * RIAL_PER_TOMAN

/** Formats an amount that is ALREADY in Rial (no unit conversion), with Persian digits + grouping. */
fun formatRial(amountRial: Double, showSuffix: Boolean = true, decimalPlaces: Int = 0): String {
    val pattern = if (decimalPlaces > 0) "#,##0." + "0".repeat(decimalPlaces) else "#,##0"
    val formatted = DecimalFormat(pattern).format(amountRial)
    val persian = PersianNumberUtils.toPersianDigits(formatted)
    return if (showSuffix) "$persian ریال" else persian
}

fun formatPercentSigned(percent: Double): String {
    val sign = if (percent >= 0) "+" else ""
    val formatted = DecimalFormat("#,##0.##").format(percent)
    return PersianNumberUtils.toPersianDigits("$sign$formatted%")
}
