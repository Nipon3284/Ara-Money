package com.aramoney.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aramoney.app.domain.model.ThemeMode
import com.aramoney.app.presentation.MainActivityUiState
import com.aramoney.app.presentation.MainViewModel
import com.aramoney.app.presentation.navigation.AraNavGraph
import com.aramoney.app.presentation.theme.AraMoneyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Tahan splash screen bawaan Android sampai preferensi pengguna (DataStore) selesai dibaca
        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value is MainActivityUiState.Loading
        }

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            when (val state = uiState) {
                is MainActivityUiState.Loading -> {
                    // Splash screen sistem sedang aktif menutupi layar, tidak me-render rute salah
                }
                is MainActivityUiState.Success -> {
                    val userPreferences = state.userPreferences
                    val isSystemDark = isSystemInDarkTheme()
                    val isDarkTheme = when (userPreferences.themeMode) {
                        ThemeMode.SYSTEM -> isSystemDark
                        ThemeMode.LIGHT -> false
                        ThemeMode.SAKURA_NIGHT -> true
                    }

                    AraMoneyTheme(darkTheme = isDarkTheme) {
                        AraNavGraph(
                            isOnboarded = userPreferences.isOnboarded
                        )
                    }
                }
            }
        }
    }
}
