package com.example.ui.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.CameraRear
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VideoSettings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CameraLens
import com.example.model.RecorderSettings
import com.example.model.StealthDisguise
import com.example.model.VideoQuality
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.StealthCyan
import com.example.ui.theme.StealthGreen
import com.example.ui.theme.StealthRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsSheet(
    settings: RecorderSettings,
    availableStorageMb: Long,
    onDismiss: () -> Unit,
    onLensSelected: (CameraLens) -> Unit,
    onQualitySelected: (VideoQuality) -> Unit,
    onToggleAudio: () -> Unit,
    onDisguiseSelected: (StealthDisguise) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = ObsidianBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(DarkSurfaceElevated, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = StealthCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Recorder Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .background(DarkSurfaceVariant, CircleShape)
                        .testTag("close_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Screen-Off Recording Explainer Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16243A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(StealthCyan.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                tint = StealthCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Screen-Off Recording Active",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = StealthCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "When recording is started, you can press your phone's physical power button to turn off the screen or switch apps. The background foreground service and wake lock ensure continuous video recording until you tap stop.",
                                fontSize = 12.sp,
                                color = Color(0xFFB0C4DE),
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                // Section 1: Camera Lens
                SettingsSection(title = "CAMERA LENS SELECTION") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LensOptionCard(
                            title = "Back Camera",
                            subtitle = "Main lens with flash support",
                            icon = Icons.Default.CameraRear,
                            isSelected = settings.selectedLens == CameraLens.BACK,
                            modifier = Modifier.weight(1f)
                        ) {
                            onLensSelected(CameraLens.BACK)
                        }

                        LensOptionCard(
                            title = "Front Camera",
                            subtitle = "Selfie / Front-facing lens",
                            icon = Icons.Default.CameraFront,
                            isSelected = settings.selectedLens == CameraLens.FRONT,
                            modifier = Modifier.weight(1f)
                        ) {
                            onLensSelected(CameraLens.FRONT)
                        }
                    }
                }

                // Section 2: Stealth Disguise Screen
                SettingsSection(title = "STEALTH COVER DISGUISE") {
                    Text(
                        text = "Choose what appears when you tap the Stealth Mode icon to disguise activity:",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    DisguiseOption(
                        title = "True Blackout OLED",
                        description = "100% black screen looking turned off. Double tap to return.",
                        icon = Icons.Default.DarkMode,
                        isSelected = settings.stealthDisguise == StealthDisguise.BLACKOUT
                    ) {
                        onDisguiseSelected(StealthDisguise.BLACKOUT)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    DisguiseOption(
                        title = "Covert Calculator",
                        description = "Working calculator mockup. Enter '5555=' to return.",
                        icon = Icons.Default.Calculate,
                        isSelected = settings.stealthDisguise == StealthDisguise.CALCULATOR
                    ) {
                        onDisguiseSelected(StealthDisguise.CALCULATOR)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    DisguiseOption(
                        title = "Ambient Desk Clock",
                        description = "Shows live time & battery level. Double tap to return.",
                        icon = Icons.Default.Schedule,
                        isSelected = settings.stealthDisguise == StealthDisguise.DIGITAL_CLOCK
                    ) {
                        onDisguiseSelected(StealthDisguise.DIGITAL_CLOCK)
                    }
                }

                // Section 3: Video Quality
                SettingsSection(title = "VIDEO RECORDING QUALITY") {
                    VideoQuality.values().forEachIndexed { index, quality ->
                        if (index > 0) Spacer(modifier = Modifier.height(8.dp))
                        QualityOption(
                            quality = quality,
                            isSelected = settings.videoQuality == quality
                        ) {
                            onQualitySelected(quality)
                        }
                    }
                }

                // Section 4: Audio & Storage
                SettingsSection(title = "AUDIO & GALLERY STORAGE") {
                    // Audio toggle row
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (settings.recordAudio) Icons.Default.Mic else Icons.Default.MicOff,
                                    contentDescription = null,
                                    tint = if (settings.recordAudio) StealthGreen else TextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Record Microphone Audio",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (settings.recordAudio) "Audio will be captured with video" else "Silent muted video recording",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Switch(
                                checked = settings.recordAudio,
                                onCheckedChange = { onToggleAudio() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = StealthGreen
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Storage info card
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderSpecial,
                                contentDescription = null,
                                tint = StealthCyan,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Auto-Saved to Gallery (DCIM)",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Available Device Storage: ${availableStorageMb / 1024.0} GB ($availableStorageMb MB)",
                                    fontSize = 11.sp,
                                    color = StealthCyan
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = StealthCyan,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        content()
    }
}

@Composable
private fun LensOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DarkSurfaceElevated else DarkSurfaceVariant
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, StealthCyan) else null,
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) StealthCyan else TextSecondary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isSelected) TextPrimary else TextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun DisguiseOption(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DarkSurfaceElevated else DarkSurfaceVariant
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, StealthCyan) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) StealthCyan else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = if (isSelected) TextPrimary else TextSecondary
                    )
                    Text(
                        text = description,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = StealthCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun QualityOption(
    quality: VideoQuality,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DarkSurfaceElevated else DarkSurfaceVariant
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, StealthCyan) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = quality.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (isSelected) TextPrimary else TextSecondary
                )
                Text(
                    text = quality.description,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = StealthCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
