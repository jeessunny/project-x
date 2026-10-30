package com.projectx.studyhelper

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.projectx.studyhelper.presentation.navigation.AppNavigation
import com.projectx.studyhelper.presentation.sessions.SessionsViewModel
import com.projectx.studyhelper.presentation.settings.SettingsViewModel
import com.projectx.studyhelper.presentation.study.TimerViewModel
import com.projectx.studyhelper.presentation.theme.StudyHelperTheme
import com.projectx.studyhelper.presentation.theme.ThemeConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.io.StringWriter

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val timerViewModel: TimerViewModel by viewModels {
        val app = application as StudyHelperApplication
        TimerViewModel.Factory(
            streakManager = app.streakManager,
            timerManager = app.timerManager
        )
    }

    private val sessionsViewModel: SessionsViewModel by viewModels {
        val app = application as StudyHelperApplication
        SessionsViewModel.Factory(
            streakRepository = app.streakRepository
        )
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        val app = application as StudyHelperApplication
        SettingsViewModel.Factory(
            themePreferences = app.themePreferences,
            streakRepository = app.streakRepository,
            updateManager = app.updateManager
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            enableEdgeToEdge()
        } catch (e: Throwable) {
            Log.w(TAG, "enableEdgeToEdge failed gracefully: ${e.message}")
        }

        val app = application as StudyHelperApplication

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                app.updateManager.checkForUpdates(isManual = false)
            } catch (e: Exception) {
                Log.w(TAG, "Update check failed non-fatally: ${e.message}")
            }
        }

        setContent {
            var compositionError by remember { mutableStateOf<Throwable?>(null) }

            if (compositionError != null) {
                CompositionErrorFallback(error = compositionError!!)
            } else {
                try {
                    val themeConfig by app.themePreferences.themeConfigFlow
                        .collectAsStateWithLifecycle(initialValue = ThemeConfig())

                    StudyHelperTheme(themeConfig = themeConfig) {
                        val navController = rememberNavController()

                        val timerUiState by timerViewModel.uiState.collectAsStateWithLifecycle()
                        val sessionsUiState by sessionsViewModel.uiState.collectAsStateWithLifecycle()
                        val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

                        AppNavigation(
                            navController = navController,
                            timerViewModel = timerViewModel,
                            sessionsViewModel = sessionsViewModel,
                            settingsViewModel = settingsViewModel,
                            updateManager = app.updateManager,
                            timerUiState = timerUiState,
                            sessionsUiState = sessionsUiState,
                            settingsUiState = settingsUiState
                        )
                    }
                } catch (t: Throwable) {
                    Log.e(TAG, "Fatal composition error intercepted", t)
                    compositionError = t
                }
            }
        }
    }
}

@Composable
private fun CompositionErrorFallback(error: Throwable) {
    val sw = StringWriter()
    error.printStackTrace(PrintWriter(sw))
    val stackTrace = sw.toString()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Startup Diagnostic",
                color = Color(0xFFFF6F00),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Text(
                text = "An issue occurred during UI initialization:",
                color = Color.White,
                fontSize = 14.sp
            )

            Text(
                text = stackTrace,
                color = Color(0xFF81C784),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E))
                    .padding(12.dp)
            )
        }
    }
}
