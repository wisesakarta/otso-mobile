package com.otso.app.viewmodel

import android.app.Application
import android.content.res.Configuration
import android.net.Uri
import android.provider.DocumentsContract
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.otso.app.core.FontManager
import com.otso.app.core.IntelligenceEngine
import com.otso.app.core.OcrEngine
import com.otso.app.core.OtsoPreferences
import com.otso.app.core.SessionIO
import com.otso.app.core.TranslationEngine
import com.otso.app.logic.DocumentData
import com.otso.app.logic.FileIOEngine
import com.otso.app.logic.FontFoundryEngine
import com.otso.app.logic.FindReplaceEngine
import com.otso.app.logic.PreferencesEngine
import com.otso.app.logic.ReplaceResult
import com.otso.app.logic.TabLifecycleEngine
import com.otso.app.logic.toContentBlock
import com.otso.app.model.ContentBlock
import com.otso.app.model.TabDocument
import com.otso.app.model.TabSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

data class FindReplaceState(
    val findQuery: String = "",
    val replaceQuery: String = "",
    val matches: List<IntRange> = emptyList(),
    val activeMatchIndex: Int = -1,
    val isFindBarVisible: Boolean = false,
    val isCaseSensitive: Boolean = false,
)

data class TranslationState(
    val isTranslationDialogOpen: Boolean = false,
    val translationSourceTag: String = "auto",
    val translationTargetTag: String = "en",
    val isTranslating: Boolean = false,
)

data class OcrState(
    val isOcrProcessing: Boolean = false,
    val ocrError: String? = null,
)

data class FontState(
    val activeFoundryFamily: FontFamily? = null,
    val activeFoundryVariantCount: Int = 0,
    val editorFontSize: Int = 15,
    val isMonospace: Boolean = false,
)

sealed interface EditorEvent {
    data class InsertTextAtSelection(
        val text: String,
    ) : EditorEvent
}

data class EditorUiState(
    val tabs: List<TabDocument> = listOf(TabDocument()),
    val activeIndex: Int = 0,
    val isMenuOpen: Boolean = false,
    val isDarkMode: Boolean = true,
    val textFieldValues: Map<String, TextFieldValue> = emptyMap(),
    val isTabSwitcherOpen: Boolean = false,
    val isSaving: Boolean = false,
    val themeMode: String = "system",
    val findReplace: FindReplaceState = FindReplaceState(),
    val translation: TranslationState = TranslationState(),
    val ocr: OcrState = OcrState(),
    val font: FontState = FontState(),
    val pendingCloseTabIndex: Int? = null,
    val showUnsavedDialog: Boolean = false,
    val editingTabIndex: Int? = null,
    val editingTabName: String = "",
    val customFontPath: String? = null,
    val customFontName: String? = null,
    val foundryFolderUri: String? = null,
    val activeFoundryFamilyName: String? = null,
    val fileAccessError: String? = null,
    val ocrEngineLabel: String = "",
    val showLinkDialog: Boolean = false,
    val linkDialogUrl: String = "",
    val pendingLinkTabId: String? = null,
    val customHighlightPalette: List<Int> = emptyList(),
    // False until font state is fully resolved on cold start.
    // EditorScreen uses this to suppress FOUT (Flash of Default Font).
    val isFontInitialized: Boolean = false,

 // key = tab.id
)


class EditorViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private companion object {
        const val MAX_CUSTOM_HIGHLIGHT_COLORS = 12
        const val MIN_EDITOR_FONT_SP = 10
        const val MAX_EDITOR_FONT_SP = 64
    }

    private val _uiState = MutableStateFlow(EditorUiState())
    private val _editorEvents = MutableSharedFlow<EditorEvent>(extraBufferCapacity = 1)
    private val findReplaceEngine = FindReplaceEngine()

    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()
    val editorEvents: SharedFlow<EditorEvent> = _editorEvents.asSharedFlow()
    private val sessionIO = SessionIO(getApplication())
    private val fileIOEngine = FileIOEngine(getApplication())
    private lateinit var tabLifecycleEngine: TabLifecycleEngine
    private lateinit var fontFoundryEngine: FontFoundryEngine
    private lateinit var preferencesEngine: PreferencesEngine
    private var preferredFontSizeSp: Int = 15
    private var isSessionRestored = false

    val activeTab: TabDocument
        get() = _uiState.value.tabs[_uiState.value.activeIndex]

    init {
        tabLifecycleEngine = TabLifecycleEngine(
            _uiState = _uiState,
            fileIOEngine = fileIOEngine,
            scope = viewModelScope,
            onToggleTabSwitcher = { isOpen -> toggleTabSwitcher(isOpen) },
        )
        FontManager.bindContext(getApplication())
        fontFoundryEngine = FontFoundryEngine(
            application = getApplication(),
            _uiState = _uiState,
            scope = viewModelScope,
        )
        fontFoundryEngine.initializeFontFlow()
        preferencesEngine = PreferencesEngine(
            application = getApplication(),
            _uiState = _uiState,
            scope = viewModelScope,
        )
        preferencesEngine.initializeFlows(
            isSystemDarkMode = { isSystemDarkMode() },
            onFontSizeLoaded = { size -> preferredFontSizeSp = size },
        )
        viewModelScope.launch {
            // Eager reads: resolve font preferences before first render to prevent FOUT.
            // .first() suspends only until DataStore emits its cached disk value (~5ms).
            val initialFontPath = OtsoPreferences.customFontPathFlow(getApplication()).first()
            val initialFontName = OtsoPreferences.customFontNameFlow(getApplication()).first()
            val initialFoundryUri = OtsoPreferences.folderFoundryUriFlow(getApplication()).first()
            // No foundry scan needed → mark font as initialized now;
            // if scan is needed, FontFoundryEngine will set isFontInitialized=true after IO.
            val needsFontScan = !initialFoundryUri.isNullOrBlank()

            val restored = sessionIO.loadSession()
            if (restored != null && restored.tabs.isNotEmpty()) {
                val migratedTabs = restored.tabs.map(::migrateLoadedTab)
                val normalizedIndex = restored.activeIndex.coerceIn(0, migratedTabs.lastIndex)
                val restoredValues = migratedTabs.associate { tab ->
                    tab.id to TextFieldValue(tab.content)
                }
                // update{} (not value=) preserves isFontInitialized if FontFoundryEngine
                // already set it true in a concurrent coroutine.
                _uiState.update { current ->
                    EditorUiState(
                        tabs = migratedTabs,
                        activeIndex = normalizedIndex,
                        isDarkMode = restored.isDarkMode,
                        textFieldValues = restoredValues,
                        themeMode = "system",
                        font = FontState(editorFontSize = preferredFontSizeSp),
                        customFontPath = initialFontPath,
                        customFontName = initialFontName,
                        isFontInitialized = current.isFontInitialized || !needsFontScan,
                    )
                }
            } else {
                val defaultTab = TabDocument(
                    id = UUID.randomUUID().toString(),
                    title = "Untitled",
                    fontSizeSp = preferredFontSizeSp,
                )
                _uiState.update { current ->
                    EditorUiState(
                        tabs = listOf(defaultTab),
                        activeIndex = 0,
                        textFieldValues = mapOf(defaultTab.id to TextFieldValue("")),
                        isDarkMode = isSystemDarkMode(),
                        themeMode = "system",
                        font = FontState(editorFontSize = preferredFontSizeSp),
                        customFontPath = initialFontPath,
                        customFontName = initialFontName,
                        isFontInitialized = current.isFontInitialized || !needsFontScan,
                    )
                }
            }
            isSessionRestored = true
            startAutoSave()
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
        viewModelScope.launch {
            OtsoPreferences.customHighlightPaletteFlow(getApplication()).collect { palette ->
                _uiState.update { it.copy(customHighlightPalette = palette) }
            }
        }
    }

    fun newTab() = tabLifecycleEngine.newTab(preferredFontSizeSp)


    fun handleFileOpened(uri: Uri) {
        viewModelScope.launch {
            val openResult = fileIOEngine.openFile(uri)
            openResult
                .onSuccess { document ->
                    _uiState.update { state ->
                        val newTab = TabDocument(
                            id = UUID.randomUUID().toString(),
                            title = uri.lastPathSegment?.substringAfterLast('/')?.ifBlank { "Untitled" } ?: "Untitled",
                            source = TabSource.SAF,
                            uriOrPath = uri.toString(),
                            content = document.content,
                            spans = document.spans,
                            fontSizeSp = preferredFontSizeSp,
                            encoding = document.encoding,
                            lineEnding = document.lineEnding,
                            isModified = false,
                        )
                        val newTabs = state.tabs + newTab
                        val newValues = state.textFieldValues.toMutableMap().apply {
                            put(newTab.id, TextFieldValue(document.content))
                        }
                        state.copy(
                            tabs = newTabs,
                            activeIndex = newTabs.lastIndex,
                            textFieldValues = newValues,
                        )
                    }
                }
                .onFailure {
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

    fun openTranslationDialog() {
        if (_uiState.value.translation.isTranslating) return
        _uiState.update { state ->
            val defaultTarget = state.translation.translationTargetTag.takeIf { it.isNotBlank() && it != "auto" }
                ?: normalizeLanguageTag(Locale.getDefault().language)
            state.copy(
                translation = state.translation.copy(
                    isTranslationDialogOpen = true,
                    translationTargetTag = defaultTarget,
                ),
            )
        }
    }

    fun closeTranslationDialog() {
        _uiState.update {
            it.copy(
                translation = it.translation.copy(isTranslationDialogOpen = false),
            )
        }
    }

    fun setTranslationSourceTag(tag: String) {
        _uiState.update {
            it.copy(
                translation = it.translation.copy(
                    translationSourceTag = normalizeLanguageTag(tag, allowAuto = true),
                ),
            )
        }
    }

    fun setTranslationTargetTag(tag: String) {
        _uiState.update {
            it.copy(
                translation = it.translation.copy(
                    translationTargetTag = normalizeLanguageTag(tag),
                ),
            )
        }
    }

    fun translateText(
        sourceText: String,
        sourceLanguage: String,
        targetLanguage: String,
    ) {
        val normalizedSource = normalizeLanguageTag(sourceLanguage, allowAuto = true)
        val normalizedTarget = normalizeLanguageTag(targetLanguage)

        if (sourceText.isBlank()) {
            _uiState.update { it.copy(fileAccessError = "Nothing to translate.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    translation = it.translation.copy(
                        isTranslating = true,
                        isTranslationDialogOpen = false,
                        translationSourceTag = normalizedSource,
                        translationTargetTag = normalizedTarget,
                    ),
                )
            }
            try {
                val translated = withContext(Dispatchers.IO) {
                    TranslationEngine.translate(
                        text = sourceText,
                        source = normalizedSource,
                        target = normalizedTarget,
                    )
                }

                if (translated.isBlank()) {
                    _uiState.update { it.copy(fileAccessError = "Translation returned empty output.") }
                    return@launch
                }

                _editorEvents.emit(EditorEvent.InsertTextAtSelection(translated))
            } catch (e: Exception) {
                val message = e.message?.takeIf { it.isNotBlank() } ?: "Unknown error"
                _uiState.update { it.copy(fileAccessError = "Translation failed: $message") }
            } finally {
                _uiState.update {
                    it.copy(
                        translation = it.translation.copy(isTranslating = false),
                    )
                }
            }
        }
    }

    fun importScannedUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    ocr = it.ocr.copy(isOcrProcessing = true, ocrError = null),
                    ocrEngineLabel = "",
                )
            }
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

                val rawText = combinedText.toString()
                if (rawText.isBlank()) {
                    _uiState.update {
                        it.copy(
                            fileAccessError = "No text detected from scan.",
                            ocr = it.ocr.copy(ocrError = "No text detected from scan."),
                            ocrEngineLabel = lastEngine,
                        )
                    }
                    return@launch
                }

                // DNA: Intelligence Layer (Auto-formatting & Entity Extraction)
                val textToInsert = withContext(Dispatchers.Default) {
                    IntelligenceEngine.extractAndFormat(getApplication(), rawText)
                }

                _uiState.update { state -> 
                    // Auto-enable monospace if text looks structured (many spaces in rows)
                    val isStructured = textToInsert.lines().any { it.count { c -> c == ' ' } > 3 }
                    state.copy(
                        font = state.font.copy(isMonospace = state.font.isMonospace || isStructured),
                        ocrEngineLabel = "$lastEngine + Intel"
                    )
                }

                _editorEvents.emit(EditorEvent.InsertTextAtSelection(textToInsert))
                _uiState.update { it.copy(ocrEngineLabel = "$lastEngine + Intel") }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        fileAccessError = "Failed to process scan.",
                        ocr = it.ocr.copy(ocrError = "Failed to process scan."),
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(
                        ocr = it.ocr.copy(isOcrProcessing = false),
                    )
                }
            }
        }
    }

    fun setThemeMode(mode: String) = preferencesEngine.setThemeMode(mode)

    fun setEditorFontSize(sizeSp: Int) = preferencesEngine.setEditorFontSize(sizeSp)

    fun commitEditorFontSizeFromGesture(sizeSp: Int) = preferencesEngine.commitEditorFontSizeFromGesture(sizeSp)

    fun updateFontSizeTemp(newSize: Int) {
        val normalized = newSize.coerceIn(MIN_EDITOR_FONT_SP, MAX_EDITOR_FONT_SP)
        _uiState.update { state ->
            if (state.font.editorFontSize == normalized) {
                return@update state
            }
            state.copy(
                font = state.font.copy(editorFontSize = normalized),
                tabs = state.tabs.map { it.copy(fontSizeSp = normalized) },
            )
        }
    }

    fun toggleTabSwitcher(open: Boolean) {
        _uiState.update { it.copy(isTabSwitcherOpen = open) }
    }

    fun switchTab(index: Int) = tabLifecycleEngine.switchTab(index)

    fun closeTab(index: Int) = tabLifecycleEngine.closeTab(index)

    fun cancelCloseTab() {
        _uiState.update { it.copy(showUnsavedDialog = false, pendingCloseTabIndex = null) }
    }

    fun discardAndCloseTab() {
        val index = _uiState.value.pendingCloseTabIndex ?: return
        tabLifecycleEngine.executeCloseTab(index)
        cancelCloseTab()
    }

    fun saveAndCloseTab() {
        val index = _uiState.value.pendingCloseTabIndex ?: return
        viewModelScope.launch {
            saveTab(index)
            tabLifecycleEngine.executeCloseTab(index)
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
                val contentChanged = value.text != tab.content
                newTabs[tabIndex] = tab.copy(
                    content = value.text,
                    spans = if (contentChanged) emptyList() else tab.spans,
                    isModified = tab.isModified || contentChanged,
                )
            }

            state.copy(textFieldValues = newValues, tabs = newTabs)
        }
    }

    fun updateContentBlock(
        tabId: String,
        block: ContentBlock,
        selection: TextRange = TextRange(block.rawText.length),
    ) {
        _uiState.update { state ->
            val tabIndex = state.tabs.indexOfFirst { it.id == tabId }
            if (tabIndex < 0) return@update state

            val safeSelection = TextRange(
                start = selection.start.coerceIn(0, block.rawText.length),
                end = selection.end.coerceIn(0, block.rawText.length),
            )
            val currentTfv = state.textFieldValues[tabId]
            val tab = state.tabs[tabIndex]
            val contentChanged = tab.content != block.rawText
            val spansChanged = tab.spans != block.spans
            val selectionChanged = currentTfv?.selection != safeSelection

            if (!contentChanged && !spansChanged && !selectionChanged && currentTfv?.text == block.rawText) {
                return@update state
            }

            val newValues = state.textFieldValues.toMutableMap().apply {
                put(tabId, TextFieldValue(text = block.rawText, selection = safeSelection))
            }
            val newTabs = state.tabs.toMutableList().apply {
                this[tabIndex] = tab.copy(
                    content = block.rawText,
                    spans = block.spans,
                    isModified = tab.isModified || contentChanged || spansChanged,
                )
            }
            state.copy(textFieldValues = newValues, tabs = newTabs)
        }
    }


    // --- LINK DIALOG LOGIC ---

    fun openLinkDialog(tabId: String) {
        _uiState.update {
            it.copy(
                showLinkDialog = true,
                linkDialogUrl = "https://",
                pendingLinkTabId = tabId,
            )
        }
    }

    fun updateLinkDialogUrl(url: String) {
        _uiState.update { it.copy(linkDialogUrl = url) }
    }

    fun closeLinkDialog() {
        _uiState.update { it.copy(showLinkDialog = false, pendingLinkTabId = null) }
    }


    fun applyLink() {
        val state = _uiState.value
        val tabId = state.pendingLinkTabId ?: return
        val url = state.linkDialogUrl.trim()
        if (url.isNotBlank()) {
            val tfv = getTextFieldValue(tabId)
            val start = tfv.selection.min.coerceIn(0, tfv.text.length)
            val end = tfv.selection.max.coerceIn(0, tfv.text.length)
            val selectedText = tfv.text.substring(start, end)
            val linkMarkdown = "[$selectedText]($url)"
            val newText = tfv.text.substring(0, start) + linkMarkdown + tfv.text.substring(end)
            val caret = start + linkMarkdown.length
            updateTextFieldValue(tabId, tfv.copy(text = newText, selection = TextRange(caret)))
        }
        closeLinkDialog()
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
        updateTextFieldValue(tabId, TextFieldValue(text = content, selection = TextRange(content.length)))
    }

    fun readInternal(fileName: String) {
        viewModelScope.launch {
            val document = fileIOEngine.readInternal(fileName).getOrNull() ?: return@launch
            if (!document.hasContentBytes) return@launch
            _uiState.update { state ->
                val tabs = state.tabs.toMutableList()
                val activeIndex = state.activeIndex.coerceIn(0, tabs.lastIndex)
                val active = tabs[activeIndex]
                tabs[activeIndex] = active.copy(
                    content = document.content,
                    spans = document.spans,
                    encoding = document.encoding,
                    lineEnding = document.lineEnding,
                    uriOrPath = fileName,
                    isModified = false,
                )

                val values = state.textFieldValues.toMutableMap().apply {
                    put(active.id, TextFieldValue(document.content))
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

    fun saveTabAs(tabId: String, uri: Uri) {
        viewModelScope.launch {
            val state = _uiState.value
            val tab = state.tabs.find { it.id == tabId } ?: return@launch
            val value = getTextFieldValue(tabId)
            _uiState.update { it.copy(isSaving = true) }
            val saveResult = fileIOEngine.saveFile(
                data = DocumentData(
                    content = value.text,
                    encoding = tab.encoding,
                    lineEnding = tab.lineEnding,
                ),
                uri = uri,
            )
            saveResult
                .onSuccess {
                    _uiState.update { current ->
                        val updatedTabs = current.tabs.toMutableList()
                        val idx = updatedTabs.indexOfFirst { it.id == tabId }
                        if (idx >= 0) {
                            updatedTabs[idx] = updatedTabs[idx].copy(
                                source = TabSource.SAF,
                                uriOrPath = uri.toString(),
                                isModified = false,
                            )
                        }
                        current.copy(tabs = updatedTabs, isSaving = false)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            fileAccessError = "Save As failed: ${error.message ?: "Unknown error"}",
                        )
                    }
                }
        }
    }

    private suspend fun saveTab(index: Int) {
        val state = _uiState.value
        val targetTab = state.tabs.getOrNull(index) ?: return
        val value = getTextFieldValue(targetTab.id)
        val fileName = resolveFileName(targetTab.title, targetTab.id)
        val targetUri = if (targetTab.source == TabSource.SAF && !targetTab.uriOrPath.isNullOrBlank()) {
            Uri.parse(targetTab.uriOrPath)
        } else {
            null
        }

        _uiState.update { it.copy(isSaving = true) }
        val saveResult = fileIOEngine.saveFile(
            data = DocumentData(
                content = value.text,
                encoding = targetTab.encoding,
                lineEnding = targetTab.lineEnding,
                internalFileName = if (targetUri == null) fileName else null,
            ),
            uri = targetUri,
        )
        saveResult
            .onSuccess {
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
            }
            .onFailure {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        fileAccessError = "Failed to save file. Please try Save As.",
                    )
                }
            }
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    private fun startAutoSave() {
        viewModelScope.launch {
            uiState
                .drop(1)
                .debounce(1500L)
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
        _uiState.update { state ->
            val opening = !state.findReplace.isFindBarVisible
            if (!opening) {
                return@update state.copy(
                    findReplace = state.findReplace.copy(isFindBarVisible = false),
                )
            }

            val activeTabId = state.tabs.getOrNull(state.activeIndex)?.id
            val tfv = activeTabId?.let { state.textFieldValues[it] }
            val selectedText = tfv?.let {
                val sel = it.selection
                if (sel.start == sel.end) "" else it.text.substring(sel.min, sel.max)
            } ?: ""

            state.copy(
                findReplace = state.findReplace.copy(
                    isFindBarVisible = true,
                    findQuery = selectedText.takeIf { it.isNotBlank() } ?: state.findReplace.findQuery,
                ),
            )
        }

        val current = _uiState.value
        if (current.findReplace.findQuery.isNotBlank()) {
            updateFindQuery(current.findReplace.findQuery)
        }
    }

    fun toggleMonospace() {
        _uiState.update {
            it.copy(
                font = it.font.copy(isMonospace = !it.font.isMonospace),
            )
        }
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
        var shouldScroll = false
        _uiState.update { state ->
            val searchText = getActiveSearchText(state)
            val matches = if (query.isBlank()) emptyList()
            else findReplaceEngine.findMatches(
                text = searchText,
                query = query,
                caseSensitive = state.findReplace.isCaseSensitive,
            )
            shouldScroll = matches.isNotEmpty()
            state.copy(
                findReplace = state.findReplace.copy(
                    findQuery = query,
                    matches = matches,
                    activeMatchIndex = if (matches.isNotEmpty()) 0 else -1,
                ),
            )
        }
        if (shouldScroll) {
            scrollToActiveMatch()
        }
    }

    fun updateReplaceQuery(query: String) {
        _uiState.update {
            it.copy(
                findReplace = it.findReplace.copy(replaceQuery = query),
            )
        }
    }

    fun findNext() {
        _uiState.update { state ->
            state.copy(findReplace = findReplaceEngine.findNext(state.findReplace))
        }
        scrollToActiveMatch()
    }

    fun findPrevious() {
        _uiState.update { state ->
            state.copy(findReplace = findReplaceEngine.findPrevious(state.findReplace))
        }
        scrollToActiveMatch()
    }

    fun onReplaceCurrentRequested(currentText: String): ReplaceResult {
        val syncedState = synchronizeFindStateWithText(_uiState.value.findReplace, currentText)
        val result = findReplaceEngine.replaceCurrent(currentText, syncedState)
        _uiState.update { it.copy(findReplace = result.newState) }
        return result
    }

    fun onReplaceAllRequested(currentText: String): ReplaceResult {
        val syncedState = synchronizeFindStateWithText(_uiState.value.findReplace, currentText)
        val result = findReplaceEngine.replaceAll(currentText, syncedState)
        _uiState.update { it.copy(findReplace = result.newState) }
        return result
    }

    fun toggleFindCaseSensitive() {
        _uiState.update {
            it.copy(
                findReplace = it.findReplace.copy(isCaseSensitive = !it.findReplace.isCaseSensitive),
            )
        }
        // Re-run find dengan sensitivity baru
        updateFindQuery(_uiState.value.findReplace.findQuery)
    }

    fun closeFindBar() {
        _uiState.update { current ->
            current.copy(
                findReplace = current.findReplace.copy(
                    isFindBarVisible = false,
                    findQuery = "",
                    replaceQuery = "",
                    matches = emptyList(),
                    activeMatchIndex = -1,
                ),
            )
        }
    }

    private fun scrollToActiveMatch() {
        val state = _uiState.value
        val activeMatch = state.findReplace.matches.getOrNull(state.findReplace.activeMatchIndex) ?: return
        val tabId = state.tabs[state.activeIndex].id
        val current = getTextFieldValue(tabId)
        updateTextFieldValue(
            tabId,
            current.copy(selection = TextRange(activeMatch.first, activeMatch.last + 1)),
        )
    }

    private fun getActiveSearchText(state: EditorUiState): String {
        val activeTabId = state.tabs.getOrNull(state.activeIndex)?.id ?: return ""
        return state.textFieldValues[activeTabId]?.text
            ?: state.tabs[state.activeIndex].content
    }

    private fun synchronizeFindStateWithText(
        findState: FindReplaceState,
        currentText: String,
    ): FindReplaceState {
        val matches = findReplaceEngine.findMatches(
            text = currentText,
            query = findState.findQuery,
            caseSensitive = findState.isCaseSensitive,
        )
        val activeIndex = when {
            matches.isEmpty() -> -1
            findState.activeMatchIndex in matches.indices -> findState.activeMatchIndex
            else -> 0
        }
        return findState.copy(
            matches = matches,
            activeMatchIndex = activeIndex,
        )
    }

    private fun migrateLoadedTab(tab: TabDocument): TabDocument {
        val safeSpans = runCatching { tab.spans }.getOrDefault(emptyList())
        if (safeSpans.isNotEmpty()) {
            return tab.copy(spans = safeSpans)
        }

        val migrated = tab.content.toContentBlock()
        if (migrated.rawText == tab.content && migrated.spans.isEmpty()) {
            return tab.copy(spans = emptyList())
        }

        return tab.copy(
            content = migrated.rawText,
            spans = migrated.spans,
        )
    }

    private fun normalizeLanguageTag(tag: String, allowAuto: Boolean = false): String {
        val normalized = tag.trim().lowercase().ifBlank { "en" }
        if (allowAuto && normalized == "auto") {
            return "auto"
        }
        return normalized.substringBefore('-')
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

    fun setFoundryFolder(uri: Uri) = fontFoundryEngine.setFoundryFolder(uri)

    fun resetCustomFont() = fontFoundryEngine.resetCustomFont()

    fun addCustomHighlightColor(colorArgb: Int) {
        val current = _uiState.value.customHighlightPalette
        val updated = (current + colorArgb)
            .distinct()
            .takeLast(MAX_CUSTOM_HIGHLIGHT_COLORS)
        if (updated == current) return

        _uiState.update { it.copy(customHighlightPalette = updated) }
        viewModelScope.launch {
            OtsoPreferences.setCustomHighlightPalette(getApplication(), updated)
        }
    }

    fun removeCustomHighlightColor(colorArgb: Int) {
        val current = _uiState.value.customHighlightPalette
        val updated = current.filterNot { it == colorArgb }
        if (updated == current) return

        _uiState.update { it.copy(customHighlightPalette = updated) }
        viewModelScope.launch {
            OtsoPreferences.setCustomHighlightPalette(getApplication(), updated)
        }
    }

}
