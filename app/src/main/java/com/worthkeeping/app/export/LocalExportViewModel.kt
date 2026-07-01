package com.worthkeeping.app.export

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.worthkeeping.app.data.WorthKeepingDatabase
import com.worthkeeping.app.data.entities.ImageAsset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ExportItem(
    val asset: ImageAsset,
    val folderName: String
)

data class ExportProgressState(
    val isExporting: Boolean = false,
    val totalItems: Int = 0,
    val exportedItems: Int = 0,
    val currentFile: String = "",
    val error: String? = null,
    val isComplete: Boolean = false
)

class LocalExportViewModel(application: Application) : AndroidViewModel(application) {
    private val db = WorthKeepingDatabase.getDatabase(application)
    private val imageDao = db.imageDao()
    private val analysisDao = db.imageAnalysisDao()
    private val decisionDao = db.reviewDecisionDao()
    private val exporter = LocalExporter(application.applicationContext)
    
    private val _uiState = MutableStateFlow(ExportProgressState())
    val uiState: StateFlow<ExportProgressState> = _uiState.asStateFlow()

    fun startExport(treeUri: Uri) {
        if (_uiState.value.isExporting) return
        
        viewModelScope.launch {
            try {
                _uiState.value = ExportProgressState(isExporting = true)
                
                val images = imageDao.getAllImagesFlow().first()
                val analyses = analysisDao.getAllAnalysesFlow().first().associateBy { it.imageId }
                val decisions = decisionDao.getAllDecisionsFlow().first().associateBy { it.imageId }
                
                val seenDuplicateGroups = mutableSetOf<String>()
                val exportItems = mutableListOf<ExportItem>()
                
                for (image in images) {
                    val analysis = analyses[image.mediaStoreId]
                    val decision = decisions[image.mediaStoreId]
                    val result = com.worthkeeping.app.scanner.CategoryHelper.analyzeKeepStatusAndCategory(
                        asset = image,
                        analysis = analysis,
                        decision = decision,
                        seenDuplicateGroups = seenDuplicateGroups
                    )
                    
                    if (result.isKeeper) {
                        exportItems.add(ExportItem(image, result.folderName))
                    }
                }
                
                _uiState.value = ExportProgressState(
                    isExporting = true,
                    totalItems = exportItems.size,
                    exportedItems = 0
                )
                
                exporter.exportItems(treeUri, exportItems) { count, fileName ->
                    _uiState.value = _uiState.value.copy(
                        exportedItems = count,
                        currentFile = fileName
                    )
                }
                
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    isComplete = true
                )
            } catch (e: Exception) {
                _uiState.value = ExportProgressState(
                    isExporting = false,
                    error = e.message ?: "An error occurred during export"
                )
            }
        }
    }
}
