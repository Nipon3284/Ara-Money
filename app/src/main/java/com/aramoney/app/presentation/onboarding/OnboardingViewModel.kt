package com.aramoney.app.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aramoney.app.data.datastore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class OnboardingUiState(
    val currentPage: Int = 0,
    val userName: String = "",
    val initialBalanceText: String = "",
    val nextAllowanceDate: LocalDate = LocalDate.now().plusDays(25),
    val isCompleted: Boolean = false,
    val errorMessage: String? = null
) {
    val initialBalance: Double
        get() = initialBalanceText.toDoubleOrNull() ?: 0.0

    val isStep1Valid: Boolean
        get() = userName.isNotBlank()

    val isStep2Valid: Boolean
        get() = initialBalance >= 0.0
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onUserNameChanged(name: String) {
        _uiState.update { it.copy(userName = name, errorMessage = null) }
    }

    fun onInitialBalanceChanged(balanceText: String) {
        val clean = balanceText.filter { it.isDigit() }
        _uiState.update { it.copy(initialBalanceText = clean, errorMessage = null) }
    }

    fun onNextAllowanceDateChanged(date: LocalDate) {
        _uiState.update { it.copy(nextAllowanceDate = date) }
    }

    fun completeOnboarding() {
        val state = _uiState.value
        if (state.userName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nama panggilan tidak boleh kosong ya, Kak! 🌸") }
            return
        }

        viewModelScope.launch {
            userPreferencesRepository.completeOnboarding(
                userName = state.userName.trim(),
                initialBalance = state.initialBalance,
                nextAllowanceDate = state.nextAllowanceDate,
                monthlyAllowanceBudget = state.initialBalance // Set default nominal bulanan sama dengan saldo awal
            )
            _uiState.update { it.copy(isCompleted = true) }
        }
    }
}
