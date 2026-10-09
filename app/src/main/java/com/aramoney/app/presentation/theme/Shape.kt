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
 * Token jarak (spacing) standar agar ritme layout konsisten antar layar.
 */
object AraSpacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    /** Padding horizontal standar layar. */
    val screen = 20.dp
    val lg = 24.dp
    val xl = 32.dp
}

/**
 * Bentuk standar komponen interaktif.
 */
object AraShape {
    val chip = RoundedCornerShape(12.dp)
    val button = RoundedCornerShape(16.dp)
    val card = RoundedCornerShape(20.dp)
    val sheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val dialog = RoundedCornerShape(28.dp)
}

/**
 * Custom modifier untuk menghasilkan soft shadow dengan tint warna pastel (bukan abu-abu kaku).
 * Menggunakan spotColor dan ambientColor pada hardware-accelerated render node Compose.
 */
fun Modifier.softShadow(
    elevation: Dp = 8.dp,
    shape: Shape = RoundedCornerShape(20.dp),
    shadowColor: Color = SoftShadowPink,
    clip: Boolean = false
): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    clip = clip,
    ambientColor = shadowColor,
    spotColor = shadowColor
)
