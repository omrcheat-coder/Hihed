package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.CameraRear
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.CameraLens
import com.example.model.RecordingStatus
import com.example.model.StealthDisguise
import com.example.recorder.RecorderViewModel
import com.example.ui.camera.CameraPreviewView
import com.example.ui.gallery.GallerySheet
import com.example.ui.settings.SettingsSheet
import com.example.ui.stealth.StealthBlackoutOverlay
import com.example.ui.stealth.StealthCalculatorOverlay
import com.example.ui.stealth.StealthClockOverlay
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.StealthAmber
import com.example.ui.theme.StealthCyan
import com.example.ui.theme.StealthGreen
import com.example.ui.theme.StealthRed
import com.example.ui.theme.StealthRedDark
import com.example.ui.theme.StealthRedGlow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StealthRecorderScreen(viewModel: RecorderViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val galleryVideos by viewModel.galleryVideos.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Required Permissions Check
    val requiredPermissions = remember {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissions.toTypedArray()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val cameraGranted = results[Manifest.permission.CAMERA] == true
        viewModel.setPermissionsGranted(cameraGranted)
    }

    LaunchedEffect(Unit) {
        val cameraGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (!cameraGranted) {
            permissionLauncher.launch(requiredPermissions)
        } else {
            viewModel.setPermissionsGranted(true)
        }
    }

    LaunchedEffect(uiState.userNoticeMessage) {
        uiState.userNoticeMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearNoticeMessage()
        }
    }

    // Stealth Mode Active Overlay
    if (uiState.isStealthModeActive) {
        when (uiState.settings.stealthDisguise) {
            StealthDisguise.BLACKOUT -> {
                StealthBlackoutOverlay(
                    status = uiState.status,
                    batteryLevel = uiState.batteryLevel,
                    onExitStealth = { viewModel.toggleStealthMode(false) }
                )
            }
            StealthDisguise.CALCULATOR -> {
                StealthCalculatorOverlay(
                    status = uiState.status,
                    onExitStealth = { viewModel.toggleStealthMode(false) }
                )
            }
            StealthDisguise.DIGITAL_CLOCK -> {
                StealthClockOverlay(
                    status = uiState.status,
                    batteryLevel = uiState.batteryLevel,
                    onExitStealth = { viewModel.toggleStealthMode(false) }
                )
            }
        }
        return
    }

    // Main App Scaffold
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ObsidianBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ObsidianBlack)
        ) {
            if (uiState.hasRequiredPermissions) {
                // Live Viewfinder Camera Stream
                CameraPreviewView(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )

                // Top & Bottom Gradient Vignettes
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                            )
                        )
                )

                // Top Overlay Controls & Status
                TopControlBar(
                    uiState = uiState,
                    galleryCount = galleryVideos.size,
                    onToggleTorch = { viewModel.toggleTorch() },
                    onToggleLens = { viewModel.toggleCameraLens() },
                    onOpenGallery = { viewModel.setGalleryOpen(true) },
                    onOpenSettings = { viewModel.setSettingsOpen(true) },
                    onEnterStealth = { viewModel.toggleStealthMode(true) },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Screen-Off Recording Indicator / Active Recording Status
                RecordingStatusBanner(
                    status = uiState.status,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 68.dp)
                )

                // Bottom Control Center
                BottomControlCenter(
                    status = uiState.status,
                    lens = uiState.settings.selectedLens,
                    isAudioEnabled = uiState.settings.recordAudio,
                    onRecordToggle = {
                        if (uiState.status is RecordingStatus.Recording) {
                            viewModel.stopRecording()
                        } else {
                            viewModel.startRecording()
                        }
                    },
                    onLensSwitch = { viewModel.toggleCameraLens() },
                    onAudioToggle = { viewModel.toggleAudio() },
                    onStealthMode = { viewModel.toggleStealthMode(true) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
                )

            } else {
                // Permission Request Card
                PermissionRequiredCard(
                    onRequestPermission = {
                        permissionLauncher.launch(requiredPermissions)
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                )
            }

            // Gallery Sheet
            if (uiState.isGalleryOpen) {
                GallerySheet(
                    videos = galleryVideos,
                    onDismiss = { viewModel.setGalleryOpen(false) },
                    onDeleteVideo = { video -> viewModel.deleteVideo(video) }
                )
            }

            // Settings Sheet
            if (uiState.isSettingsOpen) {
                SettingsSheet(
                    settings = uiState.settings,
                    availableStorageMb = uiState.availableStorageMb,
                    onDismiss = { viewModel.setSettingsOpen(false) },
                    onLensSelected = { lens -> viewModel.setCameraLens(lens) },
                    onQualitySelected = { q -> viewModel.setVideoQuality(q) },
                    onToggleAudio = { viewModel.toggleAudio() },
                    onDisguiseSelected = { d -> viewModel.setStealthDisguise(d) }
                )
            }
        }
    }
}

