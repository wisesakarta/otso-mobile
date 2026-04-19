package com.otso.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.otso.app.ui.screens.EditorScreen
import com.otso.app.ui.screens.AboutScreen
import com.otso.app.ui.theme.OtsoTheme
import com.otso.app.viewmodel.EditorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // DNA: Surgical system bar management (Karpathy Principle)
        enableEdgeToEdge()
        
        setContent {
            val navController = rememberNavController()
            val editorViewModel: EditorViewModel = viewModel()
            val uiState by editorViewModel.uiState.collectAsState()

            // DNA: Adaptive System Bars via SideEffect
            androidx.compose.runtime.SideEffect {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { uiState.isDarkMode },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { uiState.isDarkMode },
                )
            }

            OtsoTheme(darkTheme = uiState.isDarkMode) {
                NavHost(
                    navController = navController,
                    startDestination = "editor",
                ) {
                    composable("editor") {
                        EditorScreen(
                            viewModel = editorViewModel,
                            navController = navController,
                        )
                    }
                    composable("about") {
                        AboutScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
