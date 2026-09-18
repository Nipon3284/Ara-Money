package com.aramoney.app.presentation.splitbill

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.aramoney.app.presentation.theme.isAppInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aramoney.app.data.local.entity.SplitBillDebtEntity
import com.aramoney.app.domain.model.PersonDebtGroup
import com.aramoney.app.presentation.components.FloralDecoration
import com.aramoney.app.presentation.components.HansaraWatermark
import com.aramoney.app.presentation.theme.DeepBerry
import com.aramoney.app.presentation.theme.DeepBerryDark
import com.aramoney.app.presentation.theme.ErrorSoftRed
import com.aramoney.app.presentation.theme.PrimarySakuraPink
import com.aramoney.app.presentation.theme.PrimarySakuraPinkContainer
import com.aramoney.app.presentation.theme.SecondaryLavender
import com.aramoney.app.presentation.theme.SuccessMintGreen
import com.aramoney.app.presentation.theme.SurfaceCard
import com.aramoney.app.presentation.theme.SurfaceCardDark
import com.aramoney.app.presentation.theme.SurfaceElevated
import com.aramoney.app.presentation.theme.SurfaceElevatedDark
import com.aramoney.app.presentation.theme.TextPrimaryDark
import com.aramoney.app.presentation.theme.softShadow
import com.aramoney.app.util.CurrencyFormatter
import com.aramoney.app.util.DateTimeUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitBillScreen(
    modifier: Modifier = Modifier,
    viewModel: SplitBillViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = isAppInDarkTheme()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
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
                        text = "Buku Catatan 📒",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else DeepBerry
                    )
                    Text(
                        text = "Hutang & Piutang",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) TextPrimaryDark else DeepBerryDark
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                containerColor = PrimarySakuraPink,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 8.dp)
                    .softShadow(elevation = 8.dp, shape = CircleShape)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Tambah catatan hutang atau piutang baru"
                    }
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 2. Summary Card Belum Lunas
            item {
                DebtSummaryHeroCard(
                    direction = uiState.selectedTab,
                    unsettledAmount = uiState.totalUnsettledAmount,
                    activeDebtorsCount = uiState.activeDebtorsCount,
                    isDark = isDark
                )
            }

            // 3. Tab Selector ("Piutang (Mereka Berutang)" vs "Utang (Saya Berutang)")
            item {
                DebtDirectionTabs(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = { viewModel.onTabSelected(it) },
                    isDark = isDark
                )
            }

            // 4. Header Daftar Per Orang
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.selectedTab == "I_PAID_FOR_FRIEND") "Daftar Yang Berutang" else "Daftar Hutang Saya",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) TextPrimaryDark else DeepBerryDark
                    )

                    if (uiState.personGroups.isNotEmpty()) {
                        Text(
                            text = "${uiState.personGroups.size} orang",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else DeepBerry
                        )
                    }
                }
            }

            // 5. Daftar Orang (Grouped by Person) / Empty State
            if (uiState.personGroups.isEmpty()) {
                item {
                    EmptyDebtState(
                        isFriendPaid = uiState.selectedTab == "FRIEND_PAID_FOR_ME",
                        isDark = isDark
                    )
                }
            } else {
                items(
                    items = uiState.personGroups,
                    key = { it.friendName }
                ) { personGroup ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut()
                    ) {
                        PersonDebtGroupCard(
                            personGroup = personGroup,
                            onClick = { viewModel.selectPerson(personGroup) },
                            isDark = isDark
                        )
                    }
                }
            }

            // Watermark hansara di paling bawah
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    HansaraWatermark()
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Modal Bottom Sheet: Detail Catatan Orang yang Dipilih
    if (uiState.selectedPersonGroup != null) {
        val person = uiState.selectedPersonGroup!!
        PersonDetailSheet(
            personGroup = person,
            onDismissRequest = { viewModel.clearSelectedPerson() },
            onAddRecordClick = {
                viewModel.openAddDialog(prefilledFriendName = person.friendName)
            },
            onSettleClick = { debt ->
                viewModel.requestSettleDebt(debt)
            },
            onEditClick = { debt ->
                viewModel.openEditDialog(debt)
            },
            onDeleteClick = { debt ->
                viewModel.requestDeleteDebt(debt)
            },
            isDark = isDark
        )
    }

    // Modal Dialog Tambah Hutang/Piutang
    if (uiState.isAddDialogOpen) {
        AddDebtDialog(
            defaultDirection = uiState.selectedTab,
            initialFriendName = uiState.initialFriendNameForAdd,
            onDismiss = { viewModel.closeAddDialog() },
            onConfirm = { name, amount, note, direction ->
                viewModel.addDebt(name, amount, note, direction)
            },
            isDark = isDark
        )
    }

    // Modal Dialog Ubah / Edit Hutang (Full CRUD - Bisa kapan saja!)
    if (uiState.debtToEdit != null) {
        val debt = uiState.debtToEdit!!
        EditDebtDialog(
            debt = debt,
            onDismiss = { viewModel.closeEditDialog() },
            onConfirm = { updated ->
                viewModel.updateDebt(updated)
            },
            isDark = isDark
        )
    }

    // Modal Dialog Konfirmasi Pelunasan dengan Opsi Kas
    if (uiState.debtToSettle != null) {
        val debt = uiState.debtToSettle!!
        val isIncome = debt.direction == "I_PAID_FOR_FRIEND"

        AlertDialog(
            onDismissRequest = { viewModel.dismissSettleDialog() },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Selesaikan Pelunasan? ✨",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) TextPrimaryDark else DeepBerryDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = if (isIncome) {
                            "Tandai bahwa ${debt.friendName} sudah membayar ${CurrencyFormatter.formatRupiah(debt.amount)}."
                        } else {
                            "Tandai bahwa Anda sudah melunasi utang ke ${debt.friendName} sebesar ${CurrencyFormatter.formatRupiah(debt.amount)}."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        color = if (isDark) SurfaceCardDark else SurfaceCard,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setAutoCreateTransaction(!uiState.isAutoCreateTransactionChecked)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Checkbox(
                                checked = uiState.isAutoCreateTransactionChecked,
                                onCheckedChange = { viewModel.setAutoCreateTransaction(it) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = PrimarySakuraPink,
                                    checkmarkColor = Color.White
                                )
                            )
                            Column {
                                Text(
                                    text = if (isIncome) "Masukkan ke Kas (Pemasukan)" else "Potong dari Kas (Pengeluaran)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) TextPrimaryDark else DeepBerryDark
                                )
                                Text(
                                    text = if (isIncome) {
                                        "Saldo kas aplikasi bertambah ${CurrencyFormatter.formatRupiah(debt.amount)}"
                                    } else {
                                        "Saldo kas aplikasi berkurang ${CurrencyFormatter.formatRupiah(debt.amount)}"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmSettleDebt() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimarySakuraPink),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Ya, Selesaikan", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.dismissSettleDialog() },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    // Modal Dialog Konfirmasi Hapus Hutang
    if (uiState.debtToDelete != null) {
        val debt = uiState.debtToDelete!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Hapus Catatan? 🥺",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) TextPrimaryDark else DeepBerryDark
                )
            },
            text = {
                Text(
                    text = "Yakin ingin menghapus catatan ${CurrencyFormatter.formatRupiah(debt.amount)} untuk ${debt.friendName}? Tindakan ini tidak dapat dibatalkan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDeleteDebt() },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorSoftRed),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Ya, Hapus", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.dismissDeleteDialog() },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}

/**
 * Hero Card Ringkasan Total Hutang / Piutang
 */
@Composable
private fun DebtSummaryHeroCard(
    direction: String,
    unsettledAmount: Double,
    activeDebtorsCount: Int,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val isIPaid = direction == "I_PAID_FOR_FRIEND"
    val gradientColors = if (isIPaid) {
        listOf(PrimarySakuraPink, SecondaryLavender)
    } else {
        listOf(SecondaryLavender, PrimarySakuraPink.copy(alpha = 0.8f))
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                shadowColor = PrimarySakuraPink.copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(Brush.horizontalGradient(gradientColors))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isIPaid) "Total Piutang Belum Lunas 💸" else "Total Utang yang Harus Dibayar 🧾",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Text(
                    text = CurrencyFormatter.formatRupiah(unsettledAmount),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = if (unsettledAmount > 0) {
                        if (isIPaid) "Ada $activeDebtorsCount orang yang belum melunasi" else "Ada $activeDebtorsCount orang yang menunggu pelunasan"
                    } else {
                        "Semua catatan sudah beres lunas! ✨"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

/**
 * Segmented Control pastel untuk beralih antara Piutang vs Utang
 */
@Composable
private fun DebtDirectionTabs(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) SurfaceCardDark else SurfaceCard
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val isFriendOwesMe = selectedTab == "I_PAID_FOR_FRIEND"
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTabSelected("I_PAID_FOR_FRIEND") },
                shape = RoundedCornerShape(12.dp),
                color = if (isFriendOwesMe) PrimarySakuraPink else Color.Transparent
            ) {
                Text(
                    text = "🌸 Piutang (Mereka Berutang)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isFriendOwesMe) FontWeight.Bold else FontWeight.Medium,
                    color = if (isFriendOwesMe) Color.White else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }

            val isIOweFriend = selectedTab == "FRIEND_PAID_FOR_ME"
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTabSelected("FRIEND_PAID_FOR_ME") },
                shape = RoundedCornerShape(12.dp),
                color = if (isIOweFriend) PrimarySakuraPink else Color.Transparent
            ) {
                Text(
                    text = "🌷 Utang (Saya Berutang)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isIOweFriend) FontWeight.Bold else FontWeight.Medium,
                    color = if (isIOweFriend) Color.White else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
        }
    }
}

/**
 * Kartu per orang (Grouped by Person) yang menampilkan akumulasi hutang/piutang
 */
@Composable
private fun PersonDebtGroupCard(
    personGroup: PersonDebtGroup,
    onClick: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val initials = personGroup.friendName.take(2).uppercase()
    val isAllSettled = personGroup.unsettledCount == 0

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(20.dp),
                shadowColor = PrimarySakuraPink.copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isDark) SurfaceElevatedDark else SurfaceElevated
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Avatar inisial dengan background pastel
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isAllSettled) SuccessMintGreen.copy(alpha = 0.2f) else PrimarySakuraPink.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isAllSettled) (if (isDark) SuccessMintGreen else Color(0xFF2E7D32)) else PrimarySakuraPink
                )
            }

            // Info Orang & Jumlah Catatan
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = personGroup.friendName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) TextPrimaryDark else DeepBerryDark
                )

                if (isAllSettled) {
                    Text(
                        text = "Semua catatan lunas ✨",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDark) SuccessMintGreen else Color(0xFF2E7D32),
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = "${personGroup.unsettledCount} catatan aktif • Total ${personGroup.debts.size} transaksi",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Nominal Total & Chevron
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = CurrencyFormatter.formatRupiah(personGroup.totalUnsettledAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isAllSettled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        if (personGroup.direction == "I_PAID_FOR_FRIEND") PrimarySakuraPink else DeepBerry
                    }
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = "Lihat detail",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Modal Bottom Sheet: Rincian setiap sub-catatan pinjaman/pembayaran orang tertentu
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonDetailSheet(
    personGroup: PersonDebtGroup,
    onDismissRequest: () -> Unit,
    onAddRecordClick: () -> Unit,
    onSettleClick: (SplitBillDebtEntity) -> Unit,
    onEditClick: (SplitBillDebtEntity) -> Unit,
    onDeleteClick: (SplitBillDebtEntity) -> Unit,
    isDark: Boolean
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = if (isDark) SurfaceElevatedDark else SurfaceElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Sheet
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = personGroup.friendName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) TextPrimaryDark else DeepBerryDark
                    )
                    Text(
                        text = if (personGroup.direction == "I_PAID_FOR_FRIEND") "Dihutangi oleh ${personGroup.friendName}" else "Hutang ke ${personGroup.friendName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Tutup",
                        tint = if (isDark) TextPrimaryDark else DeepBerryDark
                    )
                }
            }

            // Summary Info Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = if (isDark) SurfaceCardDark else SurfaceCard
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Belum Lunas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.formatRupiah(personGroup.totalUnsettledAmount),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimarySakuraPink
                        )
                    }

                    Button(
                        onClick = onAddRecordClick,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimarySakuraPink)
                    ) {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Catat Baru", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(
                text = "Riwayat Catatan (${personGroup.debts.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) TextPrimaryDark else DeepBerryDark
            )

            // List Sub-Catatan Pinjaman / Pembayaran
            personGroup.debts.forEach { debt ->
                SubDebtItemCard(
                    debt = debt,
                    onSettle = { onSettleClick(debt) },
                    onEdit = { onEditClick(debt) },
                    onDelete = { onDeleteClick(debt) },
                    isDark = isDark
                )
            }
        }
    }
}

