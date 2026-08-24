package com.example.crypto

import com.example.data.local.CryptoAssetEntity
import kotlin.math.abs
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
        var score = 0.0
        var weight = 0.0

        asset.cmcRank?.let { rank ->
            val rankScore = (100 - (ln(rank.toDouble()) * 12)).coerceIn(0.0, 100.0)
            score += rankScore * 3; weight += 3
            reasons += "رتبه‌ی بازار #$rank"
        }

        asset.marketCapUsd?.let { cap ->
            val capScore = when {
                cap >= 10_000_000_000 -> 100.0
                cap >= 1_000_000_000 -> 75.0
                cap >= 100_000_000 -> 50.0
                cap >= 10_000_000 -> 25.0
                else -> 10.0
            }
            score += capScore * 2; weight += 2
            reasons += "ارزش بازار ${formatUsdShort(cap)}"
        }

        if (asset.maxSupply != null && asset.maxSupply!! > 0 && asset.circulatingSupply != null) {
            val ratio = (asset.circulatingSupply!! / asset.maxSupply!!).coerceIn(0.0, 1.0)
            score += (ratio * 100.0) * 1; weight += 1
            reasons += "${(ratio * 100).toInt()}٪ از عرضه‌ی حداکثری در گردش است"
        } else if (asset.infiniteSupply) {
            reasons += "عرضه‌ی حداکثری نامحدود (تورمی)"
        }

        if (weight == 0.0) return ScoreResult(0, "داده‌ی کافی برای محاسبه در دسترس نیست")
        return ScoreResult((score / weight).toInt().coerceIn(0, 100), reasons.joinToString(" • "))
    }

    /** Risk Score: 0 = ریسک بسیار کم، 100 = ریسک بسیار زیاد (معکوس، طبق نیازمندی محصول). */
    fun riskScore(asset: CryptoAssetEntity): ScoreResult {
        val reasons = mutableListOf<String>()
        var risk = 0.0
        var weight = 0.0

        asset.percentChange24h?.let { change ->
            risk += (abs(change) * 4).coerceIn(0.0, 100.0) * 2; weight += 2
            reasons += "نوسان ۲۴ ساعته ${formatSigned(change)}٪"
        }

        asset.marketCapUsd?.let { cap ->
            val sizeRisk = when {
                cap >= 10_000_000_000 -> 5.0
                cap >= 1_000_000_000 -> 20.0
                cap >= 100_000_000 -> 45.0
                cap >= 10_000_000 -> 70.0
                else -> 90.0
            }
            risk += sizeRisk * 2; weight += 2
            reasons += "ارزش بازار ${formatUsdShort(cap)}"
        }

        if (asset.infiniteSupply) {
            risk += 80.0; weight += 1
            reasons += "عرضه‌ی حداکثری نامحدود (ریسک تورمی)"
        } else if (asset.maxSupply != null && asset.maxSupply!! > 0 && asset.circulatingSupply != null) {
            val ratio = (asset.circulatingSupply!! / asset.maxSupply!!).coerceIn(0.0, 1.0)
            risk += (1.0 - ratio) * 60.0; weight += 1
        }

        if (weight == 0.0) return ScoreResult(50, "داده‌ی کافی برای محاسبه در دسترس نیست — مقدار پیش‌فرض")
        return ScoreResult((risk / weight).toInt().coerceIn(0, 100), reasons.joinToString(" • "))
    }

    private fun formatSigned(v: Double): String {
        val sign = if (v >= 0) "+" else ""
        return "$sign${"%.1f".format(v)}"
    }

    private fun formatUsdShort(v: Double): String = when {
        v >= 1_000_000_000 -> "%.1f میلیارد دلار".format(v / 1_000_000_000)
        v >= 1_000_000 -> "%.1f میلیون دلار".format(v / 1_000_000)
        else -> "%.0f دلار".format(v)
    }
}
