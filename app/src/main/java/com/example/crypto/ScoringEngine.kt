package com.example.crypto

import com.example.data.local.CryptoAssetEntity
import com.example.util.safeDiv
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import kotlin.math.ln

/**
 * Computes crypto scores (0-100) from data this app ACTUALLY has — CoinMarketCap free-tier
 * market data cached in [CryptoAssetEntity]. Intentionally a subset of the eight scores in
 * the original feature request:
 *
 *   COMPUTABLE from free-tier CMC data: Fundamental Score, Risk Score.
 *
 *   NOT COMPUTABLE without additional data sources (documented so this isn't silently
 *   missing later): Security Score (needs an audit API like GoPlus/CertiK), full Tokenomics
 *   Score (needs unlock/vesting schedules), Decentralization Score (needs on-chain
 *   validator/node data), Developer Score (needs GitHub API), Liquidity Score (needs
 *   order-book depth, not just aggregate volume), Technical Score (needs historical price
 *   data — CMC's historical endpoints are paid-tier only).
 *
 * Every [ScoreResult] includes a plain-language [ScoreResult.reason] so the UI can show
 * "why", per the product requirement that scores must be explainable, never opaque.
 */
object ScoringEngine {

    data class ScoreResult(val score: Int, val reason: String)

    /** Fundamental Score: market-cap rank, market cap size, and supply health. */
    fun fundamentalScore(asset: CryptoAssetEntity): ScoreResult {
        val reasons = mutableListOf<String>()
        var score = BigDecimal.ZERO
        var weight = BigDecimal.ZERO

        asset.cmcRank?.let { rank ->
            val rankScore = BigDecimal.valueOf((100 - (ln(rank.toDouble()) * 12)).coerceIn(0.0, 100.0))
            score = score.add(rankScore.multiply(BigDecimal("3")))
            weight = weight.add(BigDecimal("3"))
            reasons += "رتبه‌ی بازار #$rank"
        }

        asset.marketCapUsd?.let { cap ->
            val capScore = when {
                cap >= BigDecimal("10000000000") -> BigDecimal("100")
                cap >= BigDecimal("1000000000") -> BigDecimal("75")
                cap >= BigDecimal("100000000") -> BigDecimal("50")
                cap >= BigDecimal("10000000") -> BigDecimal("25")
                else -> BigDecimal("10")
            }
            score = score.add(capScore.multiply(BigDecimal("2")))
            weight = weight.add(BigDecimal("2"))
            reasons += "ارزش بازار ${formatUsdShort(cap)}"
        }

        if (asset.maxSupply != null && asset.maxSupply!!.compareTo(BigDecimal.ZERO) > 0 && asset.circulatingSupply != null) {
            val ratio = asset.circulatingSupply!!.safeDiv(asset.maxSupply!!).coerceIn(BigDecimal.ZERO, BigDecimal.ONE)
            score = score.add((ratio.multiply(BigDecimal("100"))).multiply(BigDecimal.ONE))
            weight = weight.add(BigDecimal.ONE)
            reasons += "${(ratio.multiply(BigDecimal("100"))).setScale(0, RoundingMode.HALF_UP).toPlainString()}٪ از عرضه‌ی حداکثری در گردش است"
        } else if (asset.infiniteSupply) {
            reasons += "عرضه‌ی حداکثری نامحدود (تورمی)"
        }

        if (weight.compareTo(BigDecimal.ZERO) == 0) return ScoreResult(0, "داده‌ی کافی برای محاسبه در دسترس نیست")
        return ScoreResult(score.safeDiv(weight, 0).toInt().coerceIn(0, 100), reasons.joinToString(" • "))
    }

    /** Risk Score: 0 = ریسک بسیار کم، 100 = ریسک بسیار زیاد (معکوس، طبق نیازمندی محصول). */
    fun riskScore(asset: CryptoAssetEntity): ScoreResult {
        val reasons = mutableListOf<String>()
        var risk = BigDecimal.ZERO
        var weight = BigDecimal.ZERO

        asset.percentChange24h?.let { change ->
            val changeRisk = (change.abs().multiply(BigDecimal("4"))).coerceIn(BigDecimal.ZERO, BigDecimal("100"))
            risk = risk.add(changeRisk.multiply(BigDecimal("2")))
            weight = weight.add(BigDecimal("2"))
            reasons += "نوسان ۲۴ ساعته ${formatSigned(change)}٪"
        }

        asset.marketCapUsd?.let { cap ->
            val sizeRisk = when {
                cap >= BigDecimal("10000000000") -> BigDecimal("5")
                cap >= BigDecimal("1000000000") -> BigDecimal("20")
                cap >= BigDecimal("100000000") -> BigDecimal("45")
                cap >= BigDecimal("10000000") -> BigDecimal("70")
                else -> BigDecimal("90")
            }
            risk = risk.add(sizeRisk.multiply(BigDecimal("2")))
            weight = weight.add(BigDecimal("2"))
            reasons += "ارزش بازار ${formatUsdShort(cap)}"
        }

        if (asset.infiniteSupply) {
            risk = risk.add(BigDecimal("80"))
            weight = weight.add(BigDecimal.ONE)
            reasons += "عرضه‌ی حداکثری نامحدود (ریسک تورمی)"
        } else if (asset.maxSupply != null && asset.maxSupply!!.compareTo(BigDecimal.ZERO) > 0 && asset.circulatingSupply != null) {
            val ratio = asset.circulatingSupply!!.safeDiv(asset.maxSupply!!).coerceIn(BigDecimal.ZERO, BigDecimal.ONE)
            risk = risk.add((BigDecimal.ONE.subtract(ratio)).multiply(BigDecimal("60")))
            weight = weight.add(BigDecimal.ONE)
        }

        if (weight.compareTo(BigDecimal.ZERO) == 0) return ScoreResult(50, "داده‌ی کافی برای محاسبه در دسترس نیست — مقدار پیش‌فرض")
        return ScoreResult(risk.safeDiv(weight, 0).toInt().coerceIn(0, 100), reasons.joinToString(" • "))
    }

    private fun formatSigned(v: BigDecimal): String {
        val sign = if (v >= BigDecimal.ZERO) "+" else ""
        return "$sign${String.format(Locale.US, "%.1f", v.toDouble())}"
    }

    private fun formatUsdShort(v: BigDecimal): String = when {
        v >= BigDecimal("1000000000") -> String.format(Locale.US, "%.1f میلیارد دلار", v.divide(BigDecimal("1000000000"), 1, RoundingMode.HALF_UP).toDouble())
        v >= BigDecimal("1000000") -> String.format(Locale.US, "%.1f میلیون دلار", v.divide(BigDecimal("1000000"), 1, RoundingMode.HALF_UP).toDouble())
        else -> String.format(Locale.US, "%.0f دلار", v.toDouble())
    }
}

private fun BigDecimal.coerceIn(min: BigDecimal, max: BigDecimal): BigDecimal {
    return if (this < min) min else if (this > max) max else this
}
