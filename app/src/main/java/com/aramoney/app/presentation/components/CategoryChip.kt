package com.aramoney.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.aramoney.app.presentation.theme.isAppInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Redeem
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aramoney.app.data.local.entity.CategoryEntity
import com.aramoney.app.presentation.theme.DeepBerry
import com.aramoney.app.presentation.theme.PrimarySakuraPink
import com.aramoney.app.presentation.theme.PrimarySakuraPinkContainer
import com.aramoney.app.presentation.theme.SurfaceCard
import com.aramoney.app.presentation.theme.SurfaceDark
import com.aramoney.app.presentation.theme.softShadow

/**
 * Chip Kategori estetik dengan animasi scale 1.05x saat terpilih,
 * soft shadow pastel, dan touch-target aman >= 48dp.
 */
@Composable
fun CategoryChip(
    category: CategoryEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()

    // Animasi Scale 1.05x saat terpilih
    val scaleAnim by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
        label = "ChipScale"
    )

    // Animasi Border Color
    val borderColorAnim by animateColorAsState(
        targetValue = if (isSelected) PrimarySakuraPink else Color.Transparent,
        label = "ChipBorderColor"
    )

    val chipBackground = when {
        isSelected -> if (isDark) Color(0xFF532457) else PrimarySakuraPinkContainer
        isDark -> SurfaceDark
        else -> SurfaceCard
    }

    val chipBorder = if (isSelected) {
        BorderStroke(1.8.dp, borderColorAnim)
    } else {
        BorderStroke(1.dp, if (isDark) Color(0xFF4B2E52) else Color(0xFFF7E6EE))
    }

    Surface(
        modifier = modifier
            .scale(scaleAnim)
            .defaultMinSize(minHeight = 48.dp) // Touch-target aksesibilitas >= 48dp
            .softShadow(
                elevation = if (isSelected) 6.dp else 2.dp,
                shape = RoundedCornerShape(16.dp),
                shadowColor = PrimarySakuraPink.copy(alpha = if (isSelected) 0.25f else 0.10f)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Kategori ${category.name}, ${if (isSelected) "terpilih" else "tidak terpilih"}" },
        shape = RoundedCornerShape(16.dp),
        color = chipBackground,
        border = chipBorder
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Icon kategori
            val iconVector = getIconVector(category.iconResName)
            val iconTint = runCatching { Color(android.graphics.Color.parseColor(category.tintColorHex)) }
                .getOrDefault(PrimarySakuraPink)

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = category.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) DeepBerry else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Resolver nama ikon lokal ke ImageVector Material
 */
fun getIconVector(iconName: String): ImageVector = getCategoryIconVector(iconName)
