package com.aramoney.app.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.CompositionLocalProvider
import com.aramoney.app.presentation.components.LocalAraSnackbar
import com.aramoney.app.presentation.components.rememberAraSnackbarController
import com.aramoney.app.presentation.theme.DeepBerryDark
import com.aramoney.app.presentation.theme.PrimarySakuraPinkLight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aramoney.app.presentation.addtransaction.AddTransactionSheet
import com.aramoney.app.presentation.components.SakuraBackground
import com.aramoney.app.presentation.category.ManageCategoriesScreen
import com.aramoney.app.presentation.home.HomeScreen
import com.aramoney.app.presentation.onboarding.OnboardingScreen
import com.aramoney.app.presentation.report.ReportScreen
import com.aramoney.app.presentation.settings.SettingsScreen
import com.aramoney.app.presentation.splitbill.SplitBillScreen

@Composable
fun AraNavGraph(
    isOnboarded: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // State untuk menampilkan lembar catat transaksi (ModalBottomSheet overlay)
    var isAddSheetOpen by remember { mutableStateOf(false) }

    val startDestination = if (isOnboarded) {
        AraScreen.Home.route
    } else {
        AraScreen.Onboarding.route
    }

    val isBottomBarVisible = currentRoute in listOf(
        AraScreen.Home.route,
        AraScreen.Report.route,
        AraScreen.SplitBill.route,
        AraScreen.Settings.route
    )

    val snackbarController = rememberAraSnackbarController()

    CompositionLocalProvider(LocalAraSnackbar provides snackbarController) {
    SakuraBackground(modifier = modifier) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = {
                SnackbarHost(hostState = snackbarController.hostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        shape = RoundedCornerShape(16.dp),
                        // Warna tetap gelap di kedua tema agar tombol aksi pink selalu kontras
                        containerColor = DeepBerryDark,
                        contentColor = Color.White,
                        actionColor = PrimarySakuraPinkLight,
                        dismissActionContentColor = Color.White
                    )
                }
            },
            bottomBar = {
                if (isBottomBarVisible) {
                    AraBottomBar(
                        currentRoute = currentRoute,
                        onNavigateToRoute = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (isBottomBarVisible) innerPadding.calculateBottomPadding() else 0.dp)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.fillMaxSize(),
                    enterTransition = { fadeIn(animationSpec = tween(150)) },
                    exitTransition = { fadeOut(animationSpec = tween(150)) },
                    popEnterTransition = { fadeIn(animationSpec = tween(150)) },
                    popExitTransition = { fadeOut(animationSpec = tween(150)) }
                ) {
                    // 1. Rute Onboarding (Pengguna Baru)
                    composable(AraScreen.Onboarding.route) {
                        OnboardingScreen(
                            onFinishOnboarding = {
                                navController.navigate(AraScreen.Home.route) {
                                    popUpTo(AraScreen.Onboarding.route) {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                    }

                    // 2. Rute Beranda
                    composable(AraScreen.Home.route) {
                        HomeScreen(
                            onAddTransactionClick = { isAddSheetOpen = true },
                            onNavigateToReport = {
                                navController.navigate(AraScreen.Report.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }

                    // 3. Rute Laporan
                    composable(AraScreen.Report.route) {
                        ReportScreen()
                    }

                    // 4. Rute Split Bill / Talangan
                    composable(AraScreen.SplitBill.route) {
                        SplitBillScreen()
                    }

                    // 5. Rute Pengaturan
                    composable(AraScreen.Settings.route) {
                        SettingsScreen(
                            onNavigateToManageCategories = {
                                navController.navigate(AraScreen.ManageCategories.route)
                            }
                        )
                    }

                    // 6. Rute Kelola Kategori Transaksi
                    composable(AraScreen.ManageCategories.route) {
                        ManageCategoriesScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }

                // Modal Bottom Sheet Overlay untuk Tambah Transaksi Cepat
                if (isAddSheetOpen) {
                    AddTransactionSheet(
                        onDismissRequest = { isAddSheetOpen = false },
                        onSaved = { message -> snackbarController.show(message) }
                    )
                }
            }
        }
    }
    }
}
