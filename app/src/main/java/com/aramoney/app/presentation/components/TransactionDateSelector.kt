package com.aramoney.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aramoney.app.presentation.theme.AraShape
import com.aramoney.app.presentation.theme.AraTheme
import com.aramoney.app.util.DateTimeUtil
import java.time.LocalDate

/** Batasi DatePicker agar tidak bisa memilih tanggal di masa depan. */
@OptIn(ExperimentalMaterial3Api::class)
object PastOrTodaySelectableDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
        !DateTimeUtil.utcMillisToLocalDate(utcTimeMillis).isAfter(LocalDate.now())

    override fun isSelectableYear(year: Int): Boolean = year <= LocalDate.now().year
}

/** Batasi DatePicker agar hanya tanggal hari ini atau setelahnya. */
@OptIn(ExperimentalMaterial3Api::class)
object TodayOrFutureSelectableDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
        !DateTimeUtil.utcMillisToLocalDate(utcTimeMillis).isBefore(LocalDate.now())

    override fun isSelectableYear(year: Int): Boolean = year >= LocalDate.now().year
}

/**
 * Pemilih tanggal transaksi: chip "Hari ini", "Kemarin", dan "Pilih tanggal".
 * Memungkinkan mencatat transaksi yang terlupa (backdate) tanpa bisa memilih tanggal di masa depan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDateSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val isCustom = selectedDate != today && selectedDate != yesterday

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AraSectionTitle(text = "Tanggal")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DateChip("Hari ini", selectedDate == today) { onDateSelected(today) }
            DateChip("Kemarin", selectedDate == yesterday) { onDateSelected(yesterday) }
            DateChip(
                label = if (isCustom) DateTimeUtil.formatShortLocalDate(selectedDate) else "Pilih tanggal",
                selected = isCustom,
                leading = true
            ) { showPicker = true }
        }
    }

    if (showPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = DateTimeUtil.localDateToUtcMillis(selectedDate),
            selectableDates = PastOrTodaySelectableDates
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onDateSelected(DateTimeUtil.utcMillisToLocalDate(it)) }
                    showPicker = false
                }) { Text("Pilih", color = AraTheme.colors.accent) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Batal", color = AraTheme.colors.textStrong) }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

/**
 * Dialog memilih tanggal kiriman (uang saku) berikutnya — hanya hari ini atau setelahnya.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllowanceDateDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
    initialDate: LocalDate? = null
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
            ?.takeIf { !it.isBefore(LocalDate.now()) }
            ?.let { DateTimeUtil.localDateToUtcMillis(it) },
        selectableDates = TodayOrFutureSelectableDates
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = state.selectedDateMillis != null,
                onClick = { state.selectedDateMillis?.let { onConfirm(DateTimeUtil.utcMillisToLocalDate(it)) } }
            ) { Text("Simpan", color = AraTheme.colors.accent) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = AraTheme.colors.textStrong) }
        }
    ) {
        DatePicker(
            state = state,
            title = {
                Text(
                    "Kapan uang kiriman berikutnya?",
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)
                )
            }
        )
    }
}

@Composable
private fun DateChip(
    label: String,
    selected: Boolean,
    leading: Boolean = false,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (leading) {
            { Icon(Icons.Rounded.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp)) }
        } else null,
        shape = AraShape.chip,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AraTheme.colors.selectedContainer,
            selectedLabelColor = AraTheme.colors.onSelectedContainer,
            selectedLeadingIconColor = AraTheme.colors.onSelectedContainer
        ),
        modifier = Modifier.heightIn(min = 40.dp)
    )
}
