package com.worthkeeping.app.scanner

import com.worthkeeping.app.data.entities.ImageAnalysis
import com.worthkeeping.app.data.entities.ImageAsset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryHelperTest {

    private fun createAsset(bucket: String, isScreenshot: Boolean = false): ImageAsset {
        return ImageAsset(
            mediaStoreId = 1,
            uriString = "uri",
            displayName = "test",
            mimeType = "image/jpeg",
            width = 100,
            height = 100,
            sizeBytes = 100,
            dateTaken = 100,
            dateModified = 100,
            bucketName = bucket,
            relativePath = "Pictures/",
            orientation = 0,
            sourceType = if (isScreenshot) MediaSourceType.Screenshots else MediaSourceType.Camera
        )
    }

    private fun createAnalysis(
        hasFace: Boolean = false,
        sensitive: String? = null,
        labels: String? = null,
        isScreenshot: Boolean = false,
        blurScore: Float? = 100f
    ): ImageAnalysis {
        val finalLabels = mutableListOf<String>()
        if (hasFace) finalLabels.add("Face")
        if (labels != null) finalLabels.add(labels)

        return ImageAnalysis(
            imageId = 1,
            blurScore = blurScore,
            duplicateGroupId = null,
            perceptualHash = null,
            isScreenshot = isScreenshot,
            detectedLabels = if (finalLabels.isEmpty()) null else finalLabels.joinToString(","),
            sensitiveFlags = sensitive,
            keeperScore = null,
            clutterReasons = null
        )
    }

    @Test
    fun `protected folders are always keepers`() {
        val asset = createAsset(bucket = "Important Docs")
        val result = CategoryHelper.analyzeKeepStatusAndCategory(asset, null, null)
        
        assertTrue(result.isKeeper)
        assertTrue(result.isDefaultKeeper)
    }

    @Test
    fun `sensitive items are keepers`() {
        val asset = createAsset(bucket = "Camera")
        val analysis = createAnalysis(sensitive = "Sensitive")
        val result = CategoryHelper.analyzeKeepStatusAndCategory(asset, analysis, null)
        
        assertTrue(result.isKeeper)
        assertTrue(result.reasons.contains("Sensitive"))
    }

    @Test
    fun `blurry photos are clutter`() {
        val asset = createAsset(bucket = "Camera")
        // Even with a face, blur overrides
        val analysis = createAnalysis(hasFace = true, blurScore = 50f)
        val result = CategoryHelper.analyzeKeepStatusAndCategory(asset, analysis, null)
        
        assertFalse(result.isKeeper)
        assertTrue(result.reasons.contains("Blurry"))
    }

    @Test
    fun `regular non-face photos default to clutter`() {
        val asset = createAsset(bucket = "Camera")
        val analysis = createAnalysis(hasFace = false, isScreenshot = false)
        val result = CategoryHelper.analyzeKeepStatusAndCategory(asset, analysis, null)
        
        assertFalse("Regular photos without face should default to clutter", result.isKeeper)
    }

    @Test
    fun `screenshots without ocr hit are clutter`() {
        val asset = createAsset(bucket = "Screenshots", isScreenshot = true)
        val analysis = createAnalysis(isScreenshot = true)
        val result = CategoryHelper.analyzeKeepStatusAndCategory(asset, analysis, null)
        
        assertFalse(result.isKeeper)
    }

    @Test
    fun `screenshots with document OCR are promoted to keepers`() {
        val asset = createAsset(bucket = "Screenshots", isScreenshot = true)
        val analysis = createAnalysis(isScreenshot = true, labels = "Receipt/Order")
        val result = CategoryHelper.analyzeKeepStatusAndCategory(asset, analysis, null)
        
        assertTrue(result.isKeeper)
        assertEquals("Receipts & Orders", result.folderName)
    }
}
