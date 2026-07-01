package com.worthkeeping.app.scanner

enum class MediaSourceType {
    Camera,
    Screenshots,
    Downloads,
    Messaging,
    Other,
}

data class MediaSourceSummary(
    val type: MediaSourceType,
    val imageCount: Int,
    val sizeBytes: Long,
)

data class ScanEstimate(
    val totalImages: Int = 0,
    val totalSizeBytes: Long = 0,
    val estimatedMinutes: Int = 0,
    val sources: List<MediaSourceSummary> = emptyList(),
    val hasLimitedAccess: Boolean = false,
)

data class MediaScanProgress(
    val isScanning: Boolean = false,
    val isAnalyzing: Boolean = false,
    val isComplete: Boolean = false,
    val totalImages: Int = 0,
    val scannedImages: Int = 0,
    val analyzedImages: Int = 0,
    val currentSource: MediaSourceType? = null,
    val sources: List<MediaSourceSummary> = emptyList(),
)
