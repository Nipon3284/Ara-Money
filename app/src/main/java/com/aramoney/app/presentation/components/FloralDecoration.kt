package com.aramoney.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Ornamen Bunga Sakura Mekar (Cherry Blossom Bloom)
 * Digunakan sebagai hiasan sudut (watermark/corner ornament) pada Hero Card, dialog, atau header layar.
 *
 * Menggunakan vektor matematis berbasis Canvas murni tanpa beban memori dari bitmap PNG.
 */
@Composable
fun FloralDecoration(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    tint: Color = Color.White,
    opacity: Float = 0.30f
) {
    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val radius = this.size.minDimension / 2f
        val petalRadius = radius * 0.72f
        val baseColor = tint.copy(alpha = opacity)

        // 1. Gambar 5 kelopak bunga yang melingkar simetris (masing-masing bersudut 72 derajat)
        for (i in 0 until 5) {
            val angle = i * 72f
            rotate(degrees = angle, pivot = center) {
                drawBloomingPetal(
                    center = center,
                    petalLength = petalRadius,
                    color = baseColor
                )
            }
        }

        // 2. Gambar putik dan benang sari (Stamens & Pistils) di pusat bunga
        for (i in 0 until 10) {
            val angleRad = Math.toRadians((i * 36.0 + 18.0)).toFloat()
            val stamenLength = radius * 0.32f
            val endOffset = Offset(
                x = center.x + cos(angleRad) * stamenLength,
                y = center.y + sin(angleRad) * stamenLength
            )

            // Garis benang sari
            drawLine(
                color = baseColor,
                start = center,
                end = endOffset,
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )

            // Titik serbuk sari kecil di ujung benang sari
            drawCircle(
                color = baseColor,
                radius = 3.5f,
                center = endOffset
            )
        }

        // 3. Inti tengah bunga
        drawCircle(
            color = baseColor,
            radius = radius * 0.14f
        )
    }
}

/**
 * Menggambar satu daun mahkota/kelopak bunga mekar dengan lekukan indah.
 */
private fun DrawScope.drawBloomingPetal(
    center: Offset,
    petalLength: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x, center.y)

        // Busur sisi kanan kelopak
        cubicTo(
            center.x + petalLength * 0.55f, center.y - petalLength * 0.25f,
            center.x + petalLength * 0.50f, center.y - petalLength * 0.85f,
            center.x + petalLength * 0.15f, center.y - petalLength
        )

        // Lekukan takik sakura (notched tip) di ujung kelopak
        lineTo(center.x, center.y - petalLength * 0.85f)
        lineTo(center.x - petalLength * 0.15f, center.y - petalLength)

        // Busur sisi kiri kelopak
        cubicTo(
            center.x - petalLength * 0.50f, center.y - petalLength * 0.85f,
            center.x - petalLength * 0.55f, center.y - petalLength * 0.25f,
            center.x, center.y
        )
        close()
    }

    drawPath(path = path, color = color)
}
