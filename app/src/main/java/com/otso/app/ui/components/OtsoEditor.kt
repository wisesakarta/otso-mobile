package com.otso.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otso.app.ui.theme.GeneralSans
import com.otso.app.ui.theme.OtsoSpacing
import com.otso.app.ui.theme.OtsoTypography
import com.otso.app.ui.theme.otsoColors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OtsoEditor(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    fontFamily: FontFamily = GeneralSans,
    fontSizeSp: Int = 15,
    findMatches: List<IntRange> = emptyList(),
    findActiveIndex: Int = -1,
    onFontSizeTempChange: (Int) -> Unit = {},
    onFontSizeFinalChange: (Int) -> Unit = {},
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
) {
    val otsoColors = MaterialTheme.colorScheme.otsoColors
    val density = LocalDensity.current

    // DNA Restoration: 1.7x height and -0.01em tracking
    val editorTextStyle = OtsoTypography.editorBody.copy(
        fontFamily = fontFamily,
        fontSize = fontSizeSp.sp,
        lineHeight = (fontSizeSp * 1.7f).sp,
        letterSpacing = (-0.01).sp,
        color = otsoColors.ink,
    )

    val selectionColors = TextSelectionColors(
        handleColor = otsoColors.accent,
        backgroundColor = otsoColors.accentMuted,
    )
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var caretRect by remember { mutableStateOf<Rect?>(null) }

    LaunchedEffect(caretRect) {
        val targetRect = caretRect ?: return@LaunchedEffect
        bringIntoViewRequester.bringIntoView(targetRect)
    }

    var accumulatedZoom by remember { mutableStateOf(1f) }

    CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(otsoColors.background)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        do {
                            val event = awaitPointerEvent()
                            val canceled = event.changes.any { it.isConsumed }
                            if (!canceled && event.changes.size >= 2) {
                                val zoom = event.calculateZoom()
                                if (zoom != 1f) {
                                    accumulatedZoom *= zoom
                                    val newSize = (fontSizeSp * accumulatedZoom).roundToInt().coerceIn(10, 72)
                                    onFontSizeTempChange(newSize)
                                }
                                event.changes.forEach { it.consume() }
                            }
                        } while (event.changes.any { it.pressed })
                        
                        // Finalize with the last accumulated value on release
                        onFontSizeFinalChange((fontSizeSp * accumulatedZoom).roundToInt().coerceIn(10, 72))
                        accumulatedZoom = 1f
                    }
                }
                .padding(
                    start = OtsoSpacing.globalMargin,
                    top = 16.dp,
                    end = OtsoSpacing.globalMargin,
                    bottom = OtsoSpacing.keyboardToolbarH + 16.dp,
                ),
            textStyle = editorTextStyle,
            cursorBrush = SolidColor(otsoColors.accent),
            visualTransformation = if (findMatches.isEmpty()) VisualTransformation.None
            else FindHighlightTransformation(
                matches = findMatches,
                activeIndex = findActiveIndex,
                allMatchColor = otsoColors.accentMuted,
                activeMatchColor = otsoColors.accent.copy(alpha = 0.35f),
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Default,
            ),
            keyboardActions = KeyboardActions.Default,
            maxLines = Int.MAX_VALUE,
            onTextLayout = { layoutResult ->
                val caretOffset = value.selection.end.coerceIn(0, value.text.length)
                val cursorRect = layoutResult.getCursorRect(caretOffset)
                val comfortPadding = with(density) { 40.dp.toPx() }
                caretRect = Rect(
                    left = cursorRect.left,
                    top = cursorRect.top,
                    right = cursorRect.right,
                    bottom = cursorRect.bottom + comfortPadding,
                )
            },
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.bringIntoViewRequester(bringIntoViewRequester)
                ) {
                    innerTextField()
                }
            },
        )
    }
}

class FindHighlightTransformation(
    private val matches: List<IntRange>,
    private val activeIndex: Int,
    private val allMatchColor: Color,
    private val activeMatchColor: Color,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val builder = AnnotatedString.Builder(text)
        matches.forEachIndexed { index, range ->
            val color = if (index == activeIndex) activeMatchColor else allMatchColor
            builder.addStyle(
                style = SpanStyle(background = color),
                start = range.first.coerceIn(0, text.length),
                end = (range.last + 1).coerceIn(0, text.length),
            )
        }
        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}
