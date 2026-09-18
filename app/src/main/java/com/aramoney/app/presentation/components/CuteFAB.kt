package com.aramoney.app.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aramoney.app.presentation.theme.PrimarySakuraPink
import com.aramoney.app.presentation.theme.SecondaryLavender
import com.aramoney.app.presentation.theme.softShadow

/**
 * Floating Action Button estetik berbentuk bunga sakura ("CuteFAB")
 * Dilengkapi animasi putar (rotate) dan membal (bounce scale) saat ditekan.
 */
@Composable
fun CuteFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 62.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Microinteraction: Animasi Scale saat ditekan
    val scaleAnim by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "FABScale"
    )

    // Microinteraction: Animasi Rotate bunga mekar saat ditekan
    val rotateAnim by animateFloatAsState(
        targetValue = if (isPressed) 45f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "FABRotate"
    )

    val fabGradient = Brush.linearGradient(
        colors = listOf(PrimarySakuraPink, SecondaryLavender)
    )

    Box(
        modifier = modifier
            .scale(scaleAnim)
            .rotate(rotateAnim)
            .size(size)
            .softShadow(
                elevation = 10.dp,
                shape = CircleShape,
                shadowColor = PrimarySakuraPink.copy(alpha = 0.45f)
            )
            .clip(CircleShape)
            .background(fabGradient)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .semantics {
                role = Role.Button
                contentDescription = "Tambah transaksi baru"
            },
        contentAlignment = Alignment.Center
    ) {
        // Lapisan ornamen bunga sakura kecil di belakang icon tambah
        FloralDecoration(
            modifier = Modifier.size(54.dp),
            size = 54.dp,
            tint = Color.White,
            opacity = 0.35f
        )

        // Ikon tambah Material 3 di tengah
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}
