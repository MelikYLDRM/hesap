package com.melikyldrm.hesap.ads

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * AdMob Banner Reklam Bileşeni
 *
 * Lazy loading ile yüklenir - uygulama açılış hızını düşürmez.
 * Debug modda test reklamları, Release modda gerçek reklamlar gösterilir.
 */
object AdConfig {
    // Test Ad Unit ID (Debug builds için)
    private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    // Gerçek Ad Unit ID (Production builds için)
    // AdMob hesabınızdan oluşturduğunuz Banner Ad Unit ID'yi buraya yazın
    // Format: ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
    private const val PRODUCTION_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111" // TODO: Gerçek ID ile değiştirin

    /**
     * Build türüne göre uygun Ad Unit ID'yi döndürür
     * Debug modda test reklamları, Release modda gerçek reklamlar
     */
    val BANNER_AD_UNIT_ID: String
        get() = if (com.melikyldrm.hesap.BuildConfig.DEBUG) {
            android.util.Log.d("AdMob", "Using TEST Ad Unit ID")
            TEST_BANNER_AD_UNIT_ID
        } else {
            android.util.Log.d("AdMob", "Using PRODUCTION Ad Unit ID")
            PRODUCTION_BANNER_AD_UNIT_ID
        }

    // AdMob başlatıldı mı?
    private var isInitialized = false

    /**
     * AdMob'u lazy olarak başlat - sadece reklam gösterilecekken çağrılır
     */
    suspend fun initializeIfNeeded(context: Context) {
        if (!isInitialized) {
            withContext(Dispatchers.IO) {
                MobileAds.initialize(context) {
                    isInitialized = true
                    android.util.Log.d("AdMob", "MobileAds initialized")
                }
            }
        }
    }
}

/**
 * Banner reklam bileşeni
 *
 * @param modifier Modifier
 * @param onAdLoaded Reklam yüklendiğinde çağrılır
 * @param onAdFailed Reklam yüklenemediğinde çağrılır
 */
@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    onAdLoaded: () -> Unit = {},
    onAdFailed: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var isAdLoaded by remember { mutableStateOf(false) }

    // AdMob'u lazy olarak başlat
    LaunchedEffect(Unit) {
        AdConfig.initializeIfNeeded(context)
    }

    // AdView'i hatırla ve temizle
    val adView = remember {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = AdConfig.BANNER_AD_UNIT_ID

            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    isAdLoaded = true
                    onAdLoaded()
                    android.util.Log.d("AdMob", "Banner ad loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isAdLoaded = false
                    onAdFailed(error.message)
                    android.util.Log.e("AdMob", "Banner ad failed: ${error.message}")
                }

                override fun onAdClicked() {
                    android.util.Log.d("AdMob", "Banner ad clicked")
                }
            }
        }
    }

    // Reklam yükle
    LaunchedEffect(adView) {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    // AdView'i temizle
    DisposableEffect(adView) {
        onDispose {
            adView.destroy()
        }
    }

    // Banner göster
    AndroidView(
        factory = { adView },
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp) // Banner yüksekliği
    )
}

/**
 * Adaptive Banner - Ekran genişliğine göre otomatik boyutlandırılır
 */
@Composable
fun AdaptiveBanner(
    modifier: Modifier = Modifier,
    onAdLoaded: () -> Unit = {},
    onAdFailed: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var isAdLoaded by remember { mutableStateOf(false) }

    // AdMob'u lazy olarak başlat
    LaunchedEffect(Unit) {
        AdConfig.initializeIfNeeded(context)
    }

    // Ekran genişliğine göre AdSize hesapla
    val adSize = remember {
        val displayMetrics = context.resources.displayMetrics
        val adWidthPixels = displayMetrics.widthPixels.toFloat()
        val density = displayMetrics.density
        val adWidth = (adWidthPixels / density).toInt()
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
    }

    val adView = remember {
        AdView(context).apply {
            setAdSize(adSize)
            adUnitId = AdConfig.BANNER_AD_UNIT_ID

            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    isAdLoaded = true
                    onAdLoaded()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isAdLoaded = false
                    onAdFailed(error.message)
                }
            }
        }
    }

    LaunchedEffect(adView) {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    DisposableEffect(adView) {
        onDispose {
            adView.destroy()
        }
    }

    AndroidView(
        factory = { adView },
        modifier = modifier.fillMaxWidth()
    )
}

