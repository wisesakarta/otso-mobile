package com.otso.app.logic

import androidx.compose.ui.text.input.TextFieldValue
import com.otso.app.model.TabDocument
import com.otso.app.model.TabSource
import com.otso.app.viewmodel.EditorUiState
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TabLifecycleEngine(
    private val _uiState: MutableStateFlow<EditorUiState>,
    private val fileIOEngine: FileIOEngine,
    private val scope: CoroutineScope,
    private val onToggleTabSwitcher: (Boolean) -> Unit,
) {
    fun newTab(preferredFontSizeSp: Int) {
        _uiState.update { state ->
            val newTab = TabDocument(
                id = UUID.randomUUID().toString(),
                title = "Untitled",
                fontSizeSp = preferredFontSizeSp,
            )
            val newTabs = state.tabs + newTab
            val newValues = state.textFieldValues.toMutableMap().apply {
                put(newTab.id, TextFieldValue(newTab.content))
            }
            state.copy(
                tabs = newTabs,
                activeIndex = newTabs.lastIndex,
                textFieldValues = newValues,
            )
        }
    }

    fun switchTab(index: Int) {
        _uiState.update { state ->
            state.copy(activeIndex = index.coerceIn(0, state.tabs.lastIndex))
        }
        onToggleTabSwitcher(false)
    }

    fun closeTab(index: Int) {
        val tab = _uiState.value.tabs.getOrNull(index) ?: return
        if (tab.isModified) {
            _uiState.update { it.copy(pendingCloseTabIndex = index, showUnsavedDialog = true) }
        } else {
            executeCloseTab(index)
        }
    }

    fun executeCloseTab(index: Int) {
        val removedTab = _uiState.value.tabs.getOrNull(index) ?: return
        _uiState.update { state ->
            if (state.tabs.size == 1 || index !in state.tabs.indices) return@update state
            val removed = state.tabs[index]
            val newTabs = state.tabs.toMutableList().also { it.removeAt(index) }
            val newValues = state.textFieldValues.toMutableMap().also { it.remove(removed.id) }
            val newActive = (index - 1).coerceAtLeast(0).coerceAtMost(newTabs.lastIndex)
            state.copy(
                tabs = newTabs,
                activeIndex = newActive,
                textFieldValues = newValues,
            )
        }


        // Cleanup internal scratch file
        if (removedTab.source == TabSource.INTERNAL && removedTab.uriOrPath.isNullOrBlank()) {
            scope.launch {
                fileIOEngine.deleteInternal(resolveFileName(removedTab.title, removedTab.id))
            }
        }
    }

    private fun resolveFileName(title: String, id: String): String {
        val cleanTitle = title.trim()
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .ifBlank { "note_$id" }
        return if (cleanTitle.endsWith(".txt")) cleanTitle else "$cleanTitle.txt"
    }
}
