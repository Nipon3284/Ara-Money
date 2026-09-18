package com.aramoney.app.presentation.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.aramoney.app.presentation.theme.isAppInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.aramoney.app.R
import com.aramoney.app.presentation.components.FloralDecoration
import com.aramoney.app.presentation.components.HansaraWatermark
import com.aramoney.app.presentation.components.SakuraBackground
import com.aramoney.app.presentation.theme.DeepBerry
import com.aramoney.app.presentation.theme.DeepBerryDark
import com.aramoney.app.presentation.theme.ErrorSoftRed
import com.aramoney.app.presentation.theme.PrimarySakuraPink
import com.aramoney.app.presentation.theme.PrimarySakuraPinkLight
import com.aramoney.app.presentation.theme.SecondaryLavender
import com.aramoney.app.presentation.theme.SurfaceCard
import com.aramoney.app.presentation.theme.SurfaceCardDark
import com.aramoney.app.presentation.theme.SurfaceElevated
import com.aramoney.app.presentation.theme.SurfaceElevatedDark
import com.aramoney.app.presentation.theme.TextPrimaryDark
import com.aramoney.app.presentation.theme.softShadow
import com.aramoney.app.util.CurrencyFormatter
import com.aramoney.app.util.DateTimeUtil
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = isAppInDarkTheme()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onFinishOnboarding()
        }
    }

    SakuraBackground(modifier = modifier) {
        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Indikator Pager Dots (3 titik pastel)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    repeat(3) { index ->
                        val isCurrent = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (isCurrent) 24.dp else 10.dp, 10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    if (isCurrent) PrimarySakuraPink else PrimarySakuraPink.copy(alpha = 0.25f)
                                )
                        )
                    }
                }

                // Konten 3 Langkah HorizontalPager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    userScrollEnabled = false // Navigasi melalui tombol agar teratur
                ) { page ->
                    when (page) {
                        0 -> StepWelcome(
                            userName = uiState.userName,
                            onNameChange = { viewModel.onUserNameChanged(it) },
                            isDark = isDark
                        )
                        1 -> StepInitialBalance(
                            balanceText = uiState.initialBalanceText,
                            onBalanceChange = { viewModel.onInitialBalanceChanged(it) },
                            isDark = isDark
                        )
                        2 -> StepNextAllowanceDate(
                            selectedDate = uiState.nextAllowanceDate,
                            onDateSelected = { viewModel.onNextAllowanceDateChanged(it) },
                            isDark = isDark
                        )
                    }
                }

                // Pesan Error jika ada
                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = ErrorSoftRed,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Tombol Navigasi Lanjut / Selesai
                val isLastPage = pagerState.currentPage == 2
                val canProceed = when (pagerState.currentPage) {
                    0 -> uiState.isStep1Valid
                    1 -> uiState.isStep2Valid
                    else -> true
                }

                Button(
                    onClick = {
                        if (isLastPage) {
                            viewModel.completeOnboarding()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    enabled = canProceed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .softShadow(
                            elevation = if (canProceed) 8.dp else 0.dp,
                            shape = RoundedCornerShape(20.dp),
                            shadowColor = PrimarySakuraPink.copy(alpha = 0.40f)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimarySakuraPink,
                        disabledContainerColor = if (isDark) Color(0xFF422847) else Color(0xFFE8DCE2)
                    )
                ) {
                    Text(
                        text = if (isLastPage) "Mulai Kelola Keuangan 🌸" else "Lanjut ✨",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (canProceed) Color.White else Color(0xFF9E8B95)
                    )
                }
            }
        }
    }
}

@Composable
private fun StepWelcome(
    userName: String,
    onNameChange: (String) -> Unit,
    isDark: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(110.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Ara Money Mascot",
                modifier = Modifier
                    .size(105.dp)
                    .softShadow(elevation = 8.dp, shape = RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
            )
        }

        Text(
            text = "Selamat Datang di Ara Money! 🌸",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (isDark) TextPrimaryDark else DeepBerryDark,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Teman setia mahasiswi untuk atur jajan, nabung, dan bebas cemas tanggal tua.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = onNameChange,
            label = { Text("Nama Panggilanmu") },
            placeholder = { Text("Contoh: Ara / Clara") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimarySakuraPink,
                focusedContainerColor = if (isDark) SurfaceElevatedDark else SurfaceElevated,
                unfocusedContainerColor = if (isDark) SurfaceElevatedDark else SurfaceElevated
            ),
            modifier = Modifier.fillMaxWidth()
        )

        HansaraWatermark(
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun StepInitialBalance(
    balanceText: String,
    onBalanceChange: (String) -> Unit,
    isDark: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(110.dp),
            contentAlignment = Alignment.Center
        ) {
            FloralDecoration(size = 110.dp, tint = SecondaryLavender, opacity = 0.45f)
            Icon(
                imageVector = Icons.Rounded.Savings,
                contentDescription = null,
                tint = DeepBerry,
                modifier = Modifier.size(44.dp)
            )
        }

        Text(
            text = "Berapa Uang Sakumu Saat Ini? 💰",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (isDark) TextPrimaryDark else DeepBerryDark,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Masukkan total sisa saldo di rekening atau dompet fisikmu hari ini.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Live Preview Format Rupiah
        val amount = balanceText.toDoubleOrNull() ?: 0.0
        Surface(
            color = if (isDark) SurfaceCardDark else SurfaceCard,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = CurrencyFormatter.formatRupiah(amount),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) PrimarySakuraPink else DeepBerryDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 14.dp)
            )
        }

        OutlinedTextField(
            value = balanceText,
            onValueChange = onBalanceChange,
            label = { Text("Nominal Saldo Awal (Rp)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimarySakuraPink,
                focusedContainerColor = if (isDark) SurfaceElevatedDark else SurfaceElevated,
                unfocusedContainerColor = if (isDark) SurfaceElevatedDark else SurfaceElevated
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepNextAllowanceDate(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    isDark: Boolean
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    // Update state saat user memilih tanggal pada picker
    LaunchedEffect(datePickerState.selectedDateMillis) {
        val millis = datePickerState.selectedDateMillis
        if (millis != null) {
            val picked = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
            onDateSelected(picked)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Kapan Kiriman Uang Berikutnya? 📅",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (isDark) TextPrimaryDark else DeepBerryDark,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Penting untuk menghitung batas jajan harian aman (Safe-to-Spend).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .softShadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = if (isDark) SurfaceCardDark else SurfaceCard
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    containerColor = Color.Transparent,
                    selectedDayContainerColor = PrimarySakuraPink,
                    selectedDayContentColor = Color.White,
                    todayDateBorderColor = PrimarySakuraPink
                )
            )
        }
    }
}
