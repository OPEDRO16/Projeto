package com.train.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

// ─── Performance Minimalist Design System ───────────────────────────────────
// Exact values from DESIGN.md specification

val Surface                 = Color(0xFF141313)
val SurfaceDim              = Color(0xFF141313)
val SurfaceBright           = Color(0xFF3A3939)
val SurfaceContainerLowest  = Color(0xFF0F0E0E)
val SurfaceContainerLow     = Color(0xFF1C1B1B)
val SurfaceContainer        = Color(0xFF201F1F)
val SurfaceContainerHigh    = Color(0xFF2B2A2A)
val SurfaceContainerHighest = Color(0xFF363434)
val OnSurface               = Color(0xFFE6E1E1)
val OnSurfaceVariant        = Color(0xFFCDC4C8)
val Primary                 = Color(0xFFCBC5C6)
val OnPrimary               = Color(0xFF323031)
val PrimaryContainer        = Color(0xFF191718)

// Outline values from DESIGN.md
val Outline                 = Color(0xFF968F92)   // Muted text, secondary labels
val OutlineVariant          = Color(0xFF4B4548)

// Accents (Signal Colors per DESIGN.md)
val BluePrimary             = Color(0xFF0A62D0)   // Primary actions, active states
val YellowWarning           = Color(0xFFF3D869)   // PRs, warnings, high-intensity
val PurpleRecovery          = Color(0xFF74478A)   // Recovery metrics, secondary tags

// Derived transparent accents
val TransparentBlue         = Color(0x1F0A62D0)   // 12% opacity
val TransparentPurple       = Color(0x2974478A)   // 16% opacity
val ChipPurpleBg            = Color(0x3374478A)   // 20% opacity
val ChipYellowBg            = Color(0x33F3D869)   // 20% opacity

// ─── Legacy aliases with Dynamic Theme Getters ─────────────────────────────────
var currentThemeName by mutableStateOf("DARK")
var currentCustomAccentColor by mutableStateOf("#0A62D0")

private fun safeParseColor(hex: String, fallback: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        fallback
    }
}

val BackgroundDark: Color
    get() = when (currentThemeName) {
        "LIGHT" -> Color.White
        "CUSTOM" -> Color(0xFF111116) // deep obsidian dark
        else -> Surface
    }

val SurfaceLevel0: Color
    get() = when (currentThemeName) {
        "LIGHT" -> Color(0xFFF5F5F5)
        "CUSTOM" -> Color(0xFF0F0E0E)
        else -> SurfaceContainerLowest
    }

val SurfaceLevel1: Color
    get() = when (currentThemeName) {
        "LIGHT" -> Color(0xFFEAEAEA)
        "CUSTOM" -> Color(0xFF1A1A24) // custom graphite card surface
        else -> Color(0xFF252324)
    }

val TextPrimary: Color
    get() = when (currentThemeName) {
        "LIGHT" -> Color(0xFF141313)
        "CUSTOM" -> Color(0xFFE2E8F0)
        else -> OnSurface
    }

val TextWhite: Color
    get() = if (currentThemeName == "LIGHT") Color(0xFF141313) else Color.White

val AccentBlue: Color
    get() = when (currentThemeName) {
        "CUSTOM" -> safeParseColor(currentCustomAccentColor, BluePrimary)
        else -> BluePrimary
    }

val AccentYellow: Color
    get() = YellowWarning

val AccentPurple: Color
    get() = PurpleRecovery

val OutlineBorder: Color
    get() = when (currentThemeName) {
        "LIGHT" -> Color(0xFF69706D)
        "CUSTOM" -> Color(0xFF333344)
        else -> Outline
    }

val DividerColor: Color
    get() = when (currentThemeName) {
        "LIGHT" -> Color(0x1A141313)
        "CUSTOM" -> Color(0x1AE0E5E9)
        else -> Color(0x1AE0E5E9)
    }