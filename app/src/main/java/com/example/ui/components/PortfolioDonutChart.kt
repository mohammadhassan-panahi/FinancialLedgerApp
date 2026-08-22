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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.PersianNumberUtils
import com.example.util.formatRial

data class DonutSlice(val label: String, val valueRial: Double, val color: Color)

/**
 * Portfolio-module donut chart for asset-type breakdown (gold / dollar / stock). Unlike
 * ComposePieChart (legacy, Toman-labeled, used by the calculator screens), every number
 * here is treated as already-in-Rial and the center label uses formatRial() accordingly —
 * kept as a separate small composable instead of editing the shared legacy chart.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PortfolioDonutChart(slices: List<DonutSlice>, modifier: Modifier = Modifier) {
    val total = slices.sumOf { it.valueRial }
    if (total <= 0) {
        Box(
            modifier = modifier.fillMaxWidth().height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("هنوز دارایی‌ای ثبت نشده", style = MaterialTheme.typography.bodySmall)
        }
        return
    }

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(160.dp).padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                var startAngle = -90f
                slices.filter { it.valueRial > 0 }.forEach { slice ->
                    val sweepAngle = ((slice.valueRial / total) * 360f).toFloat()
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatRial(total, showSuffix = false),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ریال",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            slices.filter { it.valueRial > 0 }.forEach { slice ->
                val pct = (slice.valueRial / total) * 100
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Box(modifier = Modifier.size(10.dp).background(slice.color, CircleShape))
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
