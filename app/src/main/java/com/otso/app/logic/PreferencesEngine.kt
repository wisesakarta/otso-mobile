package com.otso.app.logic

import android.app.Application
import com.otso.app.core.OtsoPreferences
import com.otso.app.viewmodel.EditorUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PreferencesEngine(
    private val application: Application,
    private val _uiState: MutableStateFlow<EditorUiState>,
    private val scope: CoroutineScope,
) {
    private companion object {
        const val MIN_EDITOR_FONT_SP = 10
        const val MAX_EDITOR_FONT_SP = 64
        const val GESTURE_COMMIT_DEBOUNCE_MS = 80L
    }

    private var gestureFontCommitJob: Job? = null

    fun initializeFlows(
        isSystemDarkMode: () -> Boolean,
        onFontSizeLoaded: (Int) -> Unit = {},
    ) {
        scope.launch {
            OtsoPreferences.themeModeFlow(application).collect { mode ->
                val isDark = when (mode) {
                    "dark" -> true
                    "light" -> false
                    else -> isSystemDarkMode()
                }
                val wasDark = _uiState.value.isDarkMode
                _uiState.update { it.copy(isDarkMode = isDark, themeMode = mode) }
            }
        }
        scope.launch {
            OtsoPreferences.editorFontSizeFlow(application).collect { size ->
                onFontSizeLoaded(size)
                _uiState.update { state ->
                    state.copy(
                        font = state.font.copy(editorFontSize = size),
                        tabs = state.tabs.map { tab -> tab.copy(fontSizeSp = size) },
                    )
                }
            }
        }
    }

    fun setThemeMode(mode: String) {
        val normalized = when (mode) {
            "dark", "light", "system" -> mode
            else -> "system"
        }
        scope.launch {
            OtsoPreferences.setThemeMode(application, normalized)
        }
    }

    fun setEditorFontSize(sizeSp: Int) {
        gestureFontCommitJob?.cancel()
        val normalized = sizeSp.coerceIn(MIN_EDITOR_FONT_SP, MAX_EDITOR_FONT_SP)
        scope.launch {
            OtsoPreferences.setEditorFontSize(application, normalized)
        }
    }

    fun commitEditorFontSizeFromGesture(sizeSp: Int) {
        val normalized = sizeSp.coerceIn(MIN_EDITOR_FONT_SP, MAX_EDITOR_FONT_SP)
        gestureFontCommitJob?.cancel()
        gestureFontCommitJob = scope.launch {
            delay(GESTURE_COMMIT_DEBOUNCE_MS)
            OtsoPreferences.setEditorFontSize(application, normalized)
        }
    }
}
