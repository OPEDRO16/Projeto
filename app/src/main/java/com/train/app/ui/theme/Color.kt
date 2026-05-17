package com.train.app.ui.theme

import androidx.compose.ui.graphics.Color

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

// ─── Legacy aliases ──────────────────────────────────────────────────────────
val BackgroundDark  = Surface
val SurfaceLevel0   = SurfaceContainerLowest
val SurfaceLevel1   = Color(0xFF252324)            // Level 1 card surface per DESIGN.md
val TextPrimary     = OnSurface
val AccentBlue      = BluePrimary
val AccentYellow    = YellowWarning
val AccentPurple    = PurpleRecovery

// OutlineBorder: used for muted label text (#968F92), matches Outline token
// Previously was 15%-opacity white which was nearly invisible as text color.
val OutlineBorder   = Outline                      // #968F92 — visible muted grey

// Fine 1px divider lines (very subtle per DESIGN.md "5-15% opacity white")
val DividerColor    = Color(0x1AE0E5E9)            // 10% opacity white