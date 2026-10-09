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
                            color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else DeepBerry
                        )
                        Text(
                            text = uiState.userName,
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
            // 2. Safe-To-Spend Hero Card
            item {
                SafeToSpendHeroCard(
                    state = uiState.safeToSpendState,
                    modifier = Modifier.fillMaxWidth()
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

    // Dialog Peringatan Ubah Transaksi Lama (> 24 Jam)
    if (uiState.oldTransactionWarning != null) {
        val oldTx = uiState.oldTransactionWarning!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissOldTransactionWarning() },
            shape = RoundedCornerShape(24.dp),
            icon = {
                Icon(
                    imageVector = Icons.Rounded.WarningAmber,
                    contentDescription = null,
                    tint = AraTheme.colors.warning,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Transaksi Lama (> 24 Jam) ⚠️",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AraTheme.colors.textStrong,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Transaksi senilai ${CurrencyFormatter.formatRupiah(oldTx.amount)} ini tercatat lebih dari 24 jam yang lalu. Mengubah transaksi lama dapat memengaruhi riwayat saldo masa lalu Kakak.\n\nApakah Kakak yakin tetap ingin mengubah transaksi ini?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmEditOldTransaction() },
                    colors = ButtonDefaults.buttonColors(containerColor = AraTheme.colors.action),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Tetap Ubah",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.dismissOldTransactionWarning() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Batal",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
    }

    // Modal Bottom Sheet Edit Transaksi
    if (uiState.transactionToEdit != null) {
        val txToEdit = uiState.transactionToEdit!!
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

    // Cute Confirmation Dialog saat ingin menghapus transaksi
    if (uiState.transactionToDelete != null) {
        val toDelete = uiState.transactionToDelete!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Hapus Transaksi? 🥺",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AraTheme.colors.textStrong
                )
            },
            text = {
                Text(
                    text = "Yakin ingin menghapus catatan senilai ${CurrencyFormatter.formatRupiah(toDelete.amount)}? Kakak masih bisa mengurungkannya sesaat setelah dihapus.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmDeleteTransaction { deleted ->
                            snackbar?.show(
                                message = "Transaksi ${CurrencyFormatter.formatRupiah(deleted.amount)} dihapus",
                                actionLabel = "Urungkan",
                                onAction = { viewModel.restoreTransaction(deleted) }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AraTheme.colors.danger),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Ya, Hapus",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.dismissDeleteDialog() },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Batal",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
    }
}

/**
 * Tampilan Empty State estetis saat belum ada transaksi hari ini
 */
@Composable
private fun EmptyTransactionTodayState(
    isFiltered: Boolean,
    onSeeReportsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()

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
                imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                contentDescription = null,
                tint = AraTheme.colors.accent,
                modifier = Modifier.size(36.dp)
            )
        }

        Text(
            text = if (isFiltered) "Tidak ada transaksi kategori ini hari ini 🍃" else "Belum ada transaksi hari ini 🍃",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AraTheme.colors.textStrong,
            textAlign = TextAlign.Center
        )

        Text(
            text = if (isFiltered) {
                "Coba pilih kategori lain atau catat pengeluaran/pemasukan baru."
            } else {
                "Tekan tombol bunga di pojok kanan bawah untuk mencatat jajan atau pemasukan hari ini."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        TextButton(
            onClick = onSeeReportsClick,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Lihat riwayat transaksi sebelumnya di Laporan",
                    style = MaterialTheme.typography.labelMedium,
                    color = AraTheme.colors.accent,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = AraTheme.colors.accent,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
