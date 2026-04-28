package com.otso.app.viewmodel

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.otso.app.logic.shiftOffsets
import com.otso.app.model.BlockType
import com.otso.app.model.ContentBlock
import com.otso.app.model.SpanStyleType
import com.otso.app.model.TextSpan
import java.util.Locale

/**
 * Compose-observable state holder for a document represented as ordered blocks.
 *
 * Owns both block-level content and the active cursor selection.
 * Call [onTextChange] from a BasicTextField's onValueChange callback to keep
 * the active block in sync with every keystroke while automatically shifting all
 * span boundaries via [shiftOffsets].
 */
@Stable
class RichTextState(initialBlock: ContentBlock) {

    val blocks = mutableStateListOf<ContentBlock>()

    var activeBlockId by mutableStateOf(initialBlock.blockId)
        private set

    // Temporary compatibility bridge while UI is still single-block.
    var block: ContentBlock
        get() {
            ensureAtLeastOneBlock()
            return blocks[currentBlockIndex()]
        }
        private set(value) {
            ensureAtLeastOneBlock()
            blocks[currentBlockIndex()] = value
            activeBlockId = value.blockId
        }

    var selection by mutableStateOf(TextRange.Zero)
        private set

    init {
        reset(initialBlock)
    }

    private data class InlineMarkdownPattern(
        val style: SpanStyleType,
        val regex: Regex,
    )

    private data class InlineMarkdownMatch(
        val style: SpanStyleType,
        val cleanText: String,
        val tokenStart: Int,
        val closeDelimiterStart: Int,
        val openDelimiterLength: Int,
        val closeDelimiterLength: Int,
        val cleanContentStart: Int,
        val cleanContentEnd: Int,
        val cleanCursor: Int,
    )

    private val inlineMarkdownPatterns = listOf(
        InlineMarkdownPattern(
            style = SpanStyleType.Bold,
            regex = Regex("""(?:^|\s)(\*\*([^*\n]+?)\*\*) $"""),
        ),
        InlineMarkdownPattern(
            style = SpanStyleType.Italic,
            regex = Regex("""(?:^|\s)(\*([^*\n]+?)\*) $"""),
        ),
        InlineMarkdownPattern(
            style = SpanStyleType.Strikethrough,
            regex = Regex("""(?:^|\s)(~~([^~\n]+?)~~) $"""),
        ),
        InlineMarkdownPattern(
            style = SpanStyleType.Underline,
            regex = Regex("""(?:^|\s)(_([^_\n]+?)_) $"""),
        ),
        InlineMarkdownPattern(
            style = SpanStyleType.Code,
            regex = Regex("""(?:^|\s)(`([^`\n]+?)`) $"""),
        ),
    )

    fun onTextChange(newTfv: TextFieldValue) {
        updateBlock(activeBlockId, newTfv)
    }

    fun setActiveBlock(blockId: String, cursorPosition: Int? = null) {
        val index = blocks.indexOfFirst { it.blockId == blockId }
        if (index < 0) return
        if (activeBlockId != blockId) activeBlockId = blockId
        val length = blocks[index].rawText.length
        val cursor = (cursorPosition ?: selection.end).coerceIn(0, length)
        selection = TextRange(cursor)
    }

    fun getSelectionForBlock(blockId: String): TextRange {
        val index = blocks.indexOfFirst { it.blockId == blockId }
        if (index < 0) return TextRange.Zero
        val length = blocks[index].rawText.length
        return if (activeBlockId == blockId) {
            TextRange(
                start = selection.start.coerceIn(0, length),
                end = selection.end.coerceIn(0, length),
            )
        } else {
            TextRange.Zero
        }
    }

