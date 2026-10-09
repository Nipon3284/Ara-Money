package com.aramoney.app.presentation.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aramoney.app.presentation.theme.AraShape
import com.aramoney.app.presentation.theme.AraTheme

/** Blok placeholder berdenyut lembut untuk loading skeleton. */
@Composable
fun SkeletonBlock(
    height: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = AraShape.card
) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "skeletonAlpha"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(AraTheme.colors.surfaceCard.copy(alpha = alpha))
    )
}

/** Skeleton generik: satu kartu besar + beberapa baris list. */
@Composable
fun ListScreenSkeleton(
    modifier: Modifier = Modifier,
    heroHeight: Dp = 220.dp,
    rows: Int = 4
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Memuat data" },
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SkeletonBlock(height = heroHeight, shape = AraShape.dialog)
        SkeletonBlock(height = 24.dp, modifier = Modifier.fillMaxWidth(0.5f), shape = AraShape.chip)
        repeat(rows) { SkeletonBlock(height = 72.dp) }
    }
}
