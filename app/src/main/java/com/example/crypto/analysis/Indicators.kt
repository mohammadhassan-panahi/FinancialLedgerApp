package com.example.crypto.analysis

import com.example.util.safeDiv
import com.example.util.sumOf
import java.math.BigDecimal

object Indicators {

    /**
     * Calculates Exponential Moving Average (EMA) for the entire series.
     */
    fun calculateEMA(values: List<BigDecimal>, period: Int): List<BigDecimal> {
        if (values.size < period) return emptyList()
        val emaList = mutableListOf<BigDecimal>()
        val multiplier = BigDecimal("2").safeDiv(BigDecimal.valueOf((period + 1).toLong()))
        
        // Simple average for the first EMA value
        var currentEma = values.take(period).sumOf { it }.safeDiv(BigDecimal.valueOf(period.toLong()))
        emaList.add(currentEma)

        for (i in period until values.size) {
            currentEma = (values[i].subtract(currentEma)).multiply(multiplier).add(currentEma)
            emaList.add(currentEma)
        }
        
        // Pad the beginning with nulls or zeros to match the input size
        val padding = List(period - 1) { BigDecimal.ZERO }
        return padding + emaList
    }

    /**
     * Calculates Relative Strength Index (RSI) for the entire series.
     */
    fun calculateRSI(values: List<BigDecimal>, period: Int): List<BigDecimal> {
        if (values.size <= period) return emptyList()
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

        // Pad to match original values size (period + 1 values used for first RSI)
        val padding = List(period) { BigDecimal.valueOf(50) }
        return padding + rsiList
    }
}
