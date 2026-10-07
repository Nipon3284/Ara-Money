package com.aramoney.app.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aramoney.app.domain.model.SafeToSpendState
import com.aramoney.app.presentation.theme.HeroGradientEnd
import com.aramoney.app.presentation.theme.HeroGradientStart
import com.aramoney.app.presentation.theme.softShadow
import com.aramoney.app.util.CurrencyFormatter

/**
 * Hero Card utama dengan konsep Triple-Lens Financial Horizon:
 * 1. Lensa Rata-Rata Aktual (Highlight Utama)
 * 2. Lensa Sisa Jatah Hari Ini (Micro-Action)
 * 3. Lensa Daya Tahan Saldo / Runway (Peace of Mind)
 */
@Composable
fun SafeToSpendHeroCard(
    state: SafeToSpendState,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit = {}
) {
    val targetNominal = when (state) {
        is SafeToSpendState.Aman -> state.dailyAverageExpense.toFloat()
        is SafeToSpendState.Waspada -> state.dailyAverageExpense.toFloat()
        is SafeToSpendState.Bahaya -> state.dailyAverageExpense.toFloat()
        is SafeToSpendState.EmptyBalance -> state.dailyAverageExpense.toFloat()
        else -> 0f
    }

    // Animasi Count-Up angka pengeluaran rata-rata harian (600ms smooth)
    val animatedNominal by animateFloatAsState(
        targetValue = targetNominal,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "HeroDailyAverageCountUp"
    )

    val cardGradient = Brush.linearGradient(
        colors = listOf(HeroGradientStart, HeroGradientEnd)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                shadowColor = HeroGradientStart.copy(alpha = 0.35f)
            )
            .clip(RoundedCornerShape(28.dp))
            .background(cardGradient)
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
            // 1. Header: Status Tag Empatis & Indikator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val (icon, statusText) = when (state) {
                            is SafeToSpendState.Aman -> Icons.Rounded.CheckCircle to "Rata-Rata Terkendali 🌸"
                            is SafeToSpendState.Waspada -> Icons.Rounded.WarningAmber to "Cek Pengeluaran 🍵"
                            is SafeToSpendState.Bahaya -> Icons.Rounded.Favorite to "Perlu Hemat 🎀"
                            else -> Icons.Rounded.CalendarMonth to "Kondisi Dompet 🌸"
                        }
                        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // 2. Angka Utama (Rata-rata Pengeluaran Per Hari)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Pengeluaran Rata-Rata Bulan Ini",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.88f)
                )

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = CurrencyFormatter.formatRupiah(animatedNominal.toDouble()),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "/ hari",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            // 3. Pesan Empatis Pendukung (Frosted Glass Container)
            val messageText = when (state) {
                is SafeToSpendState.Aman -> state.message
                is SafeToSpendState.Waspada -> state.message
                is SafeToSpendState.Bahaya -> state.message
                is SafeToSpendState.NeedsDateUpdate -> state.message
                is SafeToSpendState.NeedsSetup -> state.message
                is SafeToSpendState.EmptyBalance -> state.message
            }

            Surface(
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = messageText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }

            // 4. Dua Kartu Mini: Sisa Kuota Hari Ini & Daya Tahan Saldo
            val remainingTodayBudget = when (state) {
                is SafeToSpendState.Aman -> state.remainingTodayBudget
                is SafeToSpendState.Waspada -> state.remainingTodayBudget
                is SafeToSpendState.Bahaya -> state.remainingTodayBudget
                is SafeToSpendState.EmptyBalance -> state.remainingTodayBudget
                else -> 0.0
            }

            val remainingDays = when (state) {
                is SafeToSpendState.Aman -> state.remainingDays
                is SafeToSpendState.Waspada -> state.remainingDays
                is SafeToSpendState.Bahaya -> state.remainingDays
                is SafeToSpendState.EmptyBalance -> state.remainingDays
                else -> 0L
            }

            val totalBalance = when (state) {
                is SafeToSpendState.Aman -> state.totalBalance
                is SafeToSpendState.Waspada -> state.totalBalance
                is SafeToSpendState.Bahaya -> state.totalBalance
                is SafeToSpendState.NeedsDateUpdate -> state.totalBalance
                is SafeToSpendState.EmptyBalance -> state.totalBalance
                else -> 0.0
            }

            val dailyTargetBudget = when (state) {
                is SafeToSpendState.Aman -> state.dailyTargetBudget
                is SafeToSpendState.Waspada -> state.dailyTargetBudget
                is SafeToSpendState.Bahaya -> state.dailyTargetBudget
                else -> 0.0
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Sisa Kuota Hari Ini
                Surface(
                    color = Color.Black.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Jatah Hari Ini",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = CurrencyFormatter.formatRupiah(remainingTodayBudget),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Daya Tahan Saldo (Runway)
                Surface(
                    color = Color.Black.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Daya Tahan",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (remainingDays <= 0L) "< 1 Hari" else "$remainingDays Hari Lagi",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Total Saldo Berjalan & Target di bagian bawah
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Total Saldo: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = CurrencyFormatter.formatRupiah(totalBalance),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (dailyTargetBudget > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Target: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "${CurrencyFormatter.formatRupiah(dailyTargetBudget)}/hr",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
