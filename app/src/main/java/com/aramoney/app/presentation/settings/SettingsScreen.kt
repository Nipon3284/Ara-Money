package com.aramoney.app.presentation.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.SettingsSuggest
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aramoney.app.R
import com.aramoney.app.domain.model.ThemeMode
import com.aramoney.app.presentation.components.AraConfirmDialog
import com.aramoney.app.presentation.components.AraPrimaryButton
import com.aramoney.app.presentation.components.AraScreenHeader
import com.aramoney.app.presentation.components.DailyTargetPicker
import com.aramoney.app.presentation.components.DailyTargetPresets
import com.aramoney.app.presentation.components.HansaraWatermark
import com.aramoney.app.presentation.components.LocalAraSnackbar
import com.aramoney.app.presentation.components.AllowanceDateDialog
import com.aramoney.app.presentation.profile.EditProfileDialog
import com.aramoney.app.presentation.profile.UserAvatar
import com.aramoney.app.presentation.theme.AraShape
import com.aramoney.app.presentation.theme.AraTheme
import com.aramoney.app.presentation.theme.PrimarySakuraPink
import com.aramoney.app.presentation.theme.isAppInDarkTheme
import com.aramoney.app.presentation.theme.softShadow
import com.aramoney.app.util.CurrencyFormatter
import com.aramoney.app.util.DateTimeUtil

