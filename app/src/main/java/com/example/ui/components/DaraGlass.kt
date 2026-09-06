package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.ObsidianSlate700

/**
 * A glassmorphic container for Dara design system.
 */
@Composable
fun DaraGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(ObsidianSlate700.copy(alpha = 0.65f))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.02f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        content()
    }
}

/**
 * A more elevated glass container for modals and sheets.
 */
@Composable
fun DaraGlassElevated(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
            .background(Color(0xFF1E1B4B).copy(alpha = 0.85f)) // Deep Indigo Glass
            .border(
                width = 1.dp,
                color = Color(0xFF6366F1).copy(alpha = 0.25f), // Indigo border highlight
                shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
            )
    ) {
        content()
    }
}
