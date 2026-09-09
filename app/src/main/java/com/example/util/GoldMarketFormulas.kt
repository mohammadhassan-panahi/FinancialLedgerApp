package com.example.util

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Iranian gold & coin market formulas: price bubbles, union (اتحادیه طلا) wage/tax pricing,
 * retrospective "what if I had bought back then" returns, and portfolio scenario simulation.
 *
 * All monetary inputs/outputs are TOMAN unless the name says otherwise. Coin specs use the
 * Central Bank standard weights (21 karat = 0.875 pure for classic coins).
 */
object GoldMarketFormulas {

    val GRAMS_PER_TROY_OUNCE = BigDecimal("31.1034768")

    /**
     * Pure-gold content in grams for the items traded in the Iranian market.
     * classic coins are 21 karat (0.875); Parssian coins are 99.99% fine.
     */
    enum class GoldItemType(val displayName: String, val pureGoldGrams: BigDecimal, val isCoin: Boolean) {
        EMAAMI("سکه امامی", BigDecimal("8.133").multiply(BigDecimal("0.875")), true),
        BAHAR_AZADI("سکه بهار آزادی", BigDecimal("8.133").multiply(BigDecimal("0.875")), true),
        HALF_COIN("نیم سکه", BigDecimal("4.0678").multiply(BigDecimal("0.875")), true),
        QUARTER_COIN("ربع سکه", BigDecimal("2.0324").multiply(BigDecimal("0.875")), true),
        GRAM_COIN("سکه گرمی", BigDecimal("1.008").multiply(BigDecimal("0.875")), true),
        PARSIAN("سکه پارسیان (یک‌اونسی)", BigDecimal("31.1035").multiply(BigDecimal("0.9999")), true),
        GOLD_18K("طلای ۱۸ عیار (هر گرم)", BigDecimal("0.750"), false),
        GOLD_24K("طلای ۲۴ عیار (هر گرم)", BigDecimal("0.9999"), false)
    }

    data class GoldBubbleResult(
        val intrinsicValueToman: BigDecimal,   // ارزش ذاتی بر مبنای انس جهانی و دلار
        val bubbleAmountToman: BigDecimal,     // مثبت = گران‌تر از ارزش ذاتی
        val bubblePercent: BigDecimal,
        val verdict: String                // جمع‌بندی فارسی برای نمایش مستقیم
    )

    /**
     * Bubble of a coin / gold against its intrinsic value.
     *
     * intrinsic = (ounceUsd / 31.1035) × dollarToman × pureGoldGrams
     */
    fun calculateGoldBubble(
        itemType: GoldItemType,
        ouncePriceUsd: BigDecimal,
        dollarPriceToman: BigDecimal,
        marketPriceToman: BigDecimal
    ): GoldBubbleResult? {
        if (ouncePriceUsd <= BigDecimal.ZERO || dollarPriceToman <= BigDecimal.ZERO || marketPriceToman <= BigDecimal.ZERO) return null
        val intrinsic = ouncePriceUsd.safeDiv(GRAMS_PER_TROY_OUNCE).multiply(dollarPriceToman).multiply(itemType.pureGoldGrams)
        val bubble = marketPriceToman.subtract(intrinsic)
        val bubblePct = if (intrinsic.compareTo(BigDecimal.ZERO) != 0) bubble.divide(intrinsic, 4, RoundingMode.HALF_UP).multiply(BigDecimal("100")) else BigDecimal.ZERO
        val verdict = when {
            bubblePct >= BigDecimal("25") -> "حباب بسیار زیاد — خرید در این قیمت ریسک بالایی دارد"
            bubblePct >= BigDecimal("10") -> "حباب قابل توجه — قیمت از ارزش ذاتی خود فاصله گرفته"
            bubblePct >= BigDecimal.ZERO -> "حباب ملایم — نزدیک به ارزش ذاتی"
            bubblePct >= BigDecimal("-10") -> "کمی ارزان‌تر از ارزش ذاتی — فرصت نسبی برای خرید"
            else -> "به‌طور غیرعادی ارزان‌تر از ارزش ذاتی — قیمت ورودی را بررسی کن"
        }
        return GoldBubbleResult(intrinsic, bubble, bubblePct, verdict)
    }

    data class GoldWageResult(
        val basePricePerGram: BigDecimal,     // ارزش ذاتی هر گرم (مظنه)
        val wagePerGram: BigDecimal,          // اجرت ساخت
        val sellerProfitPerGram: BigDecimal,  // سود فروشنده (فقط طلای نو)
        val vatPerGram: BigDecimal,           // مالیات بر ارزش افزوده
        val finalPricePerGram: BigDecimal,
        val finalPriceTotal: BigDecimal
    )

