package com.aramoney.app.presentation.home

import com.aramoney.app.presentation.theme.AraTheme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import com.aramoney.app.presentation.theme.isAppInDarkTheme
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aramoney.app.presentation.components.AllCategoryChip
import com.aramoney.app.presentation.components.ListScreenSkeleton
import com.aramoney.app.presentation.components.AllowanceDateDialog
import com.aramoney.app.domain.model.SafeToSpendState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.aramoney.app.presentation.components.AraEmptyState
import com.aramoney.app.presentation.components.DeleteTransactionDialog
import com.aramoney.app.presentation.components.OldTransactionWarningDialog
import com.aramoney.app.presentation.components.CategoryChip
import com.aramoney.app.presentation.components.LocalAraSnackbar
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.aramoney.app.presentation.components.CuteFAB
import com.aramoney.app.presentation.components.EditTransactionSheet
import com.aramoney.app.presentation.components.FloralDecoration
import com.aramoney.app.presentation.components.HansaraWatermark
import com.aramoney.app.presentation.components.SafeToSpendHeroCard
import com.aramoney.app.presentation.components.TransactionListItem
import com.aramoney.app.presentation.profile.UserAvatar
import com.aramoney.app.presentation.theme.DeepBerry
import com.aramoney.app.presentation.theme.DeepBerryDark
import com.aramoney.app.presentation.theme.ErrorSoftRed
import com.aramoney.app.presentation.theme.PrimarySakuraPink
import com.aramoney.app.presentation.theme.SuccessMintGreen
import com.aramoney.app.presentation.theme.SurfaceCard
import com.aramoney.app.presentation.theme.SurfaceCardDark
import com.aramoney.app.presentation.theme.TextPrimaryDark
import com.aramoney.app.util.CurrencyFormatter

