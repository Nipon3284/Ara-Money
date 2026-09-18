package com.aramoney.app.presentation.profile

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Face3
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.SentimentVerySatisfied
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aramoney.app.presentation.theme.DeepBerry
import com.aramoney.app.presentation.theme.PrimarySakuraPink
import java.io.File

data class AvatarPreset(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val bgColor: Color,
    val iconColor: Color
)

object ProfileAvatarHelper {
    val presets = listOf(
        AvatarPreset(
            id = "sakura_girl",
            name = "Sakura Girl",
            icon = Icons.Rounded.Spa,
            bgColor = Color(0xFFFFDDEB),
            iconColor = Color(0xFFD81B60)
        ),
        AvatarPreset(
            id = "hijab_chic",
            name = "Hijab Chic",
            icon = Icons.Rounded.Face3,
            bgColor = Color(0xFFE8D5F5),
            iconColor = Color(0xFF8E24AA)
        ),
        AvatarPreset(
            id = "boba_cat",
            name = "Anabul Paw",
            icon = Icons.Rounded.Pets,
            bgColor = Color(0xFFD7F5DE),
            iconColor = Color(0xFF2E7D32)
        ),
        AvatarPreset(
            id = "smart_student",
            name = "Mahasiswi Pintar",
            icon = Icons.Rounded.School,
            bgColor = Color(0xFFD6EEFF),
            iconColor = Color(0xFF1976D2)
        ),
        AvatarPreset(
            id = "sweet_heart",
            name = "Sweet Heart",
            icon = Icons.Rounded.Favorite,
            bgColor = Color(0xFFFFE0E6),
            iconColor = Color(0xFFE91E63)
        ),
        AvatarPreset(
            id = "boba_lover",
            name = "Boba Lover",
            icon = Icons.Rounded.Coffee,
            bgColor = Color(0xFFFFF0D4),
            iconColor = Color(0xFFF57C00)
        ),
        AvatarPreset(
            id = "happy_smile",
            name = "Ceria Selalu",
            icon = Icons.Rounded.SentimentVerySatisfied,
            bgColor = Color(0xFFFFFAD1),
            iconColor = Color(0xFFFBC02D)
        ),
        AvatarPreset(
            id = "sparkle_magic",
            name = "Sparkle Magic",
            icon = Icons.Rounded.AutoAwesome,
            bgColor = Color(0xFFF0E4FF),
            iconColor = Color(0xFF7B1FA2)
        )
    )

    fun getPreset(id: String): AvatarPreset {
        return presets.firstOrNull { it.id == id } ?: presets.first()
    }
}

@Composable
fun UserAvatar(
    userName: String,
    avatarPresetId: String,
    profilePhotoPath: String?,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    val customBitmap = remember(profilePhotoPath) {
        if (!profilePhotoPath.isNullOrBlank()) {
            val file = File(profilePhotoPath)
            if (file.exists() && file.length() > 0) {
                runCatching { BitmapFactory.decodeFile(profilePhotoPath) }.getOrNull()
            } else null
        } else null
    }

    val preset = remember(avatarPresetId) {
        ProfileAvatarHelper.getPreset(avatarPresetId)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(2.dp, PrimarySakuraPink.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (customBitmap != null) {
            Image(
                bitmap = customBitmap.asImageBitmap(),
                contentDescription = "Foto profil $userName",
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else if (preset != null) {
            Box(
                modifier = Modifier
                    .size(size)
                    .background(preset.bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = preset.icon,
                    contentDescription = preset.name,
                    tint = preset.iconColor,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
        } else {
            // Fallback initial
            val initial = userName.trim().firstOrNull()?.uppercase() ?: "A"
            Box(
                modifier = Modifier
                    .size(size)
                    .background(Color(0xFFFFDDEB)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.45f).sp,
                    color = DeepBerry
                )
            }
        }
    }
}
