package com.melikyldrm.hesap

import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.melikyldrm.hesap.data.local.ThemeMode
import com.melikyldrm.hesap.data.local.ThemePreferences
import com.melikyldrm.hesap.ui.navigation.BottomNavigationBar
import com.melikyldrm.hesap.ads.AdBanner
import com.melikyldrm.hesap.ui.navigation.CalculatorNavHost
import com.melikyldrm.hesap.ui.navigation.Screen
import com.melikyldrm.hesap.ui.screens.onboarding.OnboardingScreen
import com.melikyldrm.hesap.ui.screens.settings.SettingsViewModel
import com.melikyldrm.hesap.ui.theme.HesapTheme
import com.melikyldrm.hesap.update.InAppUpdateManager
import com.melikyldrm.hesap.update.UpdateState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    @Inject
    lateinit var themePreferences: ThemePreferences

    // In-App Update Manager
    private lateinit var inAppUpdateManager: InAppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // In-App Update Manager'ı başlat
        inAppUpdateManager = InAppUpdateManager(this)

        // Güncelleme kontrolünü ilk frame renderlandıktan sonra yap (startup performansı için)
        lifecycleScope.launch {
            delay(5000) // İlk UI render'ı ve AdMob init'i tamamlansın
            inAppUpdateManager.checkForUpdates()
        }

        setContent {
            val themeSettings by settingsViewModel.themeSettings.collectAsState()

            // Güncelleme durumunu gözlemle
            val updateState by inAppUpdateManager.updateState.collectAsState()

            // Güncelleme durumuna göre işlem yap
            LaunchedEffect(updateState) {
                when (updateState) {
                    is UpdateState.Downloaded -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Güncelleme indirildi! Yüklemek için uygulamayı yeniden başlatın.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    is UpdateState.Failed -> {
                        Timber.e("InAppUpdate: %s", (updateState as UpdateState.Failed).message)
                    }
                    else -> {}
                }
            }

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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        onCompleteUpdate = { inAppUpdateManager.completeUpdate() },
                        updateState = updateState,
                        themePreferences = themePreferences
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::inAppUpdateManager.isInitialized) {
            inAppUpdateManager.onResume()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::inAppUpdateManager.isInitialized) {
            inAppUpdateManager.onDestroy()
        }
    }
}

@Composable
fun MainScreen(
    onCompleteUpdate: () -> Unit = {},
    updateState: UpdateState = UpdateState.Idle,
    themePreferences: ThemePreferences? = null
) {
    // Onboarding state
    val hasSeenOnboarding by (themePreferences?.hasSeenOnboarding
        ?: kotlinx.coroutines.flow.flowOf(true)).collectAsState(initial = true)
    val scope = rememberCoroutineScope()

    if (!hasSeenOnboarding) {
        OnboardingScreen(
            onComplete = {
                scope.launch {
                    themePreferences?.setOnboardingCompleted()
                }
            }
        )
        return
    }

    val navController = rememberNavController()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val onHistoryClick = remember(navController) {
        { navController.navigate(Screen.History.route) }
    }
    val onSettingsClick = remember(navController) {
        { navController.navigate(Screen.Settings.route) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // Güncelleme indirildiyse snackbar göster
        snackbarHost = {
            if (updateState is UpdateState.Downloaded) {
                Snackbar(
                    action = {
                        TextButton(onClick = onCompleteUpdate) {
                            Text("YÜKLE")
                        }
                    }
                ) {
                    Text("Güncelleme hazır!")
                }
            }
        },
        bottomBar = {
            Column {
                BottomNavigationBar(navController = navController)
                // Yatay modda reklam gizlenir - yer kalmaz
                // key ile stabilize et - tab geçişlerinde yeniden oluşturulmasını engelle
                if (!isLandscape) {
                    key("ad_banner") {
                        AdBanner()
                    }
                }
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
