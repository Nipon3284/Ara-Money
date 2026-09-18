package com.aramoney.app.presentation.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aramoney.app.R
import com.aramoney.app.domain.model.ThemeMode
import com.aramoney.app.presentation.components.HansaraWatermark
import com.aramoney.app.presentation.profile.EditProfileDialog
import com.aramoney.app.presentation.profile.UserAvatar
import com.aramoney.app.presentation.theme.DeepBerry
import com.aramoney.app.presentation.theme.DeepBerryDark
import com.aramoney.app.presentation.theme.PrimarySakuraPink
import com.aramoney.app.presentation.theme.PrimarySakuraPinkContainer
import com.aramoney.app.presentation.theme.SurfaceCard
import com.aramoney.app.presentation.theme.SurfaceCardDark
import com.aramoney.app.presentation.theme.SurfaceElevated
import com.aramoney.app.presentation.theme.SurfaceElevatedDark
import com.aramoney.app.presentation.theme.TextPrimaryDark
import com.aramoney.app.presentation.theme.isAppInDarkTheme
import com.aramoney.app.presentation.theme.softShadow
import com.aramoney.app.util.CurrencyFormatter
import com.aramoney.app.util.DateTimeUtil
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun SettingsScreen(
    onNavigateToManageCategories: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = isAppInDarkTheme()
    val snackbarHostState = remember { SnackbarHostState() }

    // SAF Launchers untuk Export dan Import JSON tanpa izin storage legacy
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.exportBackup(uri)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onImportFileSelected(uri)
        }
    }

    LaunchedEffect(uiState.feedbackMessage) {
        uiState.feedbackMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.dismissFeedback()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Sticky / Fixed Header Navbar
            Surface(
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Personalisasi & Cadangan ⚙️",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else DeepBerry
                    )
                    Text(
                        text = "Pengaturan",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) TextPrimaryDark else DeepBerryDark
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 2. Profil Pengguna (Single Local User - Nama & Foto Avatar)
            item {
                ProfileCard(
                    userName = uiState.userPreferences.userName,
                    avatarPresetId = uiState.userPreferences.avatarPresetId,
                    profilePhotoPath = uiState.userPreferences.profilePhotoPath,
                    onEditClick = { viewModel.openProfileDialog() },
                    isDark = isDark
                )
            }

            // 3. Menu Navigasi: Kelola Kategori Transaksi
            item {
                SettingsSectionCard(title = "Kategori Transaksi", isDark = isDark) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onNavigateToManageCategories)
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(PrimarySakuraPink.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Category,
                                contentDescription = null,
                                tint = DeepBerry,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kelola Kategori Transaksi",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) TextPrimaryDark else DeepBerryDark
                            )
                            Text(
                                text = "${uiState.categories.size} kategori aktif • Tambah, ubah, hapus",
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimarySakuraPink
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "Buka Kelola Kategori",
                            tint = if (isDark) Color.LightGray else DeepBerry.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // 4. Pengaturan Tanggal Kiriman & Uang Saku
            item {
                SettingsSectionCard(title = "Keuangan & Kiriman Uang", isDark = isDark) {
                    val allowanceDateText = uiState.userPreferences.nextAllowanceDate?.let {
                        DateTimeUtil.formatLocalDate(it)
                    } ?: "Belum diatur"

                    SettingsActionRow(
                        icon = Icons.Rounded.CalendarToday,
                        title = "Tanggal Kiriman Berikutnya",
                        subtitle = allowanceDateText,
                        onClick = { viewModel.openAllowanceDialog() },
                        isDark = isDark
                    )

                    SettingsActionRow(
                        icon = Icons.Rounded.Payments,
                        title = "Nominal Saku Bulanan",
                        subtitle = CurrencyFormatter.formatRupiah(uiState.userPreferences.monthlyAllowanceBudget),
                        onClick = { viewModel.openAllowanceDialog() },
                        isDark = isDark
                    )
                }
            }

            // 5. Pengaturan Tema ("Sakura Night" vs "Sakura Day")
            item {
                SettingsSectionCard(title = "Tema Tampilan", isDark = isDark) {
                    ThemeSelectionRow(
                        currentMode = uiState.userPreferences.themeMode,
                        onModeSelect = { viewModel.setThemeMode(it) },
                        isDark = isDark
                    )
                }
            }

            // 6. Cadangkan & Pulihkan (Backup & Restore SAF)
            item {
                SettingsSectionCard(title = "Privasi & Cadangan Data (100% Offline)", isDark = isDark) {
                    Text(
                        text = "Data tersimpan sepenuhnya di perangkatmu. Cadangkan secara berkala ke berkas JSON agar tidak hilang saat ganti handphone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { exportLauncher.launch("ara_money_backup_${System.currentTimeMillis()}.json") },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimarySakuraPink),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cadangkan", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/json")) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pulihkan", color = if (isDark) TextPrimaryDark else DeepBerryDark)
                        }
                    }
                }
            }

            // 7. Tentang Aplikasi & Watermark
            item {
                AboutAppCard(isDark = isDark)
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Dialog Edit Profil (Nama & Foto)
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

    // Dialog Pengaturan Tanggal Kiriman & Uang Saku
    if (uiState.isAllowanceDialogOpen) {
        EditAllowanceDialog(
            initialDate = uiState.userPreferences.nextAllowanceDate ?: LocalDate.now().plusDays(20),
            initialBudget = uiState.userPreferences.monthlyAllowanceBudget,
            onDismiss = { viewModel.closeAllowanceDialog() },
            onSave = { date, budget -> viewModel.updateAllowanceSettings(date, budget) },
            isDark = isDark
        )
    }

    // Dialog Konfirmasi Pulihkan Data Cadangan
    if (uiState.backupPreview != null) {
        val preview = uiState.backupPreview!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissRestoreDialog() },
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Pulihkan Data Cadangan? 📥",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) TextPrimaryDark else DeepBerryDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Ditemukan berkas cadangan resmi Ara Money dengan rincian:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text("• ${preview.transactionCount} catatan transaksi keuangan", style = MaterialTheme.typography.bodySmall)
                    Text("• ${preview.splitBillDebtCount} catatan hutang & piutang", style = MaterialTheme.typography.bodySmall)
                    Text("• Waktu pencadangan: ${DateTimeUtil.formatTransactionDate(preview.exportedAt)}", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Data yang ada akan digabungkan secara aman tanpa menghapus histori sebelumnya.",
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimarySakuraPink,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmRestore() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimarySakuraPink),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Lanjutkan Pulihkan 🌸", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.dismissRestoreDialog() },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}

@Composable
private fun ProfileCard(
    userName: String,
    avatarPresetId: String,
    profilePhotoPath: String?,
    onEditClick: () -> Unit,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(elevation = 3.dp, shape = RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = if (isDark) SurfaceElevatedDark else SurfaceElevated
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                UserAvatar(
                    userName = userName,
                    avatarPresetId = avatarPresetId,
                    profilePhotoPath = profilePhotoPath,
                    size = 64.dp
                )

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) TextPrimaryDark else DeepBerryDark
                    )
                    Text(
                        text = "Pengguna Lokal • 100% Offline 🌸",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedButton(
                onClick = onEditClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null,
                    tint = PrimarySakuraPink,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Ubah",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimarySakuraPink
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    isDark: Boolean,
    action: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(elevation = 3.dp, shape = RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = if (isDark) SurfaceElevatedDark else SurfaceElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) TextPrimaryDark else DeepBerryDark
                )
                action?.invoke()
            }
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
    isDark: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(PrimarySakuraPink.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = DeepBerry, modifier = Modifier.size(20.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = PrimarySakuraPink)
        }
    }
}

