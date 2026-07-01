package com.worthkeeping.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ModeSelectionScreen(
    onModeSelected: () -> Unit,
) {
    WorthKeepingScaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onModeSelected,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Outlined.ArrowForward,
                    contentDescription = "Continue"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            WorthKeepingHeader()
            SectionSpacer()
            Text(
                text = "WorthKeeping",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Preserve what matters before you switch, reset, or back up your phone.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            AssistChip(
                onClick = {},
                label = { Text("Processed locally on your device") },
                leadingIcon = { Icon(PrivacyIcon, contentDescription = null) },
            )
            SectionSpacer()
            ModeCard(
                title = "Prepare for new phone",
                body = "Get important images ready for a clean transfer or backup.",
                icon = ModeIcon,
            )
            ModeCard(
                title = "Find keepers",
                body = "Identify images worth carrying forward before you upload.",
                icon = KeeperIcon,
            )
            ModeCard(
                title = "Free up storage",
                body = "Review duplicates, blurry shots, and screenshots before deleting.",
                icon = ClutterIcon,
            )
        }
    }
}

@Composable
private fun ModeCard(
    title: String,
    body: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        modifier = Modifier,
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
