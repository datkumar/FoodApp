package com.phoenix.tech.foodapp.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val lightScheme = lightColorScheme(
    primary = Color(0xFFFC6823),          // Vibrant Food Orange
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDBD1),
    onPrimaryContainer = Color(0xFFFF5722),
    error = Color(0xFF9B0D0D),
    onError = Color(0xFFFFFFFF),
    secondary = Color(0xFF646363),        // Dark Charcoal for accents
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE7E7E7),
    onSecondaryContainer = Color(0xFF191C1C),
    tertiary = Color(0xFF0E8C72),         // Healthy Green
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF8F8F8),       // Very light grey background
    surface = Color(0xFFEEEEEE),          // Pure white cards
    onSurface = Color(0xFF191C1C),
    surfaceVariant = Color(0xFFF0F0F0),   // Search bars/input fields
    onSurfaceVariant = Color(0xFF444748),
    outline = Color(0xFF747878)
)
val darkScheme = darkColorScheme(
    primary = Color(0xFFFFB187),          // Softer orange for dark mode
    onPrimary = Color(0xFF5E1700),
    primaryContainer = Color(0xFF8D2C0D),
    onPrimaryContainer = Color(0xFFFFDBD1),
    secondary = Color(0xFFBFC8C8),
    onSecondary = Color(0xFF293232),
    secondaryContainer = Color(0xFF3F4849),
    onSecondaryContainer = Color(0xFFDDE4E4),
    tertiary = Color(0xFF81C784),         // Muted Green
    onTertiary = Color(0xFF00390A),
    background = Color(0xFF121212),       // Deep OLED Black
    surface = Color(0xFF1E1E1E),          // Dark grey cards
    onSurface = Color(0xFFE1E2E3),
    surfaceVariant = Color(0xFF444748),
    onSurfaceVariant = Color(0xFFC4C7C8),
    outline = Color(0xFF8E9192),
)


object AppTextFieldColors {
    val Success = Color(0xFF329836)
    val Error = Color(0xFFFF1818)
    val Default = Color(0xFF8F8E8E)
    val Focused = Color(0xFF286DD5)
}

fun getRatingColor(rating: Double): Color {
    return when (rating) {
        in 4.0..5.0 -> Color(0xFF1A8D51)
        in 3.0..4.0 -> Color(0xFF84BE34)
        in 2.0..3.0 -> Color(0xFFFF9800)
        in 0.0..2.0 -> Color(0xFFFA3232)
        else -> Color(0xFF696767) // Out of range
    }
}

