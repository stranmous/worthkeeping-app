package com.worthkeeping.app.scanner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.worthkeeping.app.data.WorthKeepingDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ResultsSummaryState(
    val isLoading: Boolean = true,
    val totalImages: Int = 0,
    val totalKeepers: Int = 0,
    val backupSizeBytes: Long = 0L,
    val savedSizeBytes: Long = 0L,
    val memoriesCount: Int = 0,
    val importantScreenshotsCount: Int = 0,
    val documentsCount: Int = 0,
    val receiptsCount: Int = 0,
    val sensitiveCount: Int = 0,
)

class ResultsSummaryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = WorthKeepingDatabase.getDatabase(application)
    private val imageDao = db.imageDao()
    private val decisionDao = db.reviewDecisionDao()

    val uiState: StateFlow<ResultsSummaryState> = combine(
        imageDao.getAllImagesFlow(),
        db.imageAnalysisDao().getAllAnalysesFlow(),
        decisionDao.getAllDecisionsFlow()
    ) { images, analyses, decisions ->
        val decisionMap = decisions.associateBy { it.imageId }
        val analysisMap = analyses.associateBy { it.imageId }
        
        val seenDuplicateGroups = mutableSetOf<String>()
        var totalKeepers = 0
        var backupSizeBytes = 0L
        var savedSizeBytes = 0L
        
        var memoriesCount = 0
        var importantScreenshotsCount = 0
        var documentsCount = 0
        var receiptsCount = 0
        var sensitiveCount = 0
        
        images.forEach { image ->
            val analysis = analysisMap[image.mediaStoreId]
            val decision = decisionMap[image.mediaStoreId]
            val result = com.worthkeeping.app.scanner.CategoryHelper.analyzeKeepStatusAndCategory(
                asset = image,
                analysis = analysis,
                decision = decision,
                seenDuplicateGroups = seenDuplicateGroups
            )
            
            if (result.isKeeper) {
                totalKeepers++
                backupSizeBytes += image.sizeBytes
            } else {
                savedSizeBytes += image.sizeBytes
            }
            
            // Categorize keepers only
            if (result.isKeeper) {
                when (result.folderName) {
                    "Sensitive" -> sensitiveCount++
                    "Receipts & Orders" -> receiptsCount++
                    "Documents" -> documentsCount++
                    "Important Screenshots" -> importantScreenshotsCount++
                    else -> memoriesCount++
                }
            }
        }
        
        ResultsSummaryState(
            isLoading = false,
            totalImages = images.size,
            totalKeepers = totalKeepers,
            backupSizeBytes = backupSizeBytes,
            savedSizeBytes = savedSizeBytes,
            memoriesCount = memoriesCount,
            importantScreenshotsCount = importantScreenshotsCount,
            documentsCount = documentsCount,
            receiptsCount = receiptsCount,
            sensitiveCount = sensitiveCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ResultsSummaryState(isLoading = true)
    )
}
