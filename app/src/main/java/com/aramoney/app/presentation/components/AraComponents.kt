package com.aramoney.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aramoney.app.presentation.theme.AraShape
import com.aramoney.app.presentation.theme.AraSpacing
import com.aramoney.app.presentation.theme.AraTheme
import com.aramoney.app.presentation.theme.PrimarySakuraPink
import com.aramoney.app.presentation.theme.softShadow

// ==========================================================
// TOMBOL
// ==========================================================

/**
 * Tombol utama Ara Money. Tinggi minimal 52dp (bukan tetap) agar aman saat font diperbesar.
 */
@Composable
fun AraPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
    containerColor: Color = AraTheme.colors.action,
    contentColor: Color = AraTheme.colors.onAction
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = AraShape.button,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = if (isLoading) containerColor else AraTheme.colors.disabledContainer,
            disabledContentColor = if (isLoading) contentColor else AraTheme.colors.disabledContent
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        modifier = modifier
            .heightIn(min = 52.dp)
            .softShadow(
                elevation = if (enabled) 6.dp else 0.dp,
                shape = AraShape.button,
                shadowColor = PrimarySakuraPink.copy(alpha = 0.30f)
            )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = contentColor,
                strokeWidth = 2.5.dp
            )
        } else {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Tombol konfirmasi untuk aksi destruktif (hapus). */
@Composable
fun AraDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = AraShape.button,
        colors = ButtonDefaults.buttonColors(
            containerColor = AraTheme.colors.danger,
            contentColor = AraTheme.colors.onDanger
        ),
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

/** Tombol sekunder bergaris tepi. */
@Composable
fun AraSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = AraShape.button,
        border = BorderStroke(1.dp, AraTheme.colors.border),
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp), tint = AraTheme.colors.textStrong)
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(text = text, color = AraTheme.colors.textStrong)
    }
}

// ==========================================================
// SEGMENTED CONTROL
// ==========================================================

/**
 * Segmented control pil pastel (menggantikan ±7 implementasi duplikat).
 * Memiliki semantik `selectableGroup` + `Role.Tab` dan status terpilih untuk TalkBack.
 *
 * @param selectedColor warna latar opsi terpilih per indeks (default: warna aksi).
 */
@Composable
fun <T> AraSegmentedControl(
    options: List<T>,
    selected: T,
    onSelected: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    selectedColor: (T) -> Color = { Color.Unspecified },
    containerColor: Color = AraTheme.colors.surfaceCard
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AraShape.button,
        color = containerColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selected
                val customColor = selectedColor(option)
                val activeBg = if (customColor == Color.Unspecified) AraTheme.colors.action else customColor
                val bg by animateColorAsState(
                    targetValue = if (isSelected) activeBg else Color.Transparent,
                    label = "SegmentBg"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .clip(AraShape.chip)
                        .background(bg)
                        .selectable(
                            selected = isSelected,
                            role = Role.Tab,
                            onClick = { onSelected(option) }
                        )
                        .padding(horizontal = 6.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label(option),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ==========================================================
// HEADER LAYAR
// ==========================================================

/**
 * Header layar standar: overline kecil + judul besar, opsional tombol kembali & aksi di kanan.
 */
@Composable
fun AraScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    overline: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                start = if (onBack != null) AraSpacing.xs else AraSpacing.screen,
                end = AraSpacing.xs,
                top = AraSpacing.xs,
                bottom = AraSpacing.xs
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Kembali",
                    tint = AraTheme.colors.textStrong
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            if (overline != null) {
                Text(
                    text = overline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AraTheme.colors.textStrong,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics { heading() }
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, content = actions)
    }
}

/** Judul seksi di dalam layar (mis. "Transaksi Hari Ini"). */
@Composable
fun AraSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = AraTheme.colors.textStrong,
        modifier = modifier.semantics { heading() }
    )
}

// ==========================================================
// DIALOG
// ==========================================================

/**
 * Dialog konfirmasi standar. Gunakan [isDestructive] untuk aksi hapus (tombol merah).
 */
