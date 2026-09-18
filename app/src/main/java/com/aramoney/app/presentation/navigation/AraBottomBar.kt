package com.aramoney.app.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.aramoney.app.presentation.theme.isAppInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aramoney.app.presentation.theme.BackgroundCream
import com.aramoney.app.presentation.theme.BackgroundDark
import com.aramoney.app.presentation.theme.DeepBerry
import com.aramoney.app.presentation.theme.DeepBerryDark
import com.aramoney.app.presentation.theme.PrimarySakuraPink
import com.aramoney.app.presentation.theme.PrimarySakuraPinkContainer
import com.aramoney.app.presentation.theme.softShadow

/**
 * Custom Bottom Navigation Bar bergaya floating pastel
 * dengan background gradien tipis dan indikator pill merah muda saat aktif.
 */
@Composable
fun AraBottomBar(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()

    val barGradient = Brush.verticalGradient(
        colors = if (isDark) {
            listOf(Color(0xFF381F3D), BackgroundDark)
        } else {
            listOf(Color(0xFFFFF0F5), BackgroundCream)
        }
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .softShadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(26.dp),
                shadowColor = PrimarySakuraPink.copy(alpha = 0.22f)
            ),
        shape = RoundedCornerShape(26.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(barGradient)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AraScreen.bottomNavScreens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    AraBottomNavItem(
                        screen = screen,
                        isSelected = isSelected,
                        onClick = { onNavigateToRoute(screen.route) },
                        isDark = isDark
                    )
                }
            }
        }
    }
}

@Composable
private fun AraBottomNavItem(
    screen: AraScreen,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDark: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }

    val scaleAnim by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1.0f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "NavItemScale"
    )

    val pillBackground by animateColorAsState(
        targetValue = if (isSelected) {
            if (isDark) Color(0xFF562260) else PrimarySakuraPinkContainer
        } else Color.Transparent,
        animationSpec = tween(durationMillis = 180),
        label = "PillBg"
    )

    val contentTint by animateColorAsState(
        targetValue = if (isSelected) {
            PrimarySakuraPink
        } else {
            if (isDark) Color(0xFFB38CAE) else DeepBerry.copy(alpha = 0.65f)
        },
        animationSpec = tween(durationMillis = 180),
        label = "ContentTint"
    )

    Box(
        modifier = Modifier
            .scale(scaleAnim)
            .clip(RoundedCornerShape(18.dp))
            .background(pillBackground)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .semantics {
                role = Role.Tab
                contentDescription = "Tab ${screen.title}, ${if (isSelected) "aktif" else "tidak aktif"}"
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = screen.icon,
                contentDescription = null,
                tint = contentTint,
                modifier = Modifier.size(22.dp)
            )

            // Label muncul dengan animasi halus saat tab aktif (Pill Indicator)
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(animationSpec = tween(180)) + expandHorizontally(animationSpec = tween(180)),
                exit = fadeOut(animationSpec = tween(180)) + shrinkHorizontally(animationSpec = tween(180))
            ) {
                Text(
                    text = screen.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFF8E9F0) else DeepBerryDark,
                    fontSize = 13.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