@Composable
fun HomeScreen(
    onAddTransactionClick: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToReport: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = isAppInDarkTheme()
    val scrollState = rememberLazyListState()
    val snackbar = LocalAraSnackbar.current
    var showAllowanceDatePicker by remember { mutableStateOf(false) }

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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Halo, semangat hari ini!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (uiState.isLoading) " " else uiState.userName,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.semantics { heading() },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = AraTheme.colors.textStrong
                        )
                    }

                    UserAvatar(
                        userName = uiState.userName,
                        avatarPresetId = uiState.avatarPresetId,
                        profilePhotoPath = uiState.profilePhotoPath,
                        size = 50.dp
                    )
                }
            }
        },
        floatingActionButton = {
            CuteFAB(
                onClick = onAddTransactionClick,
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Skeleton saat data pertama kali dimuat (hindari nama default & empty state palsu)
            if (uiState.isLoading) {
                item { ListScreenSkeleton() }
                return@LazyColumn
            }

            // 2. Safe-To-Spend Hero Card
            item {
                SafeToSpendHeroCard(
                    state = uiState.safeToSpendState,
                    modifier = Modifier.fillMaxWidth(),
                    onCardClick = if (uiState.safeToSpendState is SafeToSpendState.NeedsDateUpdate) {
                        { showAllowanceDatePicker = true }
                    } else null
                )
            }

            // 3. Filter kategori (hanya kategori yang dipakai hari ini) + chip "Semua"
            if (uiState.filterCategories.size > 1) item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Filter Kategori",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AraTheme.colors.textStrong,
                        modifier = Modifier.semantics { heading() }
                    )

                    LazyRow(
                        modifier = Modifier.selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        item(key = "all") {
                            AllCategoryChip(
                                isSelected = uiState.selectedCategoryId == null,
                                onClick = { viewModel.onCategorySelected(null) }
                            )
                        }
                        items(
                            items = uiState.filterCategories,
                            key = { it.id }
                        ) { category ->
                            CategoryChip(
                                category = category,
                                isSelected = category.id == uiState.selectedCategoryId,
                                onClick = { viewModel.onCategorySelected(category.id) }
                            )
                        }
                    }
                }
            }

            // 4. Section Judul Riwayat Transaksi Hari Ini
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Transaksi Hari Ini",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AraTheme.colors.textStrong,
                                modifier = Modifier.semantics { heading() }
                            )

                            if (uiState.recentTransactions.isNotEmpty()) {
                                Surface(
                                    color = PrimarySakuraPink.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${uiState.recentTransactions.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AraTheme.colors.berry,
                                        modifier = Modifier
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                            .semantics { contentDescription = "${uiState.recentTransactions.size} transaksi" }
                                    )
                                }
                            }
                        }

                        // Tombol Lihat Semua di Laporan (touch target >= 48dp)
                        TextButton(onClick = onNavigateToReport) {
                            Text(
                                text = "Laporan Lengkap",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = AraTheme.colors.accent
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = AraTheme.colors.accent,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Mini summary badge pengeluaran hari ini jika ada transaksi
                    if (uiState.todayTransactionCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AraTheme.colors.surfaceCard,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Keluar hari ini: -${CurrencyFormatter.formatRupiah(uiState.todayExpenseTotal)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = AraTheme.colors.expense
                                )
                                if (uiState.todayIncomeTotal > 0.0) {
                                    Text(
                                        text = "Masuk: +${CurrencyFormatter.formatRupiah(uiState.todayIncomeTotal)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = AraTheme.colors.income
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Daftar Transaksi Hari Ini / Empty State
            if (uiState.recentTransactions.isEmpty()) {
                item {
                    EmptyTransactionTodayState(
                        isFiltered = uiState.selectedCategoryId != null,
                        onSeeReportsClick = onNavigateToReport
                    )
                }
            } else {
                items(
                    items = uiState.recentTransactions,
                    key = { it.id }
                ) { transaction ->
                    val category = uiState.categories.find { it.id == transaction.categoryId }
                    TransactionListItem(
                        transaction = transaction,
                        category = category,
                        onClick = { viewModel.onTransactionClick(transaction) },
                        onDeleteRequest = { viewModel.requestDeleteTransaction(transaction) },
                        modifier = Modifier.animateItem()
                    )
                }
            }

            // Watermark halus hansara di bagian bawah
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

            // Spacer bawah agar konten tidak tertutup FAB
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (showAllowanceDatePicker) {
        AllowanceDateDialog(
            onDismiss = { showAllowanceDatePicker = false },
            onConfirm = {
                viewModel.updateNextAllowanceDate(it)
                showAllowanceDatePicker = false
                snackbar?.show("Tanggal kiriman berikutnya disimpan")
            }
        )
    }

    // Dialog Peringatan Ubah Transaksi Lama (> 24 Jam)
    uiState.oldTransactionWarning?.let { oldTx ->
        OldTransactionWarningDialog(
            formattedAmount = CurrencyFormatter.formatRupiah(oldTx.amount),
            onConfirm = { viewModel.confirmEditOldTransaction() },
            onDismiss = { viewModel.dismissOldTransactionWarning() }
        )
    }

    // Modal Bottom Sheet Edit Transaksi
    uiState.transactionToEdit?.let { txToEdit ->
        EditTransactionSheet(
            transaction = txToEdit,
            categories = uiState.categories,
            onDismissRequest = { viewModel.dismissEditTransaction() },
            onSave = { updated ->
                viewModel.updateTransaction(updated) {
                    snackbar?.show("Perubahan transaksi tersimpan")
                }
            },
            onDelete = {
                viewModel.dismissEditTransaction()
                viewModel.requestDeleteTransaction(txToEdit)
            }
        )
    }

    // Konfirmasi hapus + Undo via Snackbar
    uiState.transactionToDelete?.let { toDelete ->
        DeleteTransactionDialog(
            formattedAmount = CurrencyFormatter.formatRupiah(toDelete.amount),
            onConfirm = {
                viewModel.confirmDeleteTransaction { deleted ->
                    snackbar?.show(
                        message = "Transaksi ${CurrencyFormatter.formatRupiah(deleted.amount)} dihapus",
                        actionLabel = "Urungkan",
                        onAction = { viewModel.restoreTransaction(deleted) }
                    )
                }
            },
            onDismiss = { viewModel.dismissDeleteDialog() }
        )
    }
}

/**
 * Empty state saat belum ada transaksi hari ini
 */
@Composable
private fun EmptyTransactionTodayState(
    isFiltered: Boolean,
    onSeeReportsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AraEmptyState(
        icon = Icons.AutoMirrored.Rounded.ReceiptLong,
        title = if (isFiltered) "Tidak ada transaksi kategori ini hari ini" else "Belum ada transaksi hari ini",
        description = if (isFiltered) {
            "Pilih \"Semua\" atau kategori lain untuk melihat transaksi lainnya."
        } else {
            "Ketuk tombol + di kanan bawah untuk mencatat jajan atau pemasukan hari ini."
        },
        actionText = "Lihat riwayat di Laporan",
        onAction = onSeeReportsClick,
        modifier = modifier
    )
}