@Composable
fun AraConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissText: String = "Batal",
    isDestructive: Boolean = false,
    icon: ImageVector? = null,
    iconTint: Color = AraTheme.colors.warning,
    confirmEnabled: Boolean = true,
    extraContent: (@Composable () -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = AraShape.dialog,
        containerColor = AraTheme.colors.surfaceElevated,
        icon = icon?.let {
            { Icon(imageVector = it, contentDescription = null, tint = iconTint, modifier = Modifier.size(32.dp)) }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AraTheme.colors.textStrong,
                textAlign = if (icon != null) TextAlign.Center else TextAlign.Start
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AraSpacing.sm)) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                extraContent?.invoke()
            }
        },
        confirmButton = {
            if (isDestructive) {
                AraDangerButton(text = confirmText, onClick = onConfirm, enabled = confirmEnabled)
            } else {
                Button(
                    onClick = onConfirm,
                    enabled = confirmEnabled,
                    shape = AraShape.button,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AraTheme.colors.action,
                        contentColor = AraTheme.colors.onAction
                    ),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(text = confirmText, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(text = dismissText, color = AraTheme.colors.textStrong)
            }
        }
    )
}

/** Dialog peringatan sebelum mengubah transaksi berusia > 24 jam (dipakai Beranda & Laporan). */
@Composable
fun OldTransactionWarningDialog(
    formattedAmount: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AraConfirmDialog(
        title = "Ubah transaksi lama?",
        message = "Transaksi senilai $formattedAmount ini dicatat lebih dari 24 jam yang lalu. " +
            "Mengubahnya akan memengaruhi riwayat saldo sebelumnya.",
        confirmText = "Tetap Ubah",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        icon = Icons.Rounded.WarningAmber
    )
}

/** Dialog konfirmasi hapus transaksi (dipakai Beranda & Laporan). */
@Composable
fun DeleteTransactionDialog(
    formattedAmount: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AraConfirmDialog(
        title = "Hapus transaksi?",
        message = "Catatan senilai $formattedAmount akan dihapus. Kakak masih bisa mengurungkannya sesaat setelah dihapus.",
        confirmText = "Hapus",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        isDestructive = true
    )
}

// ==========================================================
// EMPTY STATE
// ==========================================================

/**
 * Empty state estetis: ikon di atas ornamen bunga, judul, deskripsi, dan aksi opsional.
 */
@Composable
fun AraEmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AraSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AraSpacing.sm)
    ) {
        Box(modifier = Modifier.size(88.dp), contentAlignment = Alignment.Center) {
            FloralDecoration(size = 88.dp, tint = PrimarySakuraPink, opacity = 0.35f)
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AraTheme.colors.accent,
                modifier = Modifier.size(34.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AraTheme.colors.textStrong,
            textAlign = TextAlign.Center
        )
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = AraSpacing.lg)
            )
        }
        if (actionText != null && onAction != null) {
            TextButton(onClick = onAction, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    color = AraTheme.colors.accent,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ==========================================================
// INPUT
// ==========================================================

/** Warna OutlinedTextField standar Ara Money. */
@Composable
fun araTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AraTheme.colors.accent,
    unfocusedBorderColor = AraTheme.colors.border,
    focusedLabelColor = AraTheme.colors.accent,
    cursorColor = AraTheme.colors.accent,
    focusedContainerColor = AraTheme.colors.surfaceElevated,
    unfocusedContainerColor = AraTheme.colors.surfaceElevated
)

/** Kartu permukaan standar dengan soft shadow pastel. */
@Composable
fun AraCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.softShadow(elevation = 4.dp, shape = AraShape.card),
        shape = AraShape.card,
        color = AraTheme.colors.surfaceElevated,
        content = content
    )
}

/** Banner info/peringatan inline. */
@Composable
fun AraInlineBanner(
    text: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    icon: ImageVector? = Icons.Rounded.WarningAmber
) {
    val bg = if (isError) AraTheme.colors.dangerContainer else AraTheme.colors.warningContainer
    val fg = if (isError) AraTheme.colors.onDangerContainer else AraTheme.colors.onWarningContainer
    Surface(color = bg, shape = AraShape.chip, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
            }
            Text(text = text, style = MaterialTheme.typography.bodySmall, color = fg)
        }
    }
}

/** Shape helper agar pemanggil tidak perlu import RoundedCornerShape untuk radius standar. */
val AraPillShape = RoundedCornerShape(50)
