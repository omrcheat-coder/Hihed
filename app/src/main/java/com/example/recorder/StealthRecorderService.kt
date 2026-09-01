package com.example.recorder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.model.RecordingStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StealthRecorderService : Service() {

    companion object {
        const val CHANNEL_ID = "stealth_recorder_channel"
        const val NOTIFICATION_ID = 9012
        const val ACTION_START = "com.example.stealthcam.ACTION_START"
        const val ACTION_STOP = "com.example.stealthcam.ACTION_STOP"
        const val ACTION_BLACKOUT = "com.example.stealthcam.ACTION_BLACKOUT"

        fun startService(context: Context) {
            val intent = Intent(context, StealthRecorderService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, StealthRecorderService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var cameraManager: CameraRecordingManager

    override fun onCreate() {
        super.onCreate()
        cameraManager = CameraRecordingManager.getInstance(applicationContext)
        createNotificationChannel()
        acquireWakeLock()
        observeRecordingState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                cameraManager.stopRecording()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                startForegroundWithNotification("Active background recording (Screen-off enabled)")
            }
        }
        return START_STICKY
    }

    private fun observeRecordingState() {
        serviceScope.launch {
            cameraManager.recordingStatus.collectLatest { status ->
                when (status) {
                    is RecordingStatus.Recording -> {
                        val minutes = status.elapsedSeconds / 60
                        val seconds = status.elapsedSeconds % 60
                        val timeStr = String.format("%02d:%02d", minutes, seconds)
                        updateNotification("Recording active: $timeStr • Screen off recording ON")
                    }
                    is RecordingStatus.Completed -> {
                        updateNotification("Video auto-saved to Gallery")
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                    is RecordingStatus.Error -> {
                        updateNotification("Recording stopped: ${status.message}")
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                    is RecordingStatus.Idle -> {
                        // Keep idle or finish if not active
                    }
                    else -> {}
                }
            }
        }
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "StealthRecorder::BackgroundWakeLock"
            ).apply {
                setReferenceCounted(false)
                acquire(2 * 60 * 60 * 1000L) // 2 hours max safe timeout
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Stealth Background Recording",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps camera recording active even when the screen is turned off or locked"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(contentText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, StealthRecorderService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("Stealth Video Recorder")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Stop & Save",
                stopPendingIntent
            )
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun startForegroundWithNotification(contentText: String) {
        val notification = buildNotification(contentText)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            var serviceType = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                serviceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            }
            startForeground(NOTIFICATION_ID, notification, serviceType)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(contentText: String) {
        val notification = buildNotification(contentText)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        releaseWakeLock()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
