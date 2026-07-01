package com.worthkeeping.app.review

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.worthkeeping.app.data.WorthKeepingDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.worthkeeping.app.data.entities.ImageAsset
import com.worthkeeping.app.data.entities.ReviewDecision

data class ReviewImageItem(
    val asset: ImageAsset,
    val isKeeper: Boolean,
    val clutterReasons: List<String> = emptyList(),
)

data class ReviewGroupUiModel(
    val bucketName: String,
    val totalImages: Int,
    val keepersCount: Int,
    val clutterCount: Int,
    val sampleThumbnails: List<String>,
    val images: List<ReviewImageItem>,
)

data class ReviewUiState(
    val isLoading: Boolean = true,
    val groups: List<ReviewGroupUiModel> = emptyList(),
)

class ReviewViewModel(application: Application) : AndroidViewModel(application) {
    private val db = WorthKeepingDatabase.getDatabase(application)
    private val imageDao = db.imageDao()
    private val decisionDao = db.reviewDecisionDao()

    val uiState: StateFlow<ReviewUiState> = combine(
        imageDao.getAllImagesFlow(),
        db.imageAnalysisDao().getAllAnalysesFlow(),
        decisionDao.getAllDecisionsFlow()
    ) { images, analyses, decisions ->
        val decisionMap = decisions.associateBy { it.imageId }
        val analysisMap = analyses.associateBy { it.imageId }
        
        val seenDuplicateGroups = mutableSetOf<String>()
        val imageItems = images.map { image ->
            val analysis = analysisMap[image.mediaStoreId]
            val decision = decisionMap[image.mediaStoreId]
            val result = com.worthkeeping.app.scanner.CategoryHelper.analyzeKeepStatusAndCategory(
                asset = image,
                analysis = analysis,
                decision = decision,
                seenDuplicateGroups = seenDuplicateGroups
            )
            
            ReviewImageItem(image, result.isKeeper, result.reasons)
        }
        
        val groups = imageItems.groupBy { it.asset.bucketName }.map { (bucket, bucketImages) ->
            var keepers = 0
            var clutter = 0
            
            bucketImages.forEach { item ->
                if (item.isKeeper) keepers++ else clutter++
            }
            
            ReviewGroupUiModel(
                bucketName = bucket,
                totalImages = bucketImages.size,
                keepersCount = keepers,
                clutterCount = clutter,
                sampleThumbnails = bucketImages.take(5).map { it.asset.uriString },
                images = bucketImages
            )
        }.sortedByDescending { it.totalImages }
        
        ReviewUiState(
            isLoading = false,
            groups = groups
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReviewUiState(isLoading = true)
    )

    fun toggleKeeperStatus(imageId: Long, currentStatus: Boolean) {
        viewModelScope.launch {
            val decision = ReviewDecision(
                imageId = imageId,
                selectedAsKeeper = !currentStatus,
                manuallyChanged = true,
                userDecisionAt = System.currentTimeMillis()
            )
            decisionDao.insertOrUpdate(decision)
        }
    }
}