@Composable
private fun ThemeSelectionRow(
    currentMode: ThemeMode,
    onModeSelect: (ThemeMode) -> Unit,
    isDark: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val modes = listOf(
            Triple(ThemeMode.SYSTEM, "Sistem", "Auto"),
            Triple(ThemeMode.LIGHT, "Sakura Day", "🌸"),
            Triple(ThemeMode.SAKURA_NIGHT, "Sakura Night", "🌙")
        )

        modes.forEach { (mode, label, emoji) ->
            val isSelected = currentMode == mode
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onModeSelect(mode) },
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) PrimarySakuraPinkContainer else if (isDark) SurfaceCardDark else SurfaceCard,
                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, PrimarySakuraPink) else null
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(text = emoji, fontSize = 20.sp)
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) DeepBerry else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutAppCard(isDark: Boolean) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(elevation = 2.dp, shape = RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = if (isDark) SurfaceElevatedDark else SurfaceElevated
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
                contentDescription = "Ara Money Mascot",
                modifier = Modifier
                    .size(76.dp)
                    .softShadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
            )
            Text(
                text = "Ara Money",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) TextPrimaryDark else DeepBerryDark
            )
            Text(
                text = "Versi 1.0.0 • 100% Offline & Menjaga Privasimu",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Dibuat dengan 💜 oleh hansara untuk seluruh mahasiswi Indonesia",
                style = MaterialTheme.typography.bodySmall,
                color = PrimarySakuraPink,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            HansaraWatermark(
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditAllowanceDialog(
    initialDate: LocalDate,
    initialBudget: Double,
    onDismiss: () -> Unit,
    onSave: (LocalDate, Double) -> Unit,
    isDark: Boolean
) {
    var budgetText by remember {
        mutableStateOf(if (initialBudget > 0) initialBudget.toLong().toString() else "")
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = "Atur Tanggal Kiriman & Saku 📅",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDark) TextPrimaryDark else DeepBerryDark
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { input -> if (input.all { it.isDigit() }) budgetText = input },
                    label = { Text("Nominal Uang Saku Bulanan (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimarySakuraPink),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Pilih tanggal kiriman berikutnya:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )

                DatePicker(
                    state = datePickerState,
                    showModeToggle = false,
                    colors = DatePickerDefaults.colors(
                        selectedDayContainerColor = PrimarySakuraPink,
                        selectedDayContentColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val budget = budgetText.toDoubleOrNull() ?: 0.0
                    val millis = datePickerState.selectedDateMillis
                    val date = if (millis != null) {
                        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    } else initialDate

                    onSave(date, budget)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimarySakuraPink),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(14.dp)) {
                Text("Batal", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}
