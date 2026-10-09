package com.aramoney.app.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DonutLarge
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.aramoney.app.R

sealed class AraScreen(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector
) {
    data object Home : AraScreen("home", R.string.nav_home, Icons.Rounded.Home)

    data object Report : AraScreen("report", R.string.nav_report, Icons.Rounded.DonutLarge)

    data object SplitBill : AraScreen("splitbill", R.string.nav_debt, Icons.Rounded.Group)

    data object Settings : AraScreen("settings", R.string.nav_settings, Icons.Rounded.Settings)

    data object Onboarding : AraScreen("onboarding", R.string.nav_onboarding, Icons.Rounded.Home)

    data object ManageCategories : AraScreen("manage_categories", R.string.nav_categories, Icons.Rounded.Category)

    companion object {
        val bottomNavScreens: List<AraScreen>
            get() = listOf(Home, Report, SplitBill, Settings)
    }
}