    fun updateBlock(blockId: String, newTfv: TextFieldValue) {
        val index = blocks.indexOfFirst { it.blockId == blockId }
        if (index < 0) return

        val current = blocks[index]
        activeBlockId = blockId
        selection = TextRange(
            start = newTfv.selection.start.coerceIn(0, newTfv.text.length),
            end = newTfv.selection.end.coerceIn(0, newTfv.text.length),
        )

        val oldText = current.rawText
        val newText = newTfv.text
        if (oldText == newText) return

        val changeIndex = firstDivergence(oldText, newText)
        val delta = newText.length - oldText.length
        val shiftedSpans = current.spans.shiftOffsets(changeIndex, delta)
        val cursor = selection.end.coerceIn(0, newText.length)
        val markdownMatch = if (
            selection.collapsed &&
            delta > 0 &&
            cursor > 0 &&
            newText[cursor - 1] == ' '
        ) {
            detectInlineMarkdown(text = newText, cursor = cursor)
        } else {
            null
        }

        if (markdownMatch == null) {
            blocks[index] = blocks[index].copy(rawText = newText, spans = shiftedSpans)
            return
        }

        var parsedSpans = shiftedSpans
            .shiftOffsets(markdownMatch.closeDelimiterStart, -markdownMatch.closeDelimiterLength)
            .shiftOffsets(markdownMatch.tokenStart, -markdownMatch.openDelimiterLength)
        val styleSpans = parsedSpans.filter { it.style == markdownMatch.style }
        val updatedStyleSpans = mergeSpans(
            styleSpans + TextSpan(
                startOffset = markdownMatch.cleanContentStart,
                endOffset = markdownMatch.cleanContentEnd,
                style = markdownMatch.style,
            ),
        )
        parsedSpans = (parsedSpans.filter { it.style != markdownMatch.style } + updatedStyleSpans).sortedWith(
            compareBy<TextSpan>(
                { it.startOffset },
                { it.endOffset },
                { it.style.ordinal },
                { it.colorHex ?: "" },
            ),
        )

        blocks[index] = blocks[index].copy(
            rawText = markdownMatch.cleanText,
            spans = parsedSpans,
        )
        selection = TextRange(markdownMatch.cleanCursor)
    }

    /**
     * Returns true when [style] fully covers the current selection.
     * For a collapsed cursor, checks the single character immediately before the caret.
     */
    fun hasStyle(style: SpanStyleType): Boolean {
        val textLength = block.rawText.length
        val (start, end) = if (selection.collapsed) {
            // If collapsed, check the character immediately preceding the cursor.
            val cursor = selection.start.coerceIn(0, textLength)
            if (cursor == 0) return false
            (cursor - 1) to cursor
        } else {
            selection.min.coerceIn(0, textLength) to selection.max.coerceIn(0, textLength)
        }
        if (start >= end) return false
        val styleSpans = block.spans.filter { it.style == style }
        return isFullyCovered(styleSpans, start, end)
    }

    /**
     * Toggles an inline style over the current selection.
     *
     * If the selection is fully covered by [style], style is removed from that range.
     * Otherwise the style is added across the whole range.
     */
    fun toggleStyle(style: SpanStyleType) {
        val (start, end) = normalizedSelection() ?: return
        val styleSpans = block.spans.filter { it.style == style }
        val updatedStyleSpans = if (isFullyCovered(styleSpans, start, end)) {
            subtractRange(styleSpans, start, end)
        } else {
            mergeSpans(styleSpans + TextSpan(start, end, style = style))
        }

        block = block.copy(
            spans = mergeAllStyles(style, updatedStyleSpans),
        )
    }

    /**
     * Applies a highlight to the current selection.
     * Existing highlight spans in the range are replaced.
     */
    fun addHighlight(colorHex: String? = null) {
        val (start, end) = normalizedSelection() ?: return
        val normalizedColor = normalizeColorHex(colorHex)
        val highlightSpans = block.spans.filter { it.style == SpanStyleType.Highlight }
        val preserved = subtractRange(highlightSpans, start, end)
        val added = TextSpan(
            startOffset = start,
            endOffset = end,
            style = SpanStyleType.Highlight,
            colorHex = normalizedColor,
        )
        block = block.copy(
            spans = mergeAllStyles(
                SpanStyleType.Highlight,
                mergeSpans(preserved + added),
            ),
        )
    }

