package com.example.model

enum class CameraLens {
    BACK,
    FRONT
}

enum class VideoQuality(val title: String, val description: String) {
    UHD_4K("4K UHD", "Highest quality (2160p)"),
    FHD_1080P("1080p FHD", "Full High Definition"),
    HD_720P("720p HD", "Standard High Definition"),
    SD_480P("480p SD", "Space saving standard quality")
}

enum class StealthDisguise(val title: String, val iconName: String) {
    BLACKOUT("True Blackout Screen", "DarkMode"),
    CALCULATOR("Covert Calculator", "Calculate"),
    DIGITAL_CLOCK("Desk Clock", "Schedule")
}

data class RecorderSettings(
    val selectedLens: CameraLens = CameraLens.BACK,
    val videoQuality: VideoQuality = VideoQuality.FHD_1080P,
    val recordAudio: Boolean = true,
    val stealthDisguise: StealthDisguise = StealthDisguise.BLACKOUT,
    val keepScreenOffRecording: Boolean = true,
    val saveFolder: String = "DCIM/Camera",
    val torchEnabled: Boolean = false
)
