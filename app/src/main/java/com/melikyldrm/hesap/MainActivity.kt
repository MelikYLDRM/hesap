package com.melikyldrm.hesap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.melikyldrm.hesap.data.local.ThemeMode
import com.melikyldrm.hesap.ui.navigation.BottomNavigationBar
import com.melikyldrm.hesap.ads.AdBanner
import com.melikyldrm.hesap.ui.navigation.CalculatorNavHost
import com.melikyldrm.hesap.ui.navigation.Screen
import com.melikyldrm.hesap.ui.screens.settings.SettingsViewModel
import com.melikyldrm.hesap.ui.theme.HesapTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeSettings by settingsViewModel.themeSettings.collectAsState()

            val isDarkTheme = when (themeSettings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // AppCompatDelegate'i sadece gerektiğinde güncelle
            LaunchedEffect(themeSettings.themeMode) {
                val nightMode = when (themeSettings.themeMode) {
                    ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                    ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                    ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
                    AppCompatDelegate.setDefaultNightMode(nightMode)
                }
            }

            HesapTheme(
                darkTheme = isDarkTheme,
                dynamicColor = themeSettings.dynamicColors
            ) {
                // İlk frame'de basit bir placeholder göster, sonra asıl UI'ı yükle
                var isReady by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    // Sistem hazır olana kadar bekle, sonra UI'ı yükle
                    delay(100)
                    isReady = true
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isReady) {
                        MainScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    val onHistoryClick = remember(navController) {
        { navController.navigate(Screen.History.route) }
    }
    val onSettingsClick = remember(navController) {
        { navController.navigate(Screen.Settings.route) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column {
                BottomNavigationBar(navController = navController)
                AdBanner()
            }
        }
    ) { innerPadding ->
        CalculatorNavHost(
            navController = navController,
            onHistoryClick = onHistoryClick,
            onSettingsClick = onSettingsClick,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
