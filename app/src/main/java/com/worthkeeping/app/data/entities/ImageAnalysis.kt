package com.worthkeeping.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_analyses")
data class ImageAnalysis(
    @PrimaryKey val imageId: Long, // References ImageAsset.mediaStoreId
    val blurScore: Float?,
    val duplicateGroupId: String?,
    val perceptualHash: String?,
    val isScreenshot: Boolean,
    val detectedLabels: String?, // JSON or comma-separated
    val sensitiveFlags: String?, // JSON or comma-separated
    val keeperScore: Float?,
    val clutterReasons: String?, // JSON or comma-separated
)
