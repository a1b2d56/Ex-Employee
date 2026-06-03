package com.powergrid.exemployee.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color



// ── Midnight (AMOLED black) ──

val MidnightColorScheme = darkColorScheme(
    primary = Color(0xFF81D4FA),
    onPrimary = Color(0xFF003549),
    primaryContainer = Color(0xFF004D69),
    onPrimaryContainer = Color(0xFFB3E5FC),
    secondary = Color(0xFF4FC3F7),
    onSecondary = Color(0xFF002538),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFF4F4F5),
    surfaceVariant = Color(0xFF0A0A0A),
    error = Color(0xFFFFB4AB),
    background = Color(0xFF000000),
    onBackground = Color(0xFFF4F4F5),
    surfaceContainer = Color(0xFF050505),
    outlineVariant = Color(0xFF18181B),
)

// ── Phantom (Luxury Deep Violet / Amethyst) ──

val PhantomColorScheme = darkColorScheme(
    primary = Color(0xFFD6BCFA),
    onPrimary = Color(0xFF3B0764),
    primaryContainer = Color(0xFF553C9A),
    onPrimaryContainer = Color(0xFFEDE9FE),
    secondary = Color(0xFFB794F4),
    onSecondary = Color(0xFF2D3748),
    surface = Color(0xFF161524),
    onSurface = Color(0xFFE2E0E7),
    surfaceVariant = Color(0xFF28263C),
    error = Color(0xFFEF4444),
    background = Color(0xFF0F0E17),
    onBackground = Color(0xFFE2E0E7),
)

// ── Obsidian (Sleek Charcoal & Cyan Tech) ──

val ObsidianColorScheme = darkColorScheme(
    primary = Color(0xFF00E5FF),
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004E57),
    onPrimaryContainer = Color(0xFFB2F8FF),
    secondary = Color(0xFF22D3EE),
    onSecondary = Color(0xFF0F172A),
    surface = Color(0xFF11171D),
    onSurface = Color(0xFFECEFF1),
    surfaceVariant = Color(0xFF202B36),
    error = Color(0xFFEF4444),
    background = Color(0xFF0A0D10),
    onBackground = Color(0xFFECEFF1),
)

// ── Espresso (Warm Rich Cocoa & Cream Gold) ──

val EspressoColorScheme = darkColorScheme(
    primary = Color(0xFFE5C49F),
    onPrimary = Color(0xFF3D2612),
    primaryContainer = Color(0xFF5C3E21),
    onPrimaryContainer = Color(0xFFFBEFE3),
    secondary = Color(0xFFBDA67A),
    onSecondary = Color(0xFF231F1E),
    surface = Color(0xFF1E1714),
    onSurface = Color(0xFFECE2DB),
    surfaceVariant = Color(0xFF2E2420),
    error = Color(0xFFEF4444),
    background = Color(0xFF140F0D),
    onBackground = Color(0xFFECE2DB),
)

// ── Matcha (Organic Soothing Sage & Green Tea) ──

val MatchaColorScheme = darkColorScheme(
    primary = Color(0xFFA7D7C5),
    onPrimary = Color(0xFF1B3B2B),
    primaryContainer = Color(0xFF2D5C43),
    onPrimaryContainer = Color(0xFFE8F5E9),
    secondary = Color(0xFF8FBC8F),
    onSecondary = Color(0xFF141C14),
    surface = Color(0xFF121A15),
    onSurface = Color(0xFFE1E8E4),
    surfaceVariant = Color(0xFF223027),
    error = Color(0xFFEF4444),
    background = Color(0xFF0B100D),
    onBackground = Color(0xFFE1E8E4),
)

// ── Nord (Arctic Clean Blue-Grey & Ice-Blue) ──

val NordColorScheme = darkColorScheme(
    primary = Color(0xFF88C0D0),
    onPrimary = Color(0xFF2E3440),
    primaryContainer = Color(0xFF434C5E),
    onPrimaryContainer = Color(0xFFD8DEE9),
    secondary = Color(0xFF81A1C1),
    onSecondary = Color(0xFF2E3440),
    surface = Color(0xFF3B4252),
    onSurface = Color(0xFFECEFF4),
    surfaceVariant = Color(0xFF4C566A),
    error = Color(0xFFEF4444),
    background = Color(0xFF2E3440),
    onBackground = Color(0xFFECEFF4),
)

// ── Rose (Premium Blush Quartz & Mauve) ──

val RoseColorScheme = darkColorScheme(
    primary = Color(0xFFF3C5D6),
    onPrimary = Color(0xFF4A1E2F),
    primaryContainer = Color(0xFF6E3249),
    onPrimaryContainer = Color(0xFFFFECF2),
    secondary = Color(0xFFE8A0BF),
    onSecondary = Color(0xFF1C1418),
    surface = Color(0xFF1A1417),
    onSurface = Color(0xFFEAE2E4),
    surfaceVariant = Color(0xFF2A2025),
    error = Color(0xFFEF4444),
    background = Color(0xFF120E10),
)

// ── PowerGrid (Premium Corporate Identity Theme) ──

val PowerGridColorScheme = lightColorScheme(
    primary = Color(0xFF04519D),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD7E8FF),
    onPrimaryContainer = Color(0xFF001B3D),
    secondary = Color(0xFF08A64E),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF08A64E),
    surface = Color(0xFFF8FBFF),
    onSurface = Color(0xFF17212B),
    surfaceVariant = Color(0xFFE4F0EA),
    onSurfaceVariant = Color(0xFF404843),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0x1F04519D),    // Translucent primary-blue glass card tiles (12% alpha)
    surfaceContainer = Color(0x2B08A64E),       // Translucent secondary-green glass nav capsule (17% alpha)
    surfaceContainerHigh = Color(0xFFE2EAF4),
    surfaceContainerHighest = Color(0xFFD7E2F0),
    outline = Color(0x4D04519D),                 // Translucent blue borders
    outlineVariant = Color(0x2604519D),          // Translucent blue dividers/outlines
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    background = Color(0xFFEEF6FF),
    onBackground = Color(0xFF17212B),
)

// ── Fallback sky-blue (pre-Android 12 devices without dynamic color) ──

val FallbackSkyBlueLightScheme = lightColorScheme(
    primary = Color(0xFF0288D1),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB3E5FC),
    onPrimaryContainer = Color(0xFF01579B),
    secondary = Color(0xFF0277BD),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1E),
)

val FallbackSkyBlueDarkScheme = darkColorScheme(
    primary = Color(0xFF81D4FA),
    onPrimary = Color(0xFF003549),
    primaryContainer = Color(0xFF004D69),
    onPrimaryContainer = Color(0xFFB3E5FC),
    secondary = Color(0xFF4FC3F7),
    surface = Color(0xFF212B36),
    onSurface = Color(0xFFE2E2E6),
)
