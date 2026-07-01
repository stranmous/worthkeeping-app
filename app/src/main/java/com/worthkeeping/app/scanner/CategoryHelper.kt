package com.worthkeeping.app.scanner

import com.worthkeeping.app.data.entities.ImageAnalysis
import com.worthkeeping.app.data.entities.ImageAsset
import com.worthkeeping.app.data.entities.ReviewDecision

data class CategoryResult(
    val isKeeper: Boolean,
    val isDefaultKeeper: Boolean,
    val folderName: String,
    val reasons: List<String>
)

object CategoryHelper {

    // Folder names (case-insensitive) that force ALL images to be keepers
    private val protectedFolderPatterns = listOf(
        "important", "imp", "do not delete", "dont delete",
        "don't delete", "keep", "do_not_delete", "dont_delete"
    )

    private fun isProtectedFolder(bucketName: String): Boolean {
        val lower = bucketName.lowercase()
        return protectedFolderPatterns.any { lower.contains(it) }
    }

    fun analyzeKeepStatusAndCategory(
        asset: ImageAsset,
        analysis: ImageAnalysis?,
        decision: ReviewDecision?,
        seenDuplicateGroups: MutableSet<String>? = null
    ): CategoryResult {
        val reasons = mutableListOf<String>()
        var defaultKeeper = false // Default is now CLUTTER — must earn keeper status
        var detectedCategory: String? = null

        // ── Rule 0: Protected folders are ALWAYS keepers, no exceptions ──
        if (isProtectedFolder(asset.bucketName)) {
            val isKeeper = decision?.selectedAsKeeper ?: true
            return CategoryResult(
                isKeeper = isKeeper,
                isDefaultKeeper = true,
                folderName = "Documents",
                reasons = emptyList()
            )
        }

        if (analysis != null) {
            val hasFace = analysis.detectedLabels?.contains("Face") == true

            // ── Rule 1: Sensitive items are always kept ──
            if (analysis.sensitiveFlags != null) {
                defaultKeeper = true
                reasons.add("Sensitive")

            // ── Rule 2: Blurry photos are always clutter (even with face) ──
            } else if (analysis.blurScore != null && analysis.blurScore < 80f) {
                defaultKeeper = false
                reasons.add("Blurry")

            // ── Rule 3: Duplicate detection ──
            } else if (analysis.duplicateGroupId != null) {
                if (seenDuplicateGroups != null) {
                    if (seenDuplicateGroups.contains(analysis.duplicateGroupId)) {
                        defaultKeeper = false
                        reasons.add("Duplicate")
                    } else {
                        seenDuplicateGroups.add(analysis.duplicateGroupId)
                        // First of a duplicate group — still needs face to be keeper
                        if (hasFace && !analysis.isScreenshot) {
                            defaultKeeper = true
                        }
                    }
                }

            // ── Rule 4: FACE is the primary keeper signal ──
            // Camera/Messaging/Other photos: face = keeper, no face = clutter
            } else if (!analysis.isScreenshot) {
                if (hasFace) {
                    defaultKeeper = true
                } else {
                    defaultKeeper = false
                    // No reason label needed — absence of face is the implicit reason
                }

            // ── Rule 5: Screenshots are clutter by default ──
            } else {
                defaultKeeper = false
            }

            // ── Rule 6: OCR-based promotion (high confidence only) ──
            // Only promote if OCR found strong document/receipt signals (≥2 keyword hits)
            if (analysis.detectedLabels != null && !reasons.contains("Sensitive")) {
                val labels = analysis.detectedLabels.split(",")
                val ocrLabels = labels.filter { it != "Face" }
                
                for (label in ocrLabels) {
                    if (label == "Receipt/Order" || label == "Document" || label == "Work Info") {
                        defaultKeeper = true
                        reasons.clear()
                        reasons.add(label)
                        detectedCategory = if (label == "Receipt/Order") "Receipts & Orders" else "Documents"
                        break
                    }
                }
            }
        }

        val isKeeper = decision?.selectedAsKeeper ?: defaultKeeper

        val finalFolderName = detectedCategory ?: when (asset.sourceType) {
            MediaSourceType.Screenshots -> "Important Screenshots"
            MediaSourceType.Downloads, MediaSourceType.Messaging -> "Saved Images"
            else -> "Memories"
        }

        return CategoryResult(
            isKeeper = isKeeper,
            isDefaultKeeper = defaultKeeper,
            folderName = finalFolderName,
            reasons = reasons
        )
    }
}
