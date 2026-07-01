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
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.worthkeeping.app.scanner.ScanUiState
import java.text.DecimalFormat

@Composable
fun ScanEstimateScreen(
    uiState: ScanUiState,
    onRefreshEstimate: () -> Unit,
    onStartScan: () -> Unit,
    onReviewKeepers: () -> Unit,
    onExportBackup: () -> Unit,
    onOpenSettings: () -> Unit,
    onCustomizeFolders: () -> Unit,
) {
    LaunchedEffect(uiState.hasImageAccess) {
        if (uiState.hasImageAccess && uiState.estimate.totalImages == 0 && !uiState.isLoadingEstimate) {
            onRefreshEstimate()
        }
    }

    WorthKeepingScaffold(
        selectedTab = AppTab.Scan,
        onReview = onReviewKeepers,
        onBackup = onExportBackup,
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
            Text(
                text = "Ready to find your keepers.",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "We'll scan all image folders on this device to identify images worth keeping.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (uiState.hasLimitedAccess) {
                Text(
                    text = "Limited photo access is enabled. WorthKeeping can only scan selected images.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoCard(
                    title = when {
                        uiState.isLoadingEstimate -> "Scanning"
                        else -> formatCount(uiState.estimate.totalImages)
                    },
                    body = "Images to scan",
                    icon = Icons.Outlined.ImageSearch,
                    modifier = Modifier.weight(1f),
                )
                InfoCard(
                    title = if (uiState.isLoadingEstimate) "..." else "~${uiState.estimate.estimatedMinutes} mins",
                    body = "Estimated scan time",
                    icon = Icons.Outlined.Timer,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoCard(
                    title = if (uiState.isLoadingEstimate) "..." else formatBytes(uiState.estimate.totalSizeBytes),
                    body = "Current gallery size",
                    icon = Icons.Outlined.Storage,
                    modifier = Modifier.weight(1f),
                )
                InfoCard(
                    title = "Pending",
                    body = "Keeper backup size",
                    icon = Icons.Outlined.FolderOpen,
                    modifier = Modifier.weight(1f),
                )
            }
            uiState.estimate.sources.takeIf { it.isNotEmpty() }?.let { sources ->
                InfoCard(
                    title = "Included folders",
                    body = sources.joinToString { "${it.type.name}: ${formatCount(it.imageCount)}" },
                    icon = Icons.Outlined.FolderOpen,
                )
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
            PrimaryAction(
                label = "Start scan",
                onClick = onStartScan,
            )
            OutlinedButton(
                onClick = onCustomizeFolders,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(),
            ) {
                Text("Customize folders")
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
