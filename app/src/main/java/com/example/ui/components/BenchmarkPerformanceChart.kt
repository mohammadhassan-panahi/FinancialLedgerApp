package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.util.Locale

data class BenchmarkPoint(
    val date: String,
    val portfolioValue: Double,
    val goldValue: Double,
    val usdValue: Double
)

@Composable
fun BenchmarkPerformanceChart(
    points: List<BenchmarkPoint>,
    modifier: Modifier = Modifier
) {
    if (points.size < 2) {
        EmptyBenchmarkState()
        return
    }

    // Normalize points to % return from start
    val startP = points.first().portfolioValue
    val startG = points.first().goldValue
    val startU = points.first().usdValue

    val normalizedPoints = points.map {
        BenchmarkPoint(
            date = it.date,
            portfolioValue = if (startP > 0) ((it.portfolioValue - startP) / startP) * 100 else 0.0,
            goldValue = if (startG > 0) ((it.goldValue - startG) / startG) * 100 else 0.0,
            usdValue = if (startU > 0) ((it.usdValue - startU) / startU) * 100 else 0.0
        )
    }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animProgress.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary),
        border = BorderStroke(0.5.dp, SlateBorderLight)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.CompareArrows, null, tint = CredifyIndigo, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("از طلا و دلار جلو زدی؟", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val padding = 20.dp.toPx()
                    
                    val allValues = normalizedPoints.flatMap { listOf(it.portfolioValue, it.goldValue, it.usdValue) }
                    val minV = allValues.min().toFloat()
                    val maxV = allValues.max().toFloat()
                    val range = if (maxV == minV) 1f else maxV - minV

                    fun getOffset(valPercent: Double, index: Int): Offset {
                        val x = (w / (normalizedPoints.size - 1)) * index
                        val norm = (valPercent.toFloat() - minV) / range
                        val y = h - padding - (norm * (h - 2 * padding))
                        return Offset(x, y)
                    }

                    clipRect(right = w * animProgress.value) {
                        drawBenchmarkLine(normalizedPoints.mapIndexed { i, p -> getOffset(p.portfolioValue, i) }, CredifyIndigo, this)
                        drawBenchmarkLine(normalizedPoints.mapIndexed { i, p -> getOffset(p.goldValue, i) }, GoldColor, this)
                        drawBenchmarkLine(normalizedPoints.mapIndexed { i, p -> getOffset(p.usdValue, i) }, UsdColor, this)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LegendItem("پورتفو", CredifyIndigo)
                LegendItem("طلا ۱۸", GoldColor)
                LegendItem("دلار", UsdColor)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = SlateBorderLight)
            Spacer(modifier = Modifier.height(12.dp))
            
            val finalP = normalizedPoints.last().portfolioValue
            val finalG = normalizedPoints.last().goldValue
            val diff = finalP - finalG
            
            Text(
                text = if (diff >= 0) 
                    "پورتفوی شما در این بازه ${String.format(Locale.US, "%.1f", diff)}٪ بهتر از طلا عمل کرده است." 
                    else "بازدهی شما در این بازه ${String.format(Locale.US, "%.1f", -diff)}٪ کمتر از طلا بوده است.",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}

fun drawBenchmarkLine(points: List<Offset>, color: Color, scope: DrawScope) {
    if (points.size < 2) return
    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        points.forEach { lineTo(it.x, it.y) }
    }
    scope.drawPath(path, color, style = Stroke(width = 2.dp.toPx(scope), cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun Dp.toPx(scope: DrawScope): Float = with(scope) { this@toPx.toPx() }

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, fontSize = 10.sp, color = TextSecondary)
    }
}

@Composable
fun EmptyBenchmarkState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSecondary)
    ) {
        Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("برای نمایش مقایسه عملکرد، به داده‌های تاریخی بیشتری نیاز است.", fontSize = 11.sp, color = TextMuted)
        }
    }
}
