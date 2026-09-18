package com.aramoney.app.presentation.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aramoney.app.data.local.entity.CategoryEntity
import com.aramoney.app.domain.model.DebtCategoryConstants
import com.aramoney.app.presentation.components.availableCategoryIcons
import com.aramoney.app.presentation.components.getCategoryIconVector
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
import com.aramoney.app.presentation.theme.isAppInDarkTheme
import com.aramoney.app.presentation.theme.softShadow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ManageCategoriesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = isAppInDarkTheme()
    val snackbarHostState = remember { SnackbarHostState() }

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
            TopAppBar(
                title = {
                    Text(
                        text = "Kategori Transaksi 🏷️",
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) TextPrimaryDark else DeepBerryDark,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Kembali",
                            tint = if (isDark) TextPrimaryDark else DeepBerryDark
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.openAddCategoryDialog() }) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Tambah Kategori",
                            tint = PrimarySakuraPink,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        val filteredCategories = remember(uiState.categories, uiState.selectedTab) {
            when (uiState.selectedTab) {
                CategoryTabFilter.ALL -> uiState.categories
                CategoryTabFilter.EXPENSE -> uiState.categories.filter { it.isExpense }
                CategoryTabFilter.INCOME -> uiState.categories.filter { !it.isExpense }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Info Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .softShadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    color = if (isDark) SurfaceElevatedDark else SurfaceElevated
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Kelola Semua Kategori 🌸",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) TextPrimaryDark else DeepBerryDark
                        )
                        Text(
                            text = "Kamu bisa menambah kategori baru, mengubah nama, ikon, atau warna pastel, serta menghapus kategori yang tidak diperlukan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 2. Filter Tabs: Semua, Pengeluaran, Pemasukan
            item {
                val expenseCount = uiState.categories.count { it.isExpense }
                val incomeCount = uiState.categories.count { !it.isExpense }
                val totalCount = uiState.categories.size

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryTabFilter.values().forEach { filter ->
                        val isSelected = uiState.selectedTab == filter
                        val count = when (filter) {
                            CategoryTabFilter.ALL -> totalCount
                            CategoryTabFilter.EXPENSE -> expenseCount
                            CategoryTabFilter.INCOME -> incomeCount
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.setTabFilter(filter) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PrimarySakuraPinkContainer else if (isDark) SurfaceCardDark else SurfaceCard,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, PrimarySakuraPink) else null
                        ) {
                            Text(
                                text = "${filter.label} ($count)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) DeepBerry else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // 3. Category Items
            if (filteredCategories.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada kategori pada filter ini.\nTekan tombol (+) di pojok kanan atas untuk menambahkan.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(
                    items = filteredCategories,
                    key = { it.id }
                ) { category ->
                    CategoryManageCard(
                        category = category,
                        onEdit = { viewModel.openEditCategoryDialog(category) },
                        onDelete = { viewModel.openDeleteConfirmation(category) },
                        isDark = isDark
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Dialog Tambah / Ubah Kategori
    if (uiState.categoryFormState != null) {
        val form = uiState.categoryFormState!!
        ManageCategoryFormDialog(
            formState = form,
            onDismiss = { viewModel.closeCategoryDialog() },
            onSave = { name, icon, color, isExpense ->
                viewModel.saveCategory(form.categoryId, name, icon, color, isExpense)
            },
            isDark = isDark
        )
    }

    // Dialog Konfirmasi Hapus Kategori
    if (uiState.categoryToDelete != null) {
        ManageCategoryDeleteDialog(
            category = uiState.categoryToDelete!!,
            onDismiss = { viewModel.closeDeleteConfirmation() },
            onConfirm = { viewModel.confirmDeleteCategory() },
            isDark = isDark
        )
    }
}

@Composable
private fun CategoryManageCard(
    category: CategoryEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) SurfaceElevatedDark else SurfaceElevated
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val color = runCatching { Color(android.graphics.Color.parseColor(category.tintColorHex)) }
                    .getOrDefault(PrimarySakuraPink)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIconVector(category.iconResName),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) TextPrimaryDark else DeepBerryDark
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (category.isExpense) PrimarySakuraPink.copy(alpha = 0.15f) else SuccessMintGreen.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (category.isExpense) "Pengeluaran" else "Pemasukan",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = if (category.isExpense) PrimarySakuraPink else SuccessMintGreen,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                fontSize = 11.sp
                            )
                        }

                        if (category.isDefault) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SecondaryLavender.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = "Bawaan",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDark) TextPrimaryDark else DeepBerry,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            if (!DebtCategoryConstants.isSystemLocked(category.id)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Ubah kategori ${category.name}",
                            tint = PrimarySakuraPink,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Hapus kategori ${category.name}",
                            tint = ErrorSoftRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFE57373).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Terkunci 🔒",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE57373),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ManageCategoryFormDialog(
    formState: CategoryFormState,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String, color: String, isExpense: Boolean) -> Unit,
    isDark: Boolean
) {
    var name by remember(formState) { mutableStateOf(formState.initialName) }
    var isExpense by remember(formState) { mutableStateOf(formState.initialIsExpense) }
    var selectedColor by remember(formState) { mutableStateOf(formState.initialColor) }
    var selectedIcon by remember(formState) { mutableStateOf(formState.initialIcon) }

    val colorOptions = listOf(
        "#F48FB1", "#CE93D8", "#FFCCBC", "#81C784",
        "#64B5F6", "#BA68C8", "#FFD54F", "#FF8A80"
    )

    val activeColor = runCatching { Color(android.graphics.Color.parseColor(selectedColor)) }
        .getOrDefault(PrimarySakuraPink)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = if (formState.isEdit) "Ubah Kategori ✏️" else "Tambah Kategori 🎀",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDark) TextPrimaryDark else DeepBerryDark
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Live Preview Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(activeColor.copy(alpha = 0.22f))
                            .border(2.dp, activeColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIconVector(selectedIcon),
                            contentDescription = null,
                            tint = activeColor,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                // Input Nama Kategori
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Kategori") },
                    placeholder = { Text("Contoh: Laundry / Gym / Tabungan") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimarySakuraPink,
                        cursorColor = PrimarySakuraPink
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Toggle Jenis: Pengeluaran / Pemasukan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { isExpense = true },
                        color = if (isExpense) PrimarySakuraPink else if (isDark) SurfaceCardDark else SurfaceCard,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Pengeluaran",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isExpense) Color.White else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { isExpense = false },
                        color = if (!isExpense) SuccessMintGreen else if (isDark) SurfaceCardDark else SurfaceCard,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Pemasukan",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (!isExpense) DeepBerryDark else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }

                // Pilihan Warna Pastel
                Text(
                    text = "Pilih Warna Aksen:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) TextPrimaryDark else DeepBerryDark
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    colorOptions.forEach { hex ->
                        val col = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(PrimarySakuraPink)
                        val isSel = selectedColor.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(col)
                                .clickable { selectedColor = hex }
                                .border(
                                    width = if (isSel) 2.5.dp else 0.dp,
                                    color = if (isSel) (if (isDark) Color.White else DeepBerryDark) else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSel) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Pilihan Ikon (Icon Picker)
                Text(
                    text = "Pilih Ikon (${availableCategoryIcons.size} Pilihan):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) TextPrimaryDark else DeepBerryDark
                )

                // Grid 5 kolom ikon
                val iconChunks = availableCategoryIcons.chunked(5)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    iconChunks.forEach { chunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            chunk.forEach { option ->
                                val isSelectedIcon = selectedIcon == option.id
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelectedIcon) activeColor.copy(alpha = 0.25f)
                                             else if (isDark) SurfaceCardDark
                                             else SurfaceCard
                                        )
                                        .border(
                                            width = if (isSelectedIcon) 2.dp else 0.5.dp,
                                            color = if (isSelectedIcon) activeColor else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedIcon = option.id },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = option.icon,
                                        contentDescription = option.label,
                                        tint = if (isSelectedIcon) activeColor else (if (isDark) Color(0xFFD3B8CE) else DeepBerry.copy(alpha = 0.7f)),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            // Slot kosong penyeimbang kolom
                            repeat(5 - chunk.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, selectedIcon, selectedColor, isExpense) },
                enabled = name.isNotBlank(),
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

@Composable
private fun ManageCategoryDeleteDialog(
    category: CategoryEntity,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isDark: Boolean
) {
    val fallbackTypeName = if (category.isExpense) "Pengeluaran Lainnya" else "Pemasukan Lainnya"
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = "Hapus Kategori? 🗑️",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDark) TextPrimaryDark else DeepBerryDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Kamu yakin ingin menghapus kategori '${category.name}'?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Catatan transaksi yang menggunakan kategori ini akan dialihkan secara otomatis ke kategori '$fallbackTypeName' agar riwayat keuanganmu tetap aman dan rapi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorSoftRed),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Hapus", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(14.dp)) {
                Text("Batal", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}
