package com.otso.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.animation.core.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavController
import androidx.activity.result.IntentSenderRequest
import androidx.compose.ui.platform.LocalContext
import android.app.Activity.RESULT_OK
import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.platform.LocalView
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
import com.otso.app.ui.components.OtsoIcons
import com.otso.app.BuildConfig
import com.otso.app.ui.theme.OtsoSpacing
import com.otso.app.ui.theme.otsoColors
import com.otso.app.ui.theme.otsoSpacing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.em
import com.otso.app.ui.components.OtsoAsteriskLoader
import com.otso.app.ui.components.OtsoTabBar
import com.otso.app.ui.components.OtsoTabSwitcherSheet
import com.otso.app.ui.components.OtsoTranslateDialog
import com.otso.app.ui.theme.OtsoColors
import com.otso.app.ui.theme.OtsoTypography
import com.otso.app.ui.theme.OtsoMotion
import com.otso.app.ui.theme.rememberDynamicFontFamily
import com.otso.app.ui.theme.otsoColors
import com.otso.app.ui.theme.otsoFloatingSolid
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

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class, ExperimentalLayoutApi::class)
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
    
    val focusManager = LocalFocusManager.current
    val view = LocalView.current
    val isImeVisible = WindowInsets.isImeVisible

    // Capture a stable status bar height ONCE before immersive mode can zero it out.
    // This prevents layout thrashing when system bars hide/show.
    val density = LocalDensity.current
    val stableStatusBarHeight = remember {
        with(density) {
            // Use the system resource directly — this never changes with immersive mode
            val resourceId = view.context.resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                view.context.resources.getDimensionPixelSize(resourceId).toDp()
            } else {
                24.dp // Safe fallback
            }
        }
    }

    val isFocusMode by remember(isImeVisible, uiState.findReplace.isFindBarVisible, uiState.editingTabIndex) {
        derivedStateOf { 
            isImeVisible && !uiState.findReplace.isFindBarVisible && uiState.editingTabIndex == null
        }
    }

    var debouncedFocusMode by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isFontLoading) {
        android.util.Log.i("OtsoEditor", "UI State Change: isFontLoading=${uiState.isFontLoading}")
    }

    LaunchedEffect(isFocusMode) {

        if (isFocusMode) {
            delay(350L)
            debouncedFocusMode = true
        } else {
            delay(200L)
            debouncedFocusMode = false
        }
    }

    LaunchedEffect(debouncedFocusMode) {
        val window = (view.context as? Activity)?.window ?: return@LaunchedEffect
        val insetsController = WindowCompat.getInsetsController(window, view)
        if (debouncedFocusMode) {
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        } else {
            insetsController.show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        }
    }

    val focusProgress by animateFloatAsState(
        targetValue = if (isFocusMode) 1f else 0f,
        animationSpec = spring(stiffness = 400f),
        label = "focus_progress"
    )
    
    // Physical layout padding stays stable to avoid LazyColumn thrashing
    // Negative offset pulls the editor up, closing the visual gap to the Focus Mode title
    val editorVerticalOffset by animateDpAsState(
        targetValue = if (isFocusMode) (-8).dp else 0.dp,
        animationSpec = spring(stiffness = 400f),
        label = "editor_offset"
    )
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
        spring<Float>(
            stiffness = 900f,
            dampingRatio = Spring.DampingRatioNoBouncy,
        )
    }
    val toolbarFadeOutSpec = remember {
        spring<Float>(
            stiffness = 700f,
            dampingRatio = Spring.DampingRatioNoBouncy,
        )
    }
    val toolbarSlideSpec = remember {
        spring<IntOffset>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        )
    }
    val otsoColors = MaterialTheme.colorScheme.otsoColors

    LaunchedEffect(isFocusMode) {
        if (!isFocusMode) {
            focusManager.clearFocus(force = true)
        }
    }

    val latestActiveTabId by rememberUpdatedState(activeTab?.id)
    val latestActiveRichTextState by rememberUpdatedState(activeRichTextState)
    // Tracks the last flat-text we pushed to the ViewModel from this editor.
    // Used to suppress spurious resets caused by the debounce firing while the user is still typing.
    // Tracks the last flat-text we pushed to the ViewModel from this editor for this specific tab.
    // Keyed by tabId to prevent cross-tab state contamination.
    var lastPushedContent by remember(activeTab?.id) { mutableStateOf(activeTab?.content ?: "") }
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

    // DNA: Content & Selection Synchronization (Karpathy Surgical Consolidation)
    // We combine content reset and selection sync into a single atomic effect keyed by the tab.
    // This prevents "Double-Jump" race conditions where text resets to end before selection kicks in.
    LaunchedEffect(activeTab?.id, activeTab?.content, activeVmSelection, activeTab?.spans) {
        val tab = activeTab ?: return@LaunchedEffect
        val richTextState = activeRichTextState ?: return@LaunchedEffect
        val vmBlock = tab.toContentBlockForEditor()
        val currentFlatText = richTextState.getFlatText("\n")
        
        // Critical: Check if the incoming ViewModel change is our own push
        // Critical Renaissance Rule: If the user is actively typing (focused), the Editor is the Source of Truth.
        // We only allow a full reset if the user is NOT focused (external update) or if the tab ID changed.
        val isFocused = view.hasFocus()
        
        // Structural Authority: If we just split a block, our local count is higher than VM.
        // We MUST NOT reset until the VM catches up, or we destroy the user's new line.
        val vmBlockCount = tab.content.split("\n").size
        val isStructuralMismatch = richTextState.blocks.size > vmBlockCount
        val isOwnPush = vmBlock.rawText == lastPushedContent
        
        if (!isOwnPush && currentFlatText != vmBlock.rawText) {
            // Telemetry: Audit why a reset is or isn't happening
            val reason = when {
                isFocused -> "Editor has focus (High Authority)"
                isStructuralMismatch -> "Structural mismatch (Split in progress)"
                else -> "External update"
            }
            
            if (!isFocused && !isStructuralMismatch) {
                android.util.Log.d("OtsoTelemetry", "Sync: ACCEPTED reset. Reason: $reason")
                richTextState.reset(vmBlock, activeVmSelection)
            } else {
                android.util.Log.d("OtsoTelemetry", "Sync: SUPPRESSED reset. Reason: $reason")
            }
        } else if (activeVmSelection != null && !isFocused && !isStructuralMismatch) {
            // Selection-only sync from VM: ONLY apply if NO structural mismatch and NOT focused.
            // This prevents the "Jump to Start" flicker when hitting Enter.
            val targetBlock = richTextState.blocks.find { it.blockId == richTextState.activeBlockId }
            if (targetBlock != null) {
                val blockLen = targetBlock.rawText.length
                val clampedSel = androidx.compose.ui.text.TextRange(
                    activeVmSelection.start.coerceIn(0, blockLen),
                    activeVmSelection.end.coerceIn(0, blockLen),
                )
                val currentSel = richTextState.getSelectionForBlock(targetBlock.blockId)
                if (currentSel != clampedSel) {
                    richTextState.updateBlock(
                        targetBlock.blockId,
                        androidx.compose.ui.text.input.TextFieldValue(
                            text = targetBlock.rawText,
                            selection = clampedSel,
                        ),
                    )
                }
            }
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
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal,
                    ),
                )
                .navigationBarsPadding()
                .imePadding(),
        ) {
            // DNA: Stable Chrome Container (Emil Design Eng)
            // Fixed height prevents cascading layout measurements in the Editor below.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = stableStatusBarHeight)
                    .height(OtsoSpacing.chromeBandH),
                contentAlignment = Alignment.Center
            ) {
                // Layer 1: TabBar (View Mode Chrome)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = 1f - focusProgress
                            translationY = -focusProgress * size.height * 0.25f // Subtle exit
                        }
                ) {
                    OtsoTabBar(
                        uiState = uiState,
                        menuProgress = menuProgress,
                        onMenuClick = { viewModel.toggleMenu(true) },
                        onSwipeDown = { viewModel.toggleTabSwitcher(true) },
                        onRenameStart = { index -> viewModel.startEditingTab(index) },
                        onRenameUpdate = { newName -> viewModel.updateEditingTabName(newName) },
                        onRenameCancel = { viewModel.cancelEditingTab() },
                        onRenameFinish = { viewModel.finishEditingTab() },
                    )
                }

                // Layer 2: Floating Title (Focus Mode Header)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 2.dp) // Optical nudge to separate from content bounds
                        .graphicsLayer {
                            alpha = focusProgress
                            translationY = (1f - focusProgress) * 8.dp.toPx()
                        },
                    contentAlignment = Alignment.BottomStart
                ) {
                    Text(
                        text = activeTab?.title ?: "Untitled",
                        style = OtsoTypography.uiCaption.copy(
                            color = otsoColors.ink.copy(alpha = 0.32f),
                            letterSpacing = (-0.02).em
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            val customFontFamily = rememberDynamicFontFamily(
                path = uiState.customFontPath,
                foundryFamily = uiState.font.activeFoundryFamily,
            )
            val allowSynthesisForEditor =
                uiState.font.activeFoundryFamily == null || uiState.font.activeFoundryVariantCount <= 1
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationY = editorVerticalOffset.toPx()
                    },
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
        // Optimized: Decoupled from main flow to prevent layout thrashing (21 FPS Fix)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .imePadding()
                .padding(bottom = 8.dp)
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = isFocusMode,
                enter = androidx.compose.animation.fadeIn(animationSpec = toolbarFadeInSpec) +
                        androidx.compose.animation.slideInVertically(
                            initialOffsetY = { it / 3 }, // Faster, snappier slide
                            animationSpec = toolbarSlideSpec,
                        ),
                exit = androidx.compose.animation.fadeOut(animationSpec = toolbarFadeOutSpec) +
                    androidx.compose.animation.slideOutVertically(
                        targetOffsetY = { it / 4 },
                        animationSpec = toolbarSlideSpec,
                    ),
            ) {
                // Stabilize selection check to avoid mass recomposition
                val hasSelection = remember(activeRichTextState?.selection) {
                    activeRichTextState?.selection?.let { it.start != it.end } ?: false
                }

                if (hasSelection) {
                    OtsoFormattingToolbar(
                        richTextState = activeRichTextState!!,
                        onLinkClick = { activeRichTextState?.insertLinkAtSelection() },
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
                        onUndo = { activeRichTextState?.undo() },
                        onRedo = { activeRichTextState?.redo() },
                        canUndo = activeRichTextState?.canUndo ?: false,
                        canRedo = activeRichTextState?.canRedo ?: false,
                        onSelectAll = { activeRichTextState?.selectAll() },
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

        // Cache last non-null error so the exit animation still shows the message
        val lastError = remember { mutableStateOf("") }
        uiState.fileAccessError?.let { error ->
            lastError.value = error
            LaunchedEffect(error) {
                delay(3000)
                viewModel.clearFileAccessError()
            }
        }
        AnimatedVisibility(
            visible = uiState.fileAccessError != null,
            enter = fadeIn(spring(stiffness = 300f, dampingRatio = Spring.DampingRatioNoBouncy)) +
                slideInVertically(spring(stiffness = 380f, dampingRatio = Spring.DampingRatioNoBouncy)) { it },
            exit = fadeOut(spring(stiffness = Spring.StiffnessMedium)) + 
                slideOutVertically(spring(stiffness = Spring.StiffnessMedium)) { it / 2 },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            if (com.otso.app.BuildConfig.DEBUG) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(otsoColors.accent.copy(alpha = 0.08f))
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = OtsoIcons.WarningCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = otsoColors.accent
                    )
                    Text(
                        text = lastError.value,
                        style = OtsoTypography.uiCaption,
                        color = otsoColors.accent,
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(otsoColors.accent.copy(alpha = 0.08f))
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = lastError.value,
                        style = OtsoTypography.uiCaption,
                        color = otsoColors.accent,
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
                createdAt = uiState.tabs.getOrNull(uiState.activeIndex)?.createdAt ?: 0L,
                lastModified = uiState.tabs.getOrNull(uiState.activeIndex)?.lastModified ?: 0L,
                wordCount = (uiState.tabs.getOrNull(uiState.activeIndex)?.content ?: "").split("\\s+".toRegex()).count { it.isNotBlank() },
                characterCount = (uiState.tabs.getOrNull(uiState.activeIndex)?.content ?: "").length,
                onDismiss = { viewModel.toggleMenu(false) },
            )
        }
    }

    // --- SYSTEM OVERLAYS (Dialog-based to ensure top-level visibility) ---

    if (uiState.ocr.isOcrProcessing || uiState.translation.isTranslating || (uiState.isFontLoading && uiState.isFontInitialized)) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = {},
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(otsoColors.background.copy(alpha = 0.72f)),
                contentAlignment = Alignment.Center
            ) {
                val cardScale = remember { Animatable(0.90f) }
                LaunchedEffect(Unit) {
                    cardScale.animateTo(1f, spring(dampingRatio = 0.68f, stiffness = 460f))
                }

                Column(
                    modifier = Modifier
                        .graphicsLayer { scaleX = cardScale.value; scaleY = cardScale.value }
                        .otsoFloatingSolid(shape = SquircleShape(20.dp), colors = otsoColors)
                        .padding(horizontal = 28.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    OtsoAsteriskLoader()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = when {
                            uiState.ocr.isOcrProcessing -> "Processing image..."
                            uiState.translation.isTranslating -> "Translating..."
                            uiState.isFontLoading -> "Loading fonts..."
                            else -> ""
                        },
                        style = OtsoTypography.uiLabelMedium,
                        color = otsoColors.ink,
                    )
                }
            }
        }
    }
}
