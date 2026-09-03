package com.badminton.scorecard.core.designsystem.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CourtGreenLight,
    onPrimary = PureWhite,
    primaryContainer = CourtGreenDark,
    onPrimaryContainer = PureWhite,
    secondary = ShuttlecockGold,
    onSecondary = TextPrimary,
    secondaryContainer = ShuttlecockGoldDark,
    onSecondaryContainer = PureWhite,
    tertiary = ShuttlecockGoldLight,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = PureWhite,
    onSurface = PureWhite,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = CourtGreen,
    onPrimary = PureWhite,
    primaryContainer = LightMintContainer,
    onPrimaryContainer = OnCourtGreenContainer,
    secondary = Color(0xFF0288D1),
    onSecondary = PureWhite,
    secondaryContainer = LightSkyContainer,
    onSecondaryContainer = Color(0xFF01579B),
    tertiary = ShuttlecockGoldDark,
    tertiaryContainer = LightGoldContainer,
    onTertiaryContainer = OnShuttlecockGoldContainer,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = Color(0xFF37474F),
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = ErrorRed
)

@Composable
fun BadmintonScorecardTheme(
    themeMode: com.badminton.scorecard.core.preferences.ThemeMode = com.badminton.scorecard.core.preferences.ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        com.badminton.scorecard.core.preferences.ThemeMode.SYSTEM -> systemInDark
        com.badminton.scorecard.core.preferences.ThemeMode.LIGHT -> false
        com.badminton.scorecard.core.preferences.ThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        isDark -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
