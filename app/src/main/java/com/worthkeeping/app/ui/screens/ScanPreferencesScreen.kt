package com.worthkeeping.app.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import android.provider.MediaStore
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worthkeeping.app.data.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ScanPreferencesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserPreferencesRepository(application)

    val skippedFolders = repository.skippedFolders

    fun toggleFolder(folder: String, isSkipped: Boolean) {
        viewModelScope.launch {
            val current = skippedFolders.first()
            val newSet = if (isSkipped) {
                current + folder
            } else {
                current - folder
            }
            repository.updateSkippedFolders(newSet)
        }
    }

    fun getAllImageFolders(): List<String> {
        val context = getApplication<Application>()
        val folders = mutableSetOf<String>()
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        
        try {
            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} ASC"
            )?.use { cursor ->
                val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    val folderName = cursor.getString(bucketColumn)
                    if (!folderName.isNullOrEmpty()) {
                        folders.add(folderName)
                    }
                }
            }
        } catch (e: Exception) {
        }
        
        return folders.toList().sorted()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanPreferencesScreen(
    onBack: () -> Unit,
    viewModel: ScanPreferencesViewModel = viewModel()
) {
    val skippedFolders = viewModel.skippedFolders.collectAsStateWithLifecycle(initialValue = emptySet())

    // Fetch dynamic folders only once when the screen is opened
    val commonFolders = remember { viewModel.getAllImageFolders() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan preferences") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Skipped Folders",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Turn on the switch to ignore photos from these folders during the scan.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (commonFolders.isEmpty()) {
                Text(
                    text = "No folders containing images were found on this device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                commonFolders.forEach { folder ->
                    val isSkipped = skippedFolders.value.contains(folder)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = folder,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = isSkipped,
                            onCheckedChange = { checked ->
                                viewModel.toggleFolder(folder, checked)
                            }
                        )
                    }
                }
            }
        }
    }
}