    /**
     * Clears highlight spans from the current selection.
     */
    fun clearHighlight() {
        val (start, end) = normalizedSelection() ?: return
        val highlightSpans = block.spans.filter { it.style == SpanStyleType.Highlight }
        val updatedHighlights = subtractRange(highlightSpans, start, end)
        block = block.copy(
            spans = mergeAllStyles(SpanStyleType.Highlight, updatedHighlights),
        )
    }

    /**
     * Inserts [insert] at the current cursor, replacing selection if present.
     * Span offsets are adjusted to preserve existing formatting geometry.
     */
    fun insertAtCursor(insert: String) {
        insertTextAtSelection(insert)
    }

    /**
     * Replaces the current selection with [insertText], or inserts at cursor when selection is empty.
     * Span offsets are adjusted for delete+insert so existing formatting stays aligned.
     */
    fun insertTextAtSelection(insertText: String) {
        if (insertText.isEmpty()) return

        val text = block.rawText
        val selectedRange = normalizedSelection()
        val start = selectedRange?.first ?: selection.min.coerceIn(0, text.length)
        val end = selectedRange?.second ?: selection.max.coerceIn(0, text.length)
        val deletedLength = end - start

        val afterDeleteSpans = if (deletedLength > 0) {
            block.spans.shiftOffsets(start, -deletedLength)
        } else {
            block.spans
        }
        val afterInsertSpans = afterDeleteSpans.shiftOffsets(start, insertText.length)

        val newText = buildString {
            append(text, 0, start)
            append(insertText)
            append(text, end, text.length)
        }
        val caret = start + insertText.length

        block = block.copy(
            rawText = newText,
            spans = afterInsertSpans,
        )
        selection = TextRange(caret)
    }

    /**
     * Replaces the whole editor text from an external domain action (e.g. Find/Replace).
     * Existing inline spans are dropped to avoid stale offsets after bulk transforms.
     */
    fun updateText(newText: String, newCursorOffset: Int? = null) {
        block = block.copy(
            rawText = newText,
            spans = emptyList(),
        )
        val cursor = (newCursorOffset ?: selection.end).coerceIn(0, newText.length)
        selection = TextRange(cursor)
    }

    /**
     * Resets the state to a fresh [newBlock], typically after external ViewModel edits.
     */
    fun reset(newBlock: ContentBlock, newSelection: TextRange = TextRange(newBlock.rawText.length)) {
        blocks.clear()
        val lines = newBlock.rawText.split("\n")
        if (lines.size == 1) {
            blocks += newBlock
        } else {
            var lineOffset = 0
            for (line in lines) {
                val lineStart = lineOffset
                val lineEnd = lineOffset + line.length
                val lineSpans = newBlock.spans.mapNotNull { span ->
                    val s = maxOf(span.startOffset, lineStart)
                    val e = minOf(span.endOffset, lineEnd)
                    if (s >= e) null else span.copy(startOffset = s - lineStart, endOffset = e - lineStart)
                }
                blocks += ContentBlock(type = BlockType.Paragraph, rawText = line, spans = lineSpans)
                lineOffset += line.length + 1
            }
        }
        // Map flat-text cursor to block-relative cursor
        val flatCursor = newSelection.end.coerceIn(0, newBlock.rawText.length)
        var cursorOffset = 0
        var targetBlock = blocks.last()
        var relCursor = targetBlock.rawText.length
        for (b in blocks) {
            val blockEnd = cursorOffset + b.rawText.length
            if (flatCursor <= blockEnd) {
                targetBlock = b
                relCursor = (flatCursor - cursorOffset).coerceIn(0, b.rawText.length)
                break
            }
            cursorOffset += b.rawText.length + 1
        }
        activeBlockId = targetBlock.blockId
        selection = TextRange(relCursor)
        ensureAtLeastOneBlock()
    }

