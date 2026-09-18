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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.aramoney.app.presentation.theme.DeepBerryDark
import com.aramoney.app.presentation.theme.PrimarySakuraPink
import com.aramoney.app.presentation.theme.SecondaryLavender
import com.aramoney.app.presentation.theme.softShadow
import com.aramoney.app.util.CurrencyFormatter

/**
 * Hero Card utama yang menampilkan batas jajan aman harian ("Safe-To-Spend")
 * dengan animasi pertambahan angka (count-up) dan ornamen bunga sakura mekar.
 */
@Composable
fun SafeToSpendHeroCard(
    state: SafeToSpendState,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit = {}
) {
    val targetNominal = when (state) {
        is SafeToSpendState.Aman -> state.dailyBudget.toFloat()
        is SafeToSpendState.Waspada -> state.dailyBudget.toFloat()
        is SafeToSpendState.Bahaya -> state.dailyBudget.toFloat()
        else -> 0f
    }

    // Animasi Count-Up angka saldo harian
    val animatedNominal by animateFloatAsState(
        targetValue = targetNominal,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "HeroCountUp"
    )

    val cardGradient = Brush.linearGradient(
        colors = listOf(PrimarySakuraPink, SecondaryLavender)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(elevation = 12.dp, shape = RoundedCornerShape(28.dp), shadowColor = PrimarySakuraPink.copy(alpha = 0.35f))
            .clip(RoundedCornerShape(28.dp))
            .background(cardGradient)
            .padding(22.dp)
    ) {
        // Ornamen Bunga Sakura di sudut kanan atas dengan opacity 0.30
        FloralDecoration(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 6.dp, y = (-6).dp),
            size = 110.dp,
            tint = Color.White,
            opacity = 0.30f
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Label Header Card
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✨ SAFE TO SPEND HARI INI",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }

            // Angka Utama Saldo Harian Aman
            when (state) {
                is SafeToSpendState.NeedsSetup, is SafeToSpendState.EmptyBalance -> {
                    Text(
                        text = "Rp 0",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                is SafeToSpendState.NeedsDateUpdate -> {
                    Text(
                        text = "Update Tanggal 📅",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                else -> {
                    Text(
                        text = CurrencyFormatter.formatRupiah(animatedNominal.toDouble()),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Pesan Copywriting Ramah Mahasiswi
            val messageText = when (state) {
                is SafeToSpendState.Aman -> state.message
                is SafeToSpendState.Waspada -> state.message
                is SafeToSpendState.Bahaya -> state.message
                is SafeToSpendState.NeedsDateUpdate -> state.message
                is SafeToSpendState.NeedsSetup -> state.message
                is SafeToSpendState.EmptyBalance -> state.message
            }

            Surface(
                color = Color.White.copy(alpha = 0.20f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = messageText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Sub-info: Total Saldo Tersisa & Sisa Hari Menuju Kiriman
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val totalBalance = when (state) {
                    is SafeToSpendState.Aman -> state.totalBalance
                    is SafeToSpendState.Waspada -> state.totalBalance
                    is SafeToSpendState.Bahaya -> state.totalBalance
                    is SafeToSpendState.NeedsDateUpdate -> state.totalBalance
                    else -> 0.0
                }

                val remainingDays = when (state) {
                    is SafeToSpendState.Aman -> state.remainingDays
                    is SafeToSpendState.Waspada -> state.remainingDays
                    is SafeToSpendState.Bahaya -> state.remainingDays
                    is SafeToSpendState.EmptyBalance -> state.remainingDays
                    else -> 0L
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Total Saldo: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = CurrencyFormatter.formatRupiah(totalBalance),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Sisa Hari: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = "$remainingDays hari",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
