package com.aramoney.app.presentation.report

import com.aramoney.app.presentation.theme.AraTheme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.aramoney.app.presentation.theme.isAppInDarkTheme
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DonutLarge
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.aramoney.app.presentation.components.LocalAraSnackbar
import com.aramoney.app.presentation.components.AraScreenHeader
import com.aramoney.app.presentation.components.AraEmptyState
import com.aramoney.app.presentation.components.ListScreenSkeleton
import com.aramoney.app.presentation.components.TransactionListItem
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import com.aramoney.app.presentation.components.PastOrTodaySelectableDates
import com.aramoney.app.util.DateTimeUtil
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import com.aramoney.app.presentation.components.AraSegmentedControl
import com.aramoney.app.presentation.components.DeleteTransactionDialog
import com.aramoney.app.presentation.components.OldTransactionWarningDialog
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aramoney.app.data.local.entity.CategoryEntity
import com.aramoney.app.data.local.entity.TransactionEntity
import com.aramoney.app.domain.model.CategoryReportItem
import com.aramoney.app.domain.model.DebtCategoryConstants
import com.aramoney.app.domain.model.ReportPeriodType
import com.aramoney.app.presentation.components.EditTransactionSheet
import com.aramoney.app.presentation.components.FloralDecoration
import com.aramoney.app.presentation.components.HansaraWatermark
import com.aramoney.app.presentation.components.getIconVector
import com.aramoney.app.presentation.theme.DeepBerry
import com.aramoney.app.presentation.theme.DeepBerryDark
import com.aramoney.app.presentation.theme.ErrorSoftRed
import com.aramoney.app.presentation.theme.PrimarySakuraPink
import com.aramoney.app.presentation.theme.SecondaryLavender
import com.aramoney.app.presentation.theme.SuccessMintGreen
import com.aramoney.app.presentation.theme.SurfaceCard
import com.aramoney.app.presentation.theme.SurfaceCardDark
import com.aramoney.app.presentation.theme.SurfaceElevated
import com.aramoney.app.presentation.theme.SurfaceElevatedDark
import com.aramoney.app.presentation.theme.TextPrimaryDark
import com.aramoney.app.presentation.theme.softShadow
import com.aramoney.app.util.CurrencyFormatter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = LocalAraSnackbar.current
    val isDark = isAppInDarkTheme()

    var showDatePicker by remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }

    // Soft Pagination: Batas awal transaksi yang ditampilkan (reset saat periode berganti)
    var displayLimit by remember(
        uiState.selectedYearMonth,
        uiState.selectedDate,
        uiState.customStartDate,
        uiState.customEndDate,
        uiState.periodType,
        uiState.selectedCategoryId
    ) { mutableIntStateOf(20) }


    // Flat list approach: Pre-compute sealed class list for true lazy rendering
    val sortedTransactions = remember(uiState.filteredTransactions) {
        uiState.filteredTransactions.sortedByDescending { it.timestamp }
    }
    val totalTransactionsCount = sortedTransactions.size
    val displayedTransactions = remember(sortedTransactions, displayLimit) {
        sortedTransactions.take(displayLimit)
    }
    // Pre-compute flat list of headers + rows (avoids forEach inside LazyColumn DSL)
    val flatTransactionItems: List<ReportListItem> = remember(displayedTransactions) {
        val zone = ZoneId.systemDefault()
        val grouped = displayedTransactions.groupBy { tx ->
            Instant.ofEpochMilli(tx.timestamp).atZone(zone).toLocalDate()
        }
        buildList {
            grouped.forEach { (date, txList) ->
                add(ReportListItem.DateHeader(
                    date = date,
                    dailyTotalExpense = txList.filter { it.type == "EXPENSE" }.sumOf { it.amount },
                    dailyTotalIncome = txList.filter { it.type == "INCOME" }.sumOf { it.amount }
                ))
                txList.forEach { tx ->
                    add(ReportListItem.TransactionRow(transaction = tx))
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            AraScreenHeader(overline = "Evaluasi keuangan", title = "Laporan")
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Segmented Filter Periode
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 3 Mode Pilihan Filter: Bulanan, Harian, Rentang Tanggal
                    AraSegmentedControl(
                        options = ReportPeriodType.entries,
                        selected = uiState.periodType,
                        onSelected = { viewModel.setPeriodType(it) },
                        label = { it.label }
                    )

                    // Baris Navigasi Tanggal sesuai Mode yang Dipilih
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = AraTheme.colors.surfaceCard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .softShadow(elevation = 2.dp)
                    ) {
                        when (uiState.periodType) {
                            ReportPeriodType.MONTHLY -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    IconButton(
                                        onClick = { viewModel.onPreviousMonth() },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                            contentDescription = "Bulan sebelumnya",
                                            tint = AraTheme.colors.textStrong
                                        )
                                    }

                                    Text(
                                        text = DateTimeUtil.formatMonth(uiState.selectedYearMonth),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AraTheme.colors.textStrong
                                    )

                                    IconButton(
                                        onClick = { viewModel.onNextMonth() },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                            contentDescription = "Bulan berikutnya",
                                            tint = AraTheme.colors.textStrong
                                        )
                                    }
                                }
                            }
                            ReportPeriodType.DAILY -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    IconButton(
                                        onClick = { viewModel.onPreviousDay() },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                            contentDescription = "Hari sebelumnya",
                                            tint = AraTheme.colors.textStrong
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { showDatePicker = true }
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.CalendarMonth,
                                            contentDescription = null,
                                            tint = AraTheme.colors.accent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = DateTimeUtil.formatDay(uiState.selectedDate),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = AraTheme.colors.textStrong
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.onNextDay() },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                            contentDescription = "Hari berikutnya",
                                            tint = AraTheme.colors.textStrong
                                        )
                                    }
                                }
                            }
                            ReportPeriodType.CUSTOM_RANGE -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showDateRangePicker = true }
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DateRange,
                                            contentDescription = null,
                                            tint = AraTheme.colors.accent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "${DateTimeUtil.formatShortLocalDate(uiState.customStartDate)} – ${DateTimeUtil.formatShortLocalDate(uiState.customEndDate)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = AraTheme.colors.textStrong
                                        )
                                    }

                                    Text(
                                        text = "Ubah",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AraTheme.colors.accent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                item { ListScreenSkeleton(heroHeight = 140.dp, rows = 3) }
                return@LazyColumn
            }

            // 2. Kartu Ringkasan (Pengeluaran, Pemasukan, Sisa)
            item {
                ReportStatCards(
                    totalExpense = uiState.reportData.totalExpense,
                    totalIncome = uiState.reportData.totalIncome,
                    netSavings = uiState.reportData.netSavings
                )
            }

            // 3. Custom Canvas Donut Chart
            item {
                CustomDonutChartCard(
                    categoryBreakdown = uiState.reportData.categoryBreakdown,
                    totalExpense = uiState.reportData.totalExpense,
                    selectedCategoryId = uiState.selectedCategoryId,
                    onCategoryClick = { viewModel.onCategorySelected(it) },
                    isDark = isDark
                )
            }

            // 4. Breakdown Kategori List Header
            item {
                Text(
                    text = "Pengeluaran per Kategori",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AraTheme.colors.textStrong
                )
            }

            // 5. Item Breakdown Kategori / Empty State
            if (uiState.reportData.categoryBreakdown.isEmpty()) {
                item {
                    EmptyReportState()
                }
            } else {
                items(
                    items = uiState.reportData.categoryBreakdown,
                    key = { it.categoryId }
                ) { item ->
                    CategoryBreakdownItem(
                        item = item,
                        isSelected = item.categoryId == uiState.selectedCategoryId,
                        onClick = { viewModel.onCategorySelected(item.categoryId) }
                    )
                }
            }

            // 6. Section Rincian Transaksi
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.selectedCategoryId != null) "Transaksi Terfilter" else "Semua Transaksi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AraTheme.colors.textStrong
                    )

                    Surface(
                        color = PrimarySakuraPink.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "$totalTransactionsCount catatan",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AraTheme.colors.berry,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (totalTransactionsCount == 0) {
                item {
                    AraEmptyState(
                        icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                        title = "Tidak ada transaksi pada periode ini",
                        description = if (uiState.selectedCategoryId != null) "Ketuk kategori yang sama sekali lagi untuk menghapus filter." else "Coba pilih periode lain."
                    )
                }
            } else {
                // Flat list rendering: Single items() call = true lazy composition
                items(
                    items = flatTransactionItems,
                    key = { listItem ->
                        when (listItem) {
                            is ReportListItem.DateHeader -> "date_header_${listItem.date}"
                            is ReportListItem.TransactionRow -> "tx_${listItem.transaction.id}"
                        }
                    },
                    contentType = { listItem ->
                        when (listItem) {
                            is ReportListItem.DateHeader -> "header"
                            is ReportListItem.TransactionRow -> "transaction"
                        }
                    }
                ) { listItem ->
                    when (listItem) {
                        is ReportListItem.DateHeader -> {
                            DateSectionHeader(
                                date = listItem.date,
                                dailyTotalExpense = listItem.dailyTotalExpense,
                                dailyTotalIncome = listItem.dailyTotalIncome
                            )
                        }
                        is ReportListItem.TransactionRow -> {
                            val category = uiState.categories.find { it.id == listItem.transaction.categoryId }
                            TransactionListItem(
                                transaction = listItem.transaction,
                                category = category,
                                onClick = { viewModel.onTransactionClick(listItem.transaction) },
                                onDeleteRequest = { viewModel.requestDeleteTransaction(listItem.transaction) },
                                showTimeOnly = true
                            )
                        }
                    }
                }

                // Tombol "Muat Lebih Banyak" jika transaksi melebihi batas saat ini
                if (displayLimit < totalTransactionsCount) {
                    val remaining = totalTransactionsCount - displayLimit
                    item(key = "load_more_button") {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { displayLimit += 30 },
                            shape = RoundedCornerShape(16.dp),
                            color = AraTheme.colors.surfaceCard
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ExpandMore,
                                    contentDescription = null,
                                    tint = AraTheme.colors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.size(6.dp))
                                Text(
                                    text = "Tampilkan ${minOf(remaining, 30)} transaksi lagi (sisa $remaining)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = AraTheme.colors.accent
                                )
                            }
                        }
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
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Modal DatePicker untuk Harian
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = DateTimeUtil.localDateToUtcMillis(uiState.selectedDate),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                            viewModel.onDateSelected(date)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Pilih", fontWeight = FontWeight.Bold, color = AraTheme.colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Modal DateRangePicker untuk Rentang Tanggal
    if (showDateRangePicker) {
        val dateRangePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = DateTimeUtil.localDateToUtcMillis(uiState.customStartDate),
            initialSelectedEndDateMillis = DateTimeUtil.localDateToUtcMillis(uiState.customEndDate),
        )
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val startMillis = dateRangePickerState.selectedStartDateMillis
                        val endMillis = dateRangePickerState.selectedEndDateMillis
                        if (startMillis != null && endMillis != null) {
                            val start = Instant.ofEpochMilli(startMillis).atZone(ZoneId.of("UTC")).toLocalDate()
                            val end = Instant.ofEpochMilli(endMillis).atZone(ZoneId.of("UTC")).toLocalDate()
                            viewModel.onDateRangeSelected(start, end)
                        }
                        showDateRangePicker = false
                    }
                ) {
                    Text("Terapkan", fontWeight = FontWeight.Bold, color = AraTheme.colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangePicker = false }) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        ) {
            DateRangePicker(state = dateRangePickerState)
        }
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
 * Header Ramping untuk Pemisah Tanggal Transaksi
 */
