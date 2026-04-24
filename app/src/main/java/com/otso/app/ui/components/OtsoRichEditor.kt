package com.otso.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.BasicRichTextEditor
import com.otso.app.ui.theme.GeneralSans
import com.otso.app.ui.theme.OtsoSpacing
import com.otso.app.ui.theme.OtsoTypography
import com.otso.app.ui.theme.otsoColors
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import kotlin.math.abs
import kotlin.math.roundToInt

private const val MIN_EDITOR_FONT_SP = 10
private const val MAX_EDITOR_FONT_SP = 64
private const val PINCH_FONT_STEP_SP = 1

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OtsoRichEditor(
    richTextState: RichTextState,
    fontSizeSp: Int = 15,
    scrollState: androidx.compose.foundation.ScrollState,
    fontFamily: androidx.compose.ui.text.font.FontFamily = GeneralSans,
    onFontSizeTempChange: (Int) -> Unit = {},
    onFontSizeFinalChange: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    val accent = Color(0xFF001AE2)
    val selectionColors = TextSelectionColors(
        handleColor = accent,
        backgroundColor = accent.copy(alpha = 0.18f),
    )

    val editorTextStyle = OtsoTypography.editorBody.copy(
        fontFamily = fontFamily,
        fontSize = fontSizeSp.sp,
        lineHeight = (fontSizeSp * 1.7f).sp,
        letterSpacing = (-0.01).sp,
        color = colors.ink,
    )
    val latestFontSizeSp by rememberUpdatedState(fontSizeSp)
    val latestTempFontUpdate by rememberUpdatedState(onFontSizeTempChange)
    val latestFinalFontUpdate by rememberUpdatedState(onFontSizeFinalChange)
    val density = LocalDensity.current
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var caretRect by remember { mutableStateOf<Rect?>(null) }

    LaunchedEffect(caretRect) {
        val targetRect = caretRect ?: return@LaunchedEffect
        bringIntoViewRequester.bringIntoView(targetRect)
    }

    val keyboardOptions = remember {
        KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            autoCorrectEnabled = true,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Default,
        )
    }

    CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
        BasicRichTextEditor(
            state = richTextState,
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(colors.background)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        val baseSizeSp = latestFontSizeSp
                        var accumulatedZoom = 1f
                        var emittedSizeSp = baseSizeSp
                        var didZoom = false
                        do {
                            val event = awaitPointerEvent()
                            val canceled = event.changes.any { it.isConsumed }
                            if (!canceled && event.changes.size >= 2) {
                                val zoom = event.calculateZoom()
                                if (zoom.isFinite() && zoom != 1f) {
                                    didZoom = true
                                    accumulatedZoom *= zoom
                                    val candidateSizeSp = (baseSizeSp * accumulatedZoom)
                                        .roundToInt()
                                        .coerceIn(MIN_EDITOR_FONT_SP, MAX_EDITOR_FONT_SP)

                                    if (abs(candidateSizeSp - emittedSizeSp) >= PINCH_FONT_STEP_SP) {
                                        emittedSizeSp = candidateSizeSp
                                        latestTempFontUpdate(candidateSizeSp)
                                    }
                                }
                                event.changes.forEach { it.consume() }
                            }
                        } while (event.changes.any { it.pressed })

                        if (didZoom) {
                            latestFinalFontUpdate(emittedSizeSp)
                        }
                    }
                }
                .padding(
                    horizontal = OtsoSpacing.globalMargin,
                    vertical = 16.dp,
                ),
            textStyle = editorTextStyle,
            enabled = true,
            readOnly = false,
            keyboardOptions = keyboardOptions,
            keyboardActions = KeyboardActions.Default,
            cursorBrush = SolidColor(accent),
            maxLines = Int.MAX_VALUE,
            onTextLayout = { layoutResult ->
                val selection = richTextState.selection
                val caretOffset = selection.max.coerceIn(0, richTextState.annotatedString.length)
                val cursorRect = layoutResult.getCursorRect(caretOffset)
                val comfortPadding = with(density) { 44.dp.toPx() }
                caretRect = Rect(
                    left = cursorRect.left,
                    top = cursorRect.top,
                    right = cursorRect.right,
                    bottom = cursorRect.bottom + comfortPadding,
                )
            },
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.bringIntoViewRequester(bringIntoViewRequester)) {
                    innerTextField()
                }
            },
        )
    }
}
