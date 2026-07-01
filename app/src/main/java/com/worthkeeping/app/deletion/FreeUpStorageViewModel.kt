package com.worthkeeping.app.deletion

import android.app.Application
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.worthkeeping.app.data.WorthKeepingDatabase
import com.worthkeeping.app.data.entities.ImageAsset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class FreeUpStorageState(
    val isLoading: Boolean = true,
    val clutterItems: List<ImageAsset> = emptyList(),
    val selectedClutterIds: Set<Long> = emptySet(),
    val totalClutterSizeBytes: Long = 0,
    val selectedSizeBytes: Long = 0,
    val isDeleting: Boolean = false,
    val deletionComplete: Boolean = false,
    val error: String? = null
)

class FreeUpStorageViewModel(application: Application) : AndroidViewModel(application) {
    private val db = WorthKeepingDatabase.getDatabase(application)
    private val imageDao = db.imageDao()
    private val analysisDao = db.imageAnalysisDao()
    private val decisionDao = db.reviewDecisionDao()

    private val _uiState = MutableStateFlow(FreeUpStorageState())
    val uiState: StateFlow<FreeUpStorageState> = _uiState.asStateFlow()

    init {
        loadClutterStats()
    }

    private fun loadClutterStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val images = imageDao.getAllImagesFlow().first()
                val analyses = analysisDao.getAllAnalysesFlow().first().associateBy { it.imageId }
                val decisions = decisionDao.getAllDecisionsFlow().first().associateBy { it.imageId }

                val seenDuplicateGroups = mutableSetOf<String>()
                val clutterList = mutableListOf<ImageAsset>()
                var totalSize = 0L

                for (image in images) {
                    val analysis = analyses[image.mediaStoreId]
                    val decision = decisions[image.mediaStoreId]
                    val result = com.worthkeeping.app.scanner.CategoryHelper.analyzeKeepStatusAndCategory(
                        asset = image,
                        analysis = analysis,
                        decision = decision,
                        seenDuplicateGroups = seenDuplicateGroups
                    )

                    if (!result.isKeeper) {
                        clutterList.add(image)
                        totalSize += image.sizeBytes
                    }
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    clutterItems = clutterList,
                    selectedClutterIds = clutterList.map { it.mediaStoreId }.toSet(),
                    totalClutterSizeBytes = totalSize,
                    selectedSizeBytes = totalSize
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load clutter stats"
                )
            }
        }
    }

    fun toggleSelection(imageId: Long) {
        val currentSelected = _uiState.value.selectedClutterIds.toMutableSet()
        if (currentSelected.contains(imageId)) {
            currentSelected.remove(imageId)
        } else {
            currentSelected.add(imageId)
        }
        
        val newSelectedSize = _uiState.value.clutterItems
            .filter { currentSelected.contains(it.mediaStoreId) }
            .sumOf { it.sizeBytes }

        _uiState.value = _uiState.value.copy(
            selectedClutterIds = currentSelected,
            selectedSizeBytes = newSelectedSize
        )
    }

    fun getSelectedUris(): List<Uri> {
        return _uiState.value.clutterItems
            .filter { _uiState.value.selectedClutterIds.contains(it.mediaStoreId) }
            .map { Uri.parse(it.uriString) }
    }

    fun confirmDeletionSuccess() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true)
            try {
                val deletedIds = _uiState.value.selectedClutterIds.toList()
                imageDao.deleteByIds(deletedIds)
                analysisDao.deleteByIds(deletedIds)
                decisionDao.deleteByIds(deletedIds)
                
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    deletionComplete = true,
                    clutterItems = emptyList(),
                    totalClutterSizeBytes = 0,
                    selectedClutterIds = emptySet(),
                    selectedSizeBytes = 0
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    error = e.message ?: "Failed to update database after deletion"
                )
            }
        }
    }
}
