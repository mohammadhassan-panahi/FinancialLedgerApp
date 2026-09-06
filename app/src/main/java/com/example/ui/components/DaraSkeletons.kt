package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@Composable
fun ShimmerBrush(): Brush {
    val shimmerColors = listOf(
        ObsidianSlate800.copy(alpha = 0.6f),
        ObsidianSlate600.copy(alpha = 0.3f),
        ObsidianSlate800.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )
}

@Composable
fun DashboardSkeleton() {
    val brush = ShimmerBrush()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(brush))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(100.dp, 16.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                    Box(modifier = Modifier.size(60.dp, 12.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                }
            }
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(brush))
        }

        // Hero Card
        Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(24.dp)).background(brush))

        // Quick Actions
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) {
                Box(modifier = Modifier.weight(1f).height(90.dp).clip(RoundedCornerShape(16.dp)).background(brush))
            }
        }

        // List Header
        Box(modifier = Modifier.size(140.dp, 20.dp).clip(RoundedCornerShape(4.dp)).background(brush))

        // List Items
        repeat(4) {
            Box(modifier = Modifier.fillMaxWidth().height(72.dp).clip(RoundedCornerShape(16.dp)).background(brush))
        }
    }
}
