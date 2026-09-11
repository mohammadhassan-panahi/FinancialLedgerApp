package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.example.crypto.analysis.CandleStick
import com.example.ui.theme.EmeraldCore
import com.example.ui.theme.RoseCoral
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * A professional-grade Candlestick chart for financial assets.
 * Renders OHLC (Open, High, Low, Close) candles using Compose Canvas.
 */
@Composable
fun CandleStickChart(
    candles: List<CandleStick>,
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                "داده‌ای برای نمایش موجود نیست",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    // Determine price boundaries for normalization
    val minPrice = candles.minOf { it.low }
    val maxPrice = candles.maxOf { it.high }
    val priceRange = maxPrice.subtract(minPrice).coerceAtLeast(BigDecimal("0.00000001"))

    Canvas(modifier = modifier.padding(vertical = 4.dp)) {
        val width = size.width
        val height = size.height
        val candleWidth = width / candles.size
        val bodyPadding = (candleWidth * 0.15f).coerceAtMost(4f)

        candles.forEachIndexed { index, candle ->
            val isUp = candle.close >= candle.open
            val color = if (isUp) EmeraldCore else RoseCoral

            val centerX = index * candleWidth + candleWidth / 2
            
            // Map prices to Y coordinates (inverted because 0 is top)
            val highY = height - ((candle.high.subtract(minPrice)).divide(priceRange, 8, RoundingMode.HALF_UP).toFloat() * height)
            val lowY = height - ((candle.low.subtract(minPrice)).divide(priceRange, 8, RoundingMode.HALF_UP).toFloat() * height)
            val openY = height - ((candle.open.subtract(minPrice)).divide(priceRange, 8, RoundingMode.HALF_UP).toFloat() * height)
            val closeY = height - ((candle.close.subtract(minPrice)).divide(priceRange, 8, RoundingMode.HALF_UP).toFloat() * height)

            // 1. Draw the wicks (high to low)
            drawLine(
                color = color,
                start = Offset(centerX, highY),
                end = Offset(centerX, lowY),
                strokeWidth = 1.dp.toPx()
            )

            // 2. Draw the candle body
            val topBodyY = minOf(openY, closeY)
            val bottomBodyY = maxOf(openY, closeY)
            val bodyHeight = (bottomBodyY - topBodyY).coerceAtLeast(1.dp.toPx())

            drawRect(
                color = color,
                topLeft = Offset(index * candleWidth + bodyPadding, topBodyY),
                size = Size(candleWidth - 2 * bodyPadding, bodyHeight)
            )
        }
    }
}
