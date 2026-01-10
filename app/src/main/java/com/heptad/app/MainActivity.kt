package com.heptad.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.heptad.app.data.preferences.ThemeMode
import com.heptad.app.ui.navigation.HeptadRoute
import com.heptad.app.ui.screens.DailyPuzzleScreen
import com.heptad.app.ui.screens.GameScreen
import com.heptad.app.ui.screens.MainMenuScreen
import com.heptad.app.ui.screens.SettingsScreen
import com.heptad.app.ui.theme.HeptadTheme
import com.heptad.app.ui.viewmodels.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HeptadApp()
        }
    }
}

@Composable
fun HeptadApp() {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val preferences by settingsViewModel.preferences.collectAsState()

    val darkTheme = when (preferences.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    HeptadTheme(darkTheme = darkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = HeptadRoute.MainMenu.route
            ) {
                composable(HeptadRoute.MainMenu.route) {
                    MainMenuScreen(
                        onNavigateToDaily = {
                            navController.navigate(HeptadRoute.DailyPuzzle.route)
                        },
                        onNavigateToRandom = {
                            navController.navigate(HeptadRoute.RandomPuzzle.route)
                        },
                        onNavigateToSettings = {
                            navController.navigate(HeptadRoute.Settings.route)
                        }
                    )
                }
                composable(HeptadRoute.DailyPuzzle.route) {
                    DailyPuzzleScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToSettings = {
                            navController.navigate(HeptadRoute.Settings.route)
                        }
                    )
                }
                composable(HeptadRoute.RandomPuzzle.route) {
                    GameScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToSettings = {
                            navController.navigate(HeptadRoute.Settings.route)
                        }
                    )
                }
                composable(HeptadRoute.Settings.route) {
                    SettingsScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
