package com.worthkeeping.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.worthkeeping.app.scanner.MediaSourceSummary
import com.worthkeeping.app.scanner.ScanUiState
import java.text.DecimalFormat

@Composable
fun ScanProgressScreen(
    uiState: ScanUiState,
) {
    val progress = uiState.scanProgress
    val progressFraction = if (progress.isAnalyzing && progress.totalImages > 0) {
        progress.analyzedImages.toFloat() / progress.totalImages.toFloat()
    } else if (progress.totalImages > 0) {
        progress.scannedImages.toFloat() / progress.totalImages.toFloat()
    } else {
        0f
    }

    WorthKeepingScaffold(selectedTab = AppTab.Scan) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            WorthKeepingHeader()
            SectionSpacer()
            Text(
                text = if (progress.isAnalyzing) "Analyzing images..." else "Scanning your images...",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Medium,
            )
            LinearProgressIndicator(
                progress = { progressFraction.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = if (progress.isAnalyzing) {
                    "Local processing - identifying keepers and clutter"
                } else {
                    progress.currentSource?.let {
                        "Local processing - scanning ${it.name.lowercase()} images"
                    } ?: "Local processing - preparing image scan"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SectionSpacer()
            SourceProgressCard("Camera", progress.sources.findByName("Camera"), progress.isComplete)
            SourceProgressCard("Screenshots", progress.sources.findByName("Screenshots"), progress.isComplete)
            SourceProgressCard("Downloads", progress.sources.findByName("Downloads"), progress.isComplete)
            SourceProgressCard("Messaging folders", progress.sources.findByName("Messaging"), progress.isComplete)
            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
            InfoCard(
                if (progress.isAnalyzing) formatCount(progress.analyzedImages) else formatCount(progress.scannedImages),
                if (progress.isAnalyzing) "of ${formatCount(progress.totalImages)} images analyzed" else "of ${formatCount(progress.totalImages)} images scanned",
                KeeperIcon,
            )
            PrimaryAction(
                label = if (progress.isComplete) "View results" else if (progress.isAnalyzing) "Analyzing..." else "Scanning...",
                onClick = {}, // Navigation handles this automatically when scan completes
            )
        }
    }
}

@Composable
private fun SourceProgressCard(
    title: String,
    source: MediaSourceSummary?,
    isComplete: Boolean,
) {
    val count = source?.imageCount ?: 0
    val icon = when {
        isComplete && count > 0 -> Icons.Outlined.CheckCircle
        count > 0 -> Icons.Outlined.Sync
        else -> Icons.Outlined.HourglassEmpty
    }
    val body = when {
        count > 0 -> "${formatCount(count)} images scanned"
        else -> "Waiting"
    }
    InfoCard(title, body, icon)
}

private fun List<MediaSourceSummary>.findByName(name: String): MediaSourceSummary? {
    return firstOrNull { it.type.name == name }
}

private fun formatCount(value: Int): String = DecimalFormat("#,###").format(value)
