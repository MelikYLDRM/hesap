package com.melikyldrm.hesap.update

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-App Update Manager
 *
 * Google Play Store'dan uygulama güncellemelerini kontrol eder ve yükler.
 * İki tür güncelleme destekler:
 * - IMMEDIATE: Kullanıcı güncellemeyi tamamlayana kadar uygulamayı kullanamaz
 * - FLEXIBLE: Kullanıcı uygulamayı kullanırken güncelleme arka planda indirilir
 */
class InAppUpdateManager(
    private val activity: ComponentActivity
) {
    companion object {
        private const val TAG = "InAppUpdate"

        // Kaç gün sonra güncelleme zorunlu olsun (IMMEDIATE)
        private const val DAYS_FOR_IMMEDIATE_UPDATE = 7

        // Update type priority threshold - bu değerin üstündeki priority IMMEDIATE güncelleme gerektirir
        private const val IMMEDIATE_UPDATE_PRIORITY = 4
    }

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private var updateResultLauncher: ActivityResultLauncher<IntentSenderRequest>? = null

    // Flexible update için listener - lazy init
    private lateinit var installStateUpdatedListener: InstallStateUpdatedListener

    private fun createInstallStateUpdatedListener(): InstallStateUpdatedListener {
        return InstallStateUpdatedListener { state ->
            when (state.installStatus()) {
                InstallStatus.DOWNLOADING -> {
                    val bytesDownloaded = state.bytesDownloaded()
                    val totalBytesToDownload = state.totalBytesToDownload()
                    val progress = if (totalBytesToDownload > 0) {
                        (bytesDownloaded * 100 / totalBytesToDownload).toInt()
                    } else 0
                    _updateState.value = UpdateState.Downloading(progress)
                    Log.d(TAG, "Downloading: $progress%")
                }
                InstallStatus.DOWNLOADED -> {
                    _updateState.value = UpdateState.Downloaded
                    Log.d(TAG, "Update downloaded, ready to install")
                }
                InstallStatus.INSTALLING -> {
                    _updateState.value = UpdateState.Installing
                    Log.d(TAG, "Installing update...")
                }
                InstallStatus.INSTALLED -> {
                    _updateState.value = UpdateState.Installed
                    Log.d(TAG, "Update installed")
                    if (::installStateUpdatedListener.isInitialized) {
                        appUpdateManager.unregisterListener(installStateUpdatedListener)
                    }
                }
                InstallStatus.FAILED -> {
                    _updateState.value = UpdateState.Failed("Güncelleme başarısız oldu")
                    Log.e(TAG, "Update failed")
                }
                InstallStatus.CANCELED -> {
                    _updateState.value = UpdateState.Idle
                    Log.d(TAG, "Update canceled by user")
                }
                else -> {}
            }
        }
    }

    init {
        // Listener'ı başlat
        installStateUpdatedListener = createInstallStateUpdatedListener()

        // Activity result launcher'ı kaydet
        updateResultLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    Log.d(TAG, "Update flow started successfully")
                    _updateState.value = UpdateState.UpdateStarted
                }
                Activity.RESULT_CANCELED -> {
                    Log.d(TAG, "Update canceled by user")
                    _updateState.value = UpdateState.Idle
                }
                else -> {
                    Log.e(TAG, "Update flow failed with result code: ${result.resultCode}")
                    _updateState.value = UpdateState.Failed("Güncelleme başlatılamadı")
                }
            }
        }
    }

    /**
     * Güncelleme olup olmadığını kontrol et
     */
    fun checkForUpdates() {
        Log.d(TAG, "Checking for updates...")
        _updateState.value = UpdateState.Checking

        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                handleUpdateInfo(appUpdateInfo)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to check for updates", exception)
                _updateState.value = UpdateState.Failed("Güncelleme kontrolü başarısız: ${exception.message}")
            }
    }

    private fun handleUpdateInfo(appUpdateInfo: AppUpdateInfo) {
        when (appUpdateInfo.updateAvailability()) {
            UpdateAvailability.UPDATE_AVAILABLE -> {
                Log.d(TAG, "Update available! Version code: ${appUpdateInfo.availableVersionCode()}")

                // Update type'ı belirle
                val updateType = determineUpdateType(appUpdateInfo)

                if (updateType == AppUpdateType.IMMEDIATE &&
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    // Zorunlu güncelleme
                    _updateState.value = UpdateState.UpdateAvailable(
                        versionCode = appUpdateInfo.availableVersionCode(),
                        isImmediate = true
                    )
                    startUpdate(appUpdateInfo, AppUpdateType.IMMEDIATE)
                } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    // Esnek güncelleme
                    _updateState.value = UpdateState.UpdateAvailable(
                        versionCode = appUpdateInfo.availableVersionCode(),
                        isImmediate = false
                    )
                    startUpdate(appUpdateInfo, AppUpdateType.FLEXIBLE)
                } else {
                    Log.d(TAG, "Update type not allowed")
                    _updateState.value = UpdateState.NoUpdate
                }
            }
            UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                Log.d(TAG, "No update available")
                _updateState.value = UpdateState.NoUpdate
            }
            UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                Log.d(TAG, "Update already in progress")
                // Devam eden güncellemeyi tamamla
                if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    startUpdate(appUpdateInfo, AppUpdateType.IMMEDIATE)
                }
            }
            else -> {
                Log.d(TAG, "Unknown update availability")
                _updateState.value = UpdateState.NoUpdate
            }
        }
    }

    private fun determineUpdateType(appUpdateInfo: AppUpdateInfo): Int {
        // Yüksek öncelikli güncellemeler için IMMEDIATE
        val priority = appUpdateInfo.updatePriority()
        if (priority >= IMMEDIATE_UPDATE_PRIORITY) {
            return AppUpdateType.IMMEDIATE
        }

        // Uzun süredir güncelleme yapılmadıysa IMMEDIATE
        val clientVersionStalenessDays = appUpdateInfo.clientVersionStalenessDays()
        if (clientVersionStalenessDays != null && clientVersionStalenessDays >= DAYS_FOR_IMMEDIATE_UPDATE) {
            return AppUpdateType.IMMEDIATE
        }

        // Varsayılan olarak FLEXIBLE
        return AppUpdateType.FLEXIBLE
    }

    private fun startUpdate(appUpdateInfo: AppUpdateInfo, updateType: Int) {
        Log.d(TAG, "Starting update with type: ${if (updateType == AppUpdateType.IMMEDIATE) "IMMEDIATE" else "FLEXIBLE"}")

        // Flexible güncelleme için listener kaydet
        if (updateType == AppUpdateType.FLEXIBLE) {
            appUpdateManager.registerListener(installStateUpdatedListener)
        }

        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                updateResultLauncher!!,
                AppUpdateOptions.newBuilder(updateType).build()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start update", e)
            _updateState.value = UpdateState.Failed("Güncelleme başlatılamadı: ${e.message}")
        }
    }

    /**
     * İndirilen güncellemeyi yükle (Flexible update için)
     */
    fun completeUpdate() {
        Log.d(TAG, "Completing update...")
        appUpdateManager.completeUpdate()
    }

    /**
     * Activity resume'da devam eden güncellemeyi kontrol et
     */
    fun onResume() {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                // IMMEDIATE güncelleme yarıda kaldıysa devam et
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        startUpdate(appUpdateInfo, AppUpdateType.IMMEDIATE)
                    }
                }

                // FLEXIBLE güncelleme indirildiyse kullanıcıya bildir
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    _updateState.value = UpdateState.Downloaded
                }
            }
    }

    /**
     * Kaynakları temizle
     */
    fun onDestroy() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }
}

/**
 * Güncelleme durumları
 */
sealed class UpdateState {
    data object Idle : UpdateState()
    data object Checking : UpdateState()
    data object NoUpdate : UpdateState()
    data class UpdateAvailable(val versionCode: Int, val isImmediate: Boolean) : UpdateState()
    data object UpdateStarted : UpdateState()
    data class Downloading(val progress: Int) : UpdateState()
    data object Downloaded : UpdateState()
    data object Installing : UpdateState()
    data object Installed : UpdateState()
    data class Failed(val message: String) : UpdateState()
}

