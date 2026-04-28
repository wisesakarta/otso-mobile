package com.otso.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.animation.core.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavController
import androidx.activity.result.IntentSenderRequest
import androidx.compose.ui.platform.LocalContext
import android.app.Activity.RESULT_OK
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.otso.app.model.ContentBlock
import com.otso.app.model.TabDocument
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import com.otso.app.ui.components.OtsoEditor
import com.otso.app.ui.components.OtsoFindBar
import com.otso.app.ui.components.OtsoFormattingToolbar
import com.otso.app.ui.components.OtsoKeyboardToolbar
import com.otso.app.ui.components.OtsoColorWheelDialog
import com.otso.app.ui.components.OtsoUnsavedDialog
import com.otso.app.ui.components.OtsoMenuSheet
import com.otso.app.ui.components.OtsoAsteriskLoader
import com.otso.app.ui.components.OtsoTabBar
import com.otso.app.ui.components.OtsoTabSwitcherSheet
import com.otso.app.ui.components.OtsoTranslateDialog
import com.otso.app.ui.theme.OtsoColors
import com.otso.app.ui.theme.OtsoTypography
import com.otso.app.ui.theme.OtsoMotion
import com.otso.app.ui.theme.rememberDynamicFontFamily
import com.otso.app.ui.theme.otsoColors
import com.otso.app.ui.theme.technicalGrain
import com.otso.app.ui.theme.SquircleShape
import com.otso.app.viewmodel.EditorEvent
import com.otso.app.viewmodel.EditorViewModel
import com.otso.app.viewmodel.RichTextState

private fun normalizeHexNoHash(raw: String?): String? {
    val cleaned = raw?.trim()?.removePrefix("#") ?: return null
    if (cleaned.length != 6) return null
    return cleaned.uppercase().takeIf { it.all { c -> c.isDigit() || c in 'A'..'F' } }
}

