package com.aramoney.app.presentation.components

import com.aramoney.app.presentation.theme.AraTheme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aramoney.app.presentation.theme.PrimarySakuraPink
import com.aramoney.app.presentation.theme.StatusDangerBgDark
import com.aramoney.app.presentation.theme.StatusDangerBgLight
import com.aramoney.app.presentation.theme.StatusDangerTextDark
import com.aramoney.app.presentation.theme.StatusDangerTextLight
import com.aramoney.app.presentation.theme.StatusSafeBgDark
import com.aramoney.app.presentation.theme.StatusSafeBgLight
import com.aramoney.app.presentation.theme.StatusSafeTextDark
import com.aramoney.app.presentation.theme.StatusSafeTextLight
import com.aramoney.app.presentation.theme.StatusWarningBgDark
import com.aramoney.app.presentation.theme.StatusWarningBgLight
import com.aramoney.app.presentation.theme.StatusWarningTextDark
import com.aramoney.app.presentation.theme.StatusWarningTextLight
import com.aramoney.app.presentation.theme.isAppInDarkTheme
import com.aramoney.app.presentation.theme.softShadow

/**
 * Kartu Standar Ara Money dengan Soft Shadow pastel dan sudut membulat 20dp.
 */
@Composable
fun AraCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.softShadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

/**
 * Chip Indikator Status Empatis (Aman, Waspada, Bahaya).
 */
@Composable
fun StatusChip(
    text: String,
    icon: ImageVector,
    statusType: StatusType,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val bgColor = when (statusType) {
        StatusType.SAFE -> if (isDark) StatusSafeBgDark else StatusSafeBgLight
        StatusType.WARNING -> if (isDark) StatusWarningBgDark else StatusWarningBgLight
        StatusType.DANGER -> if (isDark) StatusDangerBgDark else StatusDangerBgLight
    }
    val textColor = when (statusType) {
        StatusType.SAFE -> if (isDark) StatusSafeTextDark else StatusSafeTextLight
        StatusType.WARNING -> if (isDark) StatusWarningTextDark else StatusWarningTextLight
        StatusType.DANGER -> if (isDark) StatusDangerTextDark else StatusDangerTextLight
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = modifier.defaultMinSize(minHeight = 32.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

enum class StatusType { SAFE, WARNING, DANGER }

/**
 * Tombol Utama Pastel dengan Touch Target aman >= 48dp.
 */
@Composable
fun AraPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AraTheme.colors.action,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
