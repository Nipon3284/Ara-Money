package com.aramoney.app.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aramoney.app.domain.model.SafeToSpendState
import com.aramoney.app.presentation.theme.HeroGradientEnd
import com.aramoney.app.presentation.theme.HeroGradientStart
import com.aramoney.app.presentation.theme.softShadow
import com.aramoney.app.util.CurrencyFormatter

/**
 * Hero Card utama Beranda. Hierarki informasi:
 * 1. Jatah Hari Ini (angka terbesar — menjawab "hari ini aku masih boleh jajan berapa?")
 * 2. Pesan empatis sesuai status
 * 3. Metrik pendukung: Rata-rata harian bulan ini & Daya Tahan saldo
 * 4. Footer: Total saldo & target harian
 */
@Composable
fun SafeToSpendHeroCard(
    state: SafeToSpendState,
    modifier: Modifier = Modifier,
    onCardClick: (() -> Unit)? = null
) {
    val metrics = state.toHeroMetrics()

    // Animasi Count-Up angka jatah hari ini (600ms smooth)
    val animatedNominal by animateFloatAsState(
        targetValue = metrics.remainingToday.toFloat(),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "HeroRemainingTodayCountUp"
    )

    val cardGradient = Brush.linearGradient(
        colors = listOf(HeroGradientStart, HeroGradientEnd)
    )
    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(
                elevation = 12.dp,
                shape = shape,
                shadowColor = HeroGradientStart.copy(alpha = 0.35f)
            )
            .clip(shape)
            .background(cardGradient)
            .then(
                if (onCardClick != null) Modifier.clickable(onClick = onCardClick) else Modifier
            )
            .padding(20.dp)
    ) {
        // Ornamen Bunga Sakura di sudut kanan atas
        FloralDecoration(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 8.dp, y = (-8).dp),
            size = 116.dp,
            tint = Color.White,
            opacity = 0.22f
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Status Tag Empatis
            val (icon, statusText) = when (state) {
                is SafeToSpendState.Aman -> Icons.Rounded.CheckCircle to "Aman"
                is SafeToSpendState.Waspada -> Icons.Rounded.WarningAmber to "Waspada"
                is SafeToSpendState.Bahaya -> Icons.Rounded.Favorite to "Perlu Hemat"
                else -> Icons.Rounded.CalendarMonth to "Kondisi Dompet"
            }
            Surface(
                color = Color.White.copy(alpha = 0.22f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .semantics { contentDescription = "Status keuangan: $statusText" },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // 2. Angka Utama: Jatah Hari Ini
            Column(
                modifier = Modifier.clearAndSetSemantics {
                    heading()
                    contentDescription = "Jatah jajan hari ini tersisa ${CurrencyFormatter.formatRupiah(metrics.remainingToday)}"
                },
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Jatah Jajan Hari Ini",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = CurrencyFormatter.formatRupiah(animatedNominal.toDouble()),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (metrics.todayExpense > 0.0) {
                    Text(
                        text = "Sudah terpakai ${CurrencyFormatter.formatRupiah(metrics.todayExpense)} hari ini",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // 3. Pesan Empatis Pendukung (Frosted Glass Container)
            Surface(
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = metrics.message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }

            // 4. Dua Kartu Mini: Rata-rata Harian & Daya Tahan Saldo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HeroMiniMetric(
                    label = "Rata-rata / hari",
                    value = CurrencyFormatter.formatRupiah(metrics.dailyAverage),
                    modifier = Modifier.weight(1f)
                )
                HeroMiniMetric(
                    label = "Daya Tahan Saldo",
                    value = if (metrics.remainingDays <= 0L) "< 1 hari" else "${metrics.remainingDays} hari",
                    modifier = Modifier.weight(1f)
                )
            }

            // 5. Footer: Total Saldo & Target
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeroFooterLabel(label = "Total Saldo", value = CurrencyFormatter.formatRupiah(metrics.totalBalance))
                if (metrics.dailyTarget > 0) {
                    HeroFooterLabel(label = "Target", value = "${CurrencyFormatter.formatRupiah(metrics.dailyTarget)}/hari")
                }
            }
        }
    }
}

@Composable
private fun HeroMiniMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        color = Color.Black.copy(alpha = 0.14f),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .clearAndSetSemantics { contentDescription = "$label: $value" }
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HeroFooterLabel(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clearAndSetSemantics { contentDescription = "$label: $value" }
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.9f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/** Ringkasan angka yang dibutuhkan Hero Card dari setiap varian [SafeToSpendState]. */
private data class HeroMetrics(
    val remainingToday: Double,
    val todayExpense: Double,
    val dailyAverage: Double,
    val remainingDays: Long,
    val totalBalance: Double,
    val dailyTarget: Double,
    val message: String
)

private fun SafeToSpendState.toHeroMetrics(): HeroMetrics = when (this) {
    is SafeToSpendState.Aman -> HeroMetrics(remainingTodayBudget, todayExpense, dailyAverageExpense, remainingDays, totalBalance, dailyTargetBudget, message)
    is SafeToSpendState.Waspada -> HeroMetrics(remainingTodayBudget, todayExpense, dailyAverageExpense, remainingDays, totalBalance, dailyTargetBudget, message)
    is SafeToSpendState.Bahaya -> HeroMetrics(remainingTodayBudget, todayExpense, dailyAverageExpense, remainingDays, totalBalance, dailyTargetBudget, message)
    is SafeToSpendState.EmptyBalance -> HeroMetrics(remainingTodayBudget, 0.0, dailyAverageExpense, remainingDays, totalBalance, 0.0, message)
    is SafeToSpendState.NeedsDateUpdate -> HeroMetrics(0.0, 0.0, 0.0, 0L, totalBalance, 0.0, message)
    is SafeToSpendState.NeedsSetup -> HeroMetrics(0.0, 0.0, 0.0, 0L, 0.0, 0.0, message)
}
