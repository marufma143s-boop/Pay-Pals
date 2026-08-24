package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Brand Purples
val PurplePrimary = Color(0xFF7C3AED)      // Vibrant violet purple
val PurpleLight = Color(0xFFA78BFA)        // Light purple accent
val PurpleDark = Color(0xFF5B21B6)         // Deep rich purple
val PurpleDeep = Color(0xFF3B0764)         // Midnight violet
val PurpleNeon = Color(0xFF8B5CF6)         // High-glow purple

// Secondary & Accents
val DeepViolet = Color(0xFF2E1065)
val DarkPurpleCard = Color(0xFF1E1035)
val DarkBackground = Color(0xFF0D0618)
val DarkSurface = Color(0xFF160B29)
val DarkSurfaceVariant = Color(0xFF22133D)

// Status Colors
val SuccessGreen = Color(0xFF10B981)
val SuccessGreenDark = Color(0xFF059669)
val ErrorRed = Color(0xFFEF4444)
val WarningOrange = Color(0xFFF59E0B)
val InfoBlue = Color(0xFF3B82F6)

// Neutral Light
val LightBackground = Color(0xFFF8F7FC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF0EDF9)
val TextPrimaryLight = Color(0xFF1E1433)
val TextSecondaryLight = Color(0xFF6B7280)
val BorderLight = Color(0xFFE5E0F2)

// Neutral Dark
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)
val BorderDark = Color(0xFF322053)

// Premium Gradient Brushes
val WalletGradientBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF6D28D9),
        Color(0xFF4C1D95),
        Color(0xFF2E1065)
    )
)

val CardGlowGradientBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF8B5CF6),
        Color(0xFF6D28D9),
        Color(0xFF4C1D95)
    )
)

val StatCardGradientBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF2B144E),
        Color(0xFF190B30)
    )
)

val GoldAccent = Color(0xFFFBBF24)
val GoldGradientBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFFF59E0B),
        Color(0xFFD97706)
    )
)
