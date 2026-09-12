package com.example.crypto.analysis

import com.example.data.local.CryptoAssetEntity
import com.example.util.safeDiv
import com.example.util.sumOf
import java.math.BigDecimal
import java.math.RoundingMode

object TechnicalAnalysisEngine {

    /**
     * Performs a comprehensive multi-factor market analysis.
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
        
        // --- 1. TREND & EMA (20 pts) ---
        var trendScore = 0
        val ema20List = Indicators.calculateEMA(closes, 20)
        val ema50List = Indicators.calculateEMA(closes, 50)
        val ema200List = Indicators.calculateEMA(closes, 200)
        
        val ema20 = ema20List.lastOrNull() ?: BigDecimal.ZERO
        val ema50 = ema50List.lastOrNull() ?: BigDecimal.ZERO
        val ema200 = ema200List.lastOrNull() ?: BigDecimal.ZERO
        
        val isBullishEMA = currentPrice > ema20 && ema20 > ema50 && ema50 > ema200
        val isBearishEMA = currentPrice < ema20 && ema20 < ema50 && ema50 < ema200
        
        if (isBullishEMA) trendScore += 20
        else if (currentPrice > ema200) trendScore += 10
        
        val trendText = when {
            isBullishEMA -> "صعودی قدرتمند"
            isBearishEMA -> "نزولی قدرتمند"
            currentPrice > ema200 -> "صعودی میان‌مدت"
            else -> "رنج / نزولی"
        }

        // --- 2. MACD (15 pts) ---
        var macdScore = 0
        val macdResult = Indicators.calculateMACD(closes)
        val lastMacd = macdResult.macd.last()
        val lastSignal = macdResult.signal.last()
        val isMacdBullish = lastMacd > lastSignal
        
        if (isMacdBullish) {
            macdScore += 10
            if (lastMacd < BigDecimal.ZERO) macdScore += 5 
            reasons.add("تایید روند با تقاطع صعودی MACD")
        }

        // --- 3. Bollinger Bands (15 pts) ---
        var bbScore = 0
        val bbResult = Indicators.calculateBollingerBands(closes)
        val upper = bbResult.upper.last()
        val lower = bbResult.lower.last()
        val bbWidth = if (bbResult.middle.last().compareTo(BigDecimal.ZERO) != 0) 
            (upper.subtract(lower)).safeDiv(bbResult.middle.last()) 
            else BigDecimal.ZERO
        
        if (currentPrice < lower.multiply(BigDecimal("1.01"))) {
            bbScore += 15
            reasons.add("اشباع فروش در باند پایینی بولینگر")
        } else if (currentPrice > upper.multiply(BigDecimal("0.99"))) {
            bbScore -= 10
            warnings.add("برخورد به سقف باند بولینگر (احتمال اصلاح)")
        }
        
        if (bbWidth.compareTo(BigDecimal.ZERO) != 0 && bbWidth < BigDecimal("0.05")) {
            warnings.add("فشردگی شدید قیمت: احتمال جهش یا سقوط ناگهانی")
        }

        // --- 4. RSI & Technicals (15 pts) ---
        var technicalScore = 0
        val rsiList = Indicators.calculateRSI(closes, 14)
        val rsi = rsiList.lastOrNull() ?: BigDecimal.valueOf(50)
        when {
            rsi < BigDecimal("30") -> { technicalScore += 15; reasons.add("اشباع فروش RSI (قیمت جذاب)") }
            rsi > BigDecimal("70") -> { technicalScore -= 5; warnings.add("اشباع خرید RSI (ریسک اصلاح)") }
            rsi >= BigDecimal("40") && rsi <= BigDecimal("60") -> technicalScore += 5
        }

        // --- 5. Support / Resistance (15 pts) ---
        val (support, resistance) = findSupportResistance(candles)
        val distToSupport = if (currentPrice.compareTo(BigDecimal.ZERO) != 0) (currentPrice.subtract(support)).safeDiv(currentPrice) else BigDecimal.ZERO
        if (distToSupport < BigDecimal("0.03")) {
            technicalScore += 10
            reasons.add("نزدیکی به سطح حمایتی معتبر")
        }

        // --- 6. Volume & Liquidity (20 pts) ---
        var volScore = 0
        val volumes = candles.map { it.volume }
        val avgVol20 = calculateAverage(volumes.takeLast(20))
        val currentVol = volumes.last()
        
        if (currentVol > avgVol20.multiply(BigDecimal("1.5"))) {
            volScore += 10
            reasons.add("افزایش چشمگیر حجم معاملات")
        }
        
        val dailyVolume = asset.volume24hUsd ?: BigDecimal.ZERO
        val liquidityText = if (dailyVolume > BigDecimal("10000000")) "مناسب" else "پایین"

        // --- FINAL CALCULATION ---
        var finalScore = trendScore + macdScore + bbScore + technicalScore + volScore
        finalScore = finalScore.coerceIn(0, 100)
        
        val signal = when {
            finalScore >= 80 -> AnalysisSignal.STRONG_BUY
            finalScore >= 65 -> AnalysisSignal.BUY_ON_PULLBACK
            finalScore >= 50 -> AnalysisSignal.HOLD
            finalScore >= 30 -> AnalysisSignal.WAIT
            else -> AnalysisSignal.SELL
        }

        return TechnicalAnalysisResult(
            symbol = symbol,
            price = currentPrice,
            trend = trendText,
            volumeTrend = if (currentVol > avgVol20) "رو به رشد" else "ثابت",
            rsi = rsi,
            support = support,
            resistance = resistance,
            liquidity = liquidityText,
            riskScore = (100 - finalScore).coerceIn(0, 100),
            opportunityScore = finalScore,
            signal = signal,
            entryZone = if (finalScore > 60) Pair(currentPrice.multiply(BigDecimal("0.98")), currentPrice.multiply(BigDecimal("1.02"))) else null,
            stopLoss = if (finalScore > 60) support.multiply(BigDecimal("0.97")) else null,
            takeProfit = if (finalScore > 60) resistance.multiply(BigDecimal("0.97")) else null,
            riskReward = null,
            warnings = warnings,
            reasons = reasons
        )
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
