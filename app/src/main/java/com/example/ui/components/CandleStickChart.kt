package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.crypto.analysis.CandleStick
import com.example.crypto.analysis.Indicators
import com.example.ui.theme.EmeraldCore
import com.example.ui.theme.RoseCoral
import com.example.ui.theme.Slate600
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * A professional-grade Candlestick chart for financial assets.
 * Renders OHLC candles and technical indicators (EMA, RSI, MACD, Bollinger Bands).
 */
@Composable
fun CandleStickChart(
    candles: List<CandleStick>,
    modifier: Modifier = Modifier,
    showEMA: Boolean = true,
    showRSI: Boolean = true,
    showMACD: Boolean = false,
    showBollinger: Boolean = false
) {
    if (candles.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("داده‌ای برای نمایش موجود نیست", style = MaterialTheme.typography.labelSmall, color = Slate600)
        }
        return
    }

    val closes = remember(candles) { candles.map { it.close } }
    
    // Calculate Indicators
    val ema20 = remember(closes) { Indicators.calculateEMA(closes, 20) }
    val ema50 = remember(closes) { Indicators.calculateEMA(closes, 50) }
    val rsi = remember(closes) { Indicators.calculateRSI(closes, 14) }
    val macd = remember(closes) { Indicators.calculateMACD(closes) }
    val bollinger = remember(closes) { Indicators.calculateBollingerBands(closes) }

    Column(modifier = modifier) {
        // 1. Price Panel (Main)
        Box(modifier = Modifier.weight(0.6f).fillMaxWidth()) {
            PriceChartCanvas(
                candles = candles,
                ema20 = if (showEMA) ema20 else emptyList(),
                ema50 = if (showEMA) ema50 else emptyList(),
                bollinger = if (showBollinger) bollinger else null
            )
        }

        // 2. RSI Panel
        if (showRSI) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.weight(0.2f).fillMaxWidth()) {
                RsiChartCanvas(rsi = rsi)
            }
        }

        // 3. MACD Panel
        if (showMACD) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.weight(0.2f).fillMaxWidth()) {
                MacdChartCanvas(macd = macd)
            }
        }
    }
}

@Composable
fun PriceChartCanvas(
    candles: List<CandleStick>,
    ema20: List<BigDecimal>,
    ema50: List<BigDecimal>,
    bollinger: com.example.crypto.analysis.BollingerResult?
) {
    val minPrice = candles.minOf { it.low }
    val maxPrice = candles.maxOf { it.high }
    val priceRange = maxPrice.subtract(minPrice).coerceAtLeast(BigDecimal("0.00000001"))

    Canvas(modifier = Modifier.fillMaxSize().padding(vertical = 4.dp)) {
        val width = size.width
        val height = size.height
        val candleWidth = width / candles.size
        val bodyPadding = (candleWidth * 0.15f).coerceAtMost(4f)

        fun mapY(price: BigDecimal): Float {
            return height - ((price.subtract(minPrice)).divide(priceRange, 8, RoundingMode.HALF_UP).toFloat() * height)
        }

        // Bollinger Bands Area
        bollinger?.let { bb ->
            if (bb.upper.isNotEmpty() && bb.lower.isNotEmpty()) {
                val path = Path()
                var started = false
                // Top line
                bb.upper.forEachIndexed { i, v ->
                    if (v > BigDecimal.ZERO) {
                        val x = i * candleWidth + candleWidth / 2
                        val y = mapY(v)
                        if (!started) { path.moveTo(x, y); started = true } else path.lineTo(x, y)
                    }
                }
                // Bottom line (reverse)
                for (i in bb.lower.size - 1 downTo 0) {
                    val v = bb.lower[i]
                    if (v > BigDecimal.ZERO) {
                        val x = i * candleWidth + candleWidth / 2
                        val y = mapY(v)
                        path.lineTo(x, y)
                    }
                }
                path.close()
                drawPath(path = path, color = Color(0xFF6366F1).copy(alpha = 0.1f))
                
                // Draw BB lines
                drawIndicatorLine(bb.upper, candleWidth, Color(0xFF6366F1).copy(alpha = 0.3f), ::mapY)
                drawIndicatorLine(bb.lower, candleWidth, Color(0xFF6366F1).copy(alpha = 0.3f), ::mapY)
            }
        }

        // Candles
        candles.forEachIndexed { index, candle ->
            val isUp = candle.close >= candle.open
            val color = if (isUp) EmeraldCore else RoseCoral
            val centerX = index * candleWidth + candleWidth / 2
            
            val highY = mapY(candle.high)
            val lowY = mapY(candle.low)
            val openY = mapY(candle.open)
            val closeY = mapY(candle.close)

            drawLine(color = color, start = Offset(centerX, highY), end = Offset(centerX, lowY), strokeWidth = 1.dp.toPx())
            val topBodyY = minOf(openY, closeY)
            val bottomBodyY = maxOf(openY, closeY)
            drawRect(color = color, topLeft = Offset(index * candleWidth + bodyPadding, topBodyY), size = Size(candleWidth - 2 * bodyPadding, (bottomBodyY - topBodyY).coerceAtLeast(1.dp.toPx())))
        }

        // EMA Lines
        if (ema20.isNotEmpty()) drawIndicatorLine(ema20, candleWidth, Color(0xFF3B82F6), ::mapY)
        if (ema50.isNotEmpty()) drawIndicatorLine(ema50, candleWidth, Color(0xFFF59E0B), ::mapY)
    }
}

