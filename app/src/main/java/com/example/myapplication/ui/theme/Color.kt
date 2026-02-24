package com.example.myapplication.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Palette - Modern Indigo
val PrimaryIndigo = Color(0xFF5C6BC0)
val PrimaryIndigoLight = Color(0xFF8E99F3)
val PrimaryIndigoDark = Color(0xFF26418F)
val SecondaryTeal = Color(0xFF26A69A)
val SecondaryTealLight = Color(0xFF64D8CB)
val AccentAmber = Color(0xFFFFCA28)
val AccentAmberLight = Color(0xFFFFE082)

// Deep Colors
val DarkBlue = Color(0xFF1A237E)
val LightBlue = Color(0xFFE8EAF6)
val DeepPurple = Color(0xFF673AB7)
val DeepPurpleLight = Color(0xFFEDE7F6)

// Surface Colors
val SurfaceDark = Color(0xFF0F0F12)
val SurfaceDarkElevated = Color(0xFF1A1A1F)
val SurfaceLight = Color(0xFFFAFAFC)
val SurfaceLightElevated = Color(0xFFFFFFFF)

// Status Colors
val SuccessGreen = Color(0xFF4CAF50)
val SuccessGreenLight = Color(0xFFE8F5E9)
val WarningOrange = Color(0xFFFF9800)
val WarningOrangeLight = Color(0xFFFFF3E0)
val ErrorRed = Color(0xFFE91E63)
val ErrorRedLight = Color(0xFFFCE4EC)
val InfoBlue = Color(0xFF2196F3)
val InfoBlueLight = Color(0xFFE3F2FD)

// Neutrals
val Grey900 = Color(0xFF212121)
val Grey800 = Color(0xFF424242)
val Grey700 = Color(0xFF616161)
val Grey600 = Color(0xFF757575)
val Grey500 = Color(0xFF9E9E9E)
val Grey400 = Color(0xFFBDBDBD)
val Grey300 = Color(0xFFE0E0E0)
val Grey200 = Color(0xFFEEEEEE)
val Grey100 = Color(0xFFF5F5F5)
val Grey50 = Color(0xFFFAFAFA)

// Glassmorphism Colors
val GlassWhite = Color(0x40FFFFFF)
val GlassDark = Color(0x40000000)
val GlassBorder = Color(0x20FFFFFF)

// Gradient Presets
val PrimaryGradient = Brush.linearGradient(
    colors = listOf(PrimaryIndigo, DeepPurple)
)

val SunsetGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFF512F), Color(0xFFDD2476))
)

val OceanGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF2193B0), Color(0xFF6DD5ED))
)

val ForestGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF11998E), Color(0xFF38EF7D))
)

val GoldGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFF7971E), Color(0xFFFFD200))
)

val NightGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF232526), Color(0xFF414345))
)

val PremiumGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
)

// Badge Colors
val BadgeTopRated = Color(0xFFFFD700)
val BadgeVerified = Color(0xFF2196F3)
val BadgePremium = Color(0xFF9C27B0)
val BadgeNew = Color(0xFF4CAF50)
val BadgeQuickResponder = Color(0xFF00BCD4)

// Confetti Colors
val ConfettiColors = listOf(
    Color(0xFFFF6B6B),
    Color(0xFF4ECDC4),
    Color(0xFFFFE66D),
    Color(0xFF95E1D3),
    Color(0xFFF38181),
    Color(0xFFAA96DA),
    Color(0xFFFCBF1E),
    Color(0xFF6BCB77)
)

// Shimmer Colors
val ShimmerColorLight = listOf(
    Grey200,
    Grey100,
    Grey200
)

val ShimmerColorDark = listOf(
    Grey800,
    Grey700,
    Grey800
)
