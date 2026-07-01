package com.worthkeeping.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "review_decisions")
data class ReviewDecision(
    @PrimaryKey val imageId: Long, // References ImageAsset.mediaStoreId
    val selectedAsKeeper: Boolean,
    val manuallyChanged: Boolean,
    val userDecisionAt: Long,
)
