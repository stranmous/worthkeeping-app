package com.worthkeeping.app.ui.screens

import android.app.Activity
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.worthkeeping.app.deletion.FreeUpStorageViewModel

@Composable
fun FreeUpStorageScreen(
    viewModel: FreeUpStorageViewModel = viewModel(),
    onScan: () -> Unit,
    onReview: () -> Unit,
    onBackup: () -> Unit,
    onSettings: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val trashLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.confirmDeletionSuccess()
        }
    }

    WorthKeepingScaffold(
        selectedTab = AppTab.Clean,
        onScan = onScan,
        onReview = onReview,
        onBackup = onBackup,
        onClean = {},
        onSettings = onSettings,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Review Clutter",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                )
                Text(
                    text = "Tap to unselect items you want to keep.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.isLoading) {
                    Spacer(modifier = Modifier.weight(1f))
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.weight(1f))
                } else if (uiState.deletionComplete) {
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Storage Freed!",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Items have been moved to the Android Trash.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    PrimaryAction(
                        label = "Done",
                        onClick = onNavigateBack
                    )
                } else if (uiState.clutterItems.isEmpty()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "No clutter found to delete.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PrimaryAction(
                        label = "Go Back",
                        onClick = onNavigateBack
                    )
                    Spacer(modifier = Modifier.weight(1f))
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(uiState.clutterItems) { asset ->
                            val isSelected = uiState.selectedClutterIds.contains(asset.mediaStoreId)
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clickable { viewModel.toggleSelection(asset.mediaStoreId) }
                            ) {
                                AsyncImage(
                                    model = asset.uriString,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.4f))
                                    )
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .align(Alignment.TopEnd)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Sticky Bottom Bar for Deletion
            if (!uiState.isLoading && !uiState.deletionComplete && uiState.clutterItems.isNotEmpty()) {
                val sizeMb = uiState.selectedSizeBytes / (1024 * 1024)
                val count = uiState.selectedClutterIds.size
                
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (uiState.error != null) {
                            Text(
                                text = uiState.error!!,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()
                            )
                        }
                        
                        Button(
                            onClick = {
                                val uris = viewModel.getSelectedUris()
                                if (uris.isNotEmpty()) {
                                    val trashIntent = MediaStore.createTrashRequest(
                                        context.contentResolver, uris, true
                                    )
                                    trashLauncher.launch(IntentSenderRequest.Builder(trashIntent.intentSender).build())
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = count > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Move $count items (~$sizeMb MB) to Trash")
                        }
                    }
                }
            }
        }
    }
}
