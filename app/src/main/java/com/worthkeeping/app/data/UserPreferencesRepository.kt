package com.worthkeeping.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {

    private val SKIPPED_FOLDERS = stringSetPreferencesKey("skipped_folders")
    private val LOCAL_EXPORT_FOLDER_NAME = stringPreferencesKey("local_export_folder_name")

    val skippedFolders: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[SKIPPED_FOLDERS] ?: emptySet()
    }

    val localExportFolderName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LOCAL_EXPORT_FOLDER_NAME] ?: "WorthKeeping"
    }

    suspend fun updateSkippedFolders(folders: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[SKIPPED_FOLDERS] = folders
        }
    }

    suspend fun updateLocalExportFolderName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[LOCAL_EXPORT_FOLDER_NAME] = name
        }
    }
}
