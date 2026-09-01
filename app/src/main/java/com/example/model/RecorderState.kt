package com.example.model

import android.net.Uri

sealed interface RecordingStatus {
    object Idle : RecordingStatus
    object Initializing : RecordingStatus
    data class Recording(
        val elapsedSeconds: Long = 0,
        val recordedBytes: Long = 0,
        val lens: CameraLens = CameraLens.BACK,
        val isMuted: Boolean = false,
        val isTorchOn: Boolean = false,
        val isStealthActive: Boolean = false
    ) : RecordingStatus
    data class Completed(val savedUri: Uri?, val fileName: String) : RecordingStatus
    data class Error(val message: String) : RecordingStatus
}

data class AppUiState(
    val status: RecordingStatus = RecordingStatus.Idle,
    val settings: RecorderSettings = RecorderSettings(),
    val isStealthModeActive: Boolean = false,
    val isGalleryOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val availableStorageMb: Long = 0,
    val batteryLevel: Int = 100,
    val hasRequiredPermissions: Boolean = false,
    val lastRecordedUri: Uri? = null,
    val userNoticeMessage: String? = null
)
