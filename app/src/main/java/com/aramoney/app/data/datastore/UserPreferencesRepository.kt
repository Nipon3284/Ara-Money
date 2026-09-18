package com.aramoney.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aramoney.app.domain.model.ThemeMode
import com.aramoney.app.domain.model.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ara_user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val IS_ONBOARDED = booleanPreferencesKey("is_onboarded")
        val NEXT_ALLOWANCE_DATE_EPOCH_DAY = longPreferencesKey("next_allowance_date_epoch_day")
        val MONTHLY_ALLOWANCE_BUDGET = doublePreferencesKey("monthly_allowance_budget")
        val SAVINGS_TARGET_AMOUNT = doublePreferencesKey("savings_target_amount")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val INITIAL_BALANCE = doublePreferencesKey("initial_balance")
        val PROFILE_PHOTO_PATH = stringPreferencesKey("profile_photo_path")
        val AVATAR_PRESET_ID = stringPreferencesKey("avatar_preset_id")
    }

    val userPreferences: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val epochDay = preferences[PreferencesKeys.NEXT_ALLOWANCE_DATE_EPOCH_DAY]
            val nextDate = epochDay?.let { LocalDate.ofEpochDay(it) }

            val themeModeString = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            val themeMode = runCatching { ThemeMode.valueOf(themeModeString) }.getOrDefault(ThemeMode.SYSTEM)

            UserPreferences(
                userName = preferences[PreferencesKeys.USER_NAME] ?: "Kakak Mahasiswi",
                isOnboarded = preferences[PreferencesKeys.IS_ONBOARDED] ?: false,
                nextAllowanceDate = nextDate,
                monthlyAllowanceBudget = preferences[PreferencesKeys.MONTHLY_ALLOWANCE_BUDGET] ?: 0.0,
                savingsTargetAmount = preferences[PreferencesKeys.SAVINGS_TARGET_AMOUNT] ?: 0.0,
                themeMode = themeMode,
                initialBalance = preferences[PreferencesKeys.INITIAL_BALANCE] ?: 0.0,
                profilePhotoPath = preferences[PreferencesKeys.PROFILE_PHOTO_PATH],
                avatarPresetId = preferences[PreferencesKeys.AVATAR_PRESET_ID] ?: "sakura_girl"
            )
        }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
        }
    }

    suspend fun setOnboarded(isOnboarded: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ONBOARDED] = isOnboarded
        }
    }

    suspend fun setNextAllowanceDate(date: LocalDate) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NEXT_ALLOWANCE_DATE_EPOCH_DAY] = date.toEpochDay()
        }
    }

    suspend fun setMonthlyAllowanceBudget(amount: Double) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MONTHLY_ALLOWANCE_BUDGET] = amount
        }
    }

    suspend fun setSavingsTarget(amount: Double) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SAVINGS_TARGET_AMOUNT] = amount
        }
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.name
        }
    }

    suspend fun completeOnboarding(
        userName: String,
        initialBalance: Double,
        nextAllowanceDate: LocalDate,
        monthlyAllowanceBudget: Double
    ) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = userName
            preferences[PreferencesKeys.INITIAL_BALANCE] = initialBalance
            preferences[PreferencesKeys.NEXT_ALLOWANCE_DATE_EPOCH_DAY] = nextAllowanceDate.toEpochDay()
            preferences[PreferencesKeys.MONTHLY_ALLOWANCE_BUDGET] = monthlyAllowanceBudget
            preferences[PreferencesKeys.IS_ONBOARDED] = true
        }
    }

    suspend fun updateProfile(name: String, avatarPresetId: String, photoPath: String?) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
            preferences[PreferencesKeys.AVATAR_PRESET_ID] = avatarPresetId
            if (photoPath != null) {
                preferences[PreferencesKeys.PROFILE_PHOTO_PATH] = photoPath
            } else {
                preferences.remove(PreferencesKeys.PROFILE_PHOTO_PATH)
            }
        }
    }

    suspend fun clearProfilePhoto() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.PROFILE_PHOTO_PATH)
        }
    }

    suspend fun saveProfileImageFromUri(uri: android.net.Uri): String? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        runCatching {
            val destFile = java.io.File(context.filesDir, "profile_photo.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                java.io.FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        }.getOrNull()
    }

    suspend fun clearPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