@Composable
fun SettingsScreen(
    onNavigateToManageCategories: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = isAppInDarkTheme()
    val snackbar = LocalAraSnackbar.current

    // SAF Launchers untuk Export dan Import JSON tanpa izin storage legacy
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) viewModel.exportBackup(uri)
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) viewModel.onImportFileSelected(uri)
    }

    // Feedback memakai Snackbar global agar konsisten dengan layar lain
    LaunchedEffect(uiState.feedbackMessage) {
        uiState.feedbackMessage?.let {
            snackbar?.show(it)
            viewModel.dismissFeedback()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            AraScreenHeader(overline = "Personalisasi & cadangan", title = "Pengaturan")
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Profil Pengguna
            item {
                ProfileCard(
                    userName = uiState.userPreferences.userName,
                    avatarPresetId = uiState.userPreferences.avatarPresetId,
                    profilePhotoPath = uiState.userPreferences.profilePhotoPath,
                    onEditClick = { viewModel.openProfileDialog() }
                )
            }

            // 2. Keuangan
            item {
                SettingsSectionCard(title = "Keuangan") {
                    SettingsActionRow(
                        icon = Icons.Rounded.Savings,
                        title = "Target jajan harian",
                        subtitle = "${CurrencyFormatter.formatRupiah(uiState.userPreferences.dailyTargetBudget)} / hari",
                        onClick = { viewModel.openDailyTargetDialog() }
                    )
                    SettingsActionRow(
                        icon = Icons.Rounded.CalendarMonth,
                        title = "Tanggal kiriman berikutnya",
                        subtitle = uiState.userPreferences.nextAllowanceDate?.let { DateTimeUtil.formatLocalDate(it) }
                            ?: "Belum diatur",
                        onClick = { viewModel.openAllowanceDateDialog() }
                    )
                    SettingsActionRow(
                        icon = Icons.Rounded.Category,
                        title = "Kelola kategori",
                        subtitle = "${uiState.categories.size} kategori aktif",
                        onClick = onNavigateToManageCategories,
                        showChevron = true
                    )
                }
            }

            // 3. Tema
            item {
                SettingsSectionCard(title = "Tema tampilan") {
                    ThemeSelectionRow(
                        currentMode = uiState.userPreferences.themeMode,
                        onModeSelect = { viewModel.setThemeMode(it) }
                    )
                }
            }

            // 4. Cadangkan & Pulihkan
            item {
                SettingsSectionCard(title = "Cadangan data (100% offline)") {
                    Text(
                        text = "Data tersimpan hanya di perangkat ini. Cadangkan secara berkala ke berkas JSON agar tidak hilang saat ganti HP.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (uiState.isBackupInProgress) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(3.dp)),
                            color = AraTheme.colors.accent
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AraPrimaryButton(
                            text = "Cadangkan",
                            leadingIcon = Icons.Rounded.CloudUpload,
                            onClick = { exportLauncher.launch("ara_money_backup_${System.currentTimeMillis()}.json") },
                            enabled = !uiState.isBackupInProgress,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/json")) },
                            enabled = !uiState.isBackupInProgress,
                            shape = AraShape.button,
                            border = BorderStroke(1.dp, AraTheme.colors.border),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 52.dp)
                        ) {
                            Icon(Icons.Rounded.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp), tint = AraTheme.colors.textStrong)
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Pulihkan", color = AraTheme.colors.textStrong, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // 5. Tentang Aplikasi
            item { AboutAppCard() }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    // Dialog Edit Profil
    if (uiState.isProfileDialogOpen) {
        EditProfileDialog(
            initialName = uiState.userPreferences.userName,
            initialAvatarPresetId = uiState.userPreferences.avatarPresetId,
            initialProfilePhotoPath = uiState.userPreferences.profilePhotoPath,
            onDismiss = { viewModel.closeProfileDialog() },
            onSave = { name, presetId, photoUri, clearPhoto ->
                viewModel.updateProfile(name, presetId, photoUri, clearPhoto)
            },
            isDark = isDark
        )
    }

    // Dialog Target Jajan Harian
    if (uiState.isDailyTargetDialogOpen) {
        EditDailyTargetBudgetDialog(
            currentTarget = uiState.userPreferences.dailyTargetBudget,
            onDismiss = { viewModel.closeDailyTargetDialog() },
            onSave = { target -> viewModel.updateDailyTargetBudget(target) }
        )
    }

    // Dialog Tanggal Kiriman Berikutnya
    if (uiState.isAllowanceDateDialogOpen) {
        AllowanceDateDialog(
            initialDate = uiState.userPreferences.nextAllowanceDate,
            onDismiss = { viewModel.closeAllowanceDateDialog() },
            onConfirm = { viewModel.updateNextAllowanceDate(it) }
        )
    }

    // Dialog Konfirmasi Pulihkan Data Cadangan
    uiState.backupPreview?.let { preview ->
        AraConfirmDialog(
            title = "Pulihkan data cadangan?",
            message = "Berkas cadangan Ara Money ditemukan:\n" +
                "• ${preview.transactionCount} transaksi\n" +
                "• ${preview.splitBillDebtCount} catatan utang & piutang\n" +
                "• Dicadangkan ${DateTimeUtil.formatTransactionDate(preview.exportedAt)}\n\n" +
                "Data akan digabungkan dengan data yang ada tanpa menghapus riwayat sebelumnya.",
            confirmText = "Pulihkan",
            confirmEnabled = !uiState.isBackupInProgress,
            onConfirm = { viewModel.confirmRestore() },
            onDismiss = { viewModel.dismissRestoreDialog() }
        )
    }
}

@Composable
private fun ProfileCard(
    userName: String,
    avatarPresetId: String,
    profilePhotoPath: String?,
    onEditClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(elevation = 3.dp, shape = AraShape.card),
        shape = AraShape.card,
        color = AraTheme.colors.surfaceElevated
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            UserAvatar(
                userName = userName,
                avatarPresetId = avatarPresetId,
                profilePhotoPath = profilePhotoPath,
                size = 64.dp
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AraTheme.colors.textStrong
                )
                Text(
                    text = "Pengguna lokal • 100% offline",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(
                onClick = onEditClick,
                shape = AraShape.chip,
                border = BorderStroke(1.dp, AraTheme.colors.border),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Icon(Icons.Rounded.Edit, contentDescription = null, tint = AraTheme.colors.accent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.size(4.dp))
                Text("Ubah profil", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = AraTheme.colors.accent)
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(elevation = 3.dp, shape = AraShape.card),
        shape = AraShape.card,
        color = AraTheme.colors.surfaceElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AraTheme.colors.textStrong,
                modifier = Modifier.semantics { heading() }
            )
            content()
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showChevron: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(AraShape.chip)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PrimarySakuraPink.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = AraTheme.colors.berry, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AraTheme.colors.textStrong
            )
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = if (showChevron) Icons.AutoMirrored.Rounded.KeyboardArrowRight else Icons.Rounded.Edit,
            contentDescription = null,
            tint = AraTheme.colors.textMuted,
            modifier = Modifier.size(if (showChevron) 24.dp else 18.dp)
        )
    }
}

@Composable
private fun ThemeSelectionRow(
    currentMode: ThemeMode,
    onModeSelect: (ThemeMode) -> Unit
) {
    val modes = listOf(
        Triple(ThemeMode.SYSTEM, "Ikuti sistem", Icons.Rounded.SettingsSuggest),
        Triple(ThemeMode.LIGHT, "Terang", Icons.Rounded.LightMode),
        Triple(ThemeMode.SAKURA_NIGHT, "Gelap", Icons.Rounded.DarkMode)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        modes.forEach { (mode, label, icon) ->
            val isSelected = currentMode == mode
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(AraShape.chip)
                    .selectable(selected = isSelected, role = Role.RadioButton) { onModeSelect(mode) },
                shape = AraShape.chip,
                color = if (isSelected) AraTheme.colors.selectedContainer else AraTheme.colors.surfaceCard,
                border = if (isSelected) BorderStroke(1.5.dp, PrimarySakuraPink) else null
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) AraTheme.colors.onSelectedContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) AraTheme.colors.onSelectedContainer else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutAppCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(elevation = 2.dp, shape = AraShape.card),
        shape = AraShape.card,
        color = AraTheme.colors.surfaceElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
            Text(
                text = "Ara Money",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AraTheme.colors.textStrong
            )
            Text(
                text = "Versi 1.0.0 • 100% offline & menjaga privasi",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Dibuat oleh hansara untuk seluruh mahasiswi Indonesia",
                style = MaterialTheme.typography.bodySmall,
                color = AraTheme.colors.accent,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            HansaraWatermark(modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun EditDailyTargetBudgetDialog(
    currentTarget: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    val current = currentTarget.toLong()
    var selectedPreset by remember { mutableStateOf(current.takeIf { it in DailyTargetPresets }) }
    var rawCustom by remember { mutableStateOf(if (current !in DailyTargetPresets && current > 0) current.toString() else "") }
    val customAmount = rawCustom.toLongOrNull() ?: 0L
    val finalAmount = if (rawCustom.isNotBlank()) customAmount else selectedPreset ?: 0L

    AraConfirmDialog(
        title = "Target jajan harian",
        message = "Batas jajan nyaman per hari. Dipakai untuk menghitung jatah hari ini dan berapa hari saldo Kakak bertahan.",
        confirmText = "Simpan",
        confirmEnabled = finalAmount > 0,
        onConfirm = { onSave(finalAmount.toDouble()) },
        onDismiss = onDismiss,
        extraContent = {
            DailyTargetPicker(
                selectedPreset = selectedPreset,
                rawCustom = rawCustom,
                onPresetSelected = {
                    selectedPreset = it
                    rawCustom = ""
                },
                onCustomChanged = {
                    rawCustom = it
                    selectedPreset = null
                }
            )
        }
    )
}
