package com.aramoney.app.presentation.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aramoney.app.data.backup.BackupPreview
import com.aramoney.app.data.backup.JsonBackupManager
import com.aramoney.app.data.datastore.UserPreferencesRepository
import com.aramoney.app.data.local.entity.CategoryEntity
import com.aramoney.app.domain.model.ThemeMode
import com.aramoney.app.domain.model.UserPreferences
import com.aramoney.app.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class SettingsUiState(
    val userPreferences: UserPreferences = UserPreferences(),
    val categories: List<CategoryEntity> = emptyList(),
    val isProfileDialogOpen: Boolean = false,
    val isDailyTargetDialogOpen: Boolean = false,
    val isAllowanceDateDialogOpen: Boolean = false,
    val isBackupInProgress: Boolean = false,
    val backupPreview: BackupPreview? = null,
    val pendingRestoreUri: Uri? = null,
    val feedbackMessage: String? = null
)

private data class DialogStateHolder(
    val isProfileDialogOpen: Boolean = false,
    val isDailyTargetDialogOpen: Boolean = false,
    val isAllowanceDateDialogOpen: Boolean = false,
    val isBackupInProgress: Boolean = false,
    val backupPreview: BackupPreview? = null,
    val pendingRestoreUri: Uri? = null,
    val feedbackMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val categoryRepository: CategoryRepository,
    private val jsonBackupManager: JsonBackupManager
) : ViewModel() {

    private val _dialogState = MutableStateFlow(DialogStateHolder())

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferencesRepository.userPreferences,
        categoryRepository.getAllCategories(),
        _dialogState
    ) { prefs, categories, dialogs ->
        SettingsUiState(
            userPreferences = prefs,
            categories = categories,
            isProfileDialogOpen = dialogs.isProfileDialogOpen,
            isDailyTargetDialogOpen = dialogs.isDailyTargetDialogOpen,
            isAllowanceDateDialogOpen = dialogs.isAllowanceDateDialogOpen,
            isBackupInProgress = dialogs.isBackupInProgress,
            backupPreview = dialogs.backupPreview,
            pendingRestoreUri = dialogs.pendingRestoreUri,
            feedbackMessage = dialogs.feedbackMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun openProfileDialog() {
        _dialogState.update { it.copy(isProfileDialogOpen = true) }
    }

    fun closeProfileDialog() {
        _dialogState.update { it.copy(isProfileDialogOpen = false) }
    }

    fun updateProfile(name: String, avatarPresetId: String, photoUri: Uri?, clearPhoto: Boolean) {
        viewModelScope.launch {
            val photoPath = when {
                clearPhoto -> null
                photoUri != null -> userPreferencesRepository.saveProfileImageFromUri(photoUri)
                else -> uiState.value.userPreferences.profilePhotoPath
            }
            userPreferencesRepository.updateProfile(name, avatarPresetId, photoPath)
            _dialogState.update {
                it.copy(
                    isProfileDialogOpen = false,
                    feedbackMessage = "Profil berhasil diperbarui"
                )
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(mode)
        }
    }

    fun openDailyTargetDialog() {
        _dialogState.update { it.copy(isDailyTargetDialogOpen = true) }
    }

    fun closeDailyTargetDialog() {
        _dialogState.update { it.copy(isDailyTargetDialogOpen = false) }
    }

    fun updateDailyTargetBudget(target: Double) {
        viewModelScope.launch {
            userPreferencesRepository.setDailyTargetBudget(target)
            _dialogState.update {
                it.copy(
                    isDailyTargetDialogOpen = false,
                    feedbackMessage = "Target jajan harian diperbarui"
                )
            }
        }
    }

    fun openAllowanceDateDialog() {
        _dialogState.update { it.copy(isAllowanceDateDialogOpen = true) }
    }

    fun closeAllowanceDateDialog() {
        _dialogState.update { it.copy(isAllowanceDateDialogOpen = false) }
    }

    fun updateNextAllowanceDate(date: LocalDate) {
        viewModelScope.launch {
            userPreferencesRepository.setNextAllowanceDate(date)
            _dialogState.update {
                it.copy(isAllowanceDateDialogOpen = false, feedbackMessage = "Tanggal kiriman berikutnya disimpan")
            }
        }
    }

    fun exportBackup(uri: Uri) {
        _dialogState.update { it.copy(isBackupInProgress = true) }
        viewModelScope.launch {
            val result = jsonBackupManager.exportDataToJsonUri(uri)
            _dialogState.update { it.copy(isBackupInProgress = false) }
            result.fold(
                onSuccess = {
                    _dialogState.update {
                        it.copy(feedbackMessage = "Cadangan data berhasil disimpan")
                    }
                },
                onFailure = { err ->
                    _dialogState.update {
                        it.copy(feedbackMessage = "Gagal mencadangkan data. Pastikan lokasi penyimpanan bisa ditulis, lalu coba lagi.")
                    }
                }
            )
        }
    }

    fun onImportFileSelected(uri: Uri) {
        _dialogState.update { it.copy(isBackupInProgress = true) }
        viewModelScope.launch {
            val result = jsonBackupManager.inspectBackupFile(uri)
            _dialogState.update { it.copy(isBackupInProgress = false) }
            result.fold(
                onSuccess = { preview ->
                    _dialogState.update {
                        it.copy(
                            pendingRestoreUri = uri,
                            backupPreview = preview
                        )
                    }
                },
                onFailure = { err ->
                    _dialogState.update {
                        it.copy(feedbackMessage = "Berkas ini bukan cadangan Ara Money yang valid. Pilih berkas .json hasil \"Cadangkan\".")
                    }
                }
            )
        }
    }

    fun confirmRestore() {
        val uri = _dialogState.value.pendingRestoreUri ?: return
        _dialogState.update { it.copy(isBackupInProgress = true) }
        viewModelScope.launch {
            val result = jsonBackupManager.restoreDataFromJsonUri(uri)
            _dialogState.update { it.copy(isBackupInProgress = false) }
            result.fold(
                onSuccess = {
                    _dialogState.update {
                        it.copy(
                            feedbackMessage = "Data berhasil dipulihkan",
                            pendingRestoreUri = null,
                            backupPreview = null
                        )
                    }
                },
                onFailure = { err ->
                    _dialogState.update {
                        it.copy(feedbackMessage = "Gagal memulihkan data. Berkas mungkin rusak, coba berkas cadangan lain.", pendingRestoreUri = null, backupPreview = null)
                    }
                }
            )
        }
    }

    fun dismissRestoreDialog() {
        _dialogState.update {
            it.copy(
                pendingRestoreUri = null,
                backupPreview = null
            )
        }
    }

    fun dismissFeedback() {
        _dialogState.update { it.copy(feedbackMessage = null) }
    }
}
