package com.example.util

import kotlin.math.abs

/**
 * Iranian gold & coin market formulas: price bubbles, union (اتحادیه طلا) wage/tax pricing,
 * retrospective "what if I had bought back then" returns, and portfolio scenario simulation.
 *
 * All monetary inputs/outputs are TOMAN unless the name says otherwise. Coin specs use the
 * Central Bank standard weights (21 karat = 0.875 pure for classic coins).
 */
object GoldMarketFormulas {

    const val GRAMS_PER_TROY_OUNCE = 31.1034768

    /**
     * Pure-gold content in grams for the items traded in the Iranian market.
     * classic coins are 21 karat (0.875); Parssian coins are 99.99% fine.
     */
    enum class GoldItemType(val displayName: String, val pureGoldGrams: Double, val isCoin: Boolean) {
        EMAAMI("سکه امامی", 8.133 * 0.875, true),
        BAHAR_AZADI("سکه بهار آزادی", 8.133 * 0.875, true),
        HALF_COIN("نیم سکه", 4.0678 * 0.875, true),
        QUARTER_COIN("ربع سکه", 2.0324 * 0.875, true),
        GRAM_COIN("سکه گرمی", 1.008 * 0.875, true),
        PARSIAN("سکه پارسیان (یک‌اونسی)", 31.1035 * 0.9999, true),
        GOLD_18K("طلای ۱۸ عیار (هر گرم)", 1.0 * 0.750, false),
        GOLD_24K("طلای ۲۴ عیار (هر گرم)", 1.0 * 0.9999, false)
    }

    data class GoldBubbleResult(
        val intrinsicValueToman: Double,   // ارزش ذاتی بر مبنای انس جهانی و دلار
        val bubbleAmountToman: Double,     // مثبت = گران‌تر از ارزش ذاتی
        val bubblePercent: Double,
        val verdict: String                // جمع‌بندی فارسی برای نمایش مستقیم
    )

    /**
     * Bubble of a coin / gold against its intrinsic value.
     *
     * intrinsic = (ounceUsd / 31.1035) × dollarToman × pureGoldGrams
     */
    fun calculateGoldBubble(
        itemType: GoldItemType,
        ouncePriceUsd: Double,
        dollarPriceToman: Double,
        marketPriceToman: Double
    ): GoldBubbleResult? {
        if (ouncePriceUsd <= 0 || dollarPriceToman <= 0 || marketPriceToman <= 0) return null
        val intrinsic = ouncePriceUsd / GRAMS_PER_TROY_OUNCE * dollarPriceToman * itemType.pureGoldGrams
        val bubble = marketPriceToman - intrinsic
        val bubblePct = bubble / intrinsic * 100.0
        val verdict = when {
            bubblePct >= 25 -> "حباب بسیار زیاد — خرید در این قیمت ریسک بالایی دارد"
            bubblePct >= 10 -> "حباب قابل توجه — قیمت از ارزش ذاتی خود فاصله گرفته"
            bubblePct >= 0 -> "حباب ملایم — نزدیک به ارزش ذاتی"
            bubblePct >= -10 -> "کمی ارزان‌تر از ارزش ذاتی — فرصت نسبی برای خرید"
            else -> "به‌طور غیرعادی ارزان‌تر از ارزش ذاتی — قیمت ورودی را بررسی کن"
        }
        return GoldBubbleResult(intrinsic, bubble, bubblePct, verdict)
    }

    data class GoldWageResult(
        val basePricePerGram: Double,     // ارزش ذاتی هر گرم (مظنه)
        val wagePerGram: Double,          // اجرت ساخت
        val sellerProfitPerGram: Double,  // سود فروشنده (فقط طلای نو)
        val vatPerGram: Double,           // مالیات بر ارزش افزوده
        val finalPricePerGram: Double,
        val finalPriceTotal: Double
    )

    /**
     * Total cost of new / second-hand gold per the اتحادیه طلا formula:
     * new: base + wage + seller profit, VAT = vat% × (wage + profit)
     * second-hand: base + wage, VAT = vat% × wage (buying from individuals, no shop profit)
     */
    fun calculateGoldPurchasePrice(
        basePricePerGram: Double,
        weightGrams: Double,
        wagePercent: Double,
        sellerProfitPercent: Double,
        vatPercent: Double,
        isNew: Boolean
    ): GoldWageResult? {
        if (basePricePerGram <= 0 || weightGrams <= 0) return null
        val wage = basePricePerGram * wagePercent / 100.0
        val profit = if (isNew) basePricePerGram * sellerProfitPercent / 100.0 else 0.0
        val vat = (wage + profit) * vatPercent / 100.0
        val perGram = basePricePerGram + wage + profit + vat
        return GoldWageResult(
            basePricePerGram = basePricePerGram,
            wagePerGram = wage,
            sellerProfitPerGram = profit,
            vatPerGram = vat,
            finalPricePerGram = perGram,
            finalPriceTotal = perGram * weightGrams
        )
    }

    data class RetrospectiveResult(
        val quantity: Double,
        val currentValue: Double,
        val profitAmount: Double,
        val profitPercent: Double
    )

    /** "If I had spent [amount] when the price was [pastPrice], what would it be worth today?" */
    fun calculateRetrospective(
        amountToman: Double,
        pastPriceToman: Double,
        currentPriceToman: Double
    ): RetrospectiveResult? {
        if (amountToman <= 0 || pastPriceToman <= 0 || currentPriceToman <= 0) return null
        val quantity = amountToman / pastPriceToman
        val current = quantity * currentPriceToman
        val profit = current - amountToman
        return RetrospectiveResult(
            quantity = quantity,
            currentValue = current,
            profitAmount = profit,
            profitPercent = profit / amountToman * 100.0
        )
    }

    data class ScenarioLegResult(
        val name: String,
        val currentValue: Double,
        val simulatedValue: Double
    )

    data class ScenarioResult(
        val legs: List<ScenarioLegResult>,
        val currentValue: Double,
        val simulatedValue: Double,
        val changeAmount: Double,
        val changePercent: Double
    )

    /**
     * Applies a what-if price change (percent) to each portfolio leg and re-values the total.
     * Used by the "اگر دلار بشود …" simulator. percent: +20 means the asset grows 20%.
     */
    fun calculateScenario(legs: List<Triple<String, Double, Double>>): ScenarioResult? {
        val valid = legs.filter { it.second > 0 }
        if (valid.isEmpty()) return null
        val legResults = valid.map { (name, value, changePct) ->
            ScenarioLegResult(name, value, value * (1.0 + changePct / 100.0))
        }
        val current = legResults.sumOf { it.currentValue }
        val simulated = legResults.sumOf { it.simulatedValue }
        val change = simulated - current
        return ScenarioResult(
            legs = legResults,
            currentValue = current,
            simulatedValue = simulated,
            changeAmount = change,
            changePercent = if (current > 0) change / current * 100.0 else 0.0
        )
    }

    /** Gold purchasing-power check: how many grams [amountToman] buys at two price points. */
    fun gramsBuyable(amountToman: Double, pricePerGram: Double): Double? =
        if (pricePerGram > 0 && amountToman > 0) amountToman / pricePerGram else null

    /** Formatted signed percent for result banners, e.g. "+۱۲٫۵٪". */
    fun formatSignedPercent(percent: Double): String {
        val sign = if (percent >= 0) "+" else "-"
        return PersianNumberUtils.toPersianDigits("$sign${abs(percent)}٪")
    }
}