@Composable
fun RsiChartCanvas(rsi: List<BigDecimal>) {
    Canvas(modifier = Modifier.fillMaxSize().padding(vertical = 4.dp)) {
        val width = size.width
        val height = size.height
        val stepX = width / rsi.size

        val y30 = height - (30f / 100f * height)
        val y70 = height - (70f / 100f * height)
        drawLine(color = Color.Gray.copy(alpha = 0.3f), start = Offset(0f, y30), end = Offset(width, y30), strokeWidth = 1.dp.toPx())
        drawLine(color = Color.Gray.copy(alpha = 0.3f), start = Offset(0f, y70), end = Offset(width, y70), strokeWidth = 1.dp.toPx())

        if (rsi.isNotEmpty()) {
            val path = Path()
            var started = false
            rsi.forEachIndexed { index, value ->
                val x = index * stepX + stepX / 2
                val y = height - (value.toFloat() / 100f * height)
                if (!started) { path.moveTo(x, y); started = true } else path.lineTo(x, y)
            }
            drawPath(path = path, color = Color(0xFF8B5CF6), style = Stroke(width = 1.5.dp.toPx()))
        }
    }
}

@Composable
fun MacdChartCanvas(macd: com.example.crypto.analysis.MacdResult) {
    val maxHist = macd.histogram.map { it.abs() }.maxByOrNull { it } ?: BigDecimal.ONE
    
    Canvas(modifier = Modifier.fillMaxSize().padding(vertical = 4.dp)) {
        val width = size.width
        val height = size.height
        val stepX = width / macd.histogram.size
        val centerY = height / 2

        macd.histogram.forEachIndexed { i, v ->
            val x = i * stepX + stepX / 2
            val barHeight = (v.divide(maxHist, 8, RoundingMode.HALF_UP).toFloat() * (height / 2))
            val color = if (v >= BigDecimal.ZERO) EmeraldCore.copy(alpha = 0.5f) else RoseCoral.copy(alpha = 0.5f)
            drawLine(color = color, start = Offset(x, centerY), end = Offset(x, centerY - barHeight), strokeWidth = stepX * 0.8f)
        }
        
        // Simplified MACD and Signal Lines (too small for detailed lines usually, but we draw them)
        // Note: For better visibility, we scale them same as histogram here
        drawIndicatorLine(macd.macd, stepX, Color.Blue.copy(alpha = 0.7f)) { v ->
            centerY - (v.divide(maxHist, 8, RoundingMode.HALF_UP).toFloat() * (height / 2))
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawIndicatorLine(
    values: List<BigDecimal>,
    stepX: Float,
    color: Color,
    mapY: (BigDecimal) -> Float
) {
    val path = Path()
    var started = false
    values.forEachIndexed { index, value ->
        if (value.compareTo(BigDecimal.ZERO) != 0) {
            val x = index * stepX + stepX / 2
            val y = mapY(value)
            if (!started) {
                path.moveTo(x, y)
                started = true
            } else {
                path.lineTo(x, y)
            }
        }
    }
    drawPath(path = path, color = color, style = Stroke(width = 1.5.dp.toPx()))
}