/**
 * Item sub-catatan pinjaman individual
 */
@Composable
private fun SubDebtItemCard(
    debt: SplitBillDebtEntity,
    onSettle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) SurfaceCardDark else SurfaceCard,
        border = BorderStroke(
            1.dp,
            if (debt.isSettled) Color.Transparent else PrimarySakuraPink.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (debt.note.isNotBlank()) debt.note else "Tanpa catatan",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) TextPrimaryDark else DeepBerryDark
                    )
                    Text(
                        text = DateTimeUtil.formatTransactionDate(debt.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = CurrencyFormatter.formatRupiah(debt.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (debt.isSettled) MaterialTheme.colorScheme.onSurfaceVariant else PrimarySakuraPink
                )
            }

            // Status Badge & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (debt.isSettled) {
                    Surface(
                        color = SuccessMintGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = if (isDark) SuccessMintGreen else Color(0xFF2E7D32),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Lunas",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) SuccessMintGreen else Color(0xFF2E7D32)
                            )
                        }
                    }
                } else {
                    Surface(
                        color = Color(0xFFFFF3CD),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.HourglassEmpty,
                                contentDescription = null,
                                tint = Color(0xFF856404),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Belum Lunas",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF856404)
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!debt.isSettled) {
                        Button(
                            onClick = onSettle,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimarySakuraPink),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Lunaskan ✨", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Edit button (CRUD - Can edit anytime!)
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Ubah catatan",
                            tint = PrimarySakuraPink,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Hapus catatan",
                            tint = ErrorSoftRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Dialog Tambah Catatan Hutang/Piutang Baru
 */
@Composable
private fun AddDebtDialog(
    defaultDirection: String,
    initialFriendName: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, amount: Double, note: String, direction: String) -> Unit,
    isDark: Boolean
) {
    var friendName by remember { mutableStateOf(initialFriendName) }
    var rawAmount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf(defaultDirection) }

    val amountValue = rawAmount.toDoubleOrNull() ?: 0.0
    val canSubmit = friendName.isNotBlank() && amountValue > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Catat Hutang / Piutang 🎀",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDark) TextPrimaryDark else DeepBerryDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Direction selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isPiutang = direction == "I_PAID_FOR_FRIEND"
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { direction = "I_PAID_FOR_FRIEND" },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPiutang) PrimarySakuraPink else (if (isDark) SurfaceCardDark else SurfaceCard)
                    ) {
                        Text(
                            text = "Piutang (Dia Utang)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isPiutang) FontWeight.Bold else FontWeight.Normal,
                            color = if (isPiutang) Color.White else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    val isUtang = direction == "FRIEND_PAID_FOR_ME"
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { direction = "FRIEND_PAID_FOR_ME" },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isUtang) PrimarySakuraPink else (if (isDark) SurfaceCardDark else SurfaceCard)
                    ) {
                        Text(
                            text = "Utang (Saya Utang)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isUtang) FontWeight.Bold else FontWeight.Normal,
                            color = if (isUtang) Color.White else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = friendName,
                    onValueChange = { friendName = it },
                    label = { Text("Nama Teman") },
                    placeholder = { Text("Contoh: Nabila, Kak Siska") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rawAmount,
                    onValueChange = { rawAmount = it.filter { c -> c.isDigit() } },
                    label = { Text("Nominal (Rp)") },
                    placeholder = { Text("Contoh: 25000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Keterangan (Opsional)") },
                    placeholder = { Text("Beli makan siang bareng, bensin, dll") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(friendName, amountValue, note, direction) },
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = PrimarySakuraPink),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Simpan 🌸", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Batal", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}

/**
 * Dialog Ubah / Edit Catatan Hutang (CRUD - Dapat diubah kapan saja)
 */
@Composable
private fun EditDebtDialog(
    debt: SplitBillDebtEntity,
    onDismiss: () -> Unit,
    onConfirm: (SplitBillDebtEntity) -> Unit,
    isDark: Boolean
) {
    var friendName by remember(debt) { mutableStateOf(debt.friendName) }
    var rawAmount by remember(debt) { mutableStateOf(debt.amount.toLong().toString()) }
    var note by remember(debt) { mutableStateOf(debt.note) }
    var direction by remember(debt) { mutableStateOf(debt.direction) }

    val amountValue = rawAmount.toDoubleOrNull() ?: 0.0
    val canSubmit = friendName.isNotBlank() && amountValue > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Ubah Catatan Hutang ✏️",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDark) TextPrimaryDark else DeepBerryDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Direction selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isPiutang = direction == "I_PAID_FOR_FRIEND"
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { direction = "I_PAID_FOR_FRIEND" },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPiutang) PrimarySakuraPink else (if (isDark) SurfaceCardDark else SurfaceCard)
                    ) {
                        Text(
                            text = "Piutang (Dia Utang)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isPiutang) FontWeight.Bold else FontWeight.Normal,
                            color = if (isPiutang) Color.White else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    val isUtang = direction == "FRIEND_PAID_FOR_ME"
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { direction = "FRIEND_PAID_FOR_ME" },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isUtang) PrimarySakuraPink else (if (isDark) SurfaceCardDark else SurfaceCard)
                    ) {
                        Text(
                            text = "Utang (Saya Utang)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isUtang) FontWeight.Bold else FontWeight.Normal,
                            color = if (isUtang) Color.White else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = friendName,
                    onValueChange = { friendName = it },
                    label = { Text("Nama Teman") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rawAmount,
                    onValueChange = { rawAmount = it.filter { c -> c.isDigit() } },
                    label = { Text("Nominal (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Keterangan") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = debt.copy(
                        friendName = friendName.trim(),
                        amount = amountValue,
                        note = note.trim(),
                        direction = direction
                    )
                    onConfirm(updated)
                },
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = PrimarySakuraPink),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Simpan Perubahan 🌸", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Batal", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}

/**
 * Empty state saat belum ada catatan hutang/piutang
 */
@Composable
private fun EmptyDebtState(
    isFriendPaid: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(90.dp),
            contentAlignment = Alignment.Center
        ) {
            FloralDecoration(
                size = 90.dp,
                tint = PrimarySakuraPink,
                opacity = 0.35f
            )
            Icon(
                imageVector = Icons.Rounded.VolunteerActivism,
                contentDescription = null,
                tint = PrimarySakuraPink,
                modifier = Modifier.size(36.dp)
            )
        }

        Text(
            text = if (isFriendPaid) "Belum ada utang ke orang lain 🍃" else "Tidak ada yang berutang padamu 🍃",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) TextPrimaryDark else DeepBerryDark,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Tekan tombol + di pojok kanan bawah untuk mencatat hutang atau piutang baru.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}
