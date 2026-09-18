package com.aramoney.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DonutLarge
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AraScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : AraScreen(
        route = "home",
        title = "Beranda",
        icon = Icons.Rounded.Home
    )

    data object Report : AraScreen(
        route = "report",
        title = "Laporan",
        icon = Icons.Rounded.DonutLarge
    )

    data object SplitBill : AraScreen(
        route = "splitbill",
        title = "Hutang",
        icon = Icons.Rounded.Group
    )

    data object Settings : AraScreen(
        route = "settings",
        title = "Pengaturan",
        icon = Icons.Rounded.Settings
    )

    data object Onboarding : AraScreen(
        route = "onboarding",
        title = "Onboarding",
        icon = Icons.Rounded.Home
    )

    data object ManageCategories : AraScreen(
        route = "manage_categories",
        title = "Kategori Transaksi",
        icon = Icons.Rounded.Category
    )

    companion object {
        val bottomNavScreens: List<AraScreen>
            get() = listOf(Home, Report, SplitBill, Settings)
    }
}
