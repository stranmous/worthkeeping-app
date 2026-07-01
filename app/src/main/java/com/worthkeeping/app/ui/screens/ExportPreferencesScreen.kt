package com.worthkeeping.app.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worthkeeping.app.data.UserPreferencesRepository
import kotlinx.coroutines.launch

class ExportPreferencesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserPreferencesRepository(application)
    val localExportFolderName = repository.localExportFolderName

    fun updateFolderName(name: String) {
        viewModelScope.launch {
            repository.updateLocalExportFolderName(name)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportPreferencesScreen(
    onBack: () -> Unit,
    viewModel: ExportPreferencesViewModel = viewModel()
) {
    val folderNameState = viewModel.localExportFolderName.collectAsStateWithLifecycle(initialValue = "")
    var textValue by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(folderNameState.value) {
        if (textValue.isEmpty() && folderNameState.value.isNotEmpty()) {
            textValue = folderNameState.value
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export preferences") },
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
                text = "Local Export Folder Name",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Keepers will be exported to this root folder inside your selected destination.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                label = { Text("Folder Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        val newName = textValue.trim().takeIf { it.isNotEmpty() } ?: "WorthKeeping"
                        textValue = newName
                        viewModel.updateFolderName(newName)
                        focusManager.clearFocus()
                    }
                )
            )
        }
    }
}