private fun TabDocument.toContentBlockForEditor(): ContentBlock {
    return ContentBlock(rawText = content, spans = spans)
}

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    navController: NavController,
) {
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            viewModel.handleFileOpened(uri)
        }
    }
    val context = LocalContext.current
    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            // Perfectness: GMS provides high-quality rectified image
            scanResult?.pages?.firstOrNull()?.imageUri?.let { scannedUri: Uri ->
                viewModel.importScannedText(scannedUri)
            }
        }
    }
    val ocrGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        uri?.let { viewModel.importImageAsText(it) }
    }

    val fontFolderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        if (uri != null) {
            viewModel.setFoundryFolder(uri)
        }
    }

    var pendingSaveAsTabId by remember { mutableStateOf<String?>(null) }
    val saveAsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
    ) { uri ->
        if (uri != null) {
            pendingSaveAsTabId?.let { tabId -> viewModel.saveTabAs(tabId, uri) }
        }
        pendingSaveAsTabId = null
    }

    val uiState by viewModel.uiState.collectAsState()
    val activeTab = uiState.tabs.getOrNull(uiState.activeIndex)
    val lifecycleOwner = LocalLifecycleOwner.current
    var isHighlightPickerOpen by rememberSaveable { mutableStateOf(false) }
    var highlightPickerHex by rememberSaveable { mutableStateOf("F9EB73") }

    val richTextStates = remember { mutableStateMapOf<String, RichTextState>() }
    val activeRichTextState = activeTab?.let { tab ->
        richTextStates.getOrPut(tab.id) {
            RichTextState(tab.toContentBlockForEditor())
        }
    }
    val activeVmSelection = activeTab?.id?.let { tabId ->
        uiState.textFieldValues[tabId]?.selection
    }
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val focusManager = LocalFocusManager.current
    val editorScrollState: ScrollState = rememberScrollState()
    val tabSwitcherSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val menuSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // DNA: Orchestrated Physical Synchronization (Emil Design Engineering)
    // We use a spring that matches the ModalBottomSheet velocity for a "coupled" feel
    // while avoiding internal/experimental DraggableAnchors API.
    val menuProgress by animateFloatAsState(
        targetValue = if (uiState.isMenuOpen) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "menu_physical_sync"
    )
    val toolbarFadeInSpec = remember {
        tween<Float>(
            durationMillis = 150,
            easing = OtsoMotion.easeOut,
        )
    }
    val toolbarFadeOutSpec = remember {
        tween<Float>(
            durationMillis = 120,
            easing = OtsoMotion.easeInOut,
        )
    }
    val toolbarSlideSpec = remember {
        spring<IntOffset>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        )
    }
    val otsoColors = MaterialTheme.colorScheme.otsoColors

    LaunchedEffect(imeVisible) {
        if (!imeVisible) {
            focusManager.clearFocus(force = true)
        }
    }

    val latestActiveTabId by rememberUpdatedState(activeTab?.id)
    val latestActiveRichTextState by rememberUpdatedState(activeRichTextState)
    // Tracks the last flat-text we pushed to the ViewModel from this editor.
    // Used to suppress spurious resets caused by the debounce firing while the user is still typing.
    var lastPushedContent by remember { mutableStateOf("") }
    fun pushFlatTextToViewModel(tabId: String, richTextState: com.otso.app.viewmodel.RichTextState) {
        val flatText = richTextState.getFlatText("\n")
        lastPushedContent = flatText
        val syncBlock = if (richTextState.blocks.size == 1) {
            richTextState.block
        } else {
            ContentBlock(rawText = flatText, spans = emptyList())
        }
        viewModel.updateContentBlock(tabId = tabId, block = syncBlock, selection = richTextState.selection)
    }
    fun flushActiveEditorToViewModel() {
        val tabId = latestActiveTabId ?: return
        val richTextState = latestActiveRichTextState ?: return
        pushFlatTextToViewModel(tabId, richTextState)
    }

    LaunchedEffect(viewModel) {
        viewModel.editorEvents.collect { event ->
            when (event) {
                is EditorEvent.InsertTextAtSelection -> {
                    val tabId = latestActiveTabId
                    val richTextState = latestActiveRichTextState
                    if (tabId != null && richTextState != null) {
                        richTextState.insertTextAtSelection(event.text)
                        pushFlatTextToViewModel(tabId, richTextState)
                    }
                }
            }
        }
    }

    LaunchedEffect(uiState.tabs) {
        val validIds = uiState.tabs.map { it.id }.toSet()
        val staleIds = richTextStates.keys.filterNot { it in validIds }
        staleIds.forEach { staleId -> richTextStates.remove(staleId) }
    }

    LaunchedEffect(activeTab?.id, activeTab?.content, activeTab?.spans, activeVmSelection) {
        val tab = activeTab ?: return@LaunchedEffect
        val richTextState = activeRichTextState ?: return@LaunchedEffect
        val vmBlock = tab.toContentBlockForEditor()
        val vmSelection = activeVmSelection ?: richTextState.selection
        val currentFlatText = richTextState.getFlatText("\n")
        // isOwnPush: the VM content was pushed by this editor's debounce — any mismatch is just
        // in-flight user input, not an external update. Skip reset to avoid undoing live edits.
        val isOwnPush = lastPushedContent == vmBlock.rawText
        if (!isOwnPush && (currentFlatText != vmBlock.rawText || richTextState.selection != vmSelection)) {
            richTextState.reset(vmBlock, vmSelection)
        }
    }

    LaunchedEffect(activeTab?.id, activeRichTextState) {
        val tabId = activeTab?.id ?: return@LaunchedEffect
        val richTextState = activeRichTextState ?: return@LaunchedEffect
        snapshotFlow { richTextState.getFlatText("\n") to richTextState.selection }
            .distinctUntilChanged()
            .debounce(500L)
            .collect { (flatText, selection) ->
                pushFlatTextToViewModel(tabId, richTextState)
            }
    }

    DisposableEffect(activeTab?.id, activeRichTextState) {
        val tabId = activeTab?.id
        val richTextState = activeRichTextState
        onDispose {
            if (tabId != null && richTextState != null) {
                pushFlatTextToViewModel(tabId, richTextState)
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                flushActiveEditorToViewModel()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun launchDocumentScanner() {
        val activity = context as? android.app.Activity ?: return
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(1)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()

        val scanner = GmsDocumentScanning.getClient(options)
        scanner.getStartScanIntent(activity)
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
    }

    fun setActiveHighlightHex(hex: String) {
        val normalized = normalizeHexNoHash(hex) ?: return
        highlightPickerHex = normalized
    }

    fun openHighlightPicker(initialHex: String?) {
        normalizeHexNoHash(initialHex)?.let { highlightPickerHex = it }
        isHighlightPickerOpen = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(otsoColors.background)
                .technicalGrain(alpha = 0.03f) // DNA: Technical Paper Materiality Overlay
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                    ),
                )
                .navigationBarsPadding()
                .imePadding(),
        ) {
            OtsoTabBar(
                uiState = uiState,
                menuProgress = menuProgress, // DNA: Injected physical progress
                onMenuClick = { viewModel.toggleMenu(true) },
                onSwipeDown = { viewModel.toggleTabSwitcher(true) },
                onRenameStart = { index -> viewModel.startEditingTab(index) },
                onRenameUpdate = { newName -> viewModel.updateEditingTabName(newName) },
                onRenameCancel = { viewModel.cancelEditingTab() },
                onRenameFinish = { viewModel.finishEditingTab() },
            )

            val customFontFamily = if (uiState.font.isMonospace) {
                com.otso.app.ui.theme.JetBrainsMono
            } else {
                rememberDynamicFontFamily(
                    path = uiState.customFontPath,
                    foundryFamily = uiState.font.activeFoundryFamily,
                )
            }
            val allowSynthesisForEditor =
                uiState.font.activeFoundryFamily == null || uiState.font.activeFoundryVariantCount <= 1
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                if (activeTab != null && activeRichTextState != null) {
                    OtsoEditor(
                        richTextState = activeRichTextState,
                        fontFamily = customFontFamily,
                        fontSizeSp = activeTab.fontSizeSp,
                        allowStyleSynthesis = allowSynthesisForEditor,
                        findMatches = uiState.findReplace.matches,
                        findActiveIndex = uiState.findReplace.activeMatchIndex,
                        scrollState = editorScrollState,
                        onFontSizeTempChange = viewModel::updateFontSizeTemp,
                        onFontSizeFinalChange = viewModel::setEditorFontSize,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused) {
                                    flushActiveEditorToViewModel()
                                }
                            },
                    )
                }
            }

            // FindBar: Independent of keyboard visibility (Karpathy Surgical Fix)
            // Users must be able to search text even when keyboard is dismissed.
            if (uiState.findReplace.isFindBarVisible) {
                OtsoFindBar(
                    findQuery = uiState.findReplace.findQuery,
                    replaceQuery = uiState.findReplace.replaceQuery,
                    matchCount = uiState.findReplace.matches.size,
                    activeMatchIndex = uiState.findReplace.activeMatchIndex,
                    onFindQueryChange = { viewModel.updateFindQuery(it) },
                    onReplaceQueryChange = { viewModel.updateReplaceQuery(it) },
                    onFindNext = { viewModel.findNext() },
                    onFindPrevious = { viewModel.findPrevious() },
                    onReplaceCurrent = {
                        val richTextState = activeRichTextState
                        if (richTextState != null) {
                            val result = viewModel.onReplaceCurrentRequested(richTextState.block.rawText)
                            richTextState.updateText(result.newText, result.newCursorOffset)
                        }
                    },
                    onReplaceAll = {
                        val richTextState = activeRichTextState
                        if (richTextState != null) {
                            val result = viewModel.onReplaceAllRequested(richTextState.block.rawText)
                            richTextState.updateText(result.newText, result.newCursorOffset)
                        }
                    },
                    onClose = { viewModel.closeFindBar() },
                )
            }

        }

        // DNA: Floating Toolbar Overlay (Emil Design Engineering)
        // We decouple the toolbars from the main layout flow to allow for smooth 
        // physics-based entry/exit and refined "air" (padding) above the keyboard.
        androidx.compose.animation.AnimatedVisibility(
            visible = imeVisible && !uiState.findReplace.isFindBarVisible,
            enter = androidx.compose.animation.fadeIn(animationSpec = toolbarFadeInSpec) +
                    androidx.compose.animation.slideInVertically(
                        initialOffsetY = { it / 4 },
                        animationSpec = toolbarSlideSpec,
                    ),
            exit = androidx.compose.animation.fadeOut(animationSpec = toolbarFadeOutSpec) +
                androidx.compose.animation.slideOutVertically(
                    targetOffsetY = { it / 6 },
                    animationSpec = toolbarSlideSpec,
                ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val activeTabId = activeTab?.id
            val hasSelection = activeRichTextState?.selection
                ?.let { it.start != it.end } ?: false

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(bottom = 8.dp),
            ) {
                if (hasSelection) {
                    OtsoFormattingToolbar(
                        richTextState = activeRichTextState!!,
                        onLinkClick = { activeTabId?.let { viewModel.openLinkDialog(it) } },
                        customHighlightPalette = uiState.customHighlightPalette,
                        activeHighlightHex = highlightPickerHex,
                        onHighlightColorChange = { hex -> setActiveHighlightHex(hex) },
                        onOpenColorPicker = { initialHex -> openHighlightPicker(initialHex) },
                        onCustomHighlightRemove = viewModel::removeCustomHighlightColor,
                    )
                } else {
                    OtsoKeyboardToolbar(
                        onKeyInsert = { char ->
                            activeRichTextState?.insertAtCursor(char)
                        },
                        onFindClick = { viewModel.toggleFind() },
                        onScanClick = { launchDocumentScanner() },
                        onMonospaceToggle = { viewModel.toggleMonospace() },
                        isMonospaceActive = uiState.font.isMonospace,
                    )
                }
            }
        }

        // INTERCEPTOR DIALOG OVERLAY
        if (uiState.showUnsavedDialog) {
            val pendingIndex = uiState.pendingCloseTabIndex
            val pendingTab = pendingIndex?.let { uiState.tabs.getOrNull(it) }
            
            OtsoUnsavedDialog(
                fileName = pendingTab?.title ?: "Unknown",
                onCancel = { viewModel.cancelCloseTab() },
                onDiscard = { viewModel.discardAndCloseTab() },
                onSave = { viewModel.saveAndCloseTab() }
            )
        }

        
        
        if (uiState.showLinkDialog) {
            com.otso.app.ui.components.OtsoLinkDialog(
                url = uiState.linkDialogUrl,
                onUrlChange = { viewModel.updateLinkDialogUrl(it) },
                onCancel = { viewModel.closeLinkDialog() },
                onApply = { viewModel.applyLink() }
            )
        }

        if (uiState.translation.isTranslationDialogOpen) {
            val hasSelection = activeRichTextState?.selection?.let { it.start != it.end } ?: false
            OtsoTranslateDialog(
                sourceTag = uiState.translation.translationSourceTag,
                targetTag = uiState.translation.translationTargetTag,
                hasSelection = hasSelection,
                onSourceChange = viewModel::setTranslationSourceTag,
                onTargetChange = viewModel::setTranslationTargetTag,
                onCancel = viewModel::closeTranslationDialog,
                onTranslate = { selectionOnly ->
                    val richTextState = activeRichTextState
                    if (richTextState == null) {
                        viewModel.closeTranslationDialog()
                    } else {
                        val rawText = richTextState.block.rawText
                        val sourceText = if (selectionOnly) {
                            val start = richTextState.selection.min.coerceIn(0, rawText.length)
                            val end = richTextState.selection.max.coerceIn(start, rawText.length)
                            rawText.substring(start, end)
                        } else {
                            richTextState.reset(
                                newBlock = richTextState.block,
                                newSelection = TextRange(0, rawText.length),
                            )
                            activeTab?.id?.let { tabId ->
                                viewModel.updateContentBlock(
                                    tabId = tabId,
                                    block = richTextState.block,
                                    selection = richTextState.selection,
                                )
                            }
                            rawText
                        }
                        viewModel.translateText(
                            sourceText = sourceText,
                            sourceLanguage = uiState.translation.translationSourceTag,
                            targetLanguage = uiState.translation.translationTargetTag,
                        )
                    }
                },
            )
        }

        if (isHighlightPickerOpen) {
            OtsoColorWheelDialog(
                initialHex = highlightPickerHex,
                onDismiss = { isHighlightPickerOpen = false },
                onColorSelected = { selectedColorInt ->
                    val selectedHex = String.format("#%06X", (0xFFFFFF and selectedColorInt))
                    setActiveHighlightHex(selectedHex)
                    activeRichTextState?.addHighlight(selectedHex)
                    viewModel.addCustomHighlightColor(selectedColorInt)
                    isHighlightPickerOpen = false
                },
            )
        }

        uiState.fileAccessError?.let { error ->
            LaunchedEffect(error) {
                delay(3000)
                viewModel.clearFileAccessError()
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(OtsoColors.Accent.copy(alpha = 0.08f))
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            ) {
                Text(
                    text = error,
                    style = OtsoTypography.uiCaption,
                    color = OtsoColors.Accent,
                )
            }
        }

        if (uiState.ocr.isOcrProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.otsoColors.background.copy(alpha = 0.72f))
                    .semantics { contentDescription = "OCR processing overlay" },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.otsoColors.surface)
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    OtsoAsteriskLoader()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Processing image...",
                        style = OtsoTypography.uiLabelMedium,
                        color = MaterialTheme.colorScheme.otsoColors.ink,
                    )
                }
            }
        }

        if (uiState.translation.isTranslating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.otsoColors.background.copy(alpha = 0.72f))
                    .semantics { contentDescription = "Translation processing overlay" },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.otsoColors.surface)
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    OtsoAsteriskLoader()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Translating...",
                        style = OtsoTypography.uiLabelMedium,
                        color = MaterialTheme.colorScheme.otsoColors.ink,
                    )
                }
            }
        }
    }

    if (uiState.isTabSwitcherOpen) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleTabSwitcher(false) },
            sheetState = tabSwitcherSheetState,
            containerColor = otsoColors.background,
            scrimColor = otsoColors.accent.copy(alpha = 0.12f),
            shape = SquircleShape(24.dp),
            // DNA: Precise Top Edge Separator (Karpathy Surgical Fix)
            // Ensures the sheet sticks to the UI with a sharp technical line without "middle-box" artifacts.
            dragHandle = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BottomSheetDefaults.DragHandle()
                    if (otsoColors.isDarkMode) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(otsoColors.edge.copy(alpha = 0.2f))
                        )
                    }
                }
            }
        ) {
            OtsoTabSwitcherSheet(
                uiState = uiState,
                onTabSwitch = { index -> viewModel.switchTab(index) },
                onNewTab = {
                    viewModel.newTab()
                    viewModel.toggleTabSwitcher(false)
                },
                onCloseTab = { index -> viewModel.closeTab(index) },
            )
        }
    }

    if (uiState.isMenuOpen) {
        ModalBottomSheet(
            onDismissRequest = {
                if (!isHighlightPickerOpen) {
                    viewModel.toggleMenu(false)
                }
            },
            sheetState = menuSheetState,
            containerColor = otsoColors.background,
            scrimColor = otsoColors.accent.copy(alpha = 0.12f),
            shape = SquircleShape(24.dp),
            // DNA: Precise Top Edge Separator (Karpathy Surgical Fix)
            dragHandle = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BottomSheetDefaults.DragHandle()
                    if (otsoColors.isDarkMode) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(otsoColors.edge.copy(alpha = 0.2f))
                        )
                    }
                }
            }
        ) {
            OtsoMenuSheet(
                themeMode = uiState.themeMode,
                fontSizeSp = uiState.font.editorFontSize,
                onNewTab = {
                    viewModel.newTab()
                },
                onOpenFile = {
                    openDocumentLauncher.launch(arrayOf("text/*"))
                },
                onImportImage = {
                    ocrGalleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onSave = {
                    viewModel.saveActiveTab()
                },
                onSaveAs = {
                    val tab = uiState.tabs.getOrNull(uiState.activeIndex)
                    if (tab != null) {
                        pendingSaveAsTabId = tab.id
                        saveAsLauncher.launch(tab.title)
                    }
                },
                onThemeModeChange = viewModel::setThemeMode,
                onFontSizeChange = viewModel::setEditorFontSize,
                onLoadCustomFont = {
                    fontFolderPickerLauncher.launch(null)
                },
                onResetCustomFont = {
                    viewModel.resetCustomFont()
                },
                isCustomFontLoaded = uiState.font.activeFoundryFamily != null,
                customFontName = uiState.activeFoundryFamilyName,
                onAboutClick = { navController.navigate("about") },
                onTranslateClick = { viewModel.openTranslationDialog() },
                onDismiss = { viewModel.toggleMenu(false) },
            )
        }
    }
}
