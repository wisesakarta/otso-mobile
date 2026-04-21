package com.otso.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.animation.core.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavController
import androidx.activity.result.IntentSenderRequest
import androidx.compose.ui.platform.LocalContext
import android.app.Activity.RESULT_OK
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.delay
import com.otso.app.ui.components.OtsoEditor
import com.otso.app.ui.components.OtsoFindBar
import com.otso.app.ui.components.OtsoKeyboardToolbar
import com.otso.app.ui.components.OtsoUnsavedDialog
import com.otso.app.ui.components.OtsoMenuSheet
import com.otso.app.ui.components.OtsoAsteriskLoader
import com.otso.app.ui.components.OtsoTabBar
import com.otso.app.ui.components.OtsoTabSwitcherSheet
import com.otso.app.ui.theme.OtsoColors
import com.otso.app.model.TabSource
import com.otso.app.ui.theme.OtsoTheme
import com.otso.app.ui.theme.OtsoTypography
import com.otso.app.ui.theme.rememberDynamicFontFamily
import com.otso.app.ui.theme.otsoColors
import com.otso.app.ui.theme.technicalGrain
import com.otso.app.ui.theme.OtsoSquircleShape
import com.otso.app.viewmodel.EditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
    val imageOcrLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            viewModel.importImageAsText(uri)
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

    val fontPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            viewModel.importCustomFont(uri)
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val activeTab = uiState.tabs.getOrNull(uiState.activeIndex)
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
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

    val otsoColors = MaterialTheme.colorScheme.otsoColors
    val activeEditorFontName = uiState.customFontName
        ?.substringBeforeLast('.')
        ?.takeIf { it.isNotBlank() }
        ?: "General Sans"

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

            val customFontFamily = if (uiState.isMonospace) {
                com.otso.app.ui.theme.JetBrainsMono
            } else {
                rememberDynamicFontFamily(uiState.customFontPath)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(editorScrollState),
            ) {
                if (activeTab != null) {
                    OtsoEditor(
                        value = viewModel.getTextFieldValue(activeTab.id),
                        onValueChange = { viewModel.updateTextFieldValue(activeTab.id, it) },
                        fontFamily = customFontFamily,
                        fontSizeSp = activeTab.fontSizeSp,
                        findMatches = uiState.findMatches,
                        findActiveIndex = uiState.findActiveIndex,
                        onFontSizeTempChange = { newSize: Int -> viewModel.updateFontSizeTemp(newSize) },
                        onFontSizeFinalChange = { newSize: Int -> viewModel.setEditorFontSize(newSize) },
                        scrollState = editorScrollState,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            if (imeVisible) {
                if (uiState.isFindOpen) {
                    OtsoFindBar(
                        findQuery = uiState.findQuery,
                        replaceQuery = uiState.replaceQuery,
                        matchCount = uiState.findMatches.size,
                        activeMatchIndex = uiState.findActiveIndex,
                        onFindQueryChange = { viewModel.updateFindQuery(it) },
                        onReplaceQueryChange = { viewModel.updateReplaceQuery(it) },
                        onFindNext = { viewModel.findNext() },
                        onFindPrevious = { viewModel.findPrevious() },
                        onReplaceCurrent = { viewModel.replaceCurrent() },
                        onReplaceAll = { viewModel.replaceAll() },
                        onClose = { viewModel.closeFindBar() },
                    )
                } else {
                    OtsoKeyboardToolbar(
                        onKeyInsert = { insert ->
                            activeTab?.let { tab -> viewModel.insertTextAtCursor(tab.id, insert) }
                        },
                        onFindClick = { viewModel.toggleFind() },
                        onMonoToggle = { viewModel.toggleMonospace() },
                        isMonospace = uiState.isMonospace,
                        onScanClick = {
                            val options = GmsDocumentScannerOptions.Builder()
                                .setGalleryImportAllowed(true)
                                .setPageLimit(1)
                                .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
                                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                                .build()

                            val scanner = GmsDocumentScanning.getClient(options)
                            scanner.getStartScanIntent(context as android.app.Activity)
                                .addOnSuccessListener { intentSender ->
                                    scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                                }
                        }
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

        if (uiState.isOcrProcessing) {
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
    }

    if (uiState.isTabSwitcherOpen) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleTabSwitcher(false) },
            sheetState = tabSwitcherSheetState,
            containerColor = otsoColors.background,
            scrimColor = otsoColors.accent.copy(alpha = 0.12f),
            shape = OtsoSquircleShape(radius = 24.dp, smoothing = 0.8f, topOnly = true),
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
            onDismissRequest = { viewModel.toggleMenu(false) },
            sheetState = menuSheetState,
            containerColor = otsoColors.background,
            scrimColor = otsoColors.accent.copy(alpha = 0.12f),
            shape = OtsoSquircleShape(radius = 24.dp, smoothing = 0.8f, topOnly = true),
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
                fontSizeSp = uiState.editorFontSizeSp,
                onNewTab = {
                    viewModel.newTab()
                },
                onOpenFile = {
                    openDocumentLauncher.launch(arrayOf("text/*"))
                },
                onImportImage = {
                    imageOcrLauncher.launch("image/*")
                },
                onSave = {
                    viewModel.saveActiveTab()
                },
                onSaveAs = {
                    viewModel.saveActiveTab()
                },
                onThemeModeChange = viewModel::setThemeMode,
                onFontSizeChange = viewModel::setEditorFontSize,
                onLoadCustomFont = {
                    fontPickerLauncher.launch(arrayOf(/* "font/ttf", "font/otf" */ "*/*"))
                },
                onResetCustomFont = {
                    viewModel.resetCustomFont()
                },
                isCustomFontLoaded = uiState.customFontPath != null,
                customFontName = uiState.customFontName,
                onAboutClick = { navController.navigate("about") },
                onDismiss = { viewModel.toggleMenu(false) },
            )
        }
    }
}
