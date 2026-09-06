package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DaraDarkColorScheme = darkColorScheme(
    primary = IndigoElectric,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF8083FF),
    onPrimaryContainer = Color(0xFF0D0096),
    secondary = EmeraldCore,
    onSecondary = Color(0xFF003824),
    secondaryContainer = Color(0xFF00A572),
    onSecondaryContainer = Color(0xFF00311F),
    tertiary = RefinedAmberGold,
    onTertiary = Color(0xFF472A00),
    tertiaryContainer = Color(0xFFCA8100),
    onTertiaryContainer = Color(0xFF3E2400),
    background = ObsidianSlate900,
    onBackground = Slate50,
    surface = ObsidianSlate700,
    onSurface = Slate50,
    surfaceVariant = ObsidianSlate500,
    onSurfaceVariant = Color(0xFFC7C4D7),
    outline = Color(0xFF908FA0),
    outlineVariant = Color(0xFF464554),
    error = RoseCoral,
    onError = Color(0xFF690005)
)

@Composable
fun DaraTheme(
    darkTheme: Boolean = true, // Dara defaults to Dark Mode
    content: @Composable () -> Unit,
) {
    // Note: Dara is optimized for Dark Mode as per DESIGN.md
    val colorScheme = DaraDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DaraTypography,
        content = content
    )
}

/** Legacy alias for compatibility with existing code. */
@Composable
fun FinancialLedgerTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    DaraTheme(darkTheme = darkTheme, content = content)
}
