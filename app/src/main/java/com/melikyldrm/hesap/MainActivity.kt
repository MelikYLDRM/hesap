package com.melikyldrm.hesap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.melikyldrm.hesap.data.local.ThemeMode
import com.melikyldrm.hesap.ui.navigation.BottomNavigationBar
import com.melikyldrm.hesap.ui.navigation.CalculatorNavHost
import com.melikyldrm.hesap.ui.navigation.Screen
import com.melikyldrm.hesap.ui.screens.settings.SettingsViewModel
import com.melikyldrm.hesap.ui.theme.HesapTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash screen'i kur
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val themeSettings by settingsViewModel.themeSettings.collectAsStateWithLifecycle()

            val isDarkTheme = when (themeSettings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // Tema değiştiğinde AppCompatDelegate'i güncelle
            LaunchedEffect(themeSettings.themeMode) {
                when (themeSettings.themeMode) {
                    ThemeMode.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    ThemeMode.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    ThemeMode.SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }

            HesapTheme(
                darkTheme = isDarkTheme,
                dynamicColor = themeSettings.dynamicColors
            ) {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column {
                // Reklam Banner Alanı (AdMob Banner için yer tutucu)
                // TODO: AdMob entegrasyonu yapıldığında BannerAd composable ile değiştirin
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    // Reklam yüklenene kadar boş bırakılabilir
                }
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        CalculatorNavHost(
            navController = navController,
            onHistoryClick = {
                navController.navigate(Screen.History.route)
            },
            onSettingsClick = {
                navController.navigate(Screen.Settings.route)
            },
            modifier = Modifier.padding(innerPadding)
        )
    }
}
