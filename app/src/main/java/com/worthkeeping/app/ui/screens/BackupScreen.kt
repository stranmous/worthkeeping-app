package com.worthkeeping.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.services.drive.DriveScopes
import com.google.android.gms.common.api.Scope
import android.content.Intent

@Composable
fun BackupScreen(
    onScan: () -> Unit,
    onReview: () -> Unit,
    onClean: () -> Unit,
    onOpenSettings: () -> Unit,
    onStartLocalExport: (Uri) -> Unit,
    onStartDriveExport: (GoogleSignInAccount) -> Unit,
) {
    WorthKeepingScaffold(
        selectedTab = AppTab.Backup,
        onScan = onScan,
        onReview = onReview,
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
            Text(
                text = "Choose export destination",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "Original files and metadata are preserved where possible.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            InfoCard(
                title = "Local export",
                body = "Create organized folders on this phone.",
                icon = Icons.Outlined.Folder,
            )
            InfoCard(
                title = "Google Drive backup",
                body = "Back up organized folders to your Google Drive.",
                icon = Icons.Outlined.Cloud,
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
            
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocumentTree()
            ) { uri ->
                if (uri != null) {
                    onStartLocalExport(uri)
                }
            }
            
            val context = LocalContext.current
            val googleSignInLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(Exception::class.java)
                        if (account != null) {
                            onStartDriveExport(account)
                        } else {
                            android.widget.Toast.makeText(context, "Sign in failed: account is null", android.widget.Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        val msg = e.message ?: "Unknown error"
                        android.widget.Toast.makeText(context, "Google Sign-In Error: $msg (Requires Google Cloud setup for this package and SHA-1)", android.widget.Toast.LENGTH_LONG).show()
                    }
                } else {
                    android.widget.Toast.makeText(context, "Google Sign-In cancelled or failed (Result code: ${result.resultCode})", android.widget.Toast.LENGTH_LONG).show()
                }
            }
            
            PrimaryAction(
                label = "Start local export", 
                onClick = { launcher.launch(null) }
            )
            OutlinedButton(
                onClick = {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Connect Google Drive")
            }
            androidx.compose.material3.TextButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://waqasai.me/worthkeeping-app/"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Privacy Policy")
            }
        }
    }
}
