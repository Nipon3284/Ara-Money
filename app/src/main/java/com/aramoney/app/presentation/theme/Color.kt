package com.aramoney.app.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * Sistem Token Warna "Ara Money"
 * Desain feminin pastel yang nyaman dipandang, ramah kontras (WCAG AA & AAA),
 * dan memiliki variasi "Sakura Night" yang lembut untuk malam hari.
 */

// ==========================================
// PALET LIGHT THEME ("Sakura Day")
// ==========================================
val PrimarySakuraPink = Color(0xFFF48FB1)
val PrimarySakuraPinkLight = Color(0xFFF8BBD0)
val PrimarySakuraPinkContainer = Color(0xFFFFE4EC)
val SecondaryLavender = Color(0xFFCE93D8)
val SecondaryLavenderLight = Color(0xFFE1BEE7)
val TertiaryPeach = Color(0xFFFFCCBC)
val DeepBerry = Color(0xFF6A1B9A)
val DeepBerryDark = Color(0xFF4A154B)
val BackgroundCream = Color(0xFFFFF9FB)
val SurfaceCard = Color(0xFFFFF0F5)
val SurfaceElevated = Color(0xFFFFFFFF)
val ErrorSoftRed = Color(0xFFE57373)
val SuccessMintGreen = Color(0xFFA5D6A7)
val BorderSoftPink = Color(0xFFF3D9E4)

// Neutral sub-colors
val TextSecondaryLight = Color(0xFF8E6593)
val TextMutedLight = Color(0xFFB89ABF)
val DividerSoft = Color(0xFFF7E6EE)

// ==========================================
// PALET DARK THEME ("Sakura Night")
// ==========================================
val BackgroundDark = Color(0xFF2A1B2E)
val SurfaceDark = Color(0xFF3B2440)
val SurfaceCardDark = Color(0xFF432849)
val SurfaceElevatedDark = Color(0xFF4F3057)
val PrimaryDark = Color(0xFFF48FB1)
val TextPrimaryDark = Color(0xFFF8E9F0)
val TextSecondaryDark = Color(0xFFD8B8DD)
val TextMutedDark = Color(0xFF9E7C9E)
val BorderDarkPink = Color(0xFF55325C)

// ==========================================
// SHADOW TINTS & GRADIENTS
// ==========================================
val SoftShadowPink = Color(0xFFF48FB1).copy(alpha = 0.22f)
val SoftShadowDark = Color(0xFF000000).copy(alpha = 0.35f)

val HeroGradientStart = Color(0xFFE27399)
val HeroGradientEnd = Color(0xFFA566B0)

// ==========================================
// EMPATHETIC STATUS PALETTE (Aman/Waspada/Bahaya)
// ==========================================
val StatusSafeBgLight = Color(0xFFE8F5E9)
val StatusSafeTextLight = Color(0xFF1B5E20)
val StatusSafeBgDark = Color(0xFF1C3421)
val StatusSafeTextDark = Color(0xFFA5D6A7)

val StatusWarningBgLight = Color(0xFFFFF8E1)
val StatusWarningTextLight = Color(0xFFB45309)
val StatusWarningBgDark = Color(0xFF3E2C0D)
val StatusWarningTextDark = Color(0xFFFFE082)

val StatusDangerBgLight = Color(0xFFFFEBEE)
val StatusDangerTextLight = Color(0xFFB71C1C)
val StatusDangerBgDark = Color(0xFF3C181C)
val StatusDangerTextDark = Color(0xFFFFCDD2)
