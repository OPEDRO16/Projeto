package com.train.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─── Performance Minimalist Typography — DESIGN.md ──────────────────────────
// Hanken Grotesk → FontFamily.Default (fallback until font assets added)
// JetBrains Mono → FontFamily.Monospace (exact match for data labels)

val AppTypography = Typography(
    // display-lg: 48px / 800 / -0.02em
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 48.sp,
        lineHeight = 52.sp,
        letterSpacing = (-0.96).sp   // ≈ -0.02em at 48sp
    ),
    // headline-lg: 32px / 700 / 40px
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        color = Color(0xFFE6E1E1)
    ),
    // headline-lg-mobile: 28px / 700 / 36px
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        color = Color(0xFFE6E1E1)
    ),
    // headline-md: 24px / 600 / 32px
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        color = Color(0xFFE6E1E1)
    ),
    // body-lg: 18px / 400 / 28px
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp,
        color = Color(0xFFE6E1E1)
    ),
    // body-md: 16px / 400 / 24px
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = Color(0xFFE6E1E1)
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = Color(0xFFCDC4C8)
    ),
    // label-md: JetBrains Mono 14px / 500 / 0.05em
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = Color(0xFFCDC4C8)
    ),
    // label-md (data): JetBrains Mono 14px / 500 / 0.05em
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.7.sp,      // ≈ 0.05em at 14sp
        color = Color(0xFFCDC4C8)
    ),
    // label-sm: JetBrains Mono 12px / 500 / 0.05em
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.6.sp,      // ≈ 0.05em at 12sp
        color = Color(0xFF968F92)
    )
)