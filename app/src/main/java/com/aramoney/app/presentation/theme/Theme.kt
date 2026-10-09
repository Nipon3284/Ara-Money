package com.aramoney.app.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ==========================================
// LIGHT COLOR SCHEME ("Sakura Day")
// ==========================================
private val LightColorScheme = lightColorScheme(
    primary = PrimaryAction,
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
    onSurfaceVariant = Color(0xFF7B4F82),
    outline = BorderSoftPink,
    outlineVariant = DividerSoft,
    error = DangerAction,
    onError = Color.White
)

// ==========================================
// DARK COLOR SCHEME ("Sakura Night")
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
    error = Color(0xFFEF9A9A),
    onError = Color(0xFF3C181C)
)

/**
 * Token warna semantik tambahan di luar Material ColorScheme.
 * Akses melalui `AraTheme.colors.*` agar tidak perlu percabangan `if (isDark)` di setiap layar.
 */
@Immutable
data class AraColors(
    /** Teks/ikon aksi (link, tombol teks) di atas permukaan. */
    val accent: Color,
    /** Latar tombol/segmen terisi dengan teks [onAction]. */
    val action: Color,
    val onAction: Color,
    val danger: Color,
    val onDanger: Color,
    /** Nominal pemasukan. */
    val income: Color,
    /** Nominal pengeluaran. */
    val expense: Color,
    /** Teks judul utama. */
    val textStrong: Color,
    /** Teks badge/label berwarna berry. */
    val berry: Color,
    val textMuted: Color,
    val border: Color,
    val surfaceCard: Color,
    val surfaceElevated: Color,
    val selectedContainer: Color,
    val onSelectedContainer: Color,
    val disabledContainer: Color,
    val disabledContent: Color,
    val warning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val safeContainer: Color,
    val onSafeContainer: Color,
    val dangerContainer: Color,
    val onDangerContainer: Color
)

private val LightAraColors = AraColors(
    accent = PrimaryAction,
    action = PrimaryAction,
    onAction = Color.White,
    danger = DangerAction,
    onDanger = Color.White,
    income = IncomeGreen,
    expense = DeepBerryDark,
    textStrong = DeepBerryDark,
    berry = DeepBerry,
    textMuted = Color(0xFF8E6593),
    border = BorderSoftPink,
    surfaceCard = SurfaceCard,
    surfaceElevated = SurfaceElevated,
    selectedContainer = PrimarySakuraPinkContainer,
    onSelectedContainer = DeepBerryDark,
    disabledContainer = Color(0xFFE8DCE2),
    disabledContent = Color(0xFF7D6A74),
    warning = WarningOrange,
    warningContainer = StatusWarningBgLight,
    onWarningContainer = StatusWarningTextLight,
    safeContainer = StatusSafeBgLight,
    onSafeContainer = StatusSafeTextLight,
    dangerContainer = StatusDangerBgLight,
    onDangerContainer = StatusDangerTextLight
)

private val DarkAraColors = AraColors(
    accent = PrimarySakuraPink,
    action = PrimaryAction,
    onAction = Color.White,
    danger = DangerAction,
    onDanger = Color.White,
    income = Color(0xFF81C784),
    expense = PrimarySakuraPink,
    textStrong = TextPrimaryDark,
    berry = TextPrimaryDark,
    textMuted = Color(0xFFBFA0C0),
    border = Color(0xFF4B2E52),
    surfaceCard = SurfaceCardDark,
    surfaceElevated = SurfaceElevatedDark,
    selectedContainer = Color(0xFF562260),
    onSelectedContainer = TextPrimaryDark,
    disabledContainer = Color(0xFF422847),
    disabledContent = Color(0xFFB7A3AE),
    warning = Color(0xFFFFB74D),
    warningContainer = StatusWarningBgDark,
    onWarningContainer = StatusWarningTextDark,
    safeContainer = StatusSafeBgDark,
    onSafeContainer = StatusSafeTextDark,
    dangerContainer = StatusDangerBgDark,
    onDangerContainer = StatusDangerTextDark
)

val LocalAraColors = staticCompositionLocalOf { LightAraColors }

val LocalDarkTheme = staticCompositionLocalOf { false }

@Composable
fun isAppInDarkTheme(): Boolean = LocalDarkTheme.current

object AraTheme {
    val colors: AraColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAraColors.current
}

@Composable
fun AraMoneyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val araColors = if (darkTheme) DarkAraColors else LightAraColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                // Edge-to-edge aktif: warna bar transparan, cukup atur kontras ikon status/nav bar
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalAraColors provides araColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = AraShapes,
            typography = AraTypography,
            content = content
        )
    }
}
