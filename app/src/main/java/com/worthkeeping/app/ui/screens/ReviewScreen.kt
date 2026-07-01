package com.worthkeeping.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.worthkeeping.app.review.ReviewGroupUiModel
import com.worthkeeping.app.review.ReviewViewModel

@Composable
fun ReviewScreen(
    reviewViewModel: ReviewViewModel = viewModel(),
    onEventClick: (String) -> Unit,
    onBackToScan: () -> Unit,
    onExportBackup: () -> Unit,
    onClean: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val uiState = reviewViewModel.uiState.collectAsStateWithLifecycle()

    WorthKeepingScaffold(
        selectedTab = AppTab.Review,
        onScan = onBackToScan,
        onBackup = onExportBackup,
        onClean = onClean,
        onSettings = onOpenSettings,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                WorthKeepingHeader()
                SectionSpacer()
                Text(
                    text = "Review keepers",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "Review grouped events and adjust selected images.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (uiState.value.isLoading) {
                item {
                    Text(
                        text = "Loading groups...",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else if (uiState.value.groups.isEmpty()) {
                item {
                    Text(
                        text = "No images found.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                items(uiState.value.groups) { group ->
                    EventCard(
                        group = group,
                        onClick = { onEventClick(group.bucketName) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EventCard(
    group: ReviewGroupUiModel,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = group.bucketName.ifEmpty { "Unknown Folder" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "${group.keepersCount} keepers · ${group.clutterCount} not selected",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(5) { index ->
                    val thumbnailUri = group.sampleThumbnails.getOrNull(index)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (thumbnailUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(thumbnailUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Thumbnail",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (index == 4 && group.totalImages > 5) {
                            Text(
                                text = "+${group.totalImages - 4}",
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Review all")
                }
            }
        }
    }
}
