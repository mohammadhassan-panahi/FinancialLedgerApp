package com.example.crypto.analysis

data class CandleStick(
    val time: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

enum class MarketTrend {
    BULLISH, BEARISH, SIDEWAYS, REVERSAL_POSSIBLE
}

enum class VolumeState {
    BUYING_PRESSURE, WEAK_RALLY, SELLING_PRESSURE, WEAK_SELLING, NORMAL
}

enum class AnalysisSignal {
    STRONG_BUY, BUY_PULLBACK, BREAKOUT_WATCH, HOLD, WAIT, SELL_PARTIAL, SELL_NOW, AVOID
}

data class TechnicalAnalysisResult(
    val symbol: String,
    val score: Int, // 0-100
    val signal: AnalysisSignal,
    val rsi: Double,
    val ema20: Double,
    val ema50: Double,
    val ema200: Double,
    val support: Double,
    val resistance: Double,
    val reasons: List<String>
)
