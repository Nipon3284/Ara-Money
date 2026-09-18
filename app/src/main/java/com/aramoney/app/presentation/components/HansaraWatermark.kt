package com.aramoney.app.presentation.components

import com.aramoney.app.presentation.theme.isAppInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aramoney.app.presentation.theme.DeepBerry
import com.aramoney.app.presentation.theme.PrimarySakuraPink

/**
 * Watermark nama "hansara" berdesain minimalis, lembut, dan elegan.
 * Diletakkan secara halus (subtle) di bagian bawah layar tanpa mengganggu fungsi utama aplikasi.
 */
@Composable
fun HansaraWatermark(
    modifier: Modifier = Modifier,
    prefix: String = "✨",
    suffix: String = "🌸"
) {
    val isDark = isAppInDarkTheme()
    val watermarkColor = if (isDark) {
        PrimarySakuraPink.copy(alpha = 0.50f)
    } else {
        DeepBerry.copy(alpha = 0.40f)
    }

    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "$prefix hansara $suffix",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 1.8.sp,
            color = watermarkColor
        )
    }
}
