package com.worthkeeping.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_sessions")
data class ScanSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val completedAt: Long?,
    val totalImages: Int,
    val scannedImages: Int,
    val status: String,
    val errors: String?,
)
