package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.LossRed
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.ProfitGreen
import com.example.util.PersianNumberUtils

val chartColors = listOf(
    Color(0xFFC9A24B), // Gold
    Color(0xFF1B2A41), // Navy
    Color(0xFF4C7A5C), // Green
    Color(0xFFA8453A), // Red
    Color(0xFF5B85AA), // Steel Blue
    Color(0xFF8E44AD), // Purple
    Color(0xFFD35400)  // Pumpkin
)

// --- 1. PIE / DONUT CHART ---
data class PieChartSlice(
    val label: String,
    val value: Double,
    val color: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComposePieChart(
    slices: List<PieChartSlice>,
    modifier: Modifier = Modifier
) {
    val total = slices.sumOf { it.value }
    if (total <= 0) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("داده‌ای برای نمایش نمودار وجود ندارد", style = MaterialTheme.typography.bodySmall)
        }
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                var startAngle = -90f
                slices.forEach { slice ->
                    val sweepAngle = ((slice.value / total) * 360f).toFloat()
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 28.dp.toPx())
                    )
                    startAngle += sweepAngle
                }
            }
            Text(
                text = PersianNumberUtils.formatCurrency(total, showSuffix = false),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            slices.forEach { slice ->
                val pct = (slice.value / total) * 100
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(slice.color, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${slice.label} (${PersianNumberUtils.formatPercent(pct)})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// --- 2. LINE CHART ---
data class LineChartSeries(
    val name: String,
    val points: List<Pair<Double, Double>>, // (x: year/index, y: value)
    val color: Color,
    val isDotted: Boolean = false
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComposeLineChart(
    seriesList: List<LineChartSeries>,
    modifier: Modifier = Modifier,
    xAxisLabel: String = "سال",
    yAxisLabel: String = "ارزش (تومان)"
) {
    val allY = seriesList.flatMap { it.points.map { p -> p.second } }
    if (allY.isEmpty()) return

    val minY = 0.0
    val maxY = (allY.maxOrNull() ?: 1.0) * 1.1

    val allX = seriesList.flatMap { it.points.map { p -> p.first } }
    val minX = allX.minOrNull() ?: 0.0
    val maxX = allX.maxOrNull() ?: 1.0

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(yAxisLabel, style = MaterialTheme.typography.labelSmall, color = AccentGold)
            Text(
                "حداکثر: ${PersianNumberUtils.formatCurrency(maxY, showSuffix = false)}",
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        ) {
            val width = size.width
            val height = size.height

            // Grid lines
            val gridLines = 4
            for (g in 0..gridLines) {
                val y = height - (g * (height / gridLines))
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }

            seriesList.forEach { series ->
                if (series.points.size < 2) return@forEach

                val path = Path()
                series.points.forEachIndexed { idx, pt ->
                    val xRatio = if (maxX > minX) (pt.first - minX) / (maxX - minX) else 0.0
                    val yRatio = if (maxY > minY) (pt.second - minY) / (maxY - minY) else 0.0

                    val x = (xRatio * width).toFloat()
                    val y = (height - (yRatio * height)).toFloat()

                    if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)

                    // Draw point node
                    drawCircle(
                        color = series.color,
                        radius = 4f,
                        center = Offset(x, y)
                    )
                }

                val pathEffect = if (series.isDotted) PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) else null
                drawPath(
                    path = path,
                    color = series.color,
                    style = Stroke(width = 3f, pathEffect = pathEffect)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            seriesList.forEach { s ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp, 3.dp)
                            .background(s.color)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(s.name, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

// --- 3. BAR CHART ---
data class BarChartItem(
    val label: String,
    val value: Double,
    val color: Color
)

@Composable
fun ComposeBarChart(
    items: List<BarChartItem>,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    val maxValue = (items.maxOfOrNull { it.value } ?: 1.0) * 1.1

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        ) {
            val width = size.width
            val height = size.height
            val barWidth = (width / (items.size * 2)).coerceIn(16f, 40f)

            items.forEachIndexed { idx, bar ->
                val ratio = if (maxValue > 0) bar.value / maxValue else 0.0
                val barHeight = (ratio * height).toFloat()
                val x = idx * (width / items.size) + (width / items.size - barWidth) / 2
                val y = height - barHeight

                drawRect(
                    color = bar.color,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            items.forEach { bar ->
                Text(
                    text = bar.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp
                )
            }
        }
    }
}
