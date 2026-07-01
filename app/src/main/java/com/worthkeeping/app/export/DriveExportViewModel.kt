package com.worthkeeping.app.export

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.worthkeeping.app.data.WorthKeepingDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.net.Uri
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DriveExportViewModel(application: Application) : AndroidViewModel(application) {
    private val db = WorthKeepingDatabase.getDatabase(application)
    private val imageDao = db.imageDao()
    private val analysisDao = db.imageAnalysisDao()
    private val decisionDao = db.reviewDecisionDao()
    
    private val _uiState = MutableStateFlow(ExportProgressState())
    val uiState: StateFlow<ExportProgressState> = _uiState.asStateFlow()

    fun startDriveExport(account: GoogleSignInAccount) {
        if (_uiState.value.isExporting) return
        
        viewModelScope.launch {
            try {
                _uiState.value = ExportProgressState(isExporting = true)
                val exporter = DriveExporter(getApplication(), account)
                
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
                
                // 1. Create root folder
                val timestamp = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val rootFolderName = "WorthKeeping Backup ($timestamp)"
                val rootFolderId = exporter.createFolder(rootFolderName)
                
                // 2. Group items by folder
                val itemsByCategory = exportItems.groupBy { it.folderName }
                val categoryFolderIds = mutableMapOf<String, String>()
                
                // 3. Create subfolders and upload
                var count = 0
                for ((category, items) in itemsByCategory) {
                    val categoryFolderId = exporter.createFolder(category, rootFolderId)
                    categoryFolderIds[category] = categoryFolderId
                    
                    for (item in items) {
                        val uri = Uri.parse(item.asset.uriString)
                        val fileName = item.asset.displayName.takeIf { it.isNotBlank() } ?: "image_${item.asset.mediaStoreId}.jpg"
                        
                        _uiState.value = _uiState.value.copy(
                            currentFile = fileName
                        )
                        
                        exporter.uploadFile(uri, fileName, categoryFolderId)
                        count++
                        
                        _uiState.value = _uiState.value.copy(
                            exportedItems = count
                        )
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    isComplete = true
                )
            } catch (e: Exception) {
                _uiState.value = ExportProgressState(
                    isExporting = false,
                    error = e.message ?: "An error occurred during Google Drive export"
                )
            }
        }
    }
}
