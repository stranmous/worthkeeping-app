package com.worthkeeping.app.scanner

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.worthkeeping.app.data.entities.ImageAsset
import kotlinx.coroutines.tasks.await
import android.util.Log

class OcrAnalyzer(
    private val context: Context
) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // High-confidence keywords that strongly indicate a specific category.
    // Single-word matches like "total", "code", "form", "application" are intentionally
    // excluded because they appear in random app UI text and cause massive false positives.
    
    private val sensitiveKeywords = listOf(
        "password", "recovery code", "ssn", "social security",
        "account number", "passport", "license", "pin code",
        "secret key", "private key", "2fa", "otp"
    )
    
    private val receiptKeywords = listOf(
        "receipt", "invoice", "order confirmed", "shipped",
        "tracking number", "purchase", "payment received",
        "transaction", "billing", "subtotal"
    )
    
    private val documentKeywords = listOf(
        "agreement", "contract", "certificate", "itinerary",
        "boarding pass", "ticket", "tax return", "insurance",
        "terms and conditions", "signature"
    )
    
    private val workKeywords = listOf(
        "meeting", "zoom", "slack", "jira",
        "sprint", "agenda", "standup", "pull request"
    )

    suspend fun analyzeText(asset: ImageAsset): OcrResult? {
        // Only attempt on likely text images
        if (asset.sourceType != MediaSourceType.Screenshots && asset.sourceType != MediaSourceType.Downloads) {
            return null
        }

        return try {
            val uri = Uri.parse(asset.uriString)
            val image = InputImage.fromFilePath(context, uri)
            
            val textResult = recognizer.process(image).await()
            
            val rawText = textResult.text.lowercase()
            Log.d("OcrAnalyzer", "OCR length for ${asset.displayName}: ${rawText.length}")
            if (rawText.isBlank()) return null
            
            val sensitiveFlags = mutableListOf<String>()
            val labels = mutableListOf<String>()
            
            // Sensitive detection: single keyword match is fine (safety-first)
            val sensitiveHits = sensitiveKeywords.count { rawText.contains(it) }
            if (sensitiveHits >= 1) {
                sensitiveFlags.add("Sensitive")
            }
            
            // Document/Receipt classification: require >= 2 keyword hits for confidence
            val receiptHits = receiptKeywords.count { rawText.contains(it) }
            val documentHits = documentKeywords.count { rawText.contains(it) }
            val workHits = workKeywords.count { rawText.contains(it) }
            
            if (receiptHits >= 2) {
                labels.add("Receipt/Order")
            } else if (documentHits >= 2) {
                labels.add("Document")
            } else if (workHits >= 2) {
                labels.add("Work Info")
            }

            OcrResult(
                sensitiveFlags = if (sensitiveFlags.isEmpty()) null else sensitiveFlags.joinToString(","),
                labels = if (labels.isEmpty()) null else labels.joinToString(",")
            )
        } catch (e: Exception) {
            null
        }
    }
}

data class OcrResult(
    val sensitiveFlags: String?,
    val labels: String?
)
