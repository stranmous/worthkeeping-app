package com.worthkeeping.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.worthkeeping.app.export.DriveExportViewModel
import com.worthkeeping.app.export.ExportProgressState

@Composable
fun DriveExportProgressScreen(
    viewModel: DriveExportViewModel,
    onBackToSummary: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (state.error != null) {
            Text(
                text = "Export Failed",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = state.error ?: "Unknown error",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(32.dp))
            PrimaryAction(label = "Back to Backup", onClick = onBackToSummary)
        } else if (state.isComplete) {
            Text(
                text = "Backup Complete!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Successfully uploaded ${state.exportedItems} keepers to your Google Drive.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(32.dp))
            PrimaryAction(label = "Done", onClick = onBackToSummary)
        } else {
            Text(
                text = "Backing up to Google Drive...",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (state.totalItems > 0) {
                val progress = state.exportedItems.toFloat() / state.totalItems.toFloat()
                LinearProgressIndicator(progress = progress)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Uploading ${state.exportedItems} of ${state.totalItems} images",
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (state.currentFile.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Current: ${state.currentFile}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            } else {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Preparing items...",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}
