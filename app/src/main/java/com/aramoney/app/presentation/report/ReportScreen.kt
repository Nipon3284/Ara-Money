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

    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("id", "ID"))
    val dayFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale("id", "ID"))
    val rangeDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("id", "ID"))

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
                        text = "Evaluasi Keuangan 📊",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else DeepBerry
                    )

                    Text(
                        text = "Laporan Keuangan",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = AraTheme.colors.textStrong
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Segmented Filter Periode
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 3 Mode Pilihan Filter: Bulanan, Harian, Rentang Tanggal
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = AraTheme.colors.surfaceCard
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ReportPeriodType.entries.forEach { type ->
                                val isSelected = uiState.periodType == type
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.setPeriodType(type) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) AraTheme.colors.action else Color.Transparent
                                ) {
                                    Text(
                                        text = type.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

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
                                        text = uiState.selectedYearMonth.format(monthFormatter),
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
                                            text = uiState.selectedDate.format(dayFormatter),
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
                                            text = "${uiState.customStartDate.format(rangeDateFormatter)} - ${uiState.customEndDate.format(rangeDateFormatter)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = AraTheme.colors.textStrong
                                        )
                                    }

                                    Text(
                                        text = "Ubah 🗓️",
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

            // 2. Tiga Kartu Ringkasan (Pengeluaran, Pemasukan, Selisih)
            item {
                ReportStatCards(
                    totalExpense = uiState.reportData.totalExpense,
                    totalIncome = uiState.reportData.totalIncome,
                    netSavings = uiState.reportData.netSavings,
                    isDark = isDark
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
                    text = "Rincian Pengeluaran per Kategori",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AraTheme.colors.textStrong
                )
            }

            // 5. Item Breakdown Kategori / Empty State
            if (uiState.reportData.categoryBreakdown.isEmpty()) {
                item {
                    EmptyReportState(isDark = isDark)
                }
            } else {
                items(
                    items = uiState.reportData.categoryBreakdown,
                    key = { it.categoryId }
                ) { item ->
                    CategoryBreakdownItem(
                        item = item,
                        isSelected = item.categoryId == uiState.selectedCategoryId,
                        onClick = { viewModel.onCategorySelected(item.categoryId) },
                        isDark = isDark
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
                        text = if (uiState.selectedCategoryId != null) "Transaksi Terfilter" else "Daftar Setiap Transaksi",
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada transaksi pada periode ini 🍃",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                                dailyTotalIncome = listItem.dailyTotalIncome,
                                isDark = isDark
                            )
                        }
                        is ReportListItem.TransactionRow -> {
                            val category = uiState.categories.find { it.id == listItem.transaction.categoryId }
                            ReportTransactionRow(
                                transaction = listItem.transaction,
                                category = category,
                                onClick = { viewModel.onTransactionClick(listItem.transaction) },
                                onDeleteClick = { viewModel.requestDeleteTransaction(listItem.transaction) },
                                isDark = isDark
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
                                    text = "Tampilkan $remaining Transaksi Lainnya 🌸",
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
            initialSelectedDateMillis = uiState.selectedDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
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
                    Text("Pilih Tanggal", fontWeight = FontWeight.Bold, color = PrimarySakuraPink)
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
            initialSelectedStartDateMillis = uiState.customStartDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli(),
            initialSelectedEndDateMillis = uiState.customEndDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
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
                    Text("Terapkan", fontWeight = FontWeight.Bold, color = PrimarySakuraPink)
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

    // Konfirmasi Hapus Transaksi
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
 * Header Ramping untuk Pemisah Tanggal Transaksi
 */
@Composable
private fun DateSectionHeader(
    date: LocalDate,
    dailyTotalExpense: Double,
    dailyTotalIncome: Double,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val dayNameFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("id", "ID"))
    val label = when (date) {
        today -> "Hari Ini • ${date.format(dayNameFormatter)}"
        yesterday -> "Kemarin • ${date.format(dayNameFormatter)}"
        else -> date.format(dayNameFormatter)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp, start = 4.dp, end = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else DeepBerry
        )

        if (dailyTotalExpense > 0.0) {
            Text(
                text = "-${CurrencyFormatter.formatRupiah(dailyTotalExpense)}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = AraTheme.colors.expense
            )
        } else if (dailyTotalIncome > 0.0) {
            Text(
                text = "+${CurrencyFormatter.formatRupiah(dailyTotalIncome)}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = AraTheme.colors.income
            )
        }
    }
}

/**
 * Baris Transaksi Ramping & Ringan (Bebas Crash, Tanpa SwipeToDismissBox, Super Cepat)
 */
@Composable
private fun ReportTransactionRow(
    transaction: TransactionEntity,
    category: CategoryEntity?,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == "INCOME"
    val iconColor = category?.tintColorHex?.let {
        runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull()
    } ?: PrimarySakuraPink

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale("id", "ID"))
    val timeText = runCatching {
        Instant.ofEpochMilli(transaction.timestamp)
            .atZone(ZoneId.systemDefault())
            .format(timeFormatter)
    }.getOrDefault("")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = AraTheme.colors.surfaceElevated,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon Avatar Bulat Kategori
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconVector(category?.iconResName ?: "category"),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Kategori, Catatan, dan Jam
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = category?.name ?: "Transaksi",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AraTheme.colors.textStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (transaction.isSplitBillRelated && !DebtCategoryConstants.isSystemLocked(transaction.categoryId)) {
                        Surface(
                            color = PrimarySakuraPink.copy(alpha = 0.20f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Hutang",
                                style = MaterialTheme.typography.labelSmall,
                                color = AraTheme.colors.berry,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!transaction.note.isNullOrBlank()) {
                        Text(
                            text = transaction.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }

                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // Nominal Transaksi
            val amountColor = if (isIncome) {
                AraTheme.colors.income
            } else {
                AraTheme.colors.expense
            }
            val prefix = if (isIncome) "+ " else "- "

            Text(
                text = "$prefix${CurrencyFormatter.formatRupiah(transaction.amount)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )

            // Tombol Hapus Cepat
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Hapus transaksi",
                    tint = ErrorSoftRed.copy(alpha = 0.55f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * 3 Kartu statistik ringkas: Pengeluaran, Pemasukan, dan Sisa Tabungan
 */
@Composable
private fun ReportStatCards(
    totalExpense: Double,
    totalIncome: Double,
    netSavings: Double,
    isDark: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Pengeluaran
        StatSummaryCard(
            title = "Pengeluaran",
            amount = totalExpense,
            accentColor = PrimarySakuraPink,
            isDark = isDark,
            modifier = Modifier.weight(1f)
        )

        // Pemasukan
        StatSummaryCard(
            title = "Pemasukan",
            amount = totalIncome,
            accentColor = AraTheme.colors.income,
            isDark = isDark,
            modifier = Modifier.weight(1f)
        )

        // Sisa / Tabungan
        StatSummaryCard(
            title = "Sisa Saku",
            amount = netSavings,
            accentColor = SecondaryLavender,
            isDark = isDark,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatSummaryCard(
    title: String,
    amount: Double,
    accentColor: Color,
    isDark: Boolean,
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
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = CurrencyFormatter.formatRupiah(amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                maxLines = 1
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
                text = "Komposisi Pengeluaran 🍩",
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
                    "${it.categoryName} ${"%.0f".format(it.percentage)} persen"
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
    onClick: () -> Unit,
    isDark: Boolean
) {
    val categoryColor = runCatching {
        Color(android.graphics.Color.parseColor(item.tintColorHex))
    }.getOrDefault(PrimarySakuraPink)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(
                elevation = if (isSelected) 4.dp else 1.dp,
                shape = RoundedCornerShape(18.dp)
            )
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = AraTheme.colors.surfaceElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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

                    Column {
                        Text(
                            text = item.categoryName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AraTheme.colors.textStrong
                        )
                        Text(
                            text = "${item.transactionCount} transaksi",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyFormatter.formatRupiah(item.totalAmount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AraTheme.colors.textStrong
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f%%", item.percentage),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = categoryColor
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
                trackColor = categoryColor.copy(alpha = 0.15f)
            )
        }
    }
}

@Composable
private fun EmptyReportState(isDark: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            FloralDecoration(
                size = 80.dp,
                tint = PrimarySakuraPink,
                opacity = 0.35f
            )
            Icon(
                imageVector = Icons.Rounded.DonutLarge,
                contentDescription = null,
                tint = AraTheme.colors.accent,
                modifier = Modifier.size(34.dp)
            )
        }

        Text(
            text = "Belum ada pengeluaran di periode ini 🌸",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AraTheme.colors.textStrong,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Catatan keuanganmu di periode ini masih bersih dan rapi!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
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
