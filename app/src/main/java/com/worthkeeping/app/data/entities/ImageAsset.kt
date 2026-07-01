package com.worthkeeping.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.worthkeeping.app.scanner.MediaSourceType

@Entity(tableName = "image_assets")
data class ImageAsset(
    @PrimaryKey val mediaStoreId: Long,
    val uriString: String,
    val displayName: String,
    val mimeType: String,
    val width: Int,
    val height: Int,
    val sizeBytes: Long,
    val dateTaken: Long,
    val dateModified: Long,
    val bucketName: String,
    val relativePath: String,
    val orientation: Int,
    val sourceType: MediaSourceType,
)
