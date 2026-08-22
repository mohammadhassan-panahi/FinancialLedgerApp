package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Credify Fintech Palette Tokens
val CredifyIndigo = Color(0xFF4F46E5)
val CredifyViolet = Color(0xFF7C3AED)
val DarkSlateSurface = Color(0xFF0F172A)
val DarkSlateSecondary = Color(0xFF1E293B)
val SlateBorder = Color(0xFF334155)
val LightBackground = Color(0xFFF8FAFC)
val SoftCardSurface = Color(0xFFFFFFFF)
val EmeraldProfit = Color(0xFF10B981)
val RoseLoss = Color(0xFFEF4444)
val GoldAccent = Color(0xFFF59E0B)
val CyanAccent = Color(0xFF06B6D4)
val TextMuted = Color(0xFF64748B)
val TextDark = Color(0xFF0F172A)
val TextLight = Color(0xFFF8FAFC)

// --- Aliases added for the merged Calculators module (ported from the calculators UI) ---
// These map the calculators' color names onto the existing Credify palette tokens above,
// so no existing screen's colors change and no duplicate hex values are introduced.
val AccentGold = CredifyIndigo
val ProfitGreen = EmeraldProfit
val LossRed = RoseLoss
val PaperCardSurface = SoftCardSurface
val PrimaryNavy = TextDark

