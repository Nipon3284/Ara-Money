package com.aramoney.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aramoney.app.presentation.theme.AraShape
import com.aramoney.app.presentation.theme.AraTheme
import com.aramoney.app.presentation.theme.PrimarySakuraPink
import com.aramoney.app.util.AmountInput
import com.aramoney.app.util.CurrencyFormatter

/** Preset target jajan harian yang direkomendasikan. */
val DailyTargetPresets = listOf(20_000L, 30_000L, 50_000L)

/**
 * Pemilih target jajan harian (preset + nominal kustom), dipakai Onboarding & Pengaturan.
 *
 * @param rawCustom digit mentah nominal kustom ("" jika memakai preset)
 * @param selectedPreset preset terpilih, atau null jika memakai kustom
 */
@Composable
fun DailyTargetPicker(
    selectedPreset: Long?,
    rawCustom: String,
    onPresetSelected: (Long) -> Unit,
    onCustomChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    onDone: (() -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Pilihan rekomendasi",
            style = MaterialTheme.typography.labelLarge,
            color = AraTheme.colors.textStrong
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DailyTargetPresets.forEach { preset ->
                val isSelected = selectedPreset == preset && rawCustom.isBlank()
                Surface(
                    shape = AraShape.chip,
                    color = if (isSelected) AraTheme.colors.action else AraTheme.colors.surfaceCard,
                    border = BorderStroke(1.2.dp, if (isSelected) PrimarySakuraPink else AraTheme.colors.border),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clip(AraShape.chip)
                        .selectable(selected = isSelected, role = Role.RadioButton) { onPresetSelected(preset) }
                ) {
                    Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = CurrencyFormatter.formatRupiah(preset.toDouble()),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else AraTheme.colors.textStrong
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = rawCustom,
            onValueChange = { onCustomChanged(AmountInput.sanitize(it)) },
            label = { Text("Atau nominal sendiri (per hari)") },
            prefix = { Text("Rp") },
            placeholder = { Text("35.000") },
            visualTransformation = RupiahVisualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                onDone?.invoke()
            }),
            singleLine = true,
            shape = AraShape.button,
            colors = araTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
