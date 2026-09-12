package com.example.crypto.analysis

import com.example.util.safeDiv
import com.example.util.sumOf
import java.math.BigDecimal
import java.math.RoundingMode

object Indicators {

    /**
     * Calculates Exponential Moving Average (EMA) for the entire series.
     */
    fun calculateEMA(values: List<BigDecimal>, period: Int): List<BigDecimal> {
        if (values.size < period) return List(values.size) { BigDecimal.ZERO }
        val emaList = mutableListOf<BigDecimal>()
        val multiplier = BigDecimal("2").safeDiv(BigDecimal.valueOf((period + 1).toLong()))
        
        // Simple average for the first EMA value
        var currentEma = values.take(period).sumOf { it }.safeDiv(BigDecimal.valueOf(period.toLong()))
        emaList.add(currentEma)

        for (i in period until values.size) {
            currentEma = (values[i].subtract(currentEma)).multiply(multiplier).add(currentEma)
            emaList.add(currentEma)
        }
        
        // Pad the beginning with zeros to match the input size
        val padding = List(period - 1) { BigDecimal.ZERO }
        return padding + emaList
    }

    /**
     * Calculates Relative Strength Index (RSI) for the entire series.
     */
    fun calculateRSI(values: List<BigDecimal>, period: Int): List<BigDecimal> {
        if (values.size <= period) return List(values.size) { BigDecimal.valueOf(50) }
        val rsiList = mutableListOf<BigDecimal>()
        val changes = values.zipWithNext { a, b -> b.subtract(a) }
        
        var avgGain = changes.take(period).filter { it > BigDecimal.ZERO }.sumOf { it }.safeDiv(BigDecimal.valueOf(period.toLong()))
        var avgLoss = changes.take(period).filter { it < BigDecimal.ZERO }.sumOf { it.abs() }.safeDiv(BigDecimal.valueOf(period.toLong()))

        // Initial RSI
        fun computeRsi(g: BigDecimal, l: BigDecimal): BigDecimal {
            if (l.compareTo(BigDecimal.ZERO) == 0) return BigDecimal("100")
            val rs = g.safeDiv(l)
            return BigDecimal("100").subtract(BigDecimal("100").safeDiv(BigDecimal.ONE.add(rs)))
        }

        rsiList.add(computeRsi(avgGain, avgLoss))

        for (i in period until changes.size) {
            val change = changes[i]
            val gain = if (change > BigDecimal.ZERO) change else BigDecimal.ZERO
            val loss = if (change < BigDecimal.ZERO) change.abs() else BigDecimal.ZERO
            
            avgGain = (avgGain.multiply(BigDecimal.valueOf((period - 1).toLong())).add(gain)).safeDiv(BigDecimal.valueOf(period.toLong()))
            avgLoss = (avgLoss.multiply(BigDecimal.valueOf((period - 1).toLong())).add(loss)).safeDiv(BigDecimal.valueOf(period.toLong()))
            
            rsiList.add(computeRsi(avgGain, avgLoss))
        }

        val padding = List(period) { BigDecimal.valueOf(50) }
        return padding + rsiList
    }

    /**
     * Moving Average Convergence Divergence (MACD).
     */
    fun calculateMACD(values: List<BigDecimal>): MacdResult {
        val ema12 = calculateEMA(values, 12)
        val ema26 = calculateEMA(values, 26)
        
        val macdLine = ema12.zip(ema26) { fast, slow ->
            if (fast == BigDecimal.ZERO || slow == BigDecimal.ZERO) BigDecimal.ZERO 
            else fast.subtract(slow)
        }
        
        val signalLine = calculateEMA(macdLine, 9)
        val histogram = macdLine.zip(signalLine) { m, s -> m.subtract(s) }
        
        return MacdResult(macdLine, signalLine, histogram)
    }

    /**
     * Bollinger Bands calculation.
     */
    fun calculateBollingerBands(values: List<BigDecimal>, period: Int = 20, multiplier: BigDecimal = BigDecimal("2")): BollingerResult {
        if (values.size < period) return BollingerResult(emptyList(), emptyList(), emptyList())
        
        val middle = mutableListOf<BigDecimal>()
        val upper = mutableListOf<BigDecimal>()
        val lower = mutableListOf<BigDecimal>()
        
        for (i in 0 until values.size) {
            if (i < period - 1) {
                middle.add(BigDecimal.ZERO)
                upper.add(BigDecimal.ZERO)
                lower.add(BigDecimal.ZERO)
                continue
            }
            
            val window = values.subList(i - period + 1, i + 1)
            val avg = window.sumOf { it }.safeDiv(BigDecimal.valueOf(period.toLong()))
            
            // Standard Deviation
            val variance = window.map { it.subtract(avg).pow(2) }.sumOf { it }
                .safeDiv(BigDecimal.valueOf(period.toLong()))
            val stdDev = Math.sqrt(variance.toDouble()).toBigDecimal().setScale(8, RoundingMode.HALF_UP)
            
            middle.add(avg)
            upper.add(avg.add(stdDev.multiply(multiplier)))
            lower.add(avg.subtract(stdDev.multiply(multiplier)))
        }
        
        return BollingerResult(middle, upper, lower)
    }
}

data class MacdResult(val macd: List<BigDecimal>, val signal: List<BigDecimal>, val histogram: List<BigDecimal>)
data class BollingerResult(val middle: List<BigDecimal>, val upper: List<BigDecimal>, val lower: List<BigDecimal>)
