package com.example.crypto.analysis

import com.example.data.local.CryptoAssetEntity
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object TechnicalAnalysisEngine {

    /**
     * Performs a comprehensive multi-factor market analysis.
     * @param symbol The asset symbol (e.g. BTC)
     * @param candles Historical price data
     * @param asset Current market listing data
     * @param btcContext The current state of BTC (Market Leader)
     */
    fun analyze(
        symbol: String,
        candles: List<CandleStick>,
        asset: CryptoAssetEntity,
        btcContext: MarketContext? = null
    ): TechnicalAnalysisResult {
        
        if (candles.size < 200) {
            return TechnicalAnalysisResult(
                symbol = symbol, price = asset.priceUsd ?: 0.0, trend = "نامشخص",
                volumeTrend = "داده ناچیز", rsi = 0.0, support = 0.0, resistance = 0.0,
                liquidity = "نامشخص", riskScore = 0, opportunityScore = 0,
                signal = AnalysisSignal.INSUFFICIENT_DATA, entryZone = null,
                stopLoss = null, takeProfit = null, riskReward = null,
                warnings = listOf("داده‌های کافی (حداقل ۲۰۰ شمع) موجود نیست."),
                reasons = emptyList()
            )
        }

        val closes = candles.map { it.close }
        val currentPrice = closes.last()
        val reasons = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // --- 1. TREND ANALYSIS (20 pts) ---
        var trendScore = 0
        val ema20 = calculateEMA(closes, 20)
        val ema50 = calculateEMA(closes, 50)
        val ema200 = calculateEMA(closes, 200)
        
        val isBullishEMA = currentPrice > ema20 && ema20 > ema50 && ema50 > ema200
        val isBearishEMA = currentPrice < ema20 && ema20 < ema50 && ema50 < ema200
        
        // Detection of HH/HL (Higher High / Higher Low)
        val last3Highs = candles.takeLast(60).chunked(20).map { chunk -> chunk.maxOf { it.high } }
        val last3Lows = candles.takeLast(60).chunked(20).map { chunk -> chunk.minOf { it.low } }
        val isHH = last3Highs.size >= 3 && last3Highs[2] > last3Highs[1] && last3Highs[1] > last3Highs[0]
        val isHL = last3Lows.size >= 3 && last3Lows[2] > last3Lows[1] && last3Lows[1] > last3Lows[0]
        
        if (isBullishEMA) trendScore += 15
        if (isHH && isHL) trendScore += 5
        
        val trendText = when {
            isBullishEMA -> "صعودی (قوی)"
            isBearishEMA -> "نزولی (قوی)"
            currentPrice > ema200 -> "صعودی میان‌مدت"
            else -> "رنج / نزولی"
        }

        // --- 2. VOLUME ANALYSIS (20 pts) ---
        var volumeScore = 0
        val volumes = candles.map { it.volume }
        val avgVol20 = volumes.takeLast(20).average()
        val currentVol = volumes.last()
        val priceChange = currentPrice - closes[closes.size - 2]
        
        val volTrendText = when {
            currentVol > avgVol20 * 2.5 -> {
                volumeScore += 15
                "جهش ناگهانی (Spike)"
            }
            currentVol > avgVol20 * 1.2 && priceChange > 0 -> {
                volumeScore += 20
                "تقاضای رو به رشد"
            }
            currentVol > avgVol20 * 1.2 && priceChange < 0 -> {
                volumeScore -= 10
                "فشار فروش سنگین"
            }
            else -> "معمولی"
        }
        if (currentVol > avgVol20 * 1.2 && priceChange > 0) reasons.add("تایید صعود با حجم معاملات بالا")

        // --- 3. RSI & TECHNICALS (15 pts) ---
        var technicalScore = 0
        val rsi = calculateRSI(closes, 14)
        when {
            rsi < 30 -> { technicalScore += 15; reasons.add("اشباع فروش (قیمت جذاب)") }
            rsi > 70 -> { technicalScore -= 5; warnings.add("اشباع خرید (احتمال اصلاح)") }
            rsi in 40.0..60.0 -> technicalScore += 5
        }

        // --- 4. SUPPORT / RESISTANCE (15 pts) ---
        var srScore = 0
        val (support, resistance) = findSupportResistance(candles)
        val distToSupport = (currentPrice - support) / currentPrice
        val distToResistance = (resistance - currentPrice) / currentPrice
        
        if (distToSupport < 0.03) {
            srScore += 15
            reasons.add("نزدیکی به کف حمایتی معتبر")
        } else if (distToResistance < 0.02) {
            srScore -= 10
            warnings.add("نزدیکی به سقف مقاومتی (ریسک برخورد)")
        }

        // --- 5. LIQUIDITY (10 pts) ---
        var liqScore = 0
        val dailyVolumeUsd = asset.volume24hUsd ?: 0.0
        val liquidityText = when {
            dailyVolumeUsd > 100_000_000 -> { liqScore = 10; "بسیار بالا" }
            dailyVolumeUsd > 10_000_000 -> { liqScore = 7; "مناسب" }
            else -> { liqScore = 2; "پایین (پرریسک)" }
        }
        if (liqScore < 5) warnings.add("نقدشوندگی پایین: خطر لغزش قیمت")

        // --- 6. MARKET CONDITION (10 pts) ---
        var marketScore = 5
        btcContext?.let {
            if (it.isBullish) marketScore += 5 else marketScore -= 5
            if (it.volatility > 0.05) warnings.add("بازار متلاطم: احتیاط در ورود")
        }

        // --- 7. RISK CALCULATION (10 pts) ---
        val atr = calculateATR(candles, 14)
        val volatility = atr / currentPrice
        var riskPoint = 0
        if (volatility < 0.03) riskPoint += 5
        if (distToResistance > 0.10) riskPoint += 5
        val riskScoreTotal = (100 - (riskPoint * 10)).coerceIn(0, 100)

        // --- FINAL OPPORTUNITY SCORE ---
        var finalScore = trendScore + volumeScore + technicalScore + srScore + liqScore + marketScore + riskPoint
        finalScore = finalScore.coerceIn(0, 100)
        
        // --- LOGIC RULES ---
        var signal = when {
            finalScore >= 80 -> AnalysisSignal.STRONG_BUY
            finalScore >= 65 -> AnalysisSignal.BUY_ON_PULLBACK
            finalScore >= 55 -> AnalysisSignal.BREAKOUT_WATCH
            finalScore >= 45 -> AnalysisSignal.HOLD
            finalScore >= 30 -> AnalysisSignal.WAIT
            finalScore >= 15 -> AnalysisSignal.SELL_PARTIAL
            else -> AnalysisSignal.SELL
        }

        // Custom Overrides
        if (rsi > 80 && distToResistance < 0.02) {
            warnings.add("⚠️ هشدار FOMO: قیمت در حباب موقت است.")
            if (finalScore > 60) finalScore = 50 // Reduce score
        }
        if (isBearishEMA && priceChange < -0.10) {
            warnings.add("⚠️ هشدار FALLING KNIFE: سقوط آزاد قیمت.")
            signal = AnalysisSignal.WAIT
        }

        // Entry, SL, TP Calculation
        val entryZone = if (signal.name.contains("BUY")) Pair(currentPrice * 0.99, currentPrice * 1.01) else null
        val stopLoss = if (entryZone != null) support * 0.98 else null
        val takeProfit = if (entryZone != null) resistance * 0.98 else null
        val rrRatio = if (stopLoss != null && takeProfit != null && currentPrice > stopLoss) {
            (takeProfit - currentPrice) / (currentPrice - stopLoss)
        } else null

        return TechnicalAnalysisResult(
            symbol = symbol,
            price = currentPrice,
            trend = trendText,
            volumeTrend = volTrendText,
            rsi = rsi,
            support = support,
            resistance = resistance,
            liquidity = liquidityText,
            riskScore = riskScoreTotal,
            opportunityScore = finalScore,
            signal = signal,
            entryZone = entryZone,
            stopLoss = stopLoss,
            takeProfit = takeProfit,
            riskReward = rrRatio,
            warnings = warnings,
            reasons = reasons
        )
    }

    private fun calculateRSI(closes: List<Double>, period: Int): Double {
        if (closes.size <= period) return 50.0
        val changes = closes.zipWithNext { a, b -> b - a }
        var avgGain = changes.take(period).filter { it > 0 }.sum() / period
        var avgLoss = changes.take(period).filter { it < 0 }.map { abs(it) }.sum() / period

        for (i in period until changes.size) {
            val change = changes[i]
            val gain = if (change > 0) change else 0.0
            val loss = if (change < 0) abs(change) else 0.0
            avgGain = (avgGain * (period - 1) + gain) / period
            avgLoss = (avgLoss * (period - 1) + loss) / period
        }
        if (avgLoss == 0.0) return 100.0
        return 100.0 - (100.0 / (1.0 + (avgGain / avgLoss)))
    }

    private fun calculateEMA(values: List<Double>, period: Int): Double {
        if (values.size < period) return values.lastOrNull() ?: 0.0
        val multiplier = 2.0 / (period + 1)
        var ema = values.take(period).average()
        for (i in period until values.size) {
            ema = (values[i] - ema) * multiplier + ema
        }
        return ema
    }

    private fun calculateATR(candles: List<CandleStick>, period: Int): Double {
        val trs = mutableListOf<Double>()
        for (i in 1 until candles.size) {
            val h = candles[i].high
            val l = candles[i].low
            val pc = candles[i-1].close
            trs.add(max(h - l, max(abs(h - pc), abs(l - pc))))
        }
        return if (trs.size >= period) trs.takeLast(period).average() else 0.0
    }

    private fun findSupportResistance(candles: List<CandleStick>): Pair<Double, Double> {
        val last100 = candles.takeLast(100)
        return last100.minOf { it.low } to last100.maxOf { it.high }
    }
}

data class MarketContext(
    val isBullish: Boolean,
    val volatility: Double
)