    /**
     * Splits a block at [cursorPosition], inserting a new paragraph block after it.
     * The current block keeps content before cursor; the new block receives content after cursor.
     */
    fun splitBlockAtCursor(blockId: String, cursorPosition: Int) {
        val index = blocks.indexOfFirst { it.blockId == blockId }
        if (index < 0) return

        val source = blocks[index]
        val cursor = cursorPosition.coerceIn(0, source.rawText.length)
        val beforeText = source.rawText.substring(0, cursor)
        val afterText = source.rawText.substring(cursor)

        val beforeSpans = normalizeSpans(
            source.spans.mapNotNull { span ->
                val start = span.startOffset.coerceAtLeast(0)
                val end = minOf(span.endOffset, cursor)
                if (end <= start) null else span.copy(startOffset = start, endOffset = end)
            },
        )
        val afterSpans = normalizeSpans(
            source.spans.mapNotNull { span ->
                val start = maxOf(span.startOffset, cursor)
                val end = minOf(span.endOffset, source.rawText.length)
                if (end <= start) {
                    null
                } else {
                    span.copy(
                        startOffset = start - cursor,
                        endOffset = end - cursor,
                    )
                }
            },
        )

        blocks[index] = source.copy(
            rawText = beforeText,
            spans = beforeSpans,
        )
        val newBlock = ContentBlock(
            type = BlockType.Paragraph,
            rawText = afterText,
            spans = afterSpans,
        )
        blocks.add(index + 1, newBlock)
        activeBlockId = newBlock.blockId
        selection = TextRange.Zero
    }

    /**
     * Merges the block identified by [blockId] into its previous block.
     * No-op when the target is the first block.
     */
    fun mergeBlockWithPrevious(blockId: String) {
        val index = blocks.indexOfFirst { it.blockId == blockId }
        if (index <= 0) return

        val previous = blocks[index - 1]
        val current = blocks[index]
        val offset = previous.rawText.length
        val shiftedCurrentSpans = current.spans.map { span ->
            span.copy(
                startOffset = span.startOffset + offset,
                endOffset = span.endOffset + offset,
            )
        }
        val merged = previous.copy(
            rawText = previous.rawText + current.rawText,
            spans = normalizeSpans(previous.spans + shiftedCurrentSpans),
        )

        blocks[index - 1] = merged
        blocks.removeAt(index)
        ensureAtLeastOneBlock()
        activeBlockId = merged.blockId
        selection = TextRange(offset)
    }

    /**
     * Temporary fallback for legacy single-text UI pipelines during modular migration.
     */
    fun getFlatText(separator: String = "\n"): String {
        ensureAtLeastOneBlock()
        return blocks.joinToString(separator) { it.rawText }
    }

    // Finds the leftmost index where old and new text first differ.
    // For a simple insert/delete this is the exact mutation point.
    private fun firstDivergence(old: String, new: String): Int {
        val limit = minOf(old.length, new.length)
        for (i in 0 until limit) {
            if (old[i] != new[i]) return i
        }
        return limit
    }

    private fun currentBlockIndex(): Int {
        ensureAtLeastOneBlock()
        val index = blocks.indexOfFirst { it.blockId == activeBlockId }
        return if (index >= 0) index else 0
    }

    private fun ensureAtLeastOneBlock() {
        if (blocks.isEmpty()) {
            blocks += ContentBlock(type = BlockType.Paragraph)
        }
        if (blocks.none { it.blockId == activeBlockId }) {
            activeBlockId = blocks.first().blockId
        }
    }

    private fun detectInlineMarkdown(text: String, cursor: Int): InlineMarkdownMatch? {
        val prefix = text.substring(0, cursor)
        for (pattern in inlineMarkdownPatterns) {
            val match = pattern.regex.find(prefix) ?: continue
            val tokenGroup = match.groups[1] ?: continue
            val contentGroup = match.groups[2] ?: continue
            val tokenStart = tokenGroup.range.first
            val tokenEndExclusive = tokenGroup.range.last + 1
            val contentStart = contentGroup.range.first
            val contentEndExclusive = contentGroup.range.last + 1
            val openDelimiterLength = contentStart - tokenStart
            val closeDelimiterLength = tokenEndExclusive - contentEndExclusive
            if (openDelimiterLength <= 0 || closeDelimiterLength <= 0) continue

            val cleanText = buildString {
                append(text, 0, tokenStart)
                append(text, contentStart, contentEndExclusive)
                append(text, tokenEndExclusive, text.length)
            }
            val cleanContentStart = tokenStart
            val cleanContentEnd = tokenStart + (contentEndExclusive - contentStart)
            val cleanCursor = (cursor - openDelimiterLength - closeDelimiterLength).coerceIn(0, cleanText.length)

            return InlineMarkdownMatch(
                style = pattern.style,
                cleanText = cleanText,
                tokenStart = tokenStart,
                closeDelimiterStart = contentEndExclusive,
                openDelimiterLength = openDelimiterLength,
                closeDelimiterLength = closeDelimiterLength,
                cleanContentStart = cleanContentStart,
                cleanContentEnd = cleanContentEnd,
                cleanCursor = cleanCursor,
            )
        }
        return null
    }

