package com.example.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/** True when the person has turned on "حالت مخفی‌سازی مبالغ" — read via [LocalPrivacyMode.current]. */
val LocalPrivacyMode = compositionLocalOf { false }

@Composable
fun ProvidePrivacyMode(enabled: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPrivacyMode provides enabled, content = content)
}

/**
 * Drop-in replacement for Text() when showing a monetary/quantity value: renders dots instead
 * of [text] whenever privacy mode is on, so call sites don't need an if/else at every use.
 */
@Composable
fun PrivacyAwareAmountText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    fontWeight: androidx.compose.ui.text.font.FontWeight? = null
) {
    val privacyOn = LocalPrivacyMode.current
    Text(
        text = if (privacyOn) maskLength(text) else text,
        modifier = modifier,
        color = color,
        style = style,
        fontWeight = fontWeight
    )
}

private fun maskLength(text: String): String {
    // Roughly matches the visual width of the real number so layouts don't jump when toggled.
    val dotCount = text.count { it.isDigit() || it in '۰'..'۹' }.coerceAtLeast(4)
    return "•".repeat(dotCount.coerceAtMost(10))
}
