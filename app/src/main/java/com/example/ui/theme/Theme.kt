package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CredifyIndigo,
    secondary = CredifyViolet,
    tertiary = GoldAccent,
    background = DarkSlateSurface,
    surface = DarkSlateSecondary,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextLight,
    onSurface = TextLight,
    surfaceVariant = SlateBorder
)

private val LightColorScheme = lightColorScheme(
    primary = CredifyIndigo,
    secondary = CredifyViolet,
    tertiary = GoldAccent,
    background = LightBackground,
    surface = SoftCardSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextDark,
    onSurface = TextDark,
    surfaceVariant = Color(0xFFE2E8F0)
)

@Composable
fun FinancialLedgerTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // Use strict Credify fintech theme
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    FinancialLedgerTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

