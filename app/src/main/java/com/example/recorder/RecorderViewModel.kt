package com.example.recorder

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import androidx.camera.view.PreviewView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.example.gallery.GalleryRepository
import com.example.model.AppUiState
import com.example.model.CameraLens
import com.example.model.RecorderSettings
import com.example.model.RecordingStatus
import com.example.model.StealthDisguise
import com.example.model.VideoQuality
import com.example.model.VideoRecording
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecorderViewModel(application: Application) : AndroidViewModel(application) {

    private val cameraManager = CameraRecordingManager.getInstance(application)
    private val galleryRepo = GalleryRepository(application)

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private val _galleryVideos = MutableStateFlow<List<VideoRecording>>(emptyList())
    val galleryVideos: StateFlow<List<VideoRecording>> = _galleryVideos.asStateFlow()

    private var currentLifecycleOwner: LifecycleOwner? = null
    private var currentPreviewView: PreviewView? = null

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) {
                val batteryPct = (level * 100 / scale.toFloat()).toInt()
                _uiState.update { it.copy(batteryLevel = batteryPct) }
            }
        }
    }

    init {
        refreshStorageAndBattery()
        registerBatteryReceiver()
        observeCameraStatus()
        loadGalleryVideos()
    }

    private fun registerBatteryReceiver() {
        try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            getApplication<Application>().registerReceiver(batteryReceiver, filter)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun observeCameraStatus() {
        viewModelScope.launch {
            cameraManager.recordingStatus.collectLatest { status ->
                _uiState.update { current ->
                    current.copy(
                        status = status,
                        lastRecordedUri = if (status is RecordingStatus.Completed) status.savedUri else current.lastRecordedUri
                    )
                }

                if (status is RecordingStatus.Completed) {
                    loadGalleryVideos()
                    _uiState.update {
                        it.copy(
                            userNoticeMessage = "Video saved to Gallery: ${status.fileName}"
                        )
                    }
                }
            }
        }
    }

    fun attachCameraPreview(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        currentLifecycleOwner = lifecycleOwner
        currentPreviewView = previewView
        rebindCamera()
    }

    fun rebindCamera() {
        val owner = currentLifecycleOwner ?: return
        val preview = currentPreviewView
        val settings = _uiState.value.settings
        cameraManager.bindCamera(
            lifecycleOwner = owner,
            previewView = preview,
            lens = settings.selectedLens,
            quality = settings.videoQuality
        )
    }

    fun toggleCameraLens() {
        val nextLens = if (_uiState.value.settings.selectedLens == CameraLens.BACK) {
            CameraLens.FRONT
        } else {
            CameraLens.BACK
        }
        _uiState.update {
            it.copy(
                settings = it.settings.copy(
                    selectedLens = nextLens,
                    torchEnabled = false // Torch only available on back lens
                )
            )
        }
        rebindCamera()
    }

    fun setCameraLens(lens: CameraLens) {
        if (_uiState.value.settings.selectedLens != lens) {
            _uiState.update {
                it.copy(settings = it.settings.copy(selectedLens = lens, torchEnabled = false))
            }
            rebindCamera()
        }
    }

    fun setVideoQuality(quality: VideoQuality) {
        _uiState.update {
            it.copy(settings = it.settings.copy(videoQuality = quality))
        }
        rebindCamera()
    }

    fun toggleAudio() {
        _uiState.update {
            it.copy(settings = it.settings.copy(recordAudio = !it.settings.recordAudio))
        }
    }

    fun toggleTorch() {
        val currentTorch = _uiState.value.settings.torchEnabled
        val newTorch = !currentTorch
        _uiState.update {
            it.copy(settings = it.settings.copy(torchEnabled = newTorch))
        }
        cameraManager.toggleTorch(newTorch)
    }

    fun setStealthDisguise(disguise: StealthDisguise) {
        _uiState.update {
            it.copy(settings = it.settings.copy(stealthDisguise = disguise))
        }
    }

    fun toggleStealthMode(enable: Boolean? = null) {
        _uiState.update {
            val newState = enable ?: !it.isStealthModeActive
            it.copy(isStealthModeActive = newState)
        }
    }

    fun startRecording() {
        val context = getApplication<Application>()
        StealthRecorderService.startService(context)
        cameraManager.startRecording(
            recordAudio = _uiState.value.settings.recordAudio,
            onError = { errorMsg ->
                _uiState.update { it.copy(status = RecordingStatus.Error(errorMsg)) }
            }
        )
    }

    fun stopRecording() {
        val context = getApplication<Application>()
        cameraManager.stopRecording()
        StealthRecorderService.stopService(context)
    }

    fun setGalleryOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isGalleryOpen = isOpen) }
        if (isOpen) {
            loadGalleryVideos()
        }
    }

    fun setSettingsOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isSettingsOpen = isOpen) }
    }

    fun clearNoticeMessage() {
        _uiState.update { it.copy(userNoticeMessage = null) }
    }

    fun setPermissionsGranted(granted: Boolean) {
        _uiState.update { it.copy(hasRequiredPermissions = granted) }
        if (granted) {
            rebindCamera()
            loadGalleryVideos()
        }
    }

    fun loadGalleryVideos() {
        viewModelScope.launch {
            val list = galleryRepo.getRecordedVideos()
            _galleryVideos.value = list
            refreshStorageAndBattery()
        }
    }

    fun deleteVideo(video: VideoRecording) {
        viewModelScope.launch {
            val success = galleryRepo.deleteVideo(video.uri)
            if (success) {
                loadGalleryVideos()
            }
        }
    }

    private fun refreshStorageAndBattery() {
        val storageMb = galleryRepo.getAvailableStorageMb()
        _uiState.update { it.copy(availableStorageMb = storageMb) }
    }

    override fun onCleared() {
        try {
            getApplication<Application>().unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onCleared()
    }
}
