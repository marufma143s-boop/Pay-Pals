package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkCustomColorScheme = darkColorScheme(
    primary = PurpleNeon,
    onPrimary = Color.White,
    primaryContainer = PurpleDark,
    onPrimaryContainer = Color(0xFFE9D5FF),
    secondary = PurpleLight,
    onSecondary = Color(0xFF1E1035),
    secondaryContainer = DeepViolet,
    onSecondaryContainer = Color(0xFFDDD6FE),
    tertiary = GoldAccent,
    onTertiary = Color(0xFF451A03),
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    error = ErrorRed,
    onError = Color.White
)

val LightCustomColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = PurpleDeep,
    secondary = PurpleDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E8FF),
    onSecondaryContainer = PurpleDeep,
    tertiary = Color(0xFFD97706),
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun PayPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkCustomColorScheme else LightCustomColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
