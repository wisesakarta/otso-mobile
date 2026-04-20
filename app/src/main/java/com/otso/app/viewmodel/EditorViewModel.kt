package com.otso.app.viewmodel

import android.app.Application
import android.content.res.Configuration
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.otso.app.core.FileIO
import com.otso.app.core.FontManager
import com.otso.app.core.OcrEngine
import com.otso.app.core.OtsoPreferences
import com.otso.app.core.SessionIO
import com.otso.app.core.TextCodec
import com.otso.app.model.TabDocument
import com.otso.app.model.TabSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class EditorUiState(
    val tabs: List<TabDocument> = listOf(TabDocument()),
    val activeIndex: Int = 0,
    val isMenuOpen: Boolean = false,
    val isFindOpen: Boolean = false,
    val isDarkMode: Boolean = true,
    val textFieldValues: Map<String, TextFieldValue> = emptyMap(),
    val isTabSwitcherOpen: Boolean = false,
    val isSaving: Boolean = false,
    val themeMode: String = "system",
    val editorFontSizeSp: Int = 15,
    val findQuery: String = "",
    val replaceQuery: String = "",
    val findMatches: List<IntRange> = emptyList(),
    val findActiveIndex: Int = -1,
    val findCaseSensitive: Boolean = false,
    val pendingCloseTabIndex: Int? = null,
    val showUnsavedDialog: Boolean = false,
    val editingTabIndex: Int? = null,
    val editingTabName: String = "",
    val customFontPath: String? = null,
    val customFontName: String? = null,
    val fileAccessError: String? = null,
    val isOcrProcessing: Boolean = false,
    val ocrEngineLabel: String = "",
)

class EditorViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()
    private val sessionIO = SessionIO(getApplication())
    private var preferredFontSizeSp: Int = 15
    private var isSessionRestored = false

    val activeTab: TabDocument
        get() = _uiState.value.tabs[_uiState.value.activeIndex]

    init {
        FileIO.bindContext(getApplication())
        viewModelScope.launch {
            val restored = sessionIO.loadSession()
            if (restored != null && restored.tabs.isNotEmpty()) {
                val normalizedIndex = restored.activeIndex.coerceIn(0, restored.tabs.lastIndex)
                val restoredValues = restored.tabs.associate { tab ->
                    tab.id to TextFieldValue(tab.content)
                }
                _uiState.value = EditorUiState(
                    tabs = restored.tabs,
                    activeIndex = normalizedIndex,
                    isDarkMode = restored.isDarkMode,
                    textFieldValues = restoredValues,
                    themeMode = "system",
                    editorFontSizeSp = preferredFontSizeSp,
                )
            } else {
                val defaultTab = TabDocument(
                    id = UUID.randomUUID().toString(),
                    title = "Untitled",
                    fontSizeSp = preferredFontSizeSp,
                )
                _uiState.value = EditorUiState(
                    tabs = listOf(defaultTab),
                    activeIndex = 0,
                    textFieldValues = mapOf(defaultTab.id to TextFieldValue("")),
                    isDarkMode = isSystemDarkMode(),
                    themeMode = "system",
                    editorFontSizeSp = preferredFontSizeSp,
                )
            }
            isSessionRestored = true
            startAutoSave()
        }
        viewModelScope.launch {
            OtsoPreferences.themeModeFlow(getApplication()).collect { mode ->
                val isDark = when (mode) {
                    "dark" -> true
                    "light" -> false
                    else -> isSystemDarkMode()
                }
                _uiState.update { it.copy(isDarkMode = isDark, themeMode = mode) }
            }
        }
        viewModelScope.launch {
            OtsoPreferences.editorFontSizeFlow(getApplication()).collect { size ->
                preferredFontSizeSp = size
                _uiState.update { state ->
                    state.copy(
                        editorFontSizeSp = size,
                        tabs = state.tabs.map { tab -> tab.copy(fontSizeSp = size) },
                    )
                }
            }
        }
        viewModelScope.launch {
            OtsoPreferences.customFontPathFlow(getApplication()).collect { path ->
                _uiState.update { it.copy(customFontPath = path) }
            }
        }
        viewModelScope.launch {
            OtsoPreferences.customFontNameFlow(getApplication()).collect { name ->
                _uiState.update { it.copy(customFontName = name) }
            }
        }
    }

    fun newTab() {
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

    fun handleFileOpened(uri: Uri) {
        viewModelScope.launch {
            try {
                FileIO.takePersistableUriPermission(uri)
                val decoded = FileIO.openExternalFile(uri)
                _uiState.update { state ->
                    val newTab = TabDocument(
                        id = UUID.randomUUID().toString(),
                        title = uri.lastPathSegment?.substringAfterLast('/')?.ifBlank { "Untitled" } ?: "Untitled",
                        source = TabSource.SAF,
                        uriOrPath = uri.toString(),
                        content = decoded.content,
                        fontSizeSp = preferredFontSizeSp,
                        encoding = decoded.encoding,
                        lineEnding = decoded.lineEnding,
                        isModified = false,
                    )
                    val newTabs = state.tabs + newTab
                    val newValues = state.textFieldValues.toMutableMap().apply {
                        put(newTab.id, TextFieldValue(decoded.content))
                    }
                    state.copy(
                        tabs = newTabs,
                        activeIndex = newTabs.lastIndex,
                        textFieldValues = newValues,
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(fileAccessError = "Cannot access file. Permission may have been revoked.")
                }
            }
        }
    }

    fun importImageAsText(uri: Uri) {
        importScannedUris(listOf(uri))
    }

    fun importScannedText(uri: Uri) {
        // Surgical: Reuse existing robust pipeline
        importScannedUris(listOf(uri))
    }

    fun importScannedUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isOcrProcessing = true, ocrEngineLabel = "") }
            try {
                val combinedText = StringBuilder()
                var lastEngine = ""
                
                for (uri in uris) {
                    val output = withContext(Dispatchers.IO) {
                        OcrEngine.extract(getApplication(), uri)
                    }
                    if (output.text.isNotBlank()) {
                        if (combinedText.isNotEmpty()) combinedText.append("\n\n")
                        combinedText.append(output.text.trim())
                    }
                    lastEngine = output.engineUsed
                }

                val textToInsert = combinedText.toString()
                if (textToInsert.isBlank()) {
                    _uiState.update {
                        it.copy(fileAccessError = "No text detected from scan.", ocrEngineLabel = lastEngine)
                    }
                    return@launch
                }

                val active = activeTab
                val current = getTextFieldValue(active.id)
                
                // Smart newline prefix if current line isn't empty
                val insert = buildString {
                    if (current.text.isNotEmpty() && current.selection.start > 0 && current.text[current.selection.start - 1] != '\n') {
                        append('\n')
                    }
                    append(textToInsert)
                }
                
                insertTextAtCursor(active.id, insert)
                _uiState.update { it.copy(ocrEngineLabel = lastEngine) }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(fileAccessError = "Failed to process scan.")
                }
            } finally {
                _uiState.update { it.copy(isOcrProcessing = false) }
            }
        }
    }

    fun setThemeMode(mode: String) {
        val normalized = when (mode) {
            "dark", "light", "system" -> mode
            else -> "system"
        }
        viewModelScope.launch {
            OtsoPreferences.setThemeMode(getApplication(), normalized)
        }
    }

    fun setEditorFontSize(sizeSp: Int) {
        val normalized = sizeSp.coerceIn(10, 72)
        viewModelScope.launch {
            OtsoPreferences.setEditorFontSize(getApplication(), normalized)
        }
    }

    fun updateFontSizeTemp(newSize: Int) {
        val normalized = newSize.coerceIn(10, 72)
        _uiState.update { state ->
            state.copy(
                editorFontSizeSp = normalized,
                tabs = state.tabs.map { it.copy(fontSizeSp = normalized) }
            )
        }
    }

    fun toggleTabSwitcher(open: Boolean) {
        _uiState.update { it.copy(isTabSwitcherOpen = open) }
    }

    fun switchTab(index: Int) {
        _uiState.update { state ->
            state.copy(activeIndex = index.coerceIn(0, state.tabs.lastIndex))
        }
        toggleTabSwitcher(false)
    }

    fun closeTab(index: Int) {
        val tab = _uiState.value.tabs.getOrNull(index) ?: return
        if (tab.isModified) {
            _uiState.update { it.copy(pendingCloseTabIndex = index, showUnsavedDialog = true) }
        } else {
            executeCloseTab(index)
        }
    }

    private fun executeCloseTab(index: Int) {
        val removedTab = _uiState.value.tabs.getOrNull(index) ?: return
        _uiState.update { state ->
            if (state.tabs.size == 1 || index !in state.tabs.indices) return@update state
            val removed = state.tabs[index]
            val newTabs = state.tabs.toMutableList().also { it.removeAt(index) }
            val newValues = state.textFieldValues.toMutableMap().also { it.remove(removed.id) }
            val newActive = (index - 1).coerceAtLeast(0).coerceAtMost(newTabs.lastIndex)
            state.copy(tabs = newTabs, activeIndex = newActive, textFieldValues = newValues)
        }
        
        // Cleanup internal scratch file
        if (removedTab.source == TabSource.INTERNAL && removedTab.uriOrPath.isNullOrBlank()) {
            viewModelScope.launch {
                FileIO.deleteInternal(
                    context = getApplication(),
                    fileName = resolveFileName(removedTab.title, removedTab.id),
                )
            }
        }
    }

    fun cancelCloseTab() {
        _uiState.update { it.copy(showUnsavedDialog = false, pendingCloseTabIndex = null) }
    }

    fun discardAndCloseTab() {
        val index = _uiState.value.pendingCloseTabIndex ?: return
        executeCloseTab(index)
        cancelCloseTab()
    }

    fun saveAndCloseTab() {
        val index = _uiState.value.pendingCloseTabIndex ?: return
        viewModelScope.launch {
            saveTab(index)
            executeCloseTab(index)
            cancelCloseTab()
        }
    }

    fun updateTextFieldValue(tabId: String, value: TextFieldValue) {
        _uiState.update { state ->
            val newValues = state.textFieldValues.toMutableMap()
            newValues[tabId] = value

            val newTabs = state.tabs.toMutableList()
            val tabIndex = newTabs.indexOfFirst { it.id == tabId }
            if (tabIndex >= 0) {
                val tab = newTabs[tabIndex]
                val (cursorLine, cursorCol) = calculateCursor(value.text, value.selection.end)
                newTabs[tabIndex] = tab.copy(
                    content = value.text,
                    isModified = value.text != tab.content || tab.isModified,
                    cursorLine = cursorLine,
                    cursorCol = cursorCol,
                )
            }

            state.copy(textFieldValues = newValues, tabs = newTabs)
        }
    }

    fun getTextFieldValue(tabId: String): TextFieldValue {
        val state = _uiState.value
        return state.textFieldValues[tabId]
            ?: TextFieldValue(state.tabs.find { it.id == tabId }?.content ?: "")
    }

    fun insertTextAtCursor(tabId: String, insert: String) {
        val current = getTextFieldValue(tabId)
        val start = current.selection.start.coerceIn(0, current.text.length)
        val end = current.selection.end.coerceIn(0, current.text.length)
        val newText = buildString {
            append(current.text.substring(0, start))
            append(insert)
            append(current.text.substring(end))
        }
        val caret = start + insert.length
        updateTextFieldValue(
            tabId,
            current.copy(
                text = newText,
                selection = TextRange(caret),
            ),
        )
    }

    fun updateContent(content: String) {
        val tabId = _uiState.value.tabs[_uiState.value.activeIndex].id
        updateTextFieldValue(tabId, TextFieldValue(content))
    }

    fun readInternal(fileName: String) {
        viewModelScope.launch {
            val bytes = FileIO.readInternalBytes(
                context = getApplication(),
                fileName = fileName,
            )
            if (bytes.isEmpty()) return@launch

            val decoded = TextCodec.decode(bytes)
            _uiState.update { state ->
                val tabs = state.tabs.toMutableList()
                val activeIndex = state.activeIndex.coerceIn(0, tabs.lastIndex)
                val active = tabs[activeIndex]
                tabs[activeIndex] = active.copy(
                    content = decoded.content,
                    encoding = decoded.encoding,
                    lineEnding = decoded.lineEnding,
                    uriOrPath = fileName,
                    isModified = false,
                )

                val values = state.textFieldValues.toMutableMap().apply {
                    put(active.id, TextFieldValue(decoded.content))
                }
                state.copy(tabs = tabs, textFieldValues = values)
            }
        }
    }

    fun saveActiveTab() {
        viewModelScope.launch {
            saveTab(_uiState.value.activeIndex)
        }
    }

    private suspend fun saveTab(index: Int) {
        val state = _uiState.value
        val targetTab = state.tabs.getOrNull(index) ?: return
        val value = getTextFieldValue(targetTab.id)
        val fileName = resolveFileName(targetTab.title, targetTab.id)

        _uiState.update { it.copy(isSaving = true) }
        try {
            if (targetTab.source == TabSource.SAF && !targetTab.uriOrPath.isNullOrBlank()) {
                FileIO.saveExternalFile(
                    uri = Uri.parse(targetTab.uriOrPath),
                    content = value.text,
                    encoding = targetTab.encoding,
                    lineEnding = targetTab.lineEnding,
                )
            } else {
                val encoded = TextCodec.encode(
                    text = value.text,
                    encoding = targetTab.encoding,
                    lineEnding = targetTab.lineEnding,
                )
                FileIO.saveInternalBytes(
                    context = getApplication(),
                    fileName = fileName,
                    bytes = encoded,
                )
            }

            _uiState.update { current ->
                val updatedTabs = current.tabs.toMutableList()
                if (index in updatedTabs.indices) {
                    updatedTabs[index] = updatedTabs[index].copy(
                        content = value.text,
                        isModified = false,
                        uriOrPath = if (targetTab.source == TabSource.SAF) targetTab.uriOrPath else fileName,
                    )
                }
                current.copy(
                    tabs = updatedTabs,
                    isSaving = false,
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isSaving = false,
                    fileAccessError = "Failed to save file. Please try Save As.",
                )
            }
        }
    }

    private fun startAutoSave() {
        viewModelScope.launch {
            uiState
                .drop(1)
                .collect { state ->
                    if (isSessionRestored) {
                        sessionIO.saveSession(state)
                    }
                }
        }
    }

    fun toggleMenu(open: Boolean) {
        _uiState.update { it.copy(isMenuOpen = open) }
    }

    fun toggleFind() {
        _uiState.update { it.copy(isFindOpen = !it.isFindOpen) }
    }

    fun clearFileAccessError() {
        _uiState.update { it.copy(fileAccessError = null) }
    }

    // --- RENAMING LOGIC ---

    fun startEditingTab(index: Int) {
        val tab = _uiState.value.tabs.getOrNull(index) ?: return
        _uiState.update { it.copy(
            editingTabIndex = index,
            editingTabName = tab.title
        ) }
    }

    fun updateEditingTabName(newName: String) {
        _uiState.update { state ->
            val newTabs = state.tabs.toMutableList()
            val idx = state.editingTabIndex
            if (idx != null && idx in newTabs.indices) {
                newTabs[idx] = newTabs[idx].copy(title = newName)
            }
            state.copy(
                editingTabName = newName,
                tabs = newTabs,
            )
        }
    }

    fun cancelEditingTab() {
        _uiState.update { it.copy(editingTabIndex = null, editingTabName = "") }
    }

    fun finishEditingTab() {
        val state = _uiState.value
        val index = state.editingTabIndex ?: return
        val newName = state.editingTabName.trim()
        val tab = state.tabs.getOrNull(index) ?: return

        if (newName.isEmpty()) {
            cancelEditingTab()
            return
        }

        viewModelScope.launch {
            try {
                if (tab.source == TabSource.SAF && tab.uriOrPath != null) {
                    val uri = Uri.parse(tab.uriOrPath)
                    val contentResolver = getApplication<Application>().contentResolver
                    
                    val resultUri = withContext(Dispatchers.IO) {
                        DocumentsContract.renameDocument(contentResolver, uri, newName)
                    }
                    _uiState.update { s ->
                        val updatedTabs = s.tabs.toMutableList()
                        updatedTabs[index] = updatedTabs[index].copy(
                            title = newName,
                            uriOrPath = resultUri?.toString() ?: updatedTabs[index].uriOrPath
                        )
                        s.copy(tabs = updatedTabs)
                    }
                } else {
                    // Internal file, just update memory
                    _uiState.update { s ->
                        val updatedTabs = s.tabs.toMutableList()
                        updatedTabs[index] = updatedTabs[index].copy(title = newName)
                        s.copy(tabs = updatedTabs)
                    }
                }
            } catch (e: Exception) {
                // Catch SecurityException or illegal characters
                e.printStackTrace()
            } finally {
                cancelEditingTab()
            }
        }
    }

    fun updateFindQuery(query: String) {
        _uiState.update { state ->
            val matches = if (query.isBlank()) emptyList()
            else findMatches(
                text = getTextFieldValue(state.tabs[state.activeIndex].id).text,
                query = query,
                caseSensitive = state.findCaseSensitive,
            )
            state.copy(
                findQuery = query,
                findMatches = matches,
                findActiveIndex = if (matches.isNotEmpty()) 0 else -1,
            )
        }
    }

    fun updateReplaceQuery(query: String) {
        _uiState.update { it.copy(replaceQuery = query) }
    }

    fun findNext() {
        _uiState.update { state ->
            if (state.findMatches.isEmpty()) return@update state
            val next = (state.findActiveIndex + 1) % state.findMatches.size
            state.copy(findActiveIndex = next)
        }
        scrollToActiveMatch()
    }

    fun findPrevious() {
        _uiState.update { state ->
            if (state.findMatches.isEmpty()) return@update state
            val prev = if (state.findActiveIndex <= 0)
                state.findMatches.lastIndex
            else
                state.findActiveIndex - 1
            state.copy(findActiveIndex = prev)
        }
        scrollToActiveMatch()
    }

    fun replaceCurrent() {
        val state = _uiState.value
        val activeMatch = state.findMatches.getOrNull(state.findActiveIndex) ?: return
        val tabId = state.tabs[state.activeIndex].id
        val current = getTextFieldValue(tabId)
        val newText = current.text.replaceRange(activeMatch, state.replaceQuery)
        updateTextFieldValue(tabId, TextFieldValue(
            text = newText,
            selection = TextRange(activeMatch.first + state.replaceQuery.length),
        ))
        // Re-run find setelah replace
        updateFindQuery(state.findQuery)
    }

    fun replaceAll() {
        val state = _uiState.value
        if (state.findQuery.isBlank()) return
        val tabId = state.tabs[state.activeIndex].id
        val current = getTextFieldValue(tabId)
        val newText = if (state.findCaseSensitive)
            current.text.replace(state.findQuery, state.replaceQuery)
        else
            current.text.replace(state.findQuery, state.replaceQuery, ignoreCase = true)
        updateTextFieldValue(tabId, TextFieldValue(text = newText))
        updateFindQuery(state.findQuery)
    }

    fun toggleFindCaseSensitive() {
        _uiState.update { it.copy(findCaseSensitive = !it.findCaseSensitive) }
        // Re-run find dengan sensitivity baru
        updateFindQuery(_uiState.value.findQuery)
    }

    fun closeFindBar() {
        _uiState.update { current ->
            current.copy(
                isFindOpen = false,
                findQuery = "",
                replaceQuery = "",
                findMatches = emptyList(),
                findActiveIndex = -1,
            )
        }
    }

    private fun findMatches(text: String, query: String, caseSensitive: Boolean): List<IntRange> {
        if (query.isBlank()) return emptyList()
        val results = mutableListOf<IntRange>()
        val searchText = if (caseSensitive) text else text.lowercase()
        val searchQuery = if (caseSensitive) query else query.lowercase()
        var startIndex = 0
        while (startIndex < searchText.length) {
            val index = searchText.indexOf(searchQuery, startIndex)
            if (index == -1) break
            results.add(index until index + query.length)
            startIndex = index + 1
        }
        return results
    }

    private fun scrollToActiveMatch() {
        val state = _uiState.value
        val activeMatch = state.findMatches.getOrNull(state.findActiveIndex) ?: return
        val tabId = state.tabs[state.activeIndex].id
        val current = getTextFieldValue(tabId)
        updateTextFieldValue(tabId, current.copy(
            selection = TextRange(activeMatch.first, activeMatch.last + 1),
        ))
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    private fun calculateCursor(text: String, offset: Int): Pair<Int, Int> {
        val safeOffset = offset.coerceIn(0, text.length)
        val prefix = text.substring(0, safeOffset)
        val line = prefix.count { it == '\n' }
        val col = prefix.substringAfterLast('\n', "").length
        return line to col
    }

    private fun resolveFileName(title: String, id: String): String {
        val cleanTitle = title.trim()
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .ifBlank { "note_$id" }
        return if (cleanTitle.endsWith(".txt")) cleanTitle else "$cleanTitle.txt"
    }

    private fun isSystemDarkMode(): Boolean {
        val nightMode = getApplication<Application>().resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK
        return nightMode == Configuration.UI_MODE_NIGHT_YES
    }

    // --- CUSTOM FONT ENGINE ---

    fun importCustomFont(uri: Uri) {
        viewModelScope.launch {
            val fontMeta = withContext(Dispatchers.IO) {
                resolveFontMeta(uri)
            }
            if (fontMeta == null) {
                return@launch
            }
            val (fontName, extension) = fontMeta
            val importedFile = withContext(Dispatchers.IO) {
                FontManager.importFontFromUri(
                    context = getApplication(),
                    uri = uri,
                    extension = extension,
                )
            }
            if (importedFile != null && importedFile.exists()) {
                OtsoPreferences.setCustomFontPath(getApplication(), importedFile.absolutePath)
                OtsoPreferences.setCustomFontName(getApplication(), fontName)
            }
        }
    }

    fun resetCustomFont() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                FontManager.deleteCustomFont(getApplication())
            }
            OtsoPreferences.setCustomFontPath(getApplication(), null)
            OtsoPreferences.setCustomFontName(getApplication(), null)
        }
    }

    private fun resolveFontMeta(uri: Uri): Pair<String, String>? {
        val contentResolver = getApplication<Application>().contentResolver
        val displayName = try {
            contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null,
            )?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
            }
        } catch (_: Exception) {
            null
        }

        val candidate = displayName ?: uri.lastPathSegment ?: return null
        val extension = when {
            candidate.endsWith(".ttf", ignoreCase = true) -> "ttf"
            candidate.endsWith(".otf", ignoreCase = true) -> "otf"
            else -> return null
        }
        return candidate.substringAfterLast('/').trim() to extension
    }
}