    /**
     * Total cost of new / second-hand gold per the اتحادیه طلا formula:
     * new: base + wage + seller profit, VAT = vat% × (base + wage + profit)
     * Note: Actually union VAT applies to (wage + profit + maybe base depending on law changes, 
     * but usually it's base + wage + profit and then VAT on that total or just wage+profit).
     * Traditionally in Iran: (base + wage + profit) * 1.09.
     */
    fun calculateGoldPurchasePrice(
        basePricePerGram: BigDecimal,
        weightGrams: BigDecimal,
        wagePercent: BigDecimal,
        sellerProfitPercent: BigDecimal,
        vatPercent: BigDecimal,
        isNew: Boolean
    ): GoldWageResult? {
        if (basePricePerGram <= BigDecimal.ZERO || weightGrams <= BigDecimal.ZERO) return null
        val wage = basePricePerGram.multiply(wagePercent).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
        val profit = if (isNew) basePricePerGram.multiply(sellerProfitPercent).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP) else BigDecimal.ZERO
        val totalBeforeTax = basePricePerGram.add(wage).add(profit)
        val vat = totalBeforeTax.multiply(vatPercent).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
        val perGram = totalBeforeTax.add(vat)
        return GoldWageResult(
            basePricePerGram = basePricePerGram,
            wagePerGram = wage,
            sellerProfitPerGram = profit,
            vatPerGram = vat,
            finalPricePerGram = perGram,
            finalPriceTotal = perGram.multiply(weightGrams)
        )
    }

    data class RetrospectiveResult(
        val quantity: BigDecimal,
        val currentValue: BigDecimal,
        val profitAmount: BigDecimal,
        val profitPercent: BigDecimal
    )

    /** "If I had spent [amount] when the price was [pastPrice], what would it be worth today?" */
    fun calculateRetrospective(
        amountToman: BigDecimal,
        pastPriceToman: BigDecimal,
        currentPriceToman: BigDecimal
    ): RetrospectiveResult? {
        if (amountToman <= BigDecimal.ZERO || pastPriceToman <= BigDecimal.ZERO || currentPriceToman <= BigDecimal.ZERO) return null
        val quantity = amountToman.safeDiv(pastPriceToman)
        val current = quantity.multiply(currentPriceToman)
        val profit = current.subtract(amountToman)
        return RetrospectiveResult(
            quantity = quantity,
            currentValue = current,
            profitAmount = profit,
            profitPercent = if (amountToman.compareTo(BigDecimal.ZERO) != 0) profit.divide(amountToman, 4, RoundingMode.HALF_UP).multiply(BigDecimal("100")) else BigDecimal.ZERO
        )
    }

    data class ScenarioLegResult(
        val name: String,
        val currentValue: BigDecimal,
        val simulatedValue: BigDecimal
    )

    data class ScenarioResult(
        val legs: List<ScenarioLegResult>,
        val currentValue: BigDecimal,
        val simulatedValue: BigDecimal,
        val changeAmount: BigDecimal,
        val changePercent: BigDecimal
    )

    /**
     * Applies a what-if price change (percent) to each portfolio leg and re-values the total.
     * Used by the "اگر دلار بشود …" simulator. percent: +20 means the asset grows 20%.
     */
    fun calculateScenario(legs: List<Triple<String, BigDecimal, BigDecimal>>): ScenarioResult? {
        val valid = legs.filter { it.second.compareTo(BigDecimal.ZERO) > 0 }
        if (valid.isEmpty()) return null
        val legResults = valid.map { (name, value, changePct) ->
            ScenarioLegResult(name, value, value.multiply(BigDecimal.ONE.add(changePct.divide(BigDecimal("100"), 4, RoundingMode.HALF_UP))))
        }
        val current = legResults.sumOf { it.currentValue }
        val simulated = legResults.sumOf { it.simulatedValue }
        val change = simulated.subtract(current)
        return ScenarioResult(
            legs = legResults,
            currentValue = current,
            simulatedValue = simulated,
            changeAmount = change,
            changePercent = if (current.compareTo(BigDecimal.ZERO) > 0) change.divide(current, 4, RoundingMode.HALF_UP).multiply(BigDecimal("100")) else BigDecimal.ZERO
        )
    }

    /** Gold purchasing-power check: how many grams [amountToman] buys at two price points. */
    fun gramsBuyable(amountToman: BigDecimal, pricePerGram: BigDecimal): BigDecimal? =
        if (pricePerGram.compareTo(BigDecimal.ZERO) > 0 && amountToman.compareTo(BigDecimal.ZERO) > 0) amountToman.safeDiv(pricePerGram) else null

    /** Formatted signed percent for result banners, e.g. "+۱۲٫۵٪". */
    fun formatSignedPercent(percent: BigDecimal): String {
        val sign = if (percent >= BigDecimal.ZERO) "+" else "-"
        return PersianNumberUtils.toPersianDigits("$sign${percent.abs().setScale(1, RoundingMode.HALF_UP)}٪")
    }
}
