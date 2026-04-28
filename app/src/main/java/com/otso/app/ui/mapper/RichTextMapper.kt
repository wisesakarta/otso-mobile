package com.otso.app.ui.mapper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.style.TextDecoration
import com.otso.app.logic.TypefaceCache
import com.otso.app.model.ContentBlock
import com.otso.app.model.SpanStyleType
import com.otso.app.ui.theme.OtsoColorScheme

private const val HighlightBrightnessThreshold = 186f
private const val ColorChannelScale = 255f
private val DeepCharcoal = Color(0xFF121212)
private val RenaissanceTransform = TextGeometricTransform(scaleX = 0.96f, skewX = -0.25f)

/**
 * Maps a [ContentBlock] to a Compose [AnnotatedString] ready for display.
 *
 * The [rawText] is appended verbatim; all decoration comes exclusively from
 * [ContentBlock.spans], ensuring zero markdown syntax leaks into the rendered output.
 */
fun ContentBlock.toAnnotatedString(colors: OtsoColorScheme): AnnotatedString =
    buildAnnotatedString {
        append(rawText)
        val len = rawText.length
        spans.forEach { span ->
            val start = span.startOffset.coerceIn(0, len)
            val end = span.endOffset.coerceIn(0, len)
            if (start >= end) return@forEach

            val style: SpanStyle = when (span.style) {
                SpanStyleType.Bold ->
                    SpanStyle(
                        fontFamily = FontFamily(TypefaceCache.boldTypeface()),
                        fontWeight = FontWeight.Bold,
                        fontSynthesis = FontSynthesis.None,
                        textGeometricTransform = RenaissanceTransform,
                    )

                SpanStyleType.Italic ->
                    SpanStyle(
                        fontFamily = FontFamily(TypefaceCache.italicTypeface()),
                        fontStyle = FontStyle.Italic,
                        fontSynthesis = FontSynthesis.None,
                        textGeometricTransform = RenaissanceTransform,
                    )

                SpanStyleType.Strikethrough ->
                    SpanStyle(textDecoration = TextDecoration.LineThrough)

                SpanStyleType.Underline ->
                    SpanStyle(textDecoration = TextDecoration.Underline)

                SpanStyleType.Code ->
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = colors.edge.copy(alpha = 0.2f),
                    )

                SpanStyleType.Highlight -> {
                    val bg: Color? = span.colorHex
                        ?.let { hex ->
                            runCatching { Color(android.graphics.Color.parseColor(hex)).copy(alpha = 0.85f) }.getOrNull()
                        }
                        ?: colors.accent.copy(alpha = 0.30f)
                    val resolvedBackground = bg ?: colors.accent.copy(alpha = 0.30f)
                    val isBrightHighlight = isBrightHighlightSafely(bg)
                    if (isBrightHighlight) {
                        SpanStyle(
                            background = resolvedBackground,
                            color = DeepCharcoal,
                        )
                    } else {
                        SpanStyle(background = resolvedBackground)
                    }
                }
            }
            addStyle(style, start, end)
        }
    }

private fun isBrightHighlightSafely(color: Color?): Boolean {
    if (color == null) return false
    return try {
        val red = color.red * ColorChannelScale
        val green = color.green * ColorChannelScale
        val blue = color.blue * ColorChannelScale
        if (!red.isFinite() || !green.isFinite() || !blue.isFinite()) return false
        val luminance = (red * 0.299f) + (green * 0.587f) + (blue * 0.114f)
        luminance.isFinite() && luminance >= HighlightBrightnessThreshold
    } catch (_: Exception) {
        false
    }
}
