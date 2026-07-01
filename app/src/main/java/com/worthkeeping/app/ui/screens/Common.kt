package com.worthkeeping.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun WorthKeepingScaffold(
    selectedTab: AppTab? = null,
    onScan: (() -> Unit)? = null,
    onReview: (() -> Unit)? = null,
    onBackup: (() -> Unit)? = null,
    onClean: (() -> Unit)? = null,
    onSettings: (() -> Unit)? = null,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        bottomBar = {
            if (selectedTab != null) {
                WorthKeepingBottomBar(
                    selectedTab = selectedTab,
                    onScan = onScan,
                    onReview = onReview,
                    onBackup = onBackup,
                    onClean = onClean,
                    onSettings = onSettings,
                )
            }
        },
        floatingActionButton = floatingActionButton,
        content = content,
    )
}

@Composable
fun WorthKeepingHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Shield,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp),
        )
        Text(
            text = "WorthKeeping",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun InfoCard(
    title: String,
    body: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val cardModifier = if (onClick != null) {
        modifier.fillMaxWidth().clickable(onClick = onClick)
    } else {
        modifier.fillMaxWidth()
    }
    Card(
        modifier = cardModifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
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

@Composable
fun PrimaryAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
    ) {
        Text(text = label)
    }
}

@Composable
fun SectionSpacer() {
    Spacer(modifier = Modifier.height(24.dp))
}

enum class AppTab(
    val label: String,
    val icon: ImageVector,
) {
    Scan("Scan", Icons.Outlined.Search),
    Review("Review", Icons.Outlined.PhotoLibrary),
    Backup("Backup", Icons.Outlined.Folder),
    Clean("Clean", Icons.Outlined.DeleteSweep),
    Settings("Settings", Icons.Outlined.Settings),
}

@Composable
private fun WorthKeepingBottomBar(
    selectedTab: AppTab,
    onScan: (() -> Unit)?,
    onReview: (() -> Unit)?,
    onBackup: (() -> Unit)?,
    onClean: (() -> Unit)?,
    onSettings: (() -> Unit)?,
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == AppTab.Scan,
            onClick = { onScan?.invoke() },
            icon = { Icon(AppTab.Scan.icon, contentDescription = null) },
            label = { Text(AppTab.Scan.label) },
        )
        NavigationBarItem(
            selected = selectedTab == AppTab.Review,
            onClick = { onReview?.invoke() },
            icon = { Icon(AppTab.Review.icon, contentDescription = null) },
            label = { Text(AppTab.Review.label) },
        )
        NavigationBarItem(
            selected = selectedTab == AppTab.Backup,
            onClick = { onBackup?.invoke() },
            icon = { Icon(AppTab.Backup.icon, contentDescription = null) },
            label = { Text(AppTab.Backup.label) },
        )
        NavigationBarItem(
            selected = selectedTab == AppTab.Clean,
            onClick = { onClean?.invoke() },
            icon = { Icon(AppTab.Clean.icon, contentDescription = null) },
            label = { Text(AppTab.Clean.label) },
        )
        NavigationBarItem(
            selected = selectedTab == AppTab.Settings,
            onClick = { onSettings?.invoke() },
            icon = { Icon(AppTab.Settings.icon, contentDescription = null) },
            label = { Text(AppTab.Settings.label) },
        )
    }
}

val ModeIcon = Icons.Outlined.Smartphone
val KeeperIcon = Icons.Outlined.Image
val ClutterIcon = Icons.Outlined.DeleteSweep
val PrivacyIcon = Icons.Outlined.AdminPanelSettings
val ExportIcon = Icons.Outlined.CloudUpload
