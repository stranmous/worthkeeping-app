package com.worthkeeping.app.export

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.worthkeeping.app.data.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStream

class LocalExporter(private val context: Context) {
    private val prefs = UserPreferencesRepository(context)
    suspend fun exportItems(
        treeUri: Uri,
        items: List<ExportItem>,
        onProgress: suspend (exportedCount: Int, currentFileName: String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val rootDoc = DocumentFile.fromTreeUri(context, treeUri)
            ?: throw Exception("Cannot access selected directory.")
        val folderNameSetting = prefs.localExportFolderName.first()
        
        // Create base folder
        var baseFolder = rootDoc.findFile(folderNameSetting)
        if (baseFolder == null) {
            baseFolder = rootDoc.createDirectory(folderNameSetting)
        }
        if (baseFolder == null) throw Exception("Failed to create $folderNameSetting directory.")
        
        val folderCache = mutableMapOf<String, DocumentFile>()
        
        items.forEachIndexed { index, item ->
            val folderName = item.folderName
            var targetDir = folderCache[folderName]
            if (targetDir == null) {
                targetDir = baseFolder.findFile(folderName) ?: baseFolder.createDirectory(folderName)
                if (targetDir != null) {
                    folderCache[folderName] = targetDir
                }
            }
            
            if (targetDir != null) {
                // If file already exists, avoid crashing by appending timestamp or just letting create file handle it?
                // DocumentFile.createFile automatically adds (1) if name exists.
                val existingFile = targetDir.findFile(item.asset.displayName)
                if (existingFile == null) {
                    val newFile = targetDir.createFile(item.asset.mimeType.ifEmpty { "image/*" }, item.asset.displayName)
                    if (newFile != null) {
                        try {
                            context.contentResolver.openInputStream(Uri.parse(item.asset.uriString))?.use { input ->
                                context.contentResolver.openOutputStream(newFile.uri)?.use { output ->
                                    input.copyTo(output)
                                }
                            }
                        } catch (e: Exception) {
                            // Skip file if it fails to copy
                        }
                    }
                }
            }
            onProgress(index + 1, item.asset.displayName)
        }
        
        // Write manifest
        try {
            val manifest = JSONObject()
            manifest.put("exportDate", System.currentTimeMillis())
            manifest.put("totalExported", items.size)
            
            val manifestFile = baseFolder.findFile("manifest.json") ?: baseFolder.createFile("application/json", "manifest.json")
            if (manifestFile != null) {
                context.contentResolver.openOutputStream(manifestFile.uri, "w")?.use { output ->
                    output.write(manifest.toString(4).toByteArray())
                }
            }
        } catch (e: Exception) {
            // Ignore manifest failure
        }
    }
}
