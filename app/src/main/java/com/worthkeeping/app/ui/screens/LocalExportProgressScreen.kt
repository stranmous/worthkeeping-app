package com.worthkeeping.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worthkeeping.app.export.LocalExportViewModel

@Composable
fun LocalExportProgressScreen(
    viewModel: LocalExportViewModel = viewModel(),
    onComplete: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (state.error != null) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "Export Failed",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = state.error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                Button(onClick = onComplete) {
                    Text("Go Back")
                }
            } else if (state.isComplete) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "Export Complete!",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Successfully saved ${state.exportedItems} photos to your selected folder.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                Button(onClick = onComplete) {
                    Text("Done")
                }
            } else {
                CircularProgressIndicator(modifier = Modifier.padding(bottom = 24.dp))
                Text(
                    text = "Exporting Keepers...",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                if (state.totalItems > 0) {
                    val progress = state.exportedItems.toFloat() / state.totalItems.toFloat()
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                    )
                    Text(
                        text = "${state.exportedItems} of ${state.totalItems}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = state.currentFile,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
