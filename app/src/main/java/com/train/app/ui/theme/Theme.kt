package com.train.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary                = BluePrimary,
    onPrimary              = Color(0xFFFFFFFF),
    primaryContainer       = PrimaryContainer,
    onPrimaryContainer     = Color(0xFF848081),
    secondary              = Color(0xFFC2C7CB),
    onSecondary            = Color(0xFF2C3134),
    secondaryContainer     = Color(0xFF42484B),
    onSecondaryContainer   = Color(0xFFB0B6BA),
    background             = Surface,
    onBackground           = OnSurface,
    surface                = Surface,
    onSurface              = OnSurface,
    surfaceVariant         = SurfaceContainerHigh,
    onSurfaceVariant       = OnSurfaceVariant,
    surfaceContainer       = Surface,           // NavigationBar background → black
    surfaceContainerHigh   = SurfaceContainerHigh,
    surfaceContainerHighest= SurfaceContainerHighest,
    outline                = Outline,
    outlineVariant         = OutlineVariant,
    error                  = Color(0xFFFFB4AB),
    onError                = Color(0xFF690005),
    errorContainer         = Color(0xFF93000A),
    onErrorContainer       = Color(0xFFFFDAD6),
    inverseSurface         = OnSurface,
    inverseOnSurface       = Color(0xFF313030),
    inversePrimary         = Color(0xFF615D5E),
)

@Composable
fun TrainTheme(content: @Composable () -> Unit) {
    val dynamicColorScheme = darkColorScheme(
        primary                = AccentBlue,
        onPrimary              = Color.White,
        primaryContainer       = if (currentThemeName == "LIGHT") Color(0xFFEAEAEA) else PrimaryContainer,
        onPrimaryContainer     = if (currentThemeName == "LIGHT") TextPrimary else Color(0xFF848081),
        secondary              = Color(0xFFC2C7CB),
        onSecondary            = Color(0xFF2C3134),
        secondaryContainer     = Color(0xFF42484B),
        onSecondaryContainer   = Color(0xFFB0B6BA),
        background             = BackgroundDark,
        onBackground           = TextPrimary,
        surface                = BackgroundDark,
        onSurface              = TextPrimary,
        surfaceVariant         = SurfaceLevel1,
        onSurfaceVariant       = TextPrimary,
        surfaceContainer       = BackgroundDark, // NavigationBar background
        surfaceContainerHigh   = SurfaceLevel1,
        surfaceContainerHighest= SurfaceLevel1,
        outline                = OutlineBorder,
        outlineVariant         = OutlineBorder,
        error                  = Color(0xFFFFB4AB),
        onError                = Color(0xFF690005),
        errorContainer         = Color(0xFF93000A),
        onErrorContainer       = Color(0xFFFFDAD6),
        inverseSurface         = TextPrimary,
        inverseOnSurface       = BackgroundDark,
        inversePrimary         = Color(0xFF615D5E),
    )

    MaterialTheme(
        colorScheme = dynamicColorScheme,
        typography  = AppTypography,
        shapes      = AppShapes,
        content     = content
    )
}