@Composable
private fun DateSectionHeader(
    date: LocalDate,
    dailyTotalExpense: Double,
    dailyTotalIncome: Double,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val label = when (date) {
        today -> "Hari ini • ${DateTimeUtil.formatDayHeader(date)}"
        today.minusDays(1) -> "Kemarin • ${DateTimeUtil.formatDayHeader(date)}"
        else -> DateTimeUtil.formatDayHeader(date)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp, start = 4.dp, end = 4.dp)
            .semantics(mergeDescendants = true) { heading() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = AraTheme.colors.berry,
            modifier = Modifier.weight(1f, fill = false)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (dailyTotalIncome > 0.0) {
                Text(
                    text = CurrencyFormatter.formatSigned(dailyTotalIncome, isIncome = true),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AraTheme.colors.income
                )
            }
            if (dailyTotalExpense > 0.0) {
                Text(
                    text = CurrencyFormatter.formatSigned(dailyTotalExpense, isIncome = false),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AraTheme.colors.expense
                )
            }
        }
    }
}

/**
 * Kartu ringkasan: Pengeluaran & Pemasukan berdampingan, Sisa Saku penuh di bawahnya
 * (tata letak 2+1 agar nominal panjang tidak terpotong).
 */
@Composable
private fun ReportStatCards(
    totalExpense: Double,
    totalIncome: Double,
    netSavings: Double
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatSummaryCard(
                title = "Pengeluaran",
                amount = totalExpense,
                accentColor = AraTheme.colors.expense,
                modifier = Modifier.weight(1f)
            )
            StatSummaryCard(
                title = "Pemasukan",
                amount = totalIncome,
                accentColor = AraTheme.colors.income,
                modifier = Modifier.weight(1f)
            )
        }
        StatSummaryCard(
            title = if (netSavings >= 0) "Sisa Saku (Pemasukan − Pengeluaran)" else "Defisit (Pengeluaran > Pemasukan)",
            amount = netSavings,
            accentColor = if (netSavings >= 0) AraTheme.colors.berry else AraTheme.colors.danger,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StatSummaryCard(
    title: String,
    amount: Double,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.softShadow(elevation = 3.dp, shape = RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = AraTheme.colors.surfaceElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .semantics(mergeDescendants = true) {},
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = CurrencyFormatter.formatRupiah(amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Kartu Donut Chart 100% Canvas murni tanpa dependency eksternal
 */
@Composable
private fun CustomDonutChartCard(
    categoryBreakdown: List<CategoryReportItem>,
    totalExpense: Double,
    selectedCategoryId: Long?,
    onCategoryClick: (Long) -> Unit,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = AraTheme.colors.surfaceElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Komposisi Pengeluaran",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AraTheme.colors.textStrong
            )

            // Animasi donut dari 0 → 1 setiap kali komposisi data berubah
            val chartAnim = remember { Animatable(0f) }
            LaunchedEffect(categoryBreakdown, totalExpense) {
                chartAnim.snapTo(0f)
                chartAnim.animateTo(1f, animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing))
            }
            val chartProgress = chartAnim.value

            val chartDescription = if (categoryBreakdown.isEmpty() || totalExpense <= 0.0) {
                "Diagram komposisi pengeluaran: belum ada data"
            } else {
                "Diagram komposisi pengeluaran. " + categoryBreakdown.joinToString(", ") {
                    "${it.categoryName} ${CurrencyFormatter.formatPercent(it.percentage, 0)}"
                }
            }

            Box(
                modifier = Modifier
                    .size(220.dp)
                    .semantics { contentDescription = chartDescription },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(200.dp)) {
                    val strokeWidth = 32.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2f, strokeWidth / 2f)
                    val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)

                    if (categoryBreakdown.isEmpty() || totalExpense <= 0.0) {
                        drawArc(
                            color = if (isDark) Color(0xFF4A2B50) else Color(0xFFF7E6EE),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    } else {
                        var startAngle = -90f
                        val totalGap = categoryBreakdown.size * 2.5f

                        categoryBreakdown.forEach { item ->
                            val proportion = (item.percentage / 100f)
                            val sweepAngle = ((proportion * (360f - totalGap)) * chartProgress).coerceAtLeast(1.5f)

                            val sliceColor = runCatching {
                                Color(android.graphics.Color.parseColor(item.tintColorHex))
                            }.getOrDefault(PrimarySakuraPink)

                            val isSelected = item.categoryId == selectedCategoryId
                            val effectiveWidth = if (isSelected) strokeWidth * 1.25f else strokeWidth

                            drawArc(
                                color = sliceColor,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = effectiveWidth, cap = StrokeCap.Round)
                            )

                            startAngle += sweepAngle + 2.5f
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Total Keluar",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatRupiah(totalExpense),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AraTheme.colors.textStrong
                    )
                }
            }
        }
    }
}

