package com.worthkeeping.app.export

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.Collections

class DriveExporter(private val context: Context, private val account: GoogleSignInAccount) {

    private val driveService: Drive by lazy {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, Collections.singleton(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account

        Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("WorthKeeping")
            .build()
    }

    private suspend fun <T> retryWithBackoff(times: Int = 15, block: suspend () -> T): T {
        var currentDelay = 2000L
        repeat(times - 1) {
            try {
                return block()
            } catch (e: Exception) {
                kotlinx.coroutines.delay(currentDelay)
                // Increase delay by 1.5x, up to a maximum of 30 seconds
                currentDelay = (currentDelay * 1.5).toLong().coerceAtMost(30000L) 
            }
        }
        return block()
    }

    suspend fun createFolder(name: String, parentId: String? = null): String = withContext(Dispatchers.IO) {
        val fileMetadata = com.google.api.services.drive.model.File().apply {
            this.name = name
            this.mimeType = "application/vnd.google-apps.folder"
            if (parentId != null) {
                this.parents = listOf(parentId)
            }
        }
        retryWithBackoff {
            val folder = driveService.files().create(fileMetadata)
                .setFields("id")
                .execute()
            folder.id
        }
    }

    suspend fun uploadFile(uri: Uri, displayName: String, parentFolderId: String): String = withContext(Dispatchers.IO) {
        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
        
        val fileMetadata = com.google.api.services.drive.model.File().apply {
            this.name = displayName
            this.parents = listOf(parentFolderId)
        }

        retryWithBackoff {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                throw Exception("Could not open InputStream for URI: $uri")
            }

            try {
                val mediaContent = InputStreamContent(mimeType, inputStream)
                val file = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()
                file.id
            } finally {
                inputStream.close()
            }
        }
    }
}
