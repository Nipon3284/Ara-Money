package com.aramoney.app.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ==========================================
// LIGHT COLOR SCHEME ("Sakura Day")
// ==========================================
private val LightColorScheme = lightColorScheme(
    primary = PrimarySakuraPink,
    onPrimary = Color.White,
    primaryContainer = PrimarySakuraPinkContainer,
    onPrimaryContainer = DeepBerryDark,
    secondary = SecondaryLavender,
    onSecondary = Color.White,
    secondaryContainer = SecondaryLavenderLight,
    onSecondaryContainer = DeepBerryDark,
    tertiary = TertiaryPeach,
    onTertiary = DeepBerryDark,
    background = BackgroundCream,
    onBackground = DeepBerryDark,
    surface = SurfaceElevated,
    onSurface = DeepBerryDark,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = DeepBerry,
    outline = BorderSoftPink,
    outlineVariant = DividerSoft,
    error = ErrorSoftRed,
    onError = Color.White
)

// ==========================================
// DARK COLOR SCHEME ("Sakura Night")
// Bukan sekadar invert warna; palet lembut anggun bertema malam sakura
// ==========================================
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = Color(0xFF3B003E),
    primaryContainer = Color(0xFF5E2063),
    onPrimaryContainer = TextPrimaryDark,
    secondary = SecondaryLavender,
    onSecondary = Color(0xFF3D1449),
    secondaryContainer = Color(0xFF562863),
    onSecondaryContainer = TextSecondaryDark,
    tertiary = TertiaryPeach,
    onTertiary = Color(0xFF4A2016),
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceCardDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDarkPink,
    outlineVariant = Color(0xFF45244C),
    error = ErrorSoftRed,
    onError = Color.White
)

val LocalDarkTheme = staticCompositionLocalOf { false }

@Composable
fun isAppInDarkTheme(): Boolean = LocalDarkTheme.current

@Composable
fun AraMoneyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()

                val insetsController = WindowCompat.getInsetsController(window, view)
                // Light status bar saat background terang agar teks icon terbaca
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = AraShapes,
            typography = AraTypography,
            content = content
        )
    }
}
