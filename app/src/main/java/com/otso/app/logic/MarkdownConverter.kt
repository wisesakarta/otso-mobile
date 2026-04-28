package com.otso.app.logic

import com.otso.app.model.ContentBlock
import com.otso.app.model.SpanStyleType
import com.otso.app.model.TextSpan

private val BOLD_REGEX = Regex("""\*\*([^*\n]+?)\*\*""")
private val ITALIC_REGEX = Regex("""(?<!\*)\*(?!\*)([^*\n]+?)(?<!\*)\*(?!\*)""")
private val STRIKETHROUGH_REGEX = Regex("""~~([^\n]+?)~~""")
private val DEFAULT_HIGHLIGHT_REGEX = Regex("""==([^\n=]+?)==(?!#)""")
private val CUSTOM_HIGHLIGHT_REGEX = Regex("""==([^\n=]+?)==#([A-Fa-f0-9]{6})""")

private data class RawSpanMatch(
    val matchStart: Int,
    val matchEnd: Int,
    val contentStart: Int,
    val contentEnd: Int,
    val style: SpanStyleType,
    val colorHex: String?,
)

/**
 * Parses inline Markdown markers from a raw string into a [ContentBlock].
 * Markers are stripped; span offsets are adjusted to the clean text.
 * Overlapping matches are resolved first-start-wins.
 */
fun String.toContentBlock(): ContentBlock {
    val matches = mutableListOf<RawSpanMatch>()

    BOLD_REGEX.findAll(this).forEach { m ->
        val c = m.groups[1]!!.range
        matches += RawSpanMatch(m.range.first, m.range.last + 1, c.first, c.last + 1, SpanStyleType.Bold, null)
    }
    ITALIC_REGEX.findAll(this).forEach { m ->
        val c = m.groups[1]!!.range
        matches += RawSpanMatch(m.range.first, m.range.last + 1, c.first, c.last + 1, SpanStyleType.Italic, null)
    }
    STRIKETHROUGH_REGEX.findAll(this).forEach { m ->
        val c = m.groups[1]!!.range
        matches += RawSpanMatch(m.range.first, m.range.last + 1, c.first, c.last + 1, SpanStyleType.Strikethrough, null)
    }
    CUSTOM_HIGHLIGHT_REGEX.findAll(this).forEach { m ->
        val c = m.groups[1]!!.range
        matches += RawSpanMatch(m.range.first, m.range.last + 1, c.first, c.last + 1, SpanStyleType.Highlight, "#${m.groups[2]!!.value}")
    }
    DEFAULT_HIGHLIGHT_REGEX.findAll(this).forEach { m ->
        val c = m.groups[1]!!.range
        matches += RawSpanMatch(m.range.first, m.range.last + 1, c.first, c.last + 1, SpanStyleType.Highlight, null)
    }

    if (matches.isEmpty()) return ContentBlock(rawText = this)

    val sorted = matches.sortedWith(compareBy({ it.matchStart }, { it.matchEnd }))
    val nonOverlapping = mutableListOf<RawSpanMatch>()
    var lastEnd = 0
    for (m in sorted) {
        if (m.matchStart >= lastEnd) {
            nonOverlapping += m
            lastEnd = m.matchEnd
        }
    }

    val cleaned = StringBuilder()
    val spans = mutableListOf<TextSpan>()
    var pos = 0
    var shift = 0

    for (m in nonOverlapping) {
        cleaned.append(this, pos, m.matchStart)
        val prefixLen = m.contentStart - m.matchStart
        val suffixLen = m.matchEnd - m.contentEnd
        val spanStart = m.matchStart - shift
        val spanEnd = m.contentEnd - shift - prefixLen
        cleaned.append(this, m.contentStart, m.contentEnd)
        shift += prefixLen + suffixLen
        pos = m.matchEnd
        spans += TextSpan(spanStart, spanEnd, m.style, m.colorHex)
    }
    cleaned.append(this, pos, length)

    return ContentBlock(rawText = cleaned.toString(), spans = spans)
}
