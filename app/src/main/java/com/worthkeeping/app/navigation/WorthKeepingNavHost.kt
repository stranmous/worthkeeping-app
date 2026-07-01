package com.worthkeeping.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.worthkeeping.app.export.DriveExportViewModel
import com.worthkeeping.app.export.LocalExportViewModel
import com.worthkeeping.app.scanner.ScanUiEvent
import com.worthkeeping.app.scanner.ScanViewModel
import com.worthkeeping.app.ui.screens.BackupScreen
import com.worthkeeping.app.ui.screens.DriveExportProgressScreen
import com.worthkeeping.app.ui.screens.EventDetailScreen
import com.worthkeeping.app.ui.screens.LocalExportProgressScreen
import com.worthkeeping.app.ui.screens.ModeSelectionScreen
import com.worthkeeping.app.ui.screens.PrivacyIntroScreen
import com.worthkeeping.app.ui.screens.ResultsSummaryScreen
import com.worthkeeping.app.ui.screens.ReviewScreen
import com.worthkeeping.app.ui.screens.ScanEstimateScreen
import com.worthkeeping.app.ui.screens.ScanProgressScreen
import com.worthkeeping.app.ui.screens.SettingsScreen
import com.worthkeeping.app.ui.screens.FeedbackScreen

object Routes {
    const val ModeSelection = "mode-selection"
    const val PrivacyIntro = "privacy-intro"
    const val ScanEstimate = "scan-estimate"
    const val ScanProgress = "scan-progress"
    const val ResultsSummary = "results-summary"
    const val Review = "review"
    const val Backup = "backup"
    const val Clean = "clean"
    const val Settings = "settings"
    const val Feedback = "feedback"
    const val ScanPreferences = "scan-preferences"
    const val ExportPreferences = "export-preferences"
    const val EventDetail = "event-detail/{bucketName}"
    const val LocalExportProgress = "local-export-progress"
    const val DriveExportProgress = "drive-export-progress"
    fun eventDetailRoute(bucketName: String) = "event-detail/${android.net.Uri.encode(bucketName)}"
}

