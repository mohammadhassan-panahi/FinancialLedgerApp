package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Vazirmatn (https://github.com/rastikerdar/vazirmatn, OFL) as the app-wide Persian font.
 * SemiBold/ExtraBold/etc. fall back to Bold, Light falls back to Regular — Vazirmatn's
 * full weight range isn't bundled to keep the APK small.
 */
val VazirmatnFontFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_regular, FontWeight.Light),
    Font(R.font.vazirmatn_regular, FontWeight.Thin),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_medium, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
    Font(R.font.vazirmatn_bold, FontWeight.ExtraBold),
    Font(R.font.vazirmatn_bold, FontWeight.Black)
)

private val MaterialDefaults = Typography()

/** Every Material text style uses Vazirmatn so mixed-system-font rendering never leaks through. */
val Typography = MaterialDefaults.copy(
    displayLarge = MaterialDefaults.displayLarge.copy(fontFamily = VazirmatnFontFamily),
    displayMedium = MaterialDefaults.displayMedium.copy(fontFamily = VazirmatnFontFamily),
    displaySmall = MaterialDefaults.displaySmall.copy(fontFamily = VazirmatnFontFamily),
    headlineLarge = MaterialDefaults.headlineLarge.copy(fontFamily = VazirmatnFontFamily),
    headlineMedium = MaterialDefaults.headlineMedium.copy(fontFamily = VazirmatnFontFamily),
    headlineSmall = MaterialDefaults.headlineSmall.copy(fontFamily = VazirmatnFontFamily),
    titleLarge = MaterialDefaults.titleLarge.copy(fontFamily = VazirmatnFontFamily),
    titleMedium = MaterialDefaults.titleMedium.copy(fontFamily = VazirmatnFontFamily),
    titleSmall = MaterialDefaults.titleSmall.copy(fontFamily = VazirmatnFontFamily),
    bodyLarge = MaterialDefaults.bodyLarge.copy(fontFamily = VazirmatnFontFamily),
    bodyMedium = MaterialDefaults.bodyMedium.copy(fontFamily = VazirmatnFontFamily),
    bodySmall = MaterialDefaults.bodySmall.copy(fontFamily = VazirmatnFontFamily),
    labelLarge = MaterialDefaults.labelLarge.copy(fontFamily = VazirmatnFontFamily),
    labelMedium = MaterialDefaults.labelMedium.copy(fontFamily = VazirmatnFontFamily),
    labelSmall = MaterialDefaults.labelSmall.copy(fontFamily = VazirmatnFontFamily)
)

/** For TextStyle() call sites outside MaterialTheme.typography (charts, custom components). */
val VazirmatnBodyStyle = TextStyle(
    fontFamily = VazirmatnFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp
)
