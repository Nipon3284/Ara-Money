package com.aramoney.app.presentation.splitbill

import com.aramoney.app.presentation.components.AraConfirmDialog
import com.aramoney.app.presentation.components.AraEmptyState
import com.aramoney.app.presentation.components.ListScreenSkeleton
import com.aramoney.app.presentation.components.AraScreenHeader
import com.aramoney.app.presentation.components.AraSegmentedControl
import com.aramoney.app.presentation.components.CuteFAB
import com.aramoney.app.presentation.components.LocalAraSnackbar
import com.aramoney.app.presentation.components.RupiahVisualTransformation
import com.aramoney.app.presentation.components.araTextFieldColors
import com.aramoney.app.presentation.theme.AraShape
import com.aramoney.app.presentation.theme.HeroGradientEnd
import com.aramoney.app.presentation.theme.HeroGradientStart
import com.aramoney.app.util.AmountInput
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.TextButton
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization

import com.aramoney.app.presentation.theme.AraTheme

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
    val snackbar = LocalAraSnackbar.current
    val isDark = isAppInDarkTheme()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            AraScreenHeader(overline = "Buku catatan", title = "Utang & Piutang")
        },
        floatingActionButton = {
            CuteFAB(
                onClick = { viewModel.openAddDialog() },
                contentDescription = "Tambah catatan utang atau piutang",
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.isLoading) {
                item { ListScreenSkeleton(heroHeight = 120.dp, rows = 3) }
                return@LazyColumn
            }

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
                    onTabSelected = { viewModel.onTabSelected(it) }
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
                        text = if (uiState.selectedTab == "I_PAID_FOR_FRIEND") "Teman yang berutang ke Kakak" else "Utang Kakak ke teman",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AraTheme.colors.textStrong
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
                        onAddClick = { viewModel.openAddDialog() }
                    )
                }
            } else {
                items(
                    items = uiState.personGroups,
                    key = { it.friendName }
                ) { personGroup ->
                    PersonDebtGroupCard(
                        personGroup = personGroup,
                        onClick = { viewModel.selectPerson(personGroup) },
                        isDark = isDark,
                        modifier = Modifier.animateItem()
                    )
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
        DebtFormDialog(
            title = "Catat Utang / Piutang",
            confirmText = "Simpan",
            initialName = uiState.initialFriendNameForAdd,
            initialAmount = "",
            initialNote = "",
            initialDirection = uiState.selectedTab,
            onDismiss = { viewModel.closeAddDialog() },
            onConfirm = { name, amount, note, direction ->
                viewModel.addDebt(name, amount, note, direction)
                snackbar?.show("Catatan untuk $name tersimpan")
            }
        )
    }

    // Modal Dialog Ubah / Edit Hutang (Full CRUD - Bisa kapan saja!)
    if (uiState.debtToEdit != null) {
        val debt = uiState.debtToEdit!!
        DebtFormDialog(
            title = "Ubah Catatan",
            confirmText = "Simpan",
            initialName = debt.friendName,
            initialAmount = debt.amount.toLong().toString(),
            initialNote = debt.note,
            initialDirection = debt.direction,
            onDismiss = { viewModel.closeEditDialog() },
            onConfirm = { name, amount, note, direction ->
                viewModel.updateDebt(debt.copy(friendName = name, amount = amount, note = note, direction = direction))
                snackbar?.show("Perubahan catatan tersimpan")
            }
        )
    }

    // Konfirmasi pelunasan dengan opsi pencatatan ke kas
    uiState.debtToSettle?.let { debt ->
        val isIncome = debt.direction == "I_PAID_FOR_FRIEND"
        AraConfirmDialog(
            title = "Tandai lunas?",
            message = if (isIncome) {
                "Tandai bahwa ${debt.friendName} sudah membayar ${CurrencyFormatter.formatRupiah(debt.amount)}."
            } else {
                "Tandai bahwa Kakak sudah melunasi utang ke ${debt.friendName} sebesar ${CurrencyFormatter.formatRupiah(debt.amount)}."
            },
            confirmText = "Tandai Lunas",
            onConfirm = {
                viewModel.confirmSettleDebt { settled ->
                    snackbar?.show("Catatan ${settled.friendName} ditandai lunas")
                }
            },
            onDismiss = { viewModel.dismissSettleDialog() },
            extraContent = {
                Surface(
                    color = AraTheme.colors.surfaceCard,
                    shape = AraShape.button,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = uiState.isAutoCreateTransactionChecked,
                                role = Role.Checkbox,
                                onValueChange = { viewModel.setAutoCreateTransaction(it) }
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Checkbox(
                            checked = uiState.isAutoCreateTransactionChecked,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(
                                checkedColor = AraTheme.colors.action,
                                checkmarkColor = Color.White
                            )
                        )
                        Column {
                            Text(
                                text = if (isIncome) "Catat sebagai pemasukan" else "Catat sebagai pengeluaran",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = AraTheme.colors.textStrong
                            )
                            Text(
                                text = if (isIncome) {
                                    "Saldo bertambah ${CurrencyFormatter.formatRupiah(debt.amount)}"
                                } else {
                                    "Saldo berkurang ${CurrencyFormatter.formatRupiah(debt.amount)}"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        )
    }

    // Konfirmasi hapus + Undo
    uiState.debtToDelete?.let { debt ->
        AraConfirmDialog(
            title = "Hapus catatan?",
            message = "Catatan ${CurrencyFormatter.formatRupiah(debt.amount)} untuk ${debt.friendName} akan dihapus. Kakak masih bisa mengurungkannya sesaat setelah dihapus.",
            confirmText = "Hapus",
            isDestructive = true,
            onConfirm = {
                viewModel.confirmDeleteDebt { deleted ->
                    snackbar?.show(
                        message = "Catatan ${deleted.friendName} dihapus",
                        actionLabel = "Urungkan",
                        onAction = { viewModel.restoreDebt(deleted) }
                    )
                }
            },
            onDismiss = { viewModel.dismissDeleteDialog() }
        )
    }
}

/**
 * Hero Card Ringkasan Total Utang / Piutang
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
    // Gradien gelap yang sama dengan Hero Beranda agar teks putih tetap kontras (WCAG AA)
    val gradientColors = if (isIPaid) {
        listOf(HeroGradientStart, HeroGradientEnd)
    } else {
        listOf(HeroGradientEnd, HeroGradientStart)
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
                    text = if (isIPaid) "Total piutang belum lunas" else "Total utang yang harus dibayar",
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
                        "Semua catatan sudah lunas!"
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
    onTabSelected: (String) -> Unit
) {
    AraSegmentedControl(
        options = listOf("I_PAID_FOR_FRIEND", "FRIEND_PAID_FOR_ME"),
        selected = selectedTab,
        onSelected = onTabSelected,
        label = { if (it == "I_PAID_FOR_FRIEND") "Piutang" else "Utang" }
    )
}

/**
 * Kartu per orang (Grouped by Person) yang menampilkan akumulasi utang/piutang
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
        color = AraTheme.colors.surfaceElevated
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
                    color = if (isAllSettled) AraTheme.colors.income else PrimarySakuraPink
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
                    color = AraTheme.colors.textStrong
                )

                if (isAllSettled) {
                    Text(
                        text = "Semua catatan lunas",
                        style = MaterialTheme.typography.labelSmall,
                        color = AraTheme.colors.income,
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
        containerColor = AraTheme.colors.surfaceElevated
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
                        color = AraTheme.colors.textStrong
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
                        tint = AraTheme.colors.textStrong
                    )
                }
            }

            // Summary Info Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = AraTheme.colors.surfaceCard
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
                            color = AraTheme.colors.accent
                        )
                    }

                    Button(
                        onClick = onAddRecordClick,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AraTheme.colors.action)
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
                color = AraTheme.colors.textStrong
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
        color = AraTheme.colors.surfaceCard,
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
                        color = AraTheme.colors.textStrong
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
                                tint = AraTheme.colors.income,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Lunas",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AraTheme.colors.income
                            )
                        }
                    }
                } else {
                    Surface(
                        color = AraTheme.colors.warningContainer,
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
                                tint = AraTheme.colors.onWarningContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Belum Lunas",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AraTheme.colors.onWarningContainer
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
                            colors = ButtonDefaults.buttonColors(containerColor = AraTheme.colors.action),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.heightIn(min = 40.dp)
                        ) {
                            Text("Tandai Lunas", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Edit button (CRUD - Can edit anytime!)
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Ubah catatan",
                            tint = AraTheme.colors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Hapus catatan",
                            tint = AraTheme.colors.danger,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Form bersama Tambah & Ubah catatan utang/piutang.
 * Nominal diformat Rupiah saat diketik, dibatasi maksimal, dengan aksi keyboard berurutan.
 */
@Composable
private fun DebtFormDialog(
    title: String,
    confirmText: String,
    initialName: String,
    initialAmount: String,
    initialNote: String,
    initialDirection: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, amount: Double, note: String, direction: String) -> Unit
) {
    var friendName by remember { mutableStateOf(initialName) }
    var rawAmount by remember { mutableStateOf(initialAmount) }
    var note by remember { mutableStateOf(initialNote) }
    var direction by remember { mutableStateOf(initialDirection) }
    val focusManager = LocalFocusManager.current

    val amountValue = AmountInput.toAmount(rawAmount)
    val amountError = AmountInput.validationMessage(rawAmount)
    val disabledReason = when {
        friendName.isBlank() -> "Isi nama teman dulu ya, Kak"
        amountValue <= 0.0 -> "Isi nominalnya dulu ya, Kak"
        amountError != null -> amountError
        else -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = AraShape.dialog,
        containerColor = AraTheme.colors.surfaceElevated,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AraTheme.colors.textStrong
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AraSegmentedControl(
                    options = listOf("I_PAID_FOR_FRIEND", "FRIEND_PAID_FOR_ME"),
                    selected = direction,
                    onSelected = { direction = it },
                    label = { if (it == "I_PAID_FOR_FRIEND") "Dia utang ke Kakak" else "Kakak utang ke dia" }
                )

                OutlinedTextField(
                    value = friendName,
                    onValueChange = { friendName = it.take(MAX_NAME_LENGTH) },
                    label = { Text("Nama teman") },
                    placeholder = { Text("Mis. Nabila") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    shape = AraShape.button,
                    colors = araTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rawAmount,
                    onValueChange = { rawAmount = AmountInput.sanitize(it) },
                    label = { Text("Nominal") },
                    prefix = { Text("Rp") },
                    placeholder = { Text("25.000") },
                    isError = amountError != null,
                    supportingText = amountError?.let { { Text(it) } },
                    visualTransformation = RupiahVisualTransformation,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true,
                    shape = AraShape.button,
                    colors = araTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(MAX_NOTE_LENGTH) },
                    label = { Text("Keterangan (opsional)") },
                    placeholder = { Text("Mis. makan siang bareng") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    shape = AraShape.button,
                    colors = araTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (disabledReason != null && amountError == null) {
                    Text(
                        text = disabledReason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(friendName.trim(), amountValue, note.trim(), direction) },
                enabled = disabledReason == null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AraTheme.colors.action,
                    contentColor = AraTheme.colors.onAction
                ),
                shape = AraShape.button,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(confirmText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Batal", color = AraTheme.colors.textStrong)
            }
        }
    )
}

private const val MAX_NAME_LENGTH = 40
private const val MAX_NOTE_LENGTH = 80

/**
 * Empty state saat belum ada catatan utang/piutang
 */
@Composable
private fun EmptyDebtState(
    isFriendPaid: Boolean,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AraEmptyState(
        icon = Icons.Rounded.VolunteerActivism,
        title = if (isFriendPaid) "Kakak belum punya utang" else "Belum ada yang berutang ke Kakak",
        description = "Catat saat Kakak menalangi atau ditalangi teman agar tidak lupa ditagih.",
        actionText = "Catat sekarang",
        onAction = onAddClick,
        modifier = modifier
    )
}
