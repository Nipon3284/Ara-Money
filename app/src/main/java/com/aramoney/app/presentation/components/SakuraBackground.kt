package com.aramoney.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import com.aramoney.app.presentation.theme.isAppInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.aramoney.app.presentation.theme.BackgroundCream
import com.aramoney.app.presentation.theme.BackgroundDark
import com.aramoney.app.presentation.theme.PrimarySakuraPink
import com.aramoney.app.presentation.theme.PrimarySakuraPinkLight
import com.aramoney.app.presentation.theme.SecondaryLavenderLight
import kotlin.random.Random

/**
 * Representasi satu kelopak sakura yang melayang
 */
private data class Petal(
    val xRatio: Float,      // Posisi horizontal (0..1)
    val yRatio: Float,      // Posisi vertikal (0..1)
    val size: Float,        // Ukuran kelopak
    val rotation: Float,    // Rotasi dalam derajat
    val color: Color
)

/**
 * Komponen latar belakang estetis bergambar kelopak sakura melayang.
 * Digambar 100% menggunakan Canvas Compose (tanpa asset bitmap eksternal)
 * sehingga ringan, tajam di semua resolusi layar, dan 100% offline-friendly.
 */
@Composable
fun SakuraBackground(
    modifier: Modifier = Modifier,
    scrollYOffset: Float = 0f,
    content: @Composable () -> Unit
) {
    val isDark = isAppInDarkTheme()
    val bgColor = if (isDark) BackgroundDark else BackgroundCream

    // Warna kelopak yang lembut dan tidak mengganggu konten teks
    val petalColors = remember(isDark) {
        if (isDark) {
            listOf(
                Color(0xFFF48FB1).copy(alpha = 0.10f),
                Color(0xFFCE93D8).copy(alpha = 0.08f),
                Color(0xFFF8BBD0).copy(alpha = 0.06f)
            )
        } else {
            listOf(
                PrimarySakuraPinkLight.copy(alpha = 0.28f),
                PrimarySakuraPink.copy(alpha = 0.16f),
                SecondaryLavenderLight.copy(alpha = 0.22f)
            )
        }
    }

    // Sebaran posisi kelopak deterministik (menggunakan seed tetap) agar tidak berganti acak saat recompose
    val petals = remember(petalColors) {
        val random = Random(42)
        List(18) {
            Petal(
                xRatio = random.nextFloat(),
                yRatio = random.nextFloat(),
                size = 14f + random.nextFloat() * 18f,
                rotation = random.nextFloat() * 360f,
                color = petalColors[random.nextInt(petalColors.size)]
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Gambar latar dasar dengan gradien vertikal tipis
            val topGradientColor = if (isDark) Color(0xFF331E38) else Color(0xFFFFF0F5)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(topGradientColor, bgColor)
                )
            )

            // Gambar kelopak-kelopak sakura
            petals.forEach { petal ->
                // Parallax halus terhadap scroll offset
                val adjustedY = (petal.yRatio * size.height - scrollYOffset * 0.15f) % size.height
                val currentY = if (adjustedY < 0) adjustedY + size.height else adjustedY
                val currentX = petal.xRatio * size.width

                rotate(
                    degrees = petal.rotation,
                    pivot = Offset(currentX, currentY)
                ) {
                    drawSakuraPetal(
                        center = Offset(currentX, currentY),
                        petalSize = petal.size,
                        color = petal.color
                    )
                }
            }
        }

        // Konten utama aplikasi berada di atas lapisan latar belakang
        content()
    }
}

/**
 * Menggambar bentuk siluet satu kelopak bunga sakura dengan lekukan khas di ujungnya.
 */
private fun DrawScope.drawSakuraPetal(
    center: Offset,
    petalSize: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x, center.y + petalSize)
        // Sisi kanan kelopak
        cubicTo(
            center.x + petalSize * 0.8f, center.y + petalSize * 0.3f,
            center.x + petalSize * 0.6f, center.y - petalSize * 0.7f,
            center.x + petalSize * 0.18f, center.y - petalSize
        )
        // Lekukan tengah ujung kelopak sakura (notched tip khas sakura)
        lineTo(center.x, center.y - petalSize * 0.8f)
        lineTo(center.x - petalSize * 0.18f, center.y - petalSize)
        // Sisi kiri kelopak
        cubicTo(
            center.x - petalSize * 0.6f, center.y - petalSize * 0.7f,
            center.x - petalSize * 0.8f, center.y + petalSize * 0.3f,
            center.x, center.y + petalSize
        )
        close()
    }

    drawPath(path = path, color = color)
}
