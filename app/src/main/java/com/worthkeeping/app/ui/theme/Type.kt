package com.worthkeeping.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

val AppFontFamily = FontFamily.Default

val WorthKeepingTypography = Typography(
    displayLarge = Typography().displayLarge.copy(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold),
    displayMedium = Typography().displayMedium.copy(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold),
    displaySmall = Typography().displaySmall.copy(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium),
    headlineLarge = Typography().headlineLarge.copy(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold),
    headlineMedium = Typography().headlineMedium.copy(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium),
    headlineSmall = Typography().headlineSmall.copy(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium),
    titleLarge = Typography().titleLarge.copy(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold),
    titleMedium = Typography().titleMedium.copy(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium),
    titleSmall = Typography().titleSmall.copy(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium),
    bodyLarge = Typography().bodyLarge.copy(fontFamily = AppFontFamily),
    bodyMedium = Typography().bodyMedium.copy(fontFamily = AppFontFamily),
    bodySmall = Typography().bodySmall.copy(fontFamily = AppFontFamily),
    labelLarge = Typography().labelLarge.copy(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium),
    labelMedium = Typography().labelMedium.copy(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium),
    labelSmall = Typography().labelSmall.copy(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium)
)
