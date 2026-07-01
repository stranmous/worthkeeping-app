package com.worthkeeping.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Screenshot
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.worthkeeping.app.scanner.ResultsSummaryState
import java.text.DecimalFormat

@Composable
fun ResultsSummaryScreen(
    summaryState: ResultsSummaryState,
    onReviewKeepers: () -> Unit,
    onExportBackup: () -> Unit,
    onClean: () -> Unit,
    onOpenSettings: () -> Unit,
    onStartNewScan: () -> Unit,
) {
    WorthKeepingScaffold(
        selectedTab = AppTab.Scan,
        onReview = onReviewKeepers,
        onBackup = onExportBackup,
        onClean = onClean,
        onSettings = onOpenSettings,
    ) { padding ->
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
            
            if (summaryState.isLoading) {
                Text(
                    text = "Loading summary...",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "We found ${formatCount(summaryState.totalKeepers)} keepers from ${formatCount(summaryState.totalImages)} images",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "Your backup is organized into practical categories.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoCard(formatBytes(summaryState.backupSizeBytes), "Estimated backup size", ExportIcon, Modifier.weight(1f))
                    InfoCard(formatBytes(summaryState.savedSizeBytes), "Potential space saved", ClutterIcon, Modifier.weight(1f))
                }
                InfoCard("Memories", "${formatCount(summaryState.memoriesCount)} camera and shared images", KeeperIcon)
                InfoCard("Important screenshots", "${formatCount(summaryState.importantScreenshotsCount)} screenshots to review", Icons.Outlined.Screenshot)
                InfoCard("Documents", "${formatCount(summaryState.documentsCount)} utility images", Icons.Outlined.Article)
                InfoCard("Receipts & orders", "${formatCount(summaryState.receiptsCount)} items detected", Icons.Outlined.ReceiptLong)
                InfoCard("Sensitive", "${formatCount(summaryState.sensitiveCount)} items for review", Icons.Outlined.Badge)
            }
            
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
            PrimaryAction("Review keepers", onReviewKeepers)
            OutlinedButton(
                onClick = onExportBackup,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Export backup")
            }

            OutlinedButton(
                onClick = onStartNewScan,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Start new scan")
            }
        }
    }
}

private fun formatCount(value: Int): String = DecimalFormat("#,###").format(value)

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 MB"
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var index = 0
    while (value >= 1024 && index < units.lastIndex) {
        value /= 1024
        index += 1
    }
    val pattern = if (value >= 10 || index == 0) "#,##0" else "#,##0.0"
    return "${DecimalFormat(pattern).format(value)} ${units[index]}"
}
