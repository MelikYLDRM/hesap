package com.melikyldrm.hesap.ads

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber

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
    private const val PRODUCTION_BANNER_AD_UNIT_ID = "ca-app-pub-6757073244337160/8439187479"

    /**
     * Build türüne göre uygun Ad Unit ID'yi döndürür
     */
    val BANNER_AD_UNIT_ID: String
        get() = if (com.melikyldrm.hesap.BuildConfig.DEBUG) {
            Timber.d("Using TEST Ad Unit ID")
            TEST_BANNER_AD_UNIT_ID
        } else {
            PRODUCTION_BANNER_AD_UNIT_ID
        }

    // AdMob başlatıldı mı?
    @Volatile
    private var isInitialized = false

    /**
     * AdMob'u lazy olarak başlat - sadece reklam gösterilecekken çağrılır.
     * MobileAds.initialize() Main thread'de çağrılmalı.
     */
    suspend fun initializeIfNeeded(context: Context) {
        if (!isInitialized) {
            withContext(Dispatchers.Main) {
                MobileAds.initialize(context) {
                    isInitialized = true
                    Timber.d("MobileAds initialized")
                }
            }
        }
    }

    fun isReady() = isInitialized
}

/**
 * Banner reklam bileşeni - Adaptive sizing ile ekran genişliğine uyum sağlar.
 * Reklam yüklenemezse alan gizlenir.
 */
@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    onAdLoaded: () -> Unit = {},
    onAdFailed: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var isAdLoaded by remember { mutableStateOf(false) }
    var isAdReady by remember { mutableStateOf(false) }

    // AdMob'u lazy olarak başlat - init bittikten sonra AdView oluştur
    LaunchedEffect(Unit) {
        // İlk frame'lerin render edilmesini bekle, sonra AdMob'u başlat
        delay(2000)
        AdConfig.initializeIfNeeded(context)
        isAdReady = true
    }

    if (!isAdReady) return

    // Ekran genişliğine göre adaptive AdSize hesapla
    val adSize = remember {
        val displayMetrics = context.resources.displayMetrics
        val adWidthPixels = displayMetrics.widthPixels.toFloat()
        val density = displayMetrics.density
        val adWidth = (adWidthPixels / density).toInt()
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
    }

    // AdView'i hatırla ve temizle - sadece AdMob hazır olduktan sonra oluştur
    val adView = remember {
        AdView(context).apply {
            setAdSize(adSize)
            adUnitId = AdConfig.BANNER_AD_UNIT_ID

            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    isAdLoaded = true
                    onAdLoaded()
                    Timber.d("Banner ad loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isAdLoaded = false
                    onAdFailed(error.message)
                    Timber.e("Banner ad failed: %s", error.message)
                }

                override fun onAdClicked() {
                    Timber.d("Banner ad clicked")
                }
            }

            // AdMob hazır, hemen yükle
            loadAd(AdRequest.Builder().build())
        }
    }

    // AdView'i temizle
    DisposableEffect(adView) {
        onDispose {
            adView.destroy()
        }
    }

    // Tek bir AndroidView kullan - aynı adView'ı iki farklı yerde kullanmak crash'e neden olur
    AnimatedVisibility(
        visible = isAdLoaded,
        enter = fadeIn() + expandVertically()
    ) {
        AndroidView(
            factory = { adView },
            modifier = modifier
                .fillMaxWidth()
                .height(adSize.height.dp)
        )
    }
}