    private fun normalizedSelection(): Pair<Int, Int>? {
        val len = block.rawText.length
        val start = selection.min.coerceIn(0, len)
        val end = selection.max.coerceIn(0, len)
        if (start == end) return null
        return start to end
    }

    private fun isFullyCovered(spans: List<TextSpan>, start: Int, end: Int): Boolean {
        if (spans.isEmpty()) return false
        val sorted = spans.sortedBy { it.startOffset }
        var cursor = start
        for (span in sorted) {
            if (span.endOffset <= cursor || span.startOffset >= end) continue
            if (span.startOffset > cursor) return false
            cursor = maxOf(cursor, span.endOffset)
            if (cursor >= end) return true
        }
        return cursor >= end
    }

    private fun subtractRange(spans: List<TextSpan>, start: Int, end: Int): List<TextSpan> {
        return spans.flatMap { span ->
            if (span.endOffset <= start || span.startOffset >= end) {
                listOf(span)
            } else {
                buildList {
                    if (span.startOffset < start) {
                        add(
                            span.copy(
                                startOffset = span.startOffset,
                                endOffset = start,
                            ),
                        )
                    }
                    if (span.endOffset > end) {
                        add(
                            span.copy(
                                startOffset = end,
                                endOffset = span.endOffset,
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun mergeAllStyles(targetStyle: SpanStyleType, targetSpans: List<TextSpan>): List<TextSpan> {
        val others = block.spans.filter { it.style != targetStyle }
        return (others + targetSpans).sortedWith(
            compareBy<TextSpan>(
                { it.startOffset },
                { it.endOffset },
                { it.style.ordinal },
                { it.colorHex ?: "" },
            ),
        )
    }

    private fun mergeSpans(spans: List<TextSpan>): List<TextSpan> {
        if (spans.isEmpty()) return emptyList()
        val sorted = spans.sortedWith(
            compareBy<TextSpan>(
                { it.startOffset },
                { it.endOffset },
                { it.colorHex ?: "" },
            ),
        )
        val merged = mutableListOf<TextSpan>()
        for (span in sorted) {
            if (merged.isEmpty()) {
                merged += span
                continue
            }
            val last = merged.last()
            val isMergeable = last.style == span.style &&
                (last.colorHex == span.colorHex) &&
                (span.startOffset <= last.endOffset)
            if (isMergeable) {
                merged[merged.lastIndex] = last.copy(
                    endOffset = maxOf(last.endOffset, span.endOffset),
                )
            } else {
                merged += span
            }
        }
        return merged
    }

    private fun normalizeSpans(spans: List<TextSpan>): List<TextSpan> {
        if (spans.isEmpty()) return emptyList()
        val mergedByStyle = spans
            .groupBy { it.style to it.colorHex }
            .values
            .flatMap { mergeSpans(it) }
        return mergedByStyle.sortedWith(
            compareBy<TextSpan>(
                { it.startOffset },
                { it.endOffset },
                { it.style.ordinal },
                { it.colorHex ?: "" },
            ),
        )
    }

    private fun normalizeColorHex(colorHex: String?): String? {
        val cleaned = colorHex
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.removePrefix("#")
            ?.uppercase(Locale.US)
            ?: return null
        return if (cleaned.length == 6 && cleaned.all { it.isDigit() || it in 'A'..'F' }) {
            "#$cleaned"
        } else {
            null
        }
    }
}
