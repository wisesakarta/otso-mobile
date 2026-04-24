package com.otso.app

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
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.Color
import com.otso.app.ui.screens.EditorScreen
import com.otso.app.ui.screens.AboutScreen
import com.otso.app.ui.theme.OtsoMotion
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
                    enterTransition = {
                        fadeIn(
                            animationSpec = tween(
                                durationMillis = OtsoMotion.durationStandardMs,
                                easing = OtsoMotion.easeOut,
                            )
                        ) + slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(
                                durationMillis = 320,
                                easing = OtsoMotion.easeDrawer,
                            ),
                        )
                    },
                    exitTransition = {
                        fadeOut(
                            animationSpec = tween(
                                durationMillis = 220,
                                easing = OtsoMotion.easeInOut,
                            )
                        ) + slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(
                                durationMillis = 280,
                                easing = OtsoMotion.easeInOut,
                            ),
                        )
                    },
                    popEnterTransition = {
                        fadeIn(
                            animationSpec = tween(
                                durationMillis = OtsoMotion.durationStandardMs,
                                easing = OtsoMotion.easeOut,
                            )
                        ) + slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(
                                durationMillis = 320,
                                easing = OtsoMotion.easeDrawer,
                            ),
                        )
                    },
                    popExitTransition = {
                        fadeOut(
                            animationSpec = tween(
                                durationMillis = 220,
                                easing = OtsoMotion.easeInOut,
                            )
                        ) + slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(
                                durationMillis = 280,
                                easing = OtsoMotion.easeInOut,
                            ),
                        )
                    }
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
