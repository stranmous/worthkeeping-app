package com.worthkeeping.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.NoAccounts
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.worthkeeping.app.scanner.MediaPermissionHelper

@Composable
fun PrivacyIntroScreen(
    hasImageAccess: Boolean,
    onPermissionsResult: (Map<String, Boolean>) -> Unit,
    onContinue: () -> Unit,
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = onPermissionsResult,
    )

    LaunchedEffect(hasImageAccess) {
        if (hasImageAccess) {
            onContinue()
        }
    }

    WorthKeepingScaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(
                modifier = Modifier
                    .padding(PaddingValues(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 16.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                WorthKeepingHeader()
                SectionSpacer()
                Text(
                    text = "Your privacy comes first.",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "WorthKeeping needs image access to scan your gallery. You stay in control of backup and deletion.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                SectionSpacer()
                InfoCard(
                    title = "Private on-device scan",
                    body = "Analysis happens on this phone before any export.",
                    icon = Icons.Outlined.PhoneAndroid,
                )
                InfoCard(
                    title = "No account needed",
                    body = "You can scan and review without creating a profile.",
                    icon = Icons.Outlined.NoAccounts,
                )
                InfoCard(
                    title = "Nothing uploads automatically",
                    body = "Local export and Google Drive backup happen only when you choose.",
                    icon = Icons.Outlined.CloudOff,
                )
                InfoCard(
                    title = "Review before deleting",
                    body = "The app suggests clutter, but you make the final deletion decision.",
                    icon = Icons.Outlined.DeleteOutline,
                )
            }
            PrimaryAction(
                label = if (hasImageAccess) "Continue" else "Allow access and continue",
                onClick = {
                    if (hasImageAccess) {
                        onContinue()
                    } else {
                        permissionLauncher.launch(MediaPermissionHelper.requiredImagePermissions())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            )
        }
    }
}
