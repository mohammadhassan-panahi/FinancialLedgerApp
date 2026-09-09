package com.example.crypto.analysis

import com.example.data.local.CryptoAssetEntity
import com.example.util.safeDiv
import com.example.util.sumOf
import java.math.BigDecimal
import java.math.RoundingMode

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
                symbol = symbol, price = asset.priceUsd ?: BigDecimal.ZERO, trend = "نامشخص",
                volumeTrend = "داده ناچیز", rsi = BigDecimal.ZERO, support = BigDecimal.ZERO, resistance = BigDecimal.ZERO,
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
        val last3Highs = candles.takeLast(60).chunked(20).mapNotNull { chunk -> chunk.maxOfOrNull { it.high } }
        val last3Lows = candles.takeLast(60).chunked(20).mapNotNull { chunk -> chunk.minOfOrNull { it.low } }
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
        val avgVol20 = calculateAverage(volumes.takeLast(20))
        val currentVol = volumes.last()
        val priceChange = currentPrice.subtract(closes[closes.size - 2])
        
        val volTrendText = when {
            currentVol > avgVol20.multiply(BigDecimal("2.5")) -> {
                volumeScore += 15
                "جهش ناگهانی (Spike)"
            }
            currentVol > avgVol20.multiply(BigDecimal("1.2")) && priceChange > BigDecimal.ZERO -> {
                volumeScore += 20
                "تقاضای رو به رشد"
            }
            currentVol > avgVol20.multiply(BigDecimal("1.2")) && priceChange < BigDecimal.ZERO -> {
                volumeScore -= 10
                "فشار فروش سنگین"
            }
            else -> "معمولی"
        }
        if (currentVol > avgVol20.multiply(BigDecimal("1.2")) && priceChange > BigDecimal.ZERO) reasons.add("تایید صعود با حجم معاملات بالا")

        // --- 3. RSI & TECHNICALS (15 pts) ---
        var technicalScore = 0
        val rsi = calculateRSI(closes, 14)
        when {
            rsi < BigDecimal("30") -> { technicalScore += 15; reasons.add("اشباع فروش (قیمت جذاب)") }
            rsi > BigDecimal("70") -> { technicalScore -= 5; warnings.add("اشباع خرید (احتمال اصلاح)") }
            rsi >= BigDecimal("40") && rsi <= BigDecimal("60") -> technicalScore += 5
        }

        // --- 4. SUPPORT / RESISTANCE (15 pts) ---
        var srScore = 0
        val (support, resistance) = findSupportResistance(candles)
        val distToSupport = (currentPrice.subtract(support)).safeDiv(currentPrice)
        val distToResistance = (resistance.subtract(currentPrice)).safeDiv(currentPrice)
        
        if (distToSupport < BigDecimal("0.03")) {
            srScore += 15
            reasons.add("نزدیکی به کف حمایتی معتبر")
        } else if (distToResistance < BigDecimal("0.02")) {
            srScore -= 10
            warnings.add("نزدیکی به سقف مقاومتی (ریسک برخورد)")
        }

        // --- 5. LIQUIDITY (10 pts) ---
        var liqScore = 0
        val dailyVolumeUsd = asset.volume24hUsd ?: BigDecimal.ZERO
        val liquidityText = when {
            dailyVolumeUsd > BigDecimal("100000000") -> { liqScore = 10; "بسیار بالا" }
            dailyVolumeUsd > BigDecimal("10000000") -> { liqScore = 7; "مناسب" }
            else -> { liqScore = 2; "پایین (پرریسک)" }
        }
        if (liqScore < 5) warnings.add("نقدشوندگی پایین: خطر لغزش قیمت")

        // --- 6. MARKET CONDITION (10 pts) ---
        var marketScore = 5
        btcContext?.let {
            if (it.isBullish) marketScore += 5 else marketScore -= 5
            if (it.volatility > BigDecimal("0.05")) warnings.add("بازار متلاطم: احتیاط در ورود")
        }

        // --- 7. RISK CALCULATION (10 pts) ---
        val atr = calculateATR(candles, 14)
        val volatility = atr.safeDiv(currentPrice)
        var riskPoint = 0
        if (volatility < BigDecimal("0.03")) riskPoint += 5
        if (distToResistance > BigDecimal("0.10")) riskPoint += 5
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
        if (rsi > BigDecimal("80") && distToResistance < BigDecimal("0.02")) {
            warnings.add("⚠️ هشدار FOMO: قیمت در حباب موقت است.")
            if (finalScore > 60) finalScore = 50 // Reduce score
        }
        if (isBearishEMA && priceChange < currentPrice.multiply(BigDecimal("-0.10"))) {
            warnings.add("⚠️ هشدار FALLING KNIFE: سقوط آزاد قیمت.")
            signal = AnalysisSignal.WAIT
        }

        // Entry, SL, TP Calculation
        val entryZone = if (signal.name.contains("BUY")) Pair(currentPrice.multiply(BigDecimal("0.99")), currentPrice.multiply(BigDecimal("1.01"))) else null
        val stopLoss = if (entryZone != null) support.multiply(BigDecimal("0.98")) else null
        val takeProfit = if (entryZone != null) resistance.multiply(BigDecimal("0.98")) else null
        val rrRatio = if (stopLoss != null && takeProfit != null && currentPrice > stopLoss) {
            (takeProfit.subtract(currentPrice)).safeDiv(currentPrice.subtract(stopLoss))
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

    private fun calculateRSI(closes: List<BigDecimal>, period: Int): BigDecimal {
        if (closes.size <= period) return BigDecimal("50")
        val changes = closes.zipWithNext { a, b -> b.subtract(a) }
        var avgGain = changes.take(period).filter { it > BigDecimal.ZERO }.sumOf { it }.safeDiv(BigDecimal.valueOf(period.toLong()))
        var avgLoss = changes.take(period).filter { it < BigDecimal.ZERO }.sumOf { it.abs() }.safeDiv(BigDecimal.valueOf(period.toLong()))

        for (i in period until changes.size) {
            val change = changes[i]
            val gain = if (change > BigDecimal.ZERO) change else BigDecimal.ZERO
            val loss = if (change < BigDecimal.ZERO) change.abs() else BigDecimal.ZERO
            avgGain = (avgGain.multiply(BigDecimal.valueOf((period - 1).toLong())).add(gain)).safeDiv(BigDecimal.valueOf(period.toLong()))
            avgLoss = (avgLoss.multiply(BigDecimal.valueOf((period - 1).toLong())).add(loss)).safeDiv(BigDecimal.valueOf(period.toLong()))
        }
        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) return BigDecimal("100")
        return BigDecimal("100").subtract(BigDecimal("100").safeDiv(BigDecimal.ONE.add(avgGain.safeDiv(avgLoss))))
    }

    private fun calculateEMA(values: List<BigDecimal>, period: Int): BigDecimal {
        if (values.size < period) return values.lastOrNull() ?: BigDecimal.ZERO
        val multiplier = BigDecimal("2").safeDiv(BigDecimal.valueOf((period + 1).toLong()))
        var ema = calculateAverage(values.take(period))
        for (i in period until values.size) {
            ema = (values[i].subtract(ema)).multiply(multiplier).add(ema)
        }
        return ema
    }

    private fun calculateATR(candles: List<CandleStick>, period: Int): BigDecimal {
        val trs = mutableListOf<BigDecimal>()
        for (i in 1 until candles.size) {
            val h = candles[i].high
            val l = candles[i].low
            val pc = candles[i-1].close
            // max(h - l, max(abs(h - pc), abs(l - pc)))
            val tr = (h.subtract(l)).max((h.subtract(pc)).abs()).max((l.subtract(pc)).abs())
            trs.add(tr)
        }
        return if (trs.size >= period) calculateAverage(trs.takeLast(period)) else BigDecimal.ZERO
    }

    private fun findSupportResistance(candles: List<CandleStick>): Pair<BigDecimal, BigDecimal> {
        val last100 = candles.takeLast(100)
        val support = last100.minOfOrNull { it.low } ?: BigDecimal.ZERO
        val resistance = last100.maxOfOrNull { it.high } ?: BigDecimal.ZERO
        return support to resistance
    }

    private fun calculateAverage(values: List<BigDecimal>): BigDecimal {
        if (values.isEmpty()) return BigDecimal.ZERO
        return values.sumOf { it }.safeDiv(BigDecimal.valueOf(values.size.toLong()))
    }
}

data class MarketContext(
    val isBullish: Boolean,
    val volatility: BigDecimal
)
