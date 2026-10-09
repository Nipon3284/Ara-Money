package com.aramoney.app.presentation.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aramoney.app.R
import com.aramoney.app.presentation.components.AraPrimaryButton
import com.aramoney.app.presentation.components.DailyTargetPicker
import com.aramoney.app.presentation.components.FloralDecoration
import com.aramoney.app.presentation.components.HansaraWatermark
import com.aramoney.app.presentation.components.RupiahVisualTransformation
import com.aramoney.app.presentation.components.SakuraBackground
import com.aramoney.app.presentation.components.TodayOrFutureSelectableDates
import com.aramoney.app.presentation.components.araTextFieldColors
import com.aramoney.app.presentation.theme.AraShape
import com.aramoney.app.presentation.theme.AraTheme
import com.aramoney.app.presentation.theme.PrimarySakuraPink
import com.aramoney.app.presentation.theme.SecondaryLavender
import com.aramoney.app.presentation.theme.softShadow
import com.aramoney.app.util.CurrencyFormatter
import com.aramoney.app.util.DateTimeUtil
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val stepCount = OnboardingViewModel.STEP_COUNT
    val pagerState = rememberPagerState(pageCount = { stepCount })
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val currentStep = pagerState.currentPage

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) onFinishOnboarding()
    }

    fun goTo(page: Int) {
        focusManager.clearFocus()
        coroutineScope.launch { pagerState.animateScrollToPage(page) }
    }

    // Tombol Back sistem kembali ke langkah sebelumnya, bukan keluar aplikasi
    BackHandler(enabled = currentStep > 0) { goTo(currentStep - 1) }

    val validation = uiState.validationFor(currentStep)
    val isLastPage = currentStep == stepCount - 1

    SakuraBackground(modifier = modifier) {
        Scaffold(containerColor = Color.Transparent) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Baris atas: tombol kembali + indikator langkah
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(48.dp)) {
                        if (currentStep > 0) {
                            IconButton(onClick = { goTo(currentStep - 1) }) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Kembali ke langkah sebelumnya",
                                    tint = AraTheme.colors.textStrong
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .semantics(mergeDescendants = true) {
                                contentDescription = "Langkah ${currentStep + 1} dari $stepCount"
                            },
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ) {
                        repeat(stepCount) { index ->
                            val isCurrent = currentStep == index
                            Box(
                                modifier = Modifier
                                    .size(if (isCurrent) 24.dp else 10.dp, 10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(
                                        if (index <= currentStep) PrimarySakuraPink else PrimarySakuraPink.copy(alpha = 0.25f)
                                    )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(48.dp))
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    userScrollEnabled = false // Navigasi via tombol agar validasi tiap langkah terjaga
                ) { page ->
                    when (page) {
                        0 -> StepWelcome(
                            userName = uiState.userName,
                            onNameChange = viewModel::onUserNameChanged,
                            onNext = { if (uiState.validationFor(0) == null) goTo(1) }
                        )
                        1 -> StepInitialBalance(
                            balanceText = uiState.initialBalanceText,
                            onBalanceChange = viewModel::onInitialBalanceChanged,
                            onNext = { if (uiState.validationFor(1) == null) goTo(2) }
                        )
                        2 -> StepDailyTargetBudget(
                            selectedPreset = uiState.dailyTargetPreset,
                            customBudgetText = uiState.customDailyBudgetText,
                            initialBalance = uiState.initialBalance,
                            activeTarget = uiState.effectiveDailyTarget,
                            onPresetSelected = viewModel::onDailyTargetPresetSelected,
                            onCustomBudgetChange = viewModel::onCustomDailyBudgetChanged
                        )
                        3 -> StepAllowanceDate(
                            selectedDate = uiState.nextAllowanceDate,
                            initialBalance = uiState.initialBalance,
                            onDateSelected = viewModel::onNextAllowanceDateChanged
                        )
                    }
                }

                // Alasan tombol nonaktif
                if (validation != null) {
                    Text(
                        text = validation,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                AraPrimaryButton(
                    text = if (isLastPage) "Mulai Kelola Keuangan" else "Lanjut",
                    onClick = {
                        if (isLastPage) viewModel.completeOnboarding() else goTo(currentStep + 1)
                    },
                    enabled = validation == null,
                    isLoading = uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isLastPage) {
                    TextButton(
                        onClick = { viewModel.completeOnboarding(skipAllowanceDate = true) },
                        enabled = !uiState.isSaving,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("Lewati, atur nanti di Pengaturan", color = AraTheme.colors.accent)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepTitle(title: String, subtitle: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = AraTheme.colors.textStrong,
        textAlign = TextAlign.Center,
        modifier = Modifier.semantics { heading() }
    )
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@Composable
private fun StepWelcome(
    userName: String,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = null,
            modifier = Modifier
                .size(105.dp)
                .softShadow(elevation = 8.dp, shape = RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
        )

        StepTitle(
            title = "Selamat datang di Ara Money",
            subtitle = "Teman setia mahasiswi untuk atur jajan, menabung, dan bebas cemas di akhir bulan."
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = onNameChange,
            label = { Text("Nama panggilan") },
            placeholder = { Text("Mis. Ara") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { onNext() }),
            shape = AraShape.button,
            colors = araTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        HansaraWatermark(modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun StepInitialBalance(
    balanceText: String,
    onBalanceChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.size(110.dp), contentAlignment = Alignment.Center) {
            FloralDecoration(size = 110.dp, tint = SecondaryLavender, opacity = 0.45f)
            Icon(Icons.Rounded.Savings, contentDescription = null, tint = AraTheme.colors.berry, modifier = Modifier.size(44.dp))
        }

        StepTitle(
            title = "Berapa uang sakumu saat ini?",
            subtitle = "Masukkan total saldo di rekening dan dompetmu hari ini. Boleh Rp0 kalau belum ada."
        )

        OutlinedTextField(
            value = balanceText,
            onValueChange = onBalanceChange,
            label = { Text("Saldo saat ini") },
            prefix = { Text("Rp") },
            placeholder = { Text("500.000") },
            visualTransformation = RupiahVisualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { onNext() }),
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineSmall,
            shape = AraShape.button,
            colors = araTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StepDailyTargetBudget(
    selectedPreset: Long?,
    customBudgetText: String,
    initialBalance: Double,
    activeTarget: Double,
    onPresetSelected: (Long) -> Unit,
    onCustomBudgetChange: (String) -> Unit
) {
    val estimatedDays = if (activeTarget > 0 && initialBalance > 0) (initialBalance / activeTarget).toLong() else 0L

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StepTitle(
            title = "Target jajan harianmu?",
            subtitle = "Batas jajan per hari yang nyaman. Dipakai untuk menghitung jatah hari ini dan berapa lama saldo bertahan."
        )

        DailyTargetPicker(
            selectedPreset = selectedPreset,
            rawCustom = customBudgetText,
            onPresetSelected = onPresetSelected,
            onCustomChanged = onCustomBudgetChange
        )

        if (initialBalance > 0 && activeTarget > 0) {
            EstimateCard(
                title = "Perkiraan daya tahan saldo",
                body = "Dengan saldo ${CurrencyFormatter.formatRupiah(initialBalance)} dan target " +
                    "${CurrencyFormatter.formatRupiah(activeTarget)}/hari, saldo Kakak cukup untuk sekitar $estimatedDays hari."
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepAllowanceDate(
    selectedDate: LocalDate?,
    initialBalance: Double,
    onDateSelected: (LocalDate) -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.let { DateTimeUtil.localDateToUtcMillis(it) },
        selectableDates = TodayOrFutureSelectableDates
    )
    LaunchedEffect(state.selectedDateMillis) {
        state.selectedDateMillis?.let { onDateSelected(DateTimeUtil.utcMillisToLocalDate(it)) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = AraTheme.colors.accent, modifier = Modifier.size(40.dp))
        StepTitle(
            title = "Kapan uang kiriman berikutnya?",
            subtitle = "Supaya Ara bisa mengingatkan kalau saldo diperkirakan habis sebelum kiriman datang."
        )

        Surface(
            shape = AraShape.card,
            color = AraTheme.colors.surfaceElevated,
            modifier = Modifier.fillMaxWidth()
        ) {
            DatePicker(state = state, title = null, headline = null, showModeToggle = false)
        }

        if (selectedDate != null) {
            val days = ChronoUnit.DAYS.between(LocalDate.now(), selectedDate)
            EstimateCard(
                title = "${DateTimeUtil.formatLocalDate(selectedDate)} • $days hari lagi",
                body = if (initialBalance > 0 && days > 0) {
                    "Supaya cukup sampai hari itu, jatah aman Kakak sekitar ${CurrencyFormatter.formatRupiah(initialBalance / days)}/hari."
                } else {
                    "Tanggal ini bisa diubah kapan saja di Pengaturan."
                }
            )
        }
    }
}

@Composable
private fun EstimateCard(title: String, body: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(elevation = 3.dp, shape = AraShape.card),
        shape = AraShape.card,
        color = AraTheme.colors.selectedContainer
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = AraTheme.colors.onSelectedContainer
            )
            Text(text = body, style = MaterialTheme.typography.bodySmall, color = AraTheme.colors.onSelectedContainer)
        }
    }
}
