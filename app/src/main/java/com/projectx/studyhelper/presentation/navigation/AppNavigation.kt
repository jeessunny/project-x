package com.projectx.studyhelper.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.projectx.studyhelper.presentation.sessions.SessionsScreen
import com.projectx.studyhelper.presentation.sessions.SessionsViewModel
import com.projectx.studyhelper.presentation.settings.SettingsScreen
import com.projectx.studyhelper.presentation.settings.SettingsViewModel
import com.projectx.studyhelper.presentation.study.StudyScreen
import com.projectx.studyhelper.presentation.study.TimerViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController,
    timerViewModel: TimerViewModel,
    sessionsViewModel: SessionsViewModel,
    settingsViewModel: SettingsViewModel,
    timerUiState: com.projectx.studyhelper.presentation.study.StudyUiState,
    sessionsUiState: com.projectx.studyhelper.presentation.sessions.SessionsUiState,
    settingsUiState: com.projectx.studyhelper.presentation.settings.SettingsUiState
) {
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val snackbarHostState = remember { SnackbarHostState() }

    // Listen for Settings snackbar notifications
    LaunchedEffect(Unit) {
        settingsViewModel.snackbarMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val currentScreenTitle = when (currentDestination?.route) {
        Screen.Study.route -> "Project X"
        Screen.Sessions.route -> "Study Sessions"
        Screen.Settings.route -> "Settings"
        else -> "Project X"
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = currentScreenTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Screen.bottomNavDestinations.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Study.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Study.route) {
                StudyScreen(
                    uiState = timerUiState,
                    onSelectPreset = timerViewModel::selectPreset,
                    onOpenCustomPicker = timerViewModel::openCustomPicker,
                    onCloseCustomPicker = timerViewModel::closeCustomPicker,
                    onApplyCustomMinutes = timerViewModel::applyCustomMinutes,
                    onStartTimer = { timerViewModel.startTimer(context) },
                    onPromptAbort = timerViewModel::promptAbortSession,
                    onConfirmAbort = { timerViewModel.confirmAbortSession(context) },
                    onDismissAbort = timerViewModel::dismissAbortDialog,
                    onResetCompleted = timerViewModel::resetCompleted,
                    onDismissUpdateDialog = timerViewModel::dismissUpdateDialog,
                    onConfirmUpdateDownload = timerViewModel::downloadUpdate,
                    onTriggerInstall = { timerViewModel.triggerInstall(context) }
                )
            }

            composable(Screen.Sessions.route) {
                SessionsScreen(uiState = sessionsUiState)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    uiState = settingsUiState,
                    onSetThemeMode = settingsViewModel::setThemeMode,
                    onSetAmoledBlack = settingsViewModel::setAmoledBlack,
                    onSetUseDynamicColor = settingsViewModel::setUseDynamicColor,
                    onSetDefaultPreset = settingsViewModel::setDefaultPreset,
                    onCheckForUpdates = settingsViewModel::checkForUpdatesManual,
                    onDismissUpdateDialog = settingsViewModel::dismissUpdateDialog,
                    onConfirmUpdateDownload = settingsViewModel::downloadUpdate,
                    onTriggerInstall = { settingsViewModel.triggerInstall(context) }
                )
            }
        }
    }
}
