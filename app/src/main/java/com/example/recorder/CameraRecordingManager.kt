package com.example.recorder

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.model.CameraLens
import com.example.model.RecordingStatus
import com.example.model.VideoQuality
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor

class CameraRecordingManager private constructor(private val appContext: Context) {

    companion object {
        private const val TAG = "CameraRecordingManager"

        @Volatile
        private var INSTANCE: CameraRecordingManager? = null

        fun getInstance(context: Context): CameraRecordingManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CameraRecordingManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val mainExecutor: Executor = ContextCompat.getMainExecutor(appContext)
    private var cameraProvider: ProcessCameraProvider? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    private var currentCamera: Camera? = null
    private var preview: Preview? = null

    private val _recordingStatus = MutableStateFlow<RecordingStatus>(RecordingStatus.Idle)
    val recordingStatus: StateFlow<RecordingStatus> = _recordingStatus.asStateFlow()

    private var currentLens = CameraLens.BACK
    private var currentQuality = VideoQuality.FHD_1080P
    private var isAudioEnabled = true
    private var isTorchEnabled = false

    private val scope = CoroutineScope(Dispatchers.Main)

    fun initialize(onReady: () -> Unit = {}) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(appContext)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                onReady()
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing camera provider", e)
                _recordingStatus.value = RecordingStatus.Error("Failed to initialize camera: ${e.localizedMessage}")
            }
        }, mainExecutor)
    }

    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView?,
        lens: CameraLens = currentLens,
        quality: VideoQuality = currentQuality
    ) {
        currentLens = lens
        currentQuality = quality

        val provider = cameraProvider ?: run {
            initialize {
                bindCamera(lifecycleOwner, previewView, lens, quality)
            }
            return
        }

        try {
            provider.unbindAll()

            val cameraSelector = if (lens == CameraLens.FRONT && provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else if (provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }

            val preferredQualities = when (quality) {
                VideoQuality.UHD_4K -> listOf(Quality.UHD, Quality.FHD, Quality.HD, Quality.SD)
                VideoQuality.FHD_1080P -> listOf(Quality.FHD, Quality.HD, Quality.SD)
                VideoQuality.HD_720P -> listOf(Quality.HD, Quality.SD)
                VideoQuality.SD_480P -> listOf(Quality.SD, Quality.LOWEST)
            }

            val qualitySelector = QualitySelector.fromOrderedList(
                preferredQualities,
                FallbackStrategy.lowerQualityOrHigherThan(Quality.LOWEST)
            )

            val recorder = Recorder.Builder()
                .setQualitySelector(qualitySelector)
                .setExecutor(mainExecutor)
                .build()

            videoCapture = VideoCapture.withOutput(recorder)

            if (previewView != null) {
                preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                currentCamera = provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    videoCapture
                )
            } else {
                currentCamera = provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    videoCapture
                )
            }

            // Restore torch state if back camera
            if (lens == CameraLens.BACK && isTorchEnabled) {
                currentCamera?.cameraControl?.enableTorch(true)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Binding camera failed", e)
            _recordingStatus.value = RecordingStatus.Error("Failed to bind camera: ${e.localizedMessage}")
        }
    }

    @SuppressLint("MissingPermission")
    fun startRecording(
        recordAudio: Boolean = isAudioEnabled,
        onStarted: (Uri?) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val capture = videoCapture ?: run {
            onError("Camera recorder is not ready")
            return
        }

        if (activeRecording != null) {
            onError("Recording already in progress")
            return
        }

        isAudioEnabled = recordAudio
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "STEALTH_REC_$timestamp.mp4"

        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.TITLE, fileName)
            put(MediaStore.Video.Media.DESCRIPTION, "Recorded by Stealth Video Recorder")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/Camera")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
            appContext.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()

        val pendingRecording = capture.output.prepareRecording(appContext, mediaStoreOutput)

        if (recordAudio && ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                pendingRecording.withAudioEnabled()
            } catch (e: Exception) {
                Log.w(TAG, "Could not enable audio recording", e)
            }
        }

        _recordingStatus.value = RecordingStatus.Initializing

        try {
            activeRecording = pendingRecording.start(mainExecutor) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        _recordingStatus.value = RecordingStatus.Recording(
                            elapsedSeconds = 0,
                            recordedBytes = 0,
                            lens = currentLens,
                            isMuted = !recordAudio,
                            isTorchOn = isTorchEnabled
                        )
                        onStarted(null)
                    }

                    is VideoRecordEvent.Status -> {
                        val durationSeconds = event.recordingStats.recordedDurationNanos / 1_000_000_000L
                        val bytes = event.recordingStats.numBytesRecorded
                        _recordingStatus.value = RecordingStatus.Recording(
                            elapsedSeconds = durationSeconds,
                            recordedBytes = bytes,
                            lens = currentLens,
                            isMuted = !recordAudio,
                            isTorchOn = isTorchEnabled
                        )
                    }

                    is VideoRecordEvent.Finalize -> {
                        activeRecording = null
                        if (!event.hasError()) {
                            val outputUri = event.outputResults.outputUri
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                val values = ContentValues().apply {
                                    put(MediaStore.Video.Media.IS_PENDING, 0)
                                }
                                try {
                                    appContext.contentResolver.update(outputUri, values, null, null)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to update pending status", e)
                                }
                            }
                            _recordingStatus.value = RecordingStatus.Completed(outputUri, fileName)
                        } else {
                            Log.e(TAG, "Recording error: ${event.error}")
                            _recordingStatus.value = RecordingStatus.Error("Recording error code: ${event.error}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception starting video recording", e)
            _recordingStatus.value = RecordingStatus.Error("Failed to start recording: ${e.localizedMessage}")
            onError(e.localizedMessage ?: "Failed to start recording")
        }
    }

    fun stopRecording() {
        activeRecording?.stop()
        activeRecording = null
    }

    fun toggleTorch(enable: Boolean) {
        if (currentLens == CameraLens.BACK) {
            isTorchEnabled = enable
            currentCamera?.cameraControl?.enableTorch(enable)
        }
    }

    fun isRecording(): Boolean = activeRecording != null
}
