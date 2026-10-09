package com.aramoney.app.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aramoney.app.data.datastore.UserPreferencesRepository
import com.aramoney.app.presentation.components.DailyTargetPresets
import com.aramoney.app.util.AmountInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class OnboardingUiState(
    val userName: String = "",
    val initialBalanceText: String = "",
    val dailyTargetPreset: Long? = 30_000L,
    val customDailyBudgetText: String = "",
    val nextAllowanceDate: LocalDate? = null,
    val isSaving: Boolean = false,
    val isCompleted: Boolean = false
) {
    val initialBalance: Double
        get() = AmountInput.toAmount(initialBalanceText)

    val effectiveDailyTarget: Double
        get() = customDailyBudgetText.toDoubleOrNull() ?: dailyTargetPreset?.toDouble() ?: 0.0

    /** Pesan alasan tombol "Lanjut" nonaktif per langkah, atau null jika valid. */
    fun validationFor(step: Int): String? = when (step) {
        0 -> if (userName.isBlank()) "Isi nama panggilan dulu ya, Kak" else null
        1 -> if (initialBalanceText.isBlank()) "Isi saldo saat ini (boleh Rp0 jika belum ada)" else null
        2 -> if (effectiveDailyTarget <= 0.0) "Pilih atau isi target jajan harian" else null
        3 -> if (nextAllowanceDate == null) "Pilih tanggal kiriman berikutnya, atau lewati langkah ini" else null
        else -> null
    }
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    companion object {
        const val STEP_COUNT = 4
        const val MAX_NAME_LENGTH = 30
        /** Perkiraan default jika pengguna melewati langkah tanggal kiriman. */
        const val DEFAULT_ALLOWANCE_DAYS = 30L
    }

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onUserNameChanged(name: String) {
        _uiState.update { it.copy(userName = name.take(MAX_NAME_LENGTH)) }
    }

    fun onInitialBalanceChanged(balanceText: String) {
        // Saldo awal boleh "0", jadi tidak memakai AmountInput.sanitize (yang membuang nol di depan)
        val clean = balanceText.filter { it.isDigit() }.take(AmountInput.MAX_DIGITS + 1)
        val normalized = clean.trimStart('0').ifEmpty { if (clean.isNotEmpty()) "0" else "" }
        _uiState.update { it.copy(initialBalanceText = normalized) }
    }

    fun onDailyTargetPresetSelected(preset: Long) {
        _uiState.update { it.copy(dailyTargetPreset = preset, customDailyBudgetText = "") }
    }

    fun onCustomDailyBudgetChanged(text: String) {
        _uiState.update {
            it.copy(
                customDailyBudgetText = text,
                dailyTargetPreset = if (text.isBlank()) DailyTargetPresets[1] else null
            )
        }
    }

    fun onNextAllowanceDateChanged(date: LocalDate) {
        _uiState.update { it.copy(nextAllowanceDate = date) }
    }

    /**
     * Simpan hasil onboarding. Jika [skipAllowanceDate] true, tanggal kiriman diisi perkiraan
     * [DEFAULT_ALLOWANCE_DAYS] hari dan bisa diubah kapan saja di Pengaturan.
     */
    fun completeOnboarding(skipAllowanceDate: Boolean = false) {
        val state = _uiState.value
        if (state.userName.isBlank() || state.isSaving) return
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val target = if (state.effectiveDailyTarget > 0.0) state.effectiveDailyTarget else 30_000.0
            val allowanceDate = if (skipAllowanceDate || state.nextAllowanceDate == null) {
                LocalDate.now().plusDays(DEFAULT_ALLOWANCE_DAYS)
            } else {
                state.nextAllowanceDate
            }
            userPreferencesRepository.completeOnboarding(
                userName = state.userName.trim(),
                initialBalance = state.initialBalance,
                nextAllowanceDate = allowanceDate,
                monthlyAllowanceBudget = state.initialBalance,
                dailyTargetBudget = target
            )
            _uiState.update { it.copy(isSaving = false, isCompleted = true) }
        }
    }
}
