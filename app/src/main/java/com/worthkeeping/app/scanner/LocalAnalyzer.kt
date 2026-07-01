package com.worthkeeping.app.scanner

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.util.Size
import com.worthkeeping.app.data.entities.ImageAnalysis
import com.worthkeeping.app.data.entities.ImageAsset
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LocalAnalyzer(
    private val contentResolver: ContentResolver
) {
    // Configure face detector for performance (we only need to know IF a face exists)
    private val faceDetectorOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setMinFaceSize(0.15f) // Detect faces that are at least 15% of the image
        .build()

    private val faceDetector = FaceDetection.getClient(faceDetectorOptions)

    suspend fun analyze(asset: ImageAsset): ImageAnalysis = withContext(Dispatchers.Default) {
        var blurScore = 100f
        var pHash: String? = null
        var hasFace = false
        val isScreenshot = asset.sourceType == MediaSourceType.Screenshots

        try {
            val uri = Uri.parse(asset.uriString)
            // Load a small thumbnail for analysis. 256x256 is efficient.
            val thumbnail = contentResolver.loadThumbnail(uri, Size(256, 256), null)
            
            // 1. Blur detection (Variance of Laplacian)
            blurScore = calculateBlurScore(thumbnail)
            
            // 2. Perceptual Hash (dHash)
            pHash = calculateDHash(thumbnail)
            
            // 3. Face Detection (replaces broken meme detection)
            // Only run on non-screenshot images to save processing time
            if (!isScreenshot) {
                try {
                    val image = InputImage.fromBitmap(thumbnail, 0)
                    val faces = faceDetector.process(image).await()
                    hasFace = faces.isNotEmpty()
                } catch (e: Exception) {
                    // Ignore face detection failure
                }
            }
            
            thumbnail.recycle()
        } catch (e: Exception) {
            // If we fail to load thumbnail, we just leave defaults
            blurScore = 50f // Middle ground
        }

        ImageAnalysis(
            imageId = asset.mediaStoreId,
            blurScore = blurScore,
            duplicateGroupId = null, // Will group later
            perceptualHash = pHash,
            isScreenshot = isScreenshot,
            detectedLabels = if (hasFace) "Face" else null,
            sensitiveFlags = null,
            keeperScore = null,
            clutterReasons = null
        )
    }

    private fun calculateBlurScore(bitmap: Bitmap): Float {
        val width = bitmap.width
        val height = bitmap.height
        
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val grayScale = IntArray(width * height)
        for (i in pixels.indices) {
            val color = pixels[i]
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            grayScale[i] = (r * 299 + g * 587 + b * 114) / 1000
        }
        
        var sum = 0.0
        var count = 0
        
        val laplacian = IntArray(width * height)
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val top = grayScale[(y - 1) * width + x]
                val bottom = grayScale[(y + 1) * width + x]
                val left = grayScale[y * width + x - 1]
                val right = grayScale[y * width + x + 1]
                val center = grayScale[y * width + x]
                
                val value = top + bottom + left + right - 4 * center
                laplacian[y * width + x] = value
                sum += value
                count++
            }
        }
        
        if (count == 0) return 0f
        
        val mean = sum / count
        var varianceSum = 0.0
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val value = laplacian[y * width + x]
                val diff = value - mean
                varianceSum += diff * diff
            }
        }
        
        val variance = varianceSum / count
        return variance.toFloat()
    }

    private fun calculateDHash(bitmap: Bitmap): String {
        val scaled = Bitmap.createScaledBitmap(bitmap, 9, 8, true)
        val width = 9
        val height = 8
        val pixels = IntArray(width * height)
        scaled.getPixels(pixels, 0, width, 0, 0, width, height)
        
        var hash = 0UL
        for (y in 0 until height) {
            for (x in 0 until width - 1) {
                val p1 = pixels[y * width + x]
                val p2 = pixels[y * width + x + 1]
                
                val g1 = ((p1 shr 16) and 0xFF) * 299 + ((p1 shr 8) and 0xFF) * 587 + (p1 and 0xFF) * 114
                val g2 = ((p2 shr 16) and 0xFF) * 299 + ((p2 shr 8) and 0xFF) * 587 + (p2 and 0xFF) * 114
                
                hash = hash shl 1
                if (g1 > g2) {
                    hash = hash or 1UL
                }
            }
        }
        scaled.recycle()
        return hash.toString(16).padStart(16, '0')
    }

    fun assignDuplicateGroups(analyses: List<ImageAnalysis>): List<ImageAnalysis> {
        val updated = analyses.toMutableList()
        val threshold = 8 
        var nextGroupId = 1
        
        for (i in updated.indices) {
            if (updated[i].duplicateGroupId != null || updated[i].perceptualHash == null) continue
            
            val h1 = updated[i].perceptualHash!!.toULongOrNull(16) ?: continue
            val groupId = "dup_$nextGroupId"
            var foundDuplicate = false
            
            for (j in i + 1 until updated.size) {
                if (updated[j].duplicateGroupId != null || updated[j].perceptualHash == null) continue
                
                val h2 = updated[j].perceptualHash!!.toULongOrNull(16) ?: continue
                val distance = (h1 xor h2).countOneBits()
                if (distance <= threshold) {
                    updated[j] = updated[j].copy(duplicateGroupId = groupId)
                    foundDuplicate = true
                }
            }
            if (foundDuplicate) {
                updated[i] = updated[i].copy(duplicateGroupId = groupId)
                nextGroupId++
            }
        }
        return updated
    }
}