/**
 * Item Rincian Kategori dengan bar persentase dan ikon
 */
@Composable
private fun CategoryBreakdownItem(
    item: CategoryReportItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val categoryColor = runCatching {
        Color(android.graphics.Color.parseColor(item.tintColorHex))
    }.getOrDefault(PrimarySakuraPink)
    val percentText = CurrencyFormatter.formatPercent(item.percentage)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(
                elevation = if (isSelected) 4.dp else 1.dp,
                shape = RoundedCornerShape(18.dp)
            )
            .clip(RoundedCornerShape(18.dp))
            .selectable(selected = isSelected, onClick = onClick)
            .clearAndSetSemantics {
                selected = isSelected
                contentDescription = "${item.categoryName}, ${CurrencyFormatter.formatRupiah(item.totalAmount)}, " +
                    "$percentText dari total, ${item.transactionCount} transaksi. " +
                    if (isSelected) "Filter aktif" else "Ketuk untuk memfilter"
            },
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) AraTheme.colors.selectedContainer else AraTheme.colors.surfaceElevated,
        border = if (isSelected) BorderStroke(1.5.dp, PrimarySakuraPink) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconVector(item.iconResName),
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.categoryName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AraTheme.colors.textStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.transactionCount} transaksi",
                        style = MaterialTheme.typography.labelSmall,
                        color = AraTheme.colors.textMuted
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyFormatter.formatRupiah(item.totalAmount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AraTheme.colors.textStrong
                    )
                    Text(
                        text = percentText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LinearProgressIndicator(
                progress = { (item.percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = categoryColor,
                trackColor = categoryColor.copy(alpha = 0.15f),
                drawStopIndicator = {}
            )
        }
    }
}

@Composable
private fun EmptyReportState() {
    AraEmptyState(
        icon = Icons.Rounded.DonutLarge,
        title = "Belum ada pengeluaran di periode ini",
        description = "Catatan keuangan Kakak di periode ini masih bersih dan rapi!"
    )
}

/**
 * Sealed class untuk flat list rendering di LazyColumn.
 * Menggantikan forEach pattern yang menyebabkan crash karena eager evaluation.
 */
private sealed class ReportListItem {
    data class DateHeader(
        val date: LocalDate,
        val dailyTotalExpense: Double,
        val dailyTotalIncome: Double
    ) : ReportListItem()

    data class TransactionRow(
        val transaction: TransactionEntity
    ) : ReportListItem()
}
