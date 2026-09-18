package com.aramoney.app.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Sistem Bentuk Melengkung (Curved Shapes) "Ara Money"
 * Menghasilkan kesan ramah, imut, dan organik khas desain estetik.
 */
val AraShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),   // Chip kecil / pill kecil
    small = RoundedCornerShape(12.dp),       // Badge, tag kategori, tombol kecil
    medium = RoundedCornerShape(20.dp),      // Card transaksi, item list
    large = RoundedCornerShape(28.dp),       // Hero card saldo, bottom sheet
    extraLarge = RoundedCornerShape(32.dp)   // Dialog konfirmasi & alert popup
)

/**
 * Custom modifier untuk menghasilkan soft shadow dengan tint warna pastel (bukan abu-abu kaku).
 * Menggunakan spotColor dan ambientColor pada hardware-accelerated render node Compose.
 */
fun Modifier.softShadow(
    elevation: Dp = 8.dp,
    shape: Shape = RoundedCornerShape(20.dp),
    shadowColor: Color = Color(0xFFF48FB1).copy(alpha = 0.22f),
    clip: Boolean = false
): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    clip = clip,
    ambientColor = shadowColor,
    spotColor = shadowColor
)
