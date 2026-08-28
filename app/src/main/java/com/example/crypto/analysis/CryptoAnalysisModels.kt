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
    STRONG_BUY, BUY_ON_PULLBACK, BREAKOUT_WATCH, HOLD, WAIT, SELL_PARTIAL, SELL, AVOID, INSUFFICIENT_DATA
}

data class TechnicalAnalysisResult(
    val symbol: String,
    val price: Double,
    val trend: String, // Bullish, Bearish, Sideways
    val volumeTrend: String, // Increasing, Decreasing, Spike
    val rsi: Double,
    val support: Double,
    val resistance: Double,
    val liquidity: String, // High, Medium, Low
    val riskScore: Int, // 0-100
    val opportunityScore: Int, // 0-100
    val signal: AnalysisSignal,
    val entryZone: Pair<Double, Double>?,
    val stopLoss: Double?,
    val takeProfit: Double?,
    val riskReward: Double?,
    val warnings: List<String>,
    val reasons: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)