@Composable
fun WorthKeepingNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    scanViewModel: ScanViewModel = viewModel(),
) {
    val scanUiState = scanViewModel.uiState.collectAsStateWithLifecycle()
    val localExportViewModel: LocalExportViewModel = viewModel()
    val driveExportViewModel: DriveExportViewModel = viewModel()
    var selectedGoogleAccount by remember { mutableStateOf<GoogleSignInAccount?>(null) }

    LaunchedEffect(scanViewModel) {
        scanViewModel.events.collect { event ->
            when (event) {
                is ScanUiEvent.NavigateToEstimate -> navController.navigate(Routes.ScanEstimate)
                is ScanUiEvent.NavigateToResults -> navController.navigate(Routes.ResultsSummary)
            }
        }
    }

    val navigateToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.ModeSelection,
        modifier = modifier
    ) {
        composable(Routes.ModeSelection) {
            ModeSelectionScreen(
                onModeSelected = { navController.navigate(Routes.PrivacyIntro) },
            )
        }
        composable(Routes.PrivacyIntro) {
            PrivacyIntroScreen(
                hasImageAccess = scanUiState.value.hasImageAccess,
                onPermissionsResult = scanViewModel::onPermissionsResult,
                onContinue = scanViewModel::continueToEstimate,
            )
        }
        composable(Routes.ScanEstimate) {
            ScanEstimateScreen(
                uiState = scanUiState.value,
                onRefreshEstimate = scanViewModel::refreshEstimate,
                onStartScan = {
                    scanViewModel.startScan()
                    navController.navigate(Routes.ScanProgress)
                },
                onReviewKeepers = { navigateToTab(Routes.Review) },
                onExportBackup = { navigateToTab(Routes.Backup) },
                onOpenSettings = { navigateToTab(Routes.Settings) },
                onCustomizeFolders = { navController.navigate(Routes.ScanPreferences) },
            )
        }
        composable(Routes.ScanProgress) {
            ScanProgressScreen(
                uiState = scanUiState.value,
            )
        }
        composable(Routes.ResultsSummary) {
            val summaryViewModel: com.worthkeeping.app.scanner.ResultsSummaryViewModel = viewModel()
            val summaryState = summaryViewModel.uiState.collectAsStateWithLifecycle()

            ResultsSummaryScreen(
                summaryState = summaryState.value,
                onReviewKeepers = { navigateToTab(Routes.Review) },
                onExportBackup = { navigateToTab(Routes.Backup) },
                onClean = { navigateToTab(Routes.Clean) },
                onOpenSettings = { navigateToTab(Routes.Settings) },
                onStartNewScan = { navController.navigate(Routes.ScanEstimate) },
            )
        }
        composable(Routes.Review) {
            ReviewScreen(
                onEventClick = { bucketName -> 
                    navController.navigate(Routes.eventDetailRoute(bucketName))
                },
                onBackToScan = { navigateToTab(Routes.ResultsSummary) },
                onExportBackup = { navigateToTab(Routes.Backup) },
                onClean = { navigateToTab(Routes.Clean) },
                onOpenSettings = { navigateToTab(Routes.Settings) },
            )
        }
        composable(
            route = Routes.EventDetail,
            arguments = listOf(navArgument("bucketName") { type = NavType.StringType })
        ) { backStackEntry ->
            val bucketName = backStackEntry.arguments?.getString("bucketName") ?: ""
            EventDetailScreen(
                bucketName = bucketName,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.Backup) {
            BackupScreen(
                onScan = { navigateToTab(Routes.ResultsSummary) },
                onReview = { navigateToTab(Routes.Review) },
                onClean = { navigateToTab(Routes.Clean) },
                onOpenSettings = { navigateToTab(Routes.Settings) },
                onStartLocalExport = { uri -> 
                    localExportViewModel.startExport(uri)
                    navController.navigate(Routes.LocalExportProgress)
                },
                onStartDriveExport = { account ->
                    selectedGoogleAccount = account
                    driveExportViewModel.startDriveExport(account)
                    navController.navigate(Routes.DriveExportProgress)
                }
            )
        }
        composable(Routes.Clean) {
            com.worthkeeping.app.ui.screens.FreeUpStorageScreen(
                onScan = { navigateToTab(Routes.ResultsSummary) },
                onReview = { navigateToTab(Routes.Review) },
                onBackup = { navigateToTab(Routes.Backup) },
                onSettings = { navigateToTab(Routes.Settings) },
                onNavigateBack = { navigateToTab(Routes.ResultsSummary) }
            )
        }
        composable(Routes.LocalExportProgress) {
            LocalExportProgressScreen(
                viewModel = localExportViewModel,
                onComplete = { navController.popBackStack(Routes.Backup, inclusive = false) }
            )
        }
        composable(Routes.DriveExportProgress) {
            DriveExportProgressScreen(
                viewModel = driveExportViewModel,
                onBackToSummary = { navController.popBackStack(Routes.Backup, inclusive = false) }
            )
        }
        composable(Routes.Settings) {
            SettingsScreen(
                onScan = { navigateToTab(Routes.ResultsSummary) },
                onReview = { navigateToTab(Routes.Review) },
                onBackup = { navigateToTab(Routes.Backup) },
                onClean = { navigateToTab(Routes.Clean) },
                onSettings = { navigateToTab(Routes.Settings) },
                onBackToResults = { navigateToTab(Routes.ResultsSummary) },
                onNavigateToFeedback = { navController.navigate(Routes.Feedback) },
                onSettingsNavigate = { type ->
                    if (type == "scan") navController.navigate(Routes.ScanPreferences)
                    else if (type == "export") navController.navigate(Routes.ExportPreferences)
                }
            )
        }

        composable(Routes.ScanPreferences) {
            com.worthkeeping.app.ui.screens.ScanPreferencesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ExportPreferences) {
            com.worthkeeping.app.ui.screens.ExportPreferencesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Feedback) {
            FeedbackScreen(
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}
