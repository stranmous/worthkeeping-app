package com.worthkeeping.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onScan: () -> Unit,
    onReview: () -> Unit,
    onBackup: () -> Unit,
    onClean: () -> Unit,
    onSettings: () -> Unit,
    onBackToResults: () -> Unit,
    onNavigateToFeedback: () -> Unit,
    onSettingsNavigate: (String) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val showToast = { text: String ->
        android.widget.Toast.makeText(context, text, android.widget.Toast.LENGTH_SHORT).show()
    }
    WorthKeepingScaffold(
        selectedTab = AppTab.Settings,
        onScan = onScan,
        onReview = onReview,
        onBackup = onBackup,
        onClean = onClean,
        onSettings = onSettings,
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
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Medium,
            )
            InfoCard("Scan preferences", "Choose folders and scan behavior.", KeeperIcon, onClick = { onSettingsNavigate("scan") })
            InfoCard("Export preferences", "Manage local export and Google Drive.", ExportIcon, onClick = { onSettingsNavigate("export") })
            InfoCard("Feedback", "Send optional feedback without photo data.", ModeIcon, onClick = onNavigateToFeedback)
            androidx.compose.material3.TextButton(
                onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("http://waqasai.me/worthkeeping-app/"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Privacy Policy")
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(
                onClick = onBackToResults,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Back to results")
            }
        }
    }
}
