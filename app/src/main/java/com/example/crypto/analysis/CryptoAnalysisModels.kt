package com.example.crypto.analysis

import java.math.BigDecimal

data class CandleStick(
    val time: Long,
    val open: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val close: BigDecimal,
    val volume: BigDecimal
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
    val price: BigDecimal,
    val trend: String, // Bullish, Bearish, Sideways
    val volumeTrend: String, // Increasing, Decreasing, Spike
    val rsi: BigDecimal,
    val support: BigDecimal,
    val resistance: BigDecimal,
    val liquidity: String, // High, Medium, Low
    val riskScore: Int, // 0-100
    val opportunityScore: Int, // 0-100
    val signal: AnalysisSignal,
    val entryZone: Pair<BigDecimal, BigDecimal>?,
    val stopLoss: BigDecimal?,
    val takeProfit: BigDecimal?,
    val riskReward: BigDecimal?,
    val warnings: List<String>,
    val reasons: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)
