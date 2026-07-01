package com.worthkeeping.app.scanner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScanUiState(
    val hasImageAccess: Boolean = false,
    val hasLimitedAccess: Boolean = false,
    val isLoadingEstimate: Boolean = false,
    val scanProgress: MediaScanProgress = MediaScanProgress(),
    val estimate: ScanEstimate = ScanEstimate(),
    val errorMessage: String? = null,
)

sealed interface ScanUiEvent {
    object NavigateToEstimate : ScanUiEvent
    object NavigateToResults : ScanUiEvent
}

class ScanViewModel(application: Application) : AndroidViewModel(application) {
    private val scanner = MediaScanner(application.applicationContext)
    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()
    
    private val _events = Channel<ScanUiEvent>()
    val events = _events.receiveAsFlow()

    init {
        val appContext = application.applicationContext
        val permissions = MediaPermissionHelper.requiredImagePermissions().associateWith {
            androidx.core.content.ContextCompat.checkSelfPermission(appContext, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        onPermissionsResult(permissions)
    }

    fun onPermissionsResult(grants: Map<String, Boolean>) {
        val hasFullAccess = MediaPermissionHelper.hasFullImageAccess(grants)
        val hasLimitedAccess = MediaPermissionHelper.hasLimitedImageAccess(grants)

        _uiState.update {
            it.copy(
                hasImageAccess = hasFullAccess || hasLimitedAccess,
                hasLimitedAccess = hasLimitedAccess,
                errorMessage = null,
            )
        }

        if (hasFullAccess || hasLimitedAccess) {
            refreshEstimate(hasLimitedAccess)
        }
    }

    fun continueToEstimate() {
        if (_uiState.value.hasImageAccess) {
            viewModelScope.launch {
                _events.send(ScanUiEvent.NavigateToEstimate)
            }
        }
    }

    fun refreshEstimate(hasLimitedAccess: Boolean = _uiState.value.hasLimitedAccess) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingEstimate = true,
                    errorMessage = null,
                )
            }
            runCatching {
                scanner.estimateImages(hasLimitedAccess)
            }.onSuccess { estimate ->
                _uiState.update {
                    it.copy(
                        hasImageAccess = true,
                        hasLimitedAccess = estimate.hasLimitedAccess,
                        isLoadingEstimate = false,
                        estimate = estimate,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoadingEstimate = false,
                        errorMessage = error.message ?: "Could not scan image library.",
                    )
                }
            }
        }
    }

    fun startScan() {
        viewModelScope.launch {
            val hasLimitedAccess = _uiState.value.hasLimitedAccess
            _uiState.update {
                it.copy(
                    scanProgress = MediaScanProgress(
                        isScanning = true,
                        totalImages = it.estimate.totalImages,
                        sources = it.estimate.sources,
                    ),
                    errorMessage = null,
                )
            }
            runCatching {
                scanner.scanImages(hasLimitedAccess) { progress ->
                    _uiState.update {
                        it.copy(scanProgress = progress)
                    }
                }
            }.onSuccess { estimate ->
                _uiState.update {
                    it.copy(
                        estimate = estimate,
                        scanProgress = it.scanProgress.copy(
                            isScanning = false,
                            isComplete = true,
                        ),
                    )
                }
                _events.send(ScanUiEvent.NavigateToResults)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        scanProgress = it.scanProgress.copy(isScanning = false),
                        errorMessage = error.message ?: "Could not scan image library.",
                    )
                }
            }
        }
    }
}
