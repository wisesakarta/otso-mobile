package com.otso.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otso.app.model.ContentBlock
import com.otso.app.ui.mapper.toAnnotatedString
import com.otso.app.ui.theme.GeneralSans
import com.otso.app.ui.theme.OtsoColorScheme
import com.otso.app.ui.theme.OtsoSpacing
import com.otso.app.ui.theme.OtsoTypography
import com.otso.app.ui.theme.otsoColors
import com.otso.app.viewmodel.RichTextState
import com.otso.app.BuildConfig
import kotlin.math.roundToInt

private const val MIN_EDITOR_FONT_SP = 10
private const val MAX_EDITOR_FONT_SP = 64

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Suppress("UNUSED_PARAMETER")
fun OtsoEditor(
    richTextState: RichTextState,
    fontFamily: FontFamily = com.otso.app.BrandAssets.defaultFont,
    fontSizeSp: Int = 15,
    allowStyleSynthesis: Boolean = true,
    findMatches: List<IntRange> = emptyList(),
    findActiveIndex: Int = -1,
    onFontSizeTempChange: (Int) -> Unit = {},
    onFontSizeFinalChange: (Int) -> Unit = {},
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    val density = LocalDensity.current
    val editorTextStyle = remember(fontFamily, allowStyleSynthesis, fontSizeSp, colors.ink) {
        OtsoTypography.editorBody.copy(
            fontFamily = fontFamily,
            fontSynthesis = if (allowStyleSynthesis) FontSynthesis.All else FontSynthesis.None,
            fontSize = fontSizeSp.sp,
            lineHeight = (fontSizeSp * 1.7f).sp,
            letterSpacing = (-0.01).sp,
            color = colors.ink,
        )
    }
    val selectionColors = remember(colors.accent) {
        TextSelectionColors(
            handleColor = colors.accent,
            backgroundColor = colors.accent.copy(alpha = 0.28f),
        )
    }
    val latestFontSizeSp by rememberUpdatedState(fontSizeSp)
    val latestTempFontUpdate by rememberUpdatedState(onFontSizeTempChange)
    val latestFinalFontUpdate by rememberUpdatedState(onFontSizeFinalChange)
    var visualScale by remember { mutableFloatStateOf(1f) }

    CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        visualScale = 1f
                        try {
                            awaitFirstDown(
                                requireUnconsumed = false,
                                pass = PointerEventPass.Initial,
                            )

                            var currentSizeSp = latestFontSizeSp
                            var didScale = false
                            var multiTouchDetected = false
                            var lastCommitTime = System.currentTimeMillis()
                            var event: PointerEvent

                            do {
                                event = awaitPointerEvent(PointerEventPass.Initial)
                                val activePointerCount = event.changes.count { it.pressed }
                                if (activePointerCount >= 2) {
                                    multiTouchDetected = true
                                    val zoomDelta = event.calculateZoom()
                                    if (zoomDelta.isFinite() && zoomDelta > 0f && zoomDelta != 1f) {
                                        didScale = true
                                        val minScale = MIN_EDITOR_FONT_SP.toFloat() / currentSizeSp.toFloat()
                                        val maxScale = MAX_EDITOR_FONT_SP.toFloat() / currentSizeSp.toFloat()
                                        visualScale = (visualScale * zoomDelta).coerceIn(minScale, maxScale)
                                        val now = System.currentTimeMillis()
                                        if (now - lastCommitTime > 150L) {
                                            val throttledSizeSp = (currentSizeSp * visualScale)
                                                .roundToInt()
                                                .coerceIn(MIN_EDITOR_FONT_SP, MAX_EDITOR_FONT_SP)
                                            latestTempFontUpdate(throttledSizeSp)
                                            currentSizeSp = throttledSizeSp
                                            visualScale = 1f
                                            lastCommitTime = now
                                        }
                                    }
                                }

                                if (multiTouchDetected) {
                                    event.changes.forEach { pointer -> pointer.consume() }
                                }
                            } while (event.changes.any { it.pressed })

                            if (multiTouchDetected && didScale) {
                                val finalSizeSp = (currentSizeSp * visualScale)
                                    .roundToInt()
                                    .coerceIn(MIN_EDITOR_FONT_SP, MAX_EDITOR_FONT_SP)
                                latestTempFontUpdate(finalSizeSp)
                                latestFinalFontUpdate(finalSizeSp)
                            }
                        } finally {
                            visualScale = 1f
                        }
                    }
                },
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = visualScale
                        scaleY = visualScale
                    },
            ) {
                items(
                    items = richTextState.blocks,
                    key = { it.blockId },
                    contentType = { it.type },
                ) { block ->
                    val focusRequester = remember { FocusRequester() }
                    LaunchedEffect(richTextState.activeBlockId) {
                        if (richTextState.activeBlockId == block.blockId) {
                            focusRequester.requestFocus()
                        }
                    }
                    OtsoBlockNode(
                        block = block,
                        state = richTextState,
                        focusRequester = focusRequester,
                        colors = colors,
                        editorTextStyle = editorTextStyle,
                        density = density,
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OtsoBlockNode(
    block: ContentBlock,
    state: RichTextState,
    focusRequester: FocusRequester,
    colors: OtsoColorScheme,
    editorTextStyle: TextStyle,
    density: Density,
) {
    val newAnnotated = remember(block.rawText, block.spans, colors) {
        block.toAnnotatedString(colors)
    }
    var lastActiveBlockId by remember { mutableStateOf<String?>(null) }
    val isCurrentlyActive = state.activeBlockId == block.blockId

    // Multi-block selection visual highlight
    val multiBlockRange = state.getMultiBlockSelectionRange(block.blockId)
    val hasMultiBlockHighlight = multiBlockRange != null

    var localTfv by remember(block.blockId) {
        mutableStateOf(
            TextFieldValue(
                annotatedString = newAnnotated,
                selection = if (isCurrentlyActive) state.getSelectionForBlock(block.blockId) else TextRange(block.rawText.length)
            )
        )
    }

    // Track previous selection to detect boundary-reaching drags
    var previousSelection by remember { mutableStateOf(localTfv.selection) }
    // Suppresses boundary-detection when selection is set programmatically (selectAll, undo, block activation)
    val suppressBoundaryDetection = remember { java.util.concurrent.atomic.AtomicBoolean(false) }

    LaunchedEffect(localTfv.selection) {
        if (BuildConfig.DEBUG) android.util.Log.i("OtsoEditor", "Block ${block.blockId} selection changed to ${localTfv.selection} (suppress=${suppressBoundaryDetection.get()})")

        if (suppressBoundaryDetection.getAndSet(false)) {
            previousSelection = localTfv.selection
            return@LaunchedEffect
        }

        // Cross-block selection detection:
        // When user drags selection to the very start or end of a block, extend to adjacent block
        val sel = localTfv.selection
        if (!sel.collapsed && isCurrentlyActive) {
            val atStart = sel.start == 0 && sel.end > 0
            val atEnd = sel.end == block.rawText.length && sel.start < block.rawText.length

            // Detect if selection is growing toward a boundary (not just positioned there)
            val wasAtStart = previousSelection.start == 0 && !previousSelection.collapsed
            val wasAtEnd = previousSelection.end == block.rawText.length && !previousSelection.collapsed

            // Only extend if previously dragging (non-collapsed) — prevents IME word-select on tap from
            // falsely triggering cross-block selection when the word happens to start at position 0.
            if (atStart && !wasAtStart && !previousSelection.collapsed) {
                if (BuildConfig.DEBUG) android.util.Log.i("OtsoEditor", "Block ${block.blockId} hit START boundary → extending upward")
                state.extendSelectionToAdjacentBlock(block.blockId, -1)
            } else if (atEnd && !wasAtEnd && !previousSelection.collapsed) {
                if (BuildConfig.DEBUG) android.util.Log.i("OtsoEditor", "Block ${block.blockId} hit END boundary → extending downward")
                state.extendSelectionToAdjacentBlock(block.blockId, 1)
            }
        } else if (sel.collapsed && state.multiBlockSelection.isActive) {
            // Guard: Transsion IME (fixLyingSelectionRangeFromSurroundingText) collapses selection
            // to exactly block end ~700ms after a drag. Don't clear for that specific position.
            if (sel.start < block.rawText.length) {
                if (BuildConfig.DEBUG) android.util.Log.i("OtsoEditor", "Block ${block.blockId} collapsed at ${sel.start} → clearing multi-block selection")
                state.clearMultiBlockSelection()
            } else {
                if (BuildConfig.DEBUG) android.util.Log.i("OtsoEditor", "Block ${block.blockId} collapsed at block-end — skipping clear (IME artifact)")
            }
        }

        previousSelection = sel
    }

    if (isCurrentlyActive && lastActiveBlockId != block.blockId) {
        val authoritative = state.getSelectionForBlock(block.blockId)
        if (BuildConfig.DEBUG) android.util.Log.i("OtsoEditor", "OtsoBlockNode: Block ${block.blockId} became active. authoritative=$authoritative, local=${localTfv.selection}")
        if (localTfv.selection != authoritative) {
            suppressBoundaryDetection.set(true)
            localTfv = localTfv.copy(selection = authoritative)
        }
        lastActiveBlockId = block.blockId
    } else if (!isCurrentlyActive) {
        lastActiveBlockId = null
    }

    if (localTfv.text != block.rawText) {
        val authoritative = state.getSelectionForBlock(block.blockId)
        localTfv = localTfv.copy(
            annotatedString = newAnnotated,
            selection = authoritative,
        )
    } else {
        val needsAnnotation = localTfv.annotatedString != newAnnotated
        val authoritative = if (isCurrentlyActive) state.getSelectionForBlock(block.blockId) else null
        val needsSelection = authoritative != null && localTfv.selection != authoritative
        if (needsAnnotation || needsSelection) {
            if (needsSelection) suppressBoundaryDetection.set(true)
            localTfv = localTfv.copy(
                annotatedString = if (needsAnnotation) newAnnotated else localTfv.annotatedString,
                selection = if (needsSelection) authoritative!! else localTfv.selection,
            )
        }
    }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var caretRect by remember { mutableStateOf<Rect?>(null) }
    val showPlaceholder = block.rawText.isEmpty() &&
        state.blocks.size == 1 &&
        state.blocks.firstOrNull()?.blockId == block.blockId

    LaunchedEffect(caretRect) {
        caretRect?.let { rect ->

            bringIntoViewRequester.bringIntoView(rect)
        }
    }

    // Multi-block selection overlay color
    val multiBlockSelectionAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (hasMultiBlockHighlight) 0.28f else 0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 150),
        label = "multi_block_sel_alpha",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (multiBlockSelectionAlpha > 0f) {
                    Modifier.background(colors.accent.copy(alpha = multiBlockSelectionAlpha))
                } else {
                    Modifier
                }
            )
    ) {
        BasicTextField(
            value = localTfv,
            onValueChange = { newTfv ->
                localTfv = newTfv
                val textChanged = block.rawText != newTfv.text
                val selectionChanged = state.getSelectionForBlock(block.blockId) != newTfv.selection

                if (textChanged) {
                    // Text change clears multi-block selection
                    state.clearMultiBlockSelection()
                    
                    val newlineIndex = newTfv.text.indexOf('\n')
                    if (newlineIndex >= 0) {
                        val cleanText = newTfv.text.removeRange(newlineIndex, newlineIndex + 1)
                        val cleanTfv = newTfv.copy(text = cleanText, selection = TextRange(newlineIndex))
                        state.updateBlock(block.blockId, cleanTfv)
                        state.splitBlockAtCursor(block.blockId, newlineIndex)
                    } else {
                        state.updateBlock(block.blockId, newTfv)
                    }
                } else if (selectionChanged) {
                    state.updateBlock(block.blockId, newTfv)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused && state.activeBlockId != block.blockId) {
                        state.setActiveBlock(block.blockId)
                    }
                }
                .onPreviewKeyEvent { keyEvent ->
                    if (
                        keyEvent.type == KeyEventType.KeyDown &&
                        keyEvent.key == Key.Backspace
                    ) {
                        val cursorPosition = state.getSelectionForBlock(block.blockId).start
                        if (cursorPosition == 0) {
                            state.mergeBlockWithPrevious(block.blockId)
                            return@onPreviewKeyEvent true
                        }
                    }
                    false
                }
                .background(colors.background)
                .padding(horizontal = OtsoSpacing.globalMargin, vertical = 4.dp),
            textStyle = editorTextStyle,
            cursorBrush = SolidColor(colors.accent),
            visualTransformation = VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Default,
            ),
            keyboardActions = KeyboardActions.Default,
            maxLines = Int.MAX_VALUE,
            onTextLayout = { textLayoutResult ->
                if (state.activeBlockId == block.blockId) {
                    val cursorOffset = localTfv.selection.end.coerceIn(0, localTfv.text.length)
                    val cursorRect = textLayoutResult.getCursorRect(cursorOffset)
                    caretRect = androidx.compose.ui.geometry.Rect(
                        left = cursorRect.left,
                        top = cursorRect.top,
                        right = cursorRect.right,
                        bottom = cursorRect.bottom
                    )
                }
            },
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.bringIntoViewRequester(bringIntoViewRequester)) {
                    if (showPlaceholder) {
                        Text(
                            text = "Start writing...",
                            style = editorTextStyle.copy(
                                color = colors.muted.copy(alpha = 0.28f),
                            ),
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}