@Composable
private fun TopControlBar(
    uiState: com.example.model.AppUiState,
    galleryCount: Int,
    onToggleTorch: () -> Unit,
    onToggleLens: () -> Unit,
    onOpenGallery: () -> Unit,
    onOpenSettings: () -> Unit,
    onEnterStealth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Group: Quick stealth cover button & Flashlight
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Stealth Mode Action Button
            IconButton(
                onClick = onEnterStealth,
                modifier = Modifier
                    .size(42.dp)
                    .background(DarkSurfaceElevated.copy(alpha = 0.85f), CircleShape)
                    .border(1.dp, StealthCyan.copy(alpha = 0.4f), CircleShape)
                    .testTag("stealth_mode_top_button")
            ) {
                Icon(
                    imageVector = Icons.Default.VisibilityOff,
                    contentDescription = "Covert Stealth Mode",
                    tint = StealthCyan,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Flashlight button (only on back camera)
            if (uiState.settings.selectedLens == CameraLens.BACK) {
                IconButton(
                    onClick = onToggleTorch,
                    modifier = Modifier
                        .size(42.dp)
                        .background(DarkSurfaceElevated.copy(alpha = 0.85f), CircleShape)
                        .testTag("torch_toggle_button")
                ) {
                    Icon(
                        imageVector = if (uiState.settings.torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flashlight",
                        tint = if (uiState.settings.torchEnabled) StealthAmber else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Lens & Quality indicator badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceElevated.copy(alpha = 0.85f))
                    .clickable { onToggleLens() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${if (uiState.settings.selectedLens == CameraLens.BACK) "REAR" else "FRONT"} • ${uiState.settings.videoQuality.title.split(" ")[0]}",
                    color = StealthCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Right Group: Gallery & Settings
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Gallery icon with count badge
            IconButton(
                onClick = onOpenGallery,
                modifier = Modifier
                    .size(42.dp)
                    .background(DarkSurfaceElevated.copy(alpha = 0.85f), CircleShape)
                    .testTag("gallery_button")
            ) {
                BadgedBox(
                    badge = {
                        if (galleryCount > 0) {
                            Badge(
                                containerColor = StealthCyan,
                                contentColor = Color.Black
                            ) {
                                Text(text = "$galleryCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Gallery",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Settings button
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(42.dp)
                    .background(DarkSurfaceElevated.copy(alpha = 0.85f), CircleShape)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun RecordingStatusBanner(
    status: RecordingStatus,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        when (status) {
            is RecordingStatus.Recording -> {
                val infiniteTransition = rememberInfiniteTransition(label = "rec_pulse")
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseAlpha"
                )

                val minutes = status.elapsedSeconds / 60
                val seconds = status.elapsedSeconds % 60
                val formattedTime = String.format("%02d:%02d", minutes, seconds)

                val mb = status.recordedBytes / (1024.0 * 1024.0)
                val formattedMb = String.format("%.1f MB", mb)

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StealthRed.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(StealthRed.copy(alpha = pulseAlpha), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "REC  $formattedTime",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 15.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "($formattedMb)",
                                color = Color(0xFFA0A8B8),
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                tint = StealthGreen,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Screen-off recording active",
                                fontSize = 10.sp,
                                color = StealthGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            is RecordingStatus.Initializing -> {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f))
                ) {
                    Text(
                        text = "Preparing Camera & Gallery...",
                        color = StealthCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            else -> {
                // Subtle Screen-Off Ready Banner
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = StealthCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Screen-Off & Background Recording Enabled",
                            color = Color(0xFFE0E6ED),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomControlCenter(
    status: RecordingStatus,
    lens: CameraLens,
    isAudioEnabled: Boolean,
    onRecordToggle: () -> Unit,
    onLensSwitch: () -> Unit,
    onAudioToggle: () -> Unit,
    onStealthMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRecording = status is RecordingStatus.Recording

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Quick Actions Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Stealth Mode Cover Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onStealthMode,
                    modifier = Modifier
                        .size(52.dp)
                        .background(DarkSurfaceElevated.copy(alpha = 0.9f), CircleShape)
                        .border(1.5.dp, StealthCyan.copy(alpha = 0.5f), CircleShape)
                        .testTag("stealth_mode_bottom_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = "Stealth Disguise Screen",
                        tint = StealthCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Stealth Mode",
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }

            // 2. Large Central Shutter / Stop Button
            ShutterButton(
                isRecording = isRecording,
                onClick = onRecordToggle
            )

            // 3. Camera Lens Switcher (Front/Back)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onLensSwitch,
                    modifier = Modifier
                        .size(52.dp)
                        .background(DarkSurfaceElevated.copy(alpha = 0.9f), CircleShape)
                        .border(1.5.dp, BorderSubtle, CircleShape)
                        .testTag("switch_camera_lens_button")
                ) {
                    Icon(
                        imageVector = if (lens == CameraLens.BACK) Icons.Default.CameraRear else Icons.Default.CameraFront,
                        contentDescription = "Switch Camera Lens",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (lens == CameraLens.BACK) "Back Lens" else "Front Lens",
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Power button screen-off tip
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.45f))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = null,
                tint = Color(0xFFA0A8B8),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isRecording) "Recording! You can press Power button to lock screen" else "Press Power button to turn off screen anytime during recording",
                fontSize = 10.sp,
                color = Color(0xFFC0CAD8)
            )
        }
    }
}

@Composable
private fun ShutterButton(
    isRecording: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shutter_glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    Box(
        modifier = Modifier.size(88.dp),
        contentAlignment = Alignment.Center
    ) {
        // Pulsing glow ring when recording
        if (isRecording) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .scale(glowScale)
                    .background(StealthRedGlow, CircleShape)
            )
        }

        // Outer Ring
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color.Transparent, CircleShape)
                .border(
                    width = 4.dp,
                    color = if (isRecording) StealthRed else Color.White,
                    shape = CircleShape
                )
                .clickable { onClick() }
                .testTag("shutter_button"),
            contentAlignment = Alignment.Center
        ) {
            // Inner Shape (Circle when idle, Square when recording)
            if (isRecording) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(StealthRed, RoundedCornerShape(6.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .background(StealthRed, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun PermissionRequiredCard(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(StealthCyan.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = StealthCyan,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Camera & Audio Access Needed",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "To record background videos and automatically save them to your gallery with screen-off capabilities, please grant Camera and Microphone permissions.",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = StealthCyan),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("grant_permissions_button")
            ) {
                Text(
                    text = "Grant Permissions",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

private val BorderSubtle = Color(0x33FFFFFF)
