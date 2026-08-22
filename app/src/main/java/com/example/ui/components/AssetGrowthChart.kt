package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CredifyIndigo
import com.example.ui.theme.CredifyViolet
import com.example.ui.theme.DarkSlateSecondary
import com.example.ui.theme.DarkSlateSurface
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import java.text.NumberFormat
import java.util.Locale

/**
 * Data point model representing a label-value pair for the Asset Growth Chart.
 */
data class ChartDataPoint(
    val label: String,
    val value: Float
)

/**
 * Converts English digits to Persian digits for UI consistency.
 */
private fun toPersianDigits(input: String): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return input.map { char ->
        if (char in '0'..'9') {
            persianDigits[char - '0']
        } else {
            char
        }
    }.joinToString("")
}

/**
 * Formats a raw numerical amount into a localized Persian currency string.
 */
private fun formatChartAmount(amount: Float, currency: String = "تومان"): String {
    val formattedNumber = NumberFormat.getNumberInstance(Locale.US).format(amount.toLong())
    return "${toPersianDigits(formattedNumber)} $currency"
}

/**
 * High-performance, animated Line Chart component designed for the Credify Dark Fintech theme.
 * Features smooth cubic bezier curves, touch gesture selection, floating tooltips, and progressive drawing animation.
 */
@Composable
fun AssetGrowthChart(
    dataPoints: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    title: String = "روند رشد دارایی‌ها",
    currencySymbol: String = "تومان",
    onPointSelected: ((ChartDataPoint?) -> Unit)? = null
) {
    val points = if (dataPoints.isEmpty()) {
        listOf(
            ChartDataPoint("۱۴۰۳/۰۱", 1000000000f),
            ChartDataPoint("۱۴۰۳/۰۲", 1500000000f),
            ChartDataPoint("۱۴۰۳/۰۳", 1800000000f),
            ChartDataPoint("۱۴۰۳/۰۴", 2200000000f),
            ChartDataPoint("۱۴۰۳/۰۵", 2450800000f)
        )
    } else {
        dataPoints
    }

    // Entrance path drawing animation
    val animatableProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animatableProgress.snapTo(0f)
        animatableProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val latestPoint = points.lastOrNull()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("asset_growth_chart_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary),
        border = BorderStroke(1.dp, SlateBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = CredifyIndigo.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = null,
                                tint = CredifyIndigo,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = TextLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (latestPoint != null) {
                            Text(
                                text = "آخرین بروزرسانی: ${latestPoint.label}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (latestPoint != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EmeraldProfit.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, EmeraldProfit.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = formatChartAmount(latestPoint.value, currencySymbol),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = EmeraldProfit,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tooltip Floating Header when point selected
            val activePoint = selectedIndex?.let { points.getOrNull(it) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                contentAlignment = Alignment.Center
            ) {
                if (activePoint != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = DarkSlateSurface,
                        border = BorderStroke(1.dp, CredifyIndigo.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = activePoint.label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "•",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            Text(
                                text = formatChartAmount(activePoint.value, currencySymbol),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = TextLight,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    Text(
                        text = "برای دیدن جزئیات، روی نمودار لمس کنید",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextMuted.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Canvas Line Chart Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                androidx.compose.foundation.Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .pointerInput(points) {
                            detectTapGestures { offset ->
                                val width = size.width.toFloat()
                                val pointWidth = width / (points.size - 1).coerceAtLeast(1)
                                val index = (offset.x / pointWidth)
                                    .toInt()
                                    .coerceIn(0, points.size - 1)
                                selectedIndex = index
                                onPointSelected?.invoke(points[index])
                            }
                        }
                        .pointerInput(points) {
                            detectDragGestures { change, _ ->
                                val width = size.width.toFloat()
                                val pointWidth = width / (points.size - 1).coerceAtLeast(1)
                                val index = (change.position.x / pointWidth)
                                    .toInt()
                                    .coerceIn(0, points.size - 1)
                                selectedIndex = index
                                onPointSelected?.invoke(points[index])
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    val topPadding = 16.dp.toPx()
                    val bottomPadding = 24.dp.toPx()
                    val usableHeight = canvasHeight - topPadding - bottomPadding

                    val minValue = points.minOfOrNull { it.value } ?: 0f
                    val maxValue = points.maxOfOrNull { it.value } ?: 100f
                    val valueRange = if (maxValue == minValue) 1f else (maxValue - minValue)

                    // Draw Horizontal Gridlines
                    val gridLineCount = 4
                    val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    for (i in 0 until gridLineCount) {
                        val y = topPadding + (usableHeight / (gridLineCount - 1)) * i
                        drawLine(
                            color = SlateBorder.copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = dashPathEffect
                        )
                    }

                    // Compute Point Offsets
                    val computedPoints = points.mapIndexed { index, point ->
                        val x = if (points.size > 1) {
                            (canvasWidth / (points.size - 1)) * index
                        } else {
                            canvasWidth / 2f
                        }
                        val normalizedValue = (point.value - minValue) / valueRange
                        val y = (topPadding + usableHeight) - (normalizedValue * usableHeight)
                        Offset(x, y)
                    }

                    if (computedPoints.size >= 2) {
                        // Construct Cubic Bezier Curve Path
                        val strokePath = Path().apply {
                            moveTo(computedPoints.first().x, computedPoints.first().y)
                            for (i in 0 until computedPoints.size - 1) {
                                val p1 = computedPoints[i]
                                val p2 = computedPoints[i + 1]
                                val controlX = (p1.x + p2.x) / 2f
                                cubicTo(
                                    x1 = controlX,
                                    y1 = p1.y,
                                    x2 = controlX,
                                    y2 = p2.y,
                                    x3 = p2.x,
                                    y3 = p2.y
                                )
                            }
                        }

                        // Construct Area Fill Path
                        val fillPath = Path().apply {
                            addPath(strokePath)
                            lineTo(computedPoints.last().x, canvasHeight - bottomPadding)
                            lineTo(computedPoints.first().x, canvasHeight - bottomPadding)
                            close()
                        }

                        // Clip drawing based on entrance animation progress
                        clipRect(right = canvasWidth * animatableProgress.value) {
                            // Draw Gradient Area Fill under curve
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        CredifyIndigo.copy(alpha = 0.35f),
                                        CredifyIndigo.copy(alpha = 0.0f)
                                    ),
                                    startY = topPadding,
                                    endY = canvasHeight - bottomPadding
                                )
                            )

                            // Draw Gradient Line Curve
                            drawPath(
                                path = strokePath,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(CredifyIndigo, CredifyViolet)
                                ),
                                style = Stroke(
                                    width = 3.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )

                            // Draw Data Point Circles
                            computedPoints.forEach { pt ->
                                drawCircle(
                                    color = DarkSlateSecondary,
                                    radius = 5.dp.toPx(),
                                    center = pt
                                )
                                drawCircle(
                                    color = CredifyIndigo,
                                    radius = 3.dp.toPx(),
                                    center = pt
                                )
                            }
                        }

                        // Highlight Selected Point & Draw Vertical Indicator Line
                        selectedIndex?.let { index ->
                            computedPoints.getOrNull(index)?.let { selectedPoint ->
                                // Vertical Indicator Line
                                drawLine(
                                    color = CredifyIndigo.copy(alpha = 0.8f),
                                    start = Offset(selectedPoint.x, topPadding),
                                    end = Offset(selectedPoint.x, canvasHeight - bottomPadding),
                                    strokeWidth = 1.5.dp.toPx(),
                                    pathEffect = dashPathEffect
                                )

                                // Outer Glow Halo
                                drawCircle(
                                    color = CredifyIndigo.copy(alpha = 0.25f),
                                    radius = 12.dp.toPx(),
                                    center = selectedPoint
                                )

                                // Inner Highlight Dot
                                drawCircle(
                                    color = Color.White,
                                    radius = 5.dp.toPx(),
                                    center = selectedPoint
                                )
                                drawCircle(
                                    color = CredifyIndigo,
                                    radius = 3.dp.toPx(),
                                    center = selectedPoint
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // X-Axis Date Labels Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Display up to 5 distributed date labels
                val labelStep = (points.size / 4).coerceAtLeast(1)
                points.filterIndexed { index, _ ->
                    index == 0 || index == points.size - 1 || index % labelStep == 0
                }.take(5).forEach { dataPoint ->
                    Text(
                        text = dataPoint.label,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
