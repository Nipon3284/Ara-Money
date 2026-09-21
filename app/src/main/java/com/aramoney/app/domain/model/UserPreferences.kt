package com.aramoney.app.domain.model

import java.time.LocalDate

/**
 * Model preferensi pengguna yang disimpan secara lokal di DataStore.
 */
data class UserPreferences(
    val userName: String = "Kakak Mahasiswi",
    val isOnboarded: Boolean = false,
    val nextAllowanceDate: LocalDate? = null,
    val monthlyAllowanceBudget: Double = 0.0,
    val savingsTargetAmount: Double = 0.0,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val initialBalance: Double = 0.0,
    val profilePhotoPath: String? = null,
    val avatarPresetId: String = "sakura_girl",
    val dailyTargetBudget: Double = 30_000.0
)
