package com.shsshobuj.shstube.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── SHS Tube Premium Dark Color Palette ───────────────────────────────────────
val ObsidianBlack = Color(0xFF080C14)       // Primary background: deep space black
val SurfaceDark   = Color(0xFF111827)       // Card/surface background
val SurfaceDarker = Color(0xFF0D1420)       // Elevated/modal backgrounds
val MetallicBlue  = Color(0xFF0052D4)       // Primary accent: metallic blue
val CopperOrange  = Color(0xFFF2994A)       // Secondary accent: copper orange
val TextPrimary   = Color(0xFFEEEEF4)       // Primary text
val TextSecondary = Color(0xFF8A8A9A)       // Secondary/hint text
val ErrorRed      = Color(0xFFE53935)       // Error / cancel states
val SuccessGreen  = Color(0xFF4CAF50)       // Success states

// ── Material3 Dark Color Scheme ────────────────────────────────────────────────
private val SHSDarkColorScheme = darkColorScheme(
    primary             = MetallicBlue,
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFF001A6E),
    onPrimaryContainer  = Color(0xFF9EBEFF),
    secondary           = CopperOrange,
    onSecondary         = Color(0xFF1A0C00),
    secondaryContainer  = Color(0xFF3D2000),
    onSecondaryContainer= Color(0xFFFFDBAF),
    tertiary            = Color(0xFF8A2BE2),  // Purple accent
    onTertiary          = Color.White,
    background          = ObsidianBlack,
    onBackground        = TextPrimary,
    surface             = SurfaceDark,
    onSurface           = TextPrimary,
    surfaceVariant      = SurfaceDarker,
    onSurfaceVariant    = TextSecondary,
    outline             = Color(0xFF3A3A4A),
    error               = ErrorRed,
    onError             = Color.White,
    inverseSurface      = Color(0xFFE8E8F0),
    inverseOnSurface    = ObsidianBlack,
    scrim               = Color(0x99000000)
)

@Composable
fun SHSTubeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SHSDarkColorScheme,
        typography  = SHSTubeTypography,
        content     = content
    )
}
