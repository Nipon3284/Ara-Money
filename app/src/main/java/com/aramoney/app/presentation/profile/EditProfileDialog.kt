package com.aramoney.app.presentation.profile

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aramoney.app.presentation.theme.DeepBerry
import com.aramoney.app.presentation.theme.DeepBerryDark
import com.aramoney.app.presentation.theme.ErrorSoftRed
import com.aramoney.app.presentation.theme.PrimarySakuraPink
import com.aramoney.app.presentation.theme.SurfaceCard
import com.aramoney.app.presentation.theme.SurfaceCardDark
import com.aramoney.app.presentation.theme.TextPrimaryDark
import java.io.File

@Composable
fun EditProfileDialog(
    initialName: String,
    initialAvatarPresetId: String,
    initialProfilePhotoPath: String?,
    onDismiss: () -> Unit,
    onSave: (name: String, avatarPresetId: String, photoUri: Uri?, clearPhoto: Boolean) -> Unit,
    isDark: Boolean
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialName) }
    var selectedPresetId by remember { mutableStateOf(initialAvatarPresetId) }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var isPhotoCleared by remember { mutableStateOf(false) }

    // Android PhotoPicker (100% offline, modern SAF without permissions)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPhotoUri = uri
            isPhotoCleared = false
        }
    }

    // Resolve current custom bitmap for preview
    val previewBitmap = remember(selectedPhotoUri, isPhotoCleared, initialProfilePhotoPath) {
        if (isPhotoCleared) {
            null
        } else if (selectedPhotoUri != null) {
            runCatching {
                context.contentResolver.openInputStream(selectedPhotoUri!!)?.use {
                    BitmapFactory.decodeStream(it)
                }
            }.getOrNull()
        } else if (!initialProfilePhotoPath.isNullOrBlank()) {
            val file = File(initialProfilePhotoPath)
            if (file.exists() && file.length() > 0) {
                runCatching { BitmapFactory.decodeFile(initialProfilePhotoPath) }.getOrNull()
            } else null
        } else null
    }

    val selectedPreset = remember(selectedPresetId) {
        ProfileAvatarHelper.getPreset(selectedPresetId)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = "Profil Pengguna 🌸",
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Avatar Preview Besar
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .border(3.dp, PrimarySakuraPink, CircleShape)
                        .background(if (isDark) SurfaceCardDark else SurfaceCard),
                    contentAlignment = Alignment.Center
                ) {
                    if (previewBitmap != null) {
                        Image(
                            bitmap = previewBitmap.asImageBitmap(),
                            contentDescription = "Preview Foto Profil",
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .background(selectedPreset.bgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = selectedPreset.icon,
                                contentDescription = selectedPreset.name,
                                tint = selectedPreset.iconColor,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }
                }

                // 2. Tombol Aksi Foto Galeri / Hapus Foto
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = PrimarySakuraPink
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (previewBitmap != null) "Ganti Foto" else "Pilih dari Galeri",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isDark) TextPrimaryDark else DeepBerryDark
                        )
                    }

                    if (previewBitmap != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = {
                                selectedPhotoUri = null
                                isPhotoCleared = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = ErrorSoftRed
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = ErrorSoftRed
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Hapus",
                                style = MaterialTheme.typography.labelMedium,
                                color = ErrorSoftRed
                            )
                        }
                    }
                }

                // 3. Input Nama Pengguna
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Panggilan") },
                    placeholder = { Text("Contoh: Kakak Ara") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimarySakuraPink,
                        cursorColor = PrimarySakuraPink
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // 4. Pilihan Avatar Karakter Lucu (Preset)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Atau Pilih Karakter Avatar Lucu:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) TextPrimaryDark else DeepBerryDark
                    )

                    val rows = ProfileAvatarHelper.presets.chunked(4)
                    rows.forEach { rowPresets ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowPresets.forEach { preset ->
                                val isSelected = (previewBitmap == null && selectedPresetId == preset.id)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(preset.bgColor)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) PrimarySakuraPink else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            selectedPresetId = preset.id
                                            selectedPhotoUri = null
                                            isPhotoCleared = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = preset.icon,
                                        contentDescription = preset.name,
                                        tint = preset.iconColor,
                                        modifier = Modifier.size(30.dp)
                                    )
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(PrimarySakuraPink),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = if (name.isNotBlank()) name.trim() else initialName
                    onSave(finalName, selectedPresetId, selectedPhotoUri, isPhotoCleared)
                },
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
