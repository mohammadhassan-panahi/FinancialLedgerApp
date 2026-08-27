package com.example.crypto.analysis

import com.example.data.local.CryptoAssetEntity
import kotlin.math.abs

object TechnicalAnalysisEngine {

    /**
     * Performs a comprehensive technical analysis on a cryptocurrency based on historical candles
     * and current market data.
     */
    fun analyze(
        symbol: String,
        candles: List<CandleStick>,
        currentMarket: CryptoAssetEntity
    ): TechnicalAnalysisResult {
        // We need a decent amount of data for EMA200 and stable RSI/EMA calculations
        if (candles.size < 200) {
            return TechnicalAnalysisResult(
                symbol = symbol,
                score = 50,
                signal = AnalysisSignal.WAIT,
                rsi = 0.0,
                ema20 = 0.0,
                ema50 = 0.0,
                ema200 = 0.0,
                support = 0.0,
                resistance = 0.0,
                reasons = listOf("داده‌های کافی برای تحلیل تکنیکال موجود نیست (حداقل ۲۰۰ شمع مورد نیاز است)")
            )
        }

        val closes = candles.map { it.close }
        val latestPrice = closes.last()

        // 1. Technical Indicators
        val rsi = calculateRSI(closes, 14)
        val ema20 = calculateEMA(closes, 20)
        val ema50 = calculateEMA(closes, 50)
        val ema200 = calculateEMA(closes, 200)

        // 2. Support and Resistance (Simplified local extremes)
        val (support, resistance) = findSupportResistance(candles)

        // 3. Volume Analysis
        val recentCandles = candles.takeLast(20)
        val avgVolume = recentCandles.map { it.volume }.average()
        val currentVolume = candles.last().volume

        val reasons = mutableListOf<String>()
        var score = 50

        // RSI Logic
        when {
            rsi < 30 -> {
                score += 20
                reasons.add("اشباع فروش (RSI: ${rsi.toInt()}): احتمال بازگشت قیمت به سمت بالا")
            }
            rsi > 70 -> {
                score -= 20
                reasons.add("اشباع خرید (RSI: ${rsi.toInt()}): احتمال اصلاح قیمت یا بازگشت روند")
            }
            rsi in 40.0..60.0 -> {
                reasons.add("شاخص قدرت نسبی (RSI) در وضعیت متعادل قرار دارد")
            }
        }

        // EMA Trend Logic
        if (latestPrice > ema20 && ema20 > ema50 && ema50 > ema200) {
            score += 25
            reasons.add("روند صعودی قوی (Golden Alignment): قیمت بالای تمام میانگین‌های اصلی است")
        } else if (latestPrice < ema20 && ema20 < ema50 && ema50 < ema200) {
            score -= 25
            reasons.add("روند نزولی قوی: قیمت پایین‌تر از میانگین‌های متحرک ۲۰، ۵۰ و ۲۰۰ است")
        } else {
            if (latestPrice > ema20) score += 5 else score -= 5
            if (ema20 > ema50) score += 5 else score -= 5
        }

        // Volume logic
        if (currentVolume > avgVolume * 1.5) {
            val priceChange = latestPrice - closes[closes.size - 2]
            if (priceChange > 0) {
                score += 10
                reasons.add("افزایش حجم همراه با صعود قیمت: تایید ورود نقدینگی هوشمند")
            } else {
                score -= 10
                reasons.add("افزایش حجم همراه با نزول قیمت: تایید فشار فروش سنگین")
            }
        }

        // Support/Resistance proximity
        val proximityThreshold = 0.02 // 2%
        if (abs(latestPrice - support) / latestPrice <= proximityThreshold) {
            score += 10
            reasons.add("قیمت در نزدیکی سطح حمایتی معتبر قرار دارد")
        }
        if (abs(latestPrice - resistance) / latestPrice <= proximityThreshold) {
            score -= 10
            reasons.add("قیمت در نزدیکی سطح مقاومتی قوی قرار دارد")
        }

        // Final Score & Signal
        score = score.coerceIn(0, 100)
        val signal = when {
            score >= 80 -> AnalysisSignal.STRONG_BUY
            score >= 65 -> AnalysisSignal.BUY_PULLBACK
            score >= 55 -> AnalysisSignal.BREAKOUT_WATCH
            score >= 45 -> AnalysisSignal.HOLD
            score >= 30 -> AnalysisSignal.WAIT
            score >= 15 -> AnalysisSignal.SELL_PARTIAL
            else -> AnalysisSignal.SELL_NOW
        }

        return TechnicalAnalysisResult(
            symbol = symbol,
            score = score,
            signal = signal,
            rsi = rsi,
            ema20 = ema20,
            ema50 = ema50,
            ema200 = ema200,
            support = support,
            resistance = resistance,
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
        val rs = avgGain / avgLoss
        return 100.0 - (100.0 / (1.0 + rs))
    }

    private fun calculateEMA(values: List<Double>, period: Int): Double {
        if (values.size < period) return values.lastOrNull() ?: 0.0
        val multiplier = 2.0 / (period + 1)
        var ema = values.take(period).average() // Start with SMA

        for (i in period until values.size) {
            ema = (values[i] - ema) * multiplier + ema
        }
        return ema
    }

    private fun findSupportResistance(candles: List<CandleStick>): Pair<Double, Double> {
        if (candles.isEmpty()) return 0.0 to 0.0
        val last100 = candles.takeLast(100)
        val support = last100.minOf { it.low }
        val resistance = last100.maxOf { it.high }
        return support to resistance
    }
}
