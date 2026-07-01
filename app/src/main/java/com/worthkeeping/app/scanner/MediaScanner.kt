package com.worthkeeping.app.scanner

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.worthkeeping.app.data.WorthKeepingDatabase
import com.worthkeeping.app.data.entities.ImageAsset
import com.worthkeeping.app.data.entities.ScanSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.ceil

class MediaScanner(
    private val context: Context,
) {
    private val db = WorthKeepingDatabase.getDatabase(context)
    private val imageDao = db.imageDao()
    private val sessionDao = db.scanSessionDao()
    private val prefs = com.worthkeeping.app.data.UserPreferencesRepository(context)
    
    suspend fun estimateImages(hasLimitedAccess: Boolean): ScanEstimate = withContext(Dispatchers.IO) {
        val skippedFolders = prefs.skippedFolders.first()
        val summaries = linkedMapOf<MediaSourceType, MutableSummary>()
        val resolver = context.contentResolver
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.RELATIVE_PATH,
        )

        resolver.querySafely(collection, projection)?.use { cursor ->
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)

            while (cursor.moveToNext()) {
                val bucket = cursor.getStringOrEmpty(bucketColumn)
                if (skippedFolders.any { it.equals(bucket, ignoreCase = true) }) continue
                
                val size = cursor.getLongOrZero(sizeColumn)
                val path = cursor.getStringOrEmpty(pathColumn)
                val type = classifySource(bucket, path)
                val summary = summaries.getOrPut(type) { MutableSummary(type) }
                summary.imageCount += 1
                summary.sizeBytes += size
            }
        }

        val sources = MediaSourceType.entries.mapNotNull { type ->
            summaries[type]?.toImmutable()
        }
        val totalImages = sources.sumOf { it.imageCount }
        val totalSizeBytes = sources.sumOf { it.sizeBytes }

        ScanEstimate(
            totalImages = totalImages,
            totalSizeBytes = totalSizeBytes,
            estimatedMinutes = estimateMinutes(totalImages),
            sources = sources,
            hasLimitedAccess = hasLimitedAccess,
        )
    }

    suspend fun scanImages(
        hasLimitedAccess: Boolean,
        onProgress: suspend (MediaScanProgress) -> Unit,
    ): ScanEstimate = withContext(Dispatchers.IO) {
        val skippedFolders = prefs.skippedFolders.first()
        val summaries = linkedMapOf<MediaSourceType, MutableSummary>()
        val resolver = context.contentResolver
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.RELATIVE_PATH,
            MediaStore.Images.Media.ORIENTATION,
        )

        val initialEstimate = estimateImages(hasLimitedAccess)
        
        val sessionId = sessionDao.insert(
            ScanSession(
                startedAt = System.currentTimeMillis(),
                completedAt = null,
                totalImages = initialEstimate.totalImages,
                scannedImages = 0,
                status = "RUNNING",
                errors = null
            )
        )
        
        imageDao.clearAll()

        onProgress(
            MediaScanProgress(
                isScanning = true,
                totalImages = initialEstimate.totalImages,
                scannedImages = 0,
                sources = initialEstimate.sources,
            ),
        )

        var scannedImages = 0
        val batch = mutableListOf<ImageAsset>()
        
        resolver.querySafely(collection, projection)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val takenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val modColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)
            val orientColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION)

            while (cursor.moveToNext()) {
                val bucket = cursor.getStringOrEmpty(bucketColumn)
                if (skippedFolders.any { it.equals(bucket, ignoreCase = true) }) continue
                
                val mediaStoreId = cursor.getLongOrZero(idColumn)
                val size = cursor.getLongOrZero(sizeColumn)
                val path = cursor.getStringOrEmpty(pathColumn)
                val type = classifySource(bucket, path)
                
                val asset = ImageAsset(
                    mediaStoreId = mediaStoreId,
                    uriString = "$collection/$mediaStoreId",
                    displayName = cursor.getStringOrEmpty(nameColumn),
                    mimeType = cursor.getStringOrEmpty(mimeColumn),
                    width = cursor.getIntOrZero(widthColumn),
                    height = cursor.getIntOrZero(heightColumn),
                    sizeBytes = size,
                    dateTaken = cursor.getLongOrZero(takenColumn),
                    dateModified = cursor.getLongOrZero(modColumn),
                    bucketName = bucket,
                    relativePath = path,
                    orientation = cursor.getIntOrZero(orientColumn),
                    sourceType = type,
                )
                batch.add(asset)
                
                val summary = summaries.getOrPut(type) { MutableSummary(type) }
                summary.imageCount += 1
                summary.sizeBytes += size
                scannedImages += 1

                if (batch.size >= 500) {
                    imageDao.insertAll(batch)
                    batch.clear()
                }

                if (scannedImages == 1 || scannedImages % 250 == 0 || scannedImages == initialEstimate.totalImages) {
                    onProgress(
                        MediaScanProgress(
                            isScanning = true,
                            totalImages = initialEstimate.totalImages,
                            scannedImages = scannedImages,
                            currentSource = type,
                            sources = summaries.toSourceList(),
                        ),
                    )
                }
            }
            if (batch.isNotEmpty()) {
                imageDao.insertAll(batch)
            }
        }
        
        val finalSources = imageDao.getSourceSummaries()
        
        sessionDao.update(
            ScanSession(
                id = sessionId,
                startedAt = System.currentTimeMillis(), 
                completedAt = null,
                totalImages = scannedImages,
                scannedImages = scannedImages,
                status = "ANALYZING",
                errors = null
            )
        )

        onProgress(
            MediaScanProgress(
                isScanning = false,
                isAnalyzing = true,
                totalImages = scannedImages,
                scannedImages = scannedImages,
                analyzedImages = 0,
                sources = finalSources,
            )
        )

        val allAssets = imageDao.getAllImages()
        val analyzer = LocalAnalyzer(context.contentResolver)
        val ocrAnalyzer = OcrAnalyzer(context)
        val analysisDao = db.imageAnalysisDao()
        val analyses = mutableListOf<com.worthkeeping.app.data.entities.ImageAnalysis>()
        
        allAssets.forEachIndexed { index, asset ->
            val baseAnalysis = analyzer.analyze(asset)
            val ocrResult = ocrAnalyzer.analyzeText(asset)
            
            val finalAnalysis = if (ocrResult != null) {
                val mergedLabels = listOfNotNull(baseAnalysis.detectedLabels, ocrResult.labels)
                    .joinToString(",")
                    .takeIf { it.isNotEmpty() }
                
                baseAnalysis.copy(
                    sensitiveFlags = ocrResult.sensitiveFlags,
                    detectedLabels = mergedLabels
                )
            } else {
                baseAnalysis
            }
            analyses.add(finalAnalysis)
            
            if (index % 50 == 0 || index == allAssets.size - 1) {
                onProgress(
                    MediaScanProgress(
                        isScanning = false,
                        isAnalyzing = true,
                        totalImages = scannedImages,
                        scannedImages = scannedImages,
                        analyzedImages = index + 1,
                        sources = finalSources,
                    )
                )
            }
        }
        
        val groupedAnalyses = analyzer.assignDuplicateGroups(analyses)
        analysisDao.insertAll(groupedAnalyses)

        sessionDao.update(
            ScanSession(
                id = sessionId,
                startedAt = System.currentTimeMillis(), 
                completedAt = System.currentTimeMillis(),
                totalImages = scannedImages,
                scannedImages = scannedImages,
                status = "COMPLETED",
                errors = null
            )
        )

        val finalEstimate = ScanEstimate(
            totalImages = scannedImages,
            totalSizeBytes = finalSources.sumOf { it.sizeBytes },
            estimatedMinutes = estimateMinutes(scannedImages),
            sources = finalSources,
            hasLimitedAccess = hasLimitedAccess,
        )
        onProgress(
            MediaScanProgress(
                isScanning = false,
                isAnalyzing = false,
                isComplete = true,
                totalImages = finalEstimate.totalImages,
                scannedImages = scannedImages,
                analyzedImages = scannedImages,
                sources = finalSources,
            ),
        )
        finalEstimate
    }

    private fun ContentResolver.querySafely(
        uri: Uri,
        projection: Array<String>,
    ) = query(
        uri,
        projection,
        null,
        null,
        "${MediaStore.Images.Media.DATE_MODIFIED} DESC",
    )

    private fun classifySource(bucket: String, path: String): MediaSourceType {
        val text = "$bucket/$path".lowercase(Locale.US)
        return when {
            "screenshot" in text || "screen_shot" in text -> MediaSourceType.Screenshots
            "camera" in text || "dcim" in text -> MediaSourceType.Camera
            "download" in text -> MediaSourceType.Downloads
            "whatsapp" in text ||
                "telegram" in text ||
                "messenger" in text ||
                "signal" in text ||
                "instagram" in text -> MediaSourceType.Messaging
            else -> MediaSourceType.Other
        }
    }

    private fun estimateMinutes(totalImages: Int): Int {
        if (totalImages == 0) return 0
        return ceil(totalImages / 4_500.0).toInt().coerceAtLeast(1)
    }
}

private fun Map<MediaSourceType, MutableSummary>.toSourceList(): List<MediaSourceSummary> {
    return MediaSourceType.entries.mapNotNull { type -> this[type]?.toImmutable() }
}

private data class MutableSummary(
    val type: MediaSourceType,
    var imageCount: Int = 0,
    var sizeBytes: Long = 0,
) {
    fun toImmutable() = MediaSourceSummary(
        type = type,
        imageCount = imageCount,
        sizeBytes = sizeBytes,
    )
}

private fun android.database.Cursor.getLongOrZero(columnIndex: Int): Long {
    return if (isNull(columnIndex)) 0L else getLong(columnIndex).coerceAtLeast(0L)
}

private fun android.database.Cursor.getIntOrZero(columnIndex: Int): Int {
    return if (isNull(columnIndex)) 0 else getInt(columnIndex).coerceAtLeast(0)
}

private fun android.database.Cursor.getStringOrEmpty(columnIndex: Int): String {
    return if (isNull(columnIndex)) "" else getString(columnIndex).orEmpty()
}
