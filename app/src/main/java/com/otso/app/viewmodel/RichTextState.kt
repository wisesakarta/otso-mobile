package com.otso.app.viewmodel

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.otso.app.logic.atomicReplaceSpans
import com.otso.app.logic.shiftOffsets
import com.otso.app.model.BlockType
import com.otso.app.model.ContentBlock
import com.otso.app.model.SpanStyleType
import com.otso.app.model.TextSpan
import java.util.Locale
import com.otso.app.BuildConfig

data class EditorSnapshot(
    val blocks: List<ContentBlock>,
    val selection: TextRange,
    val activeBlockId: String
)

/**
 * Represents a selection that spans multiple blocks.
 * [anchorBlockId] is where the selection started, [focusBlockId] is where it ends.
 * [anchorOffset] and [focusOffset] are character positions within their respective blocks.
 */
data class MultiBlockSelection(
    val anchorBlockId: String,
    val anchorOffset: Int,
    val focusBlockId: String,
    val focusOffset: Int,
) {
    companion object {
        val None = MultiBlockSelection("", 0, "", 0)
    }
    val isActive: Boolean get() = anchorBlockId.isNotEmpty() && focusBlockId.isNotEmpty()
}

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

    /**
     * Tracks a selection that spans across multiple blocks.
     * When active, all blocks between anchor and focus (inclusive) are considered selected.
     */
    var multiBlockSelection by mutableStateOf(MultiBlockSelection.None)
        private set

    private val undoStack = kotlin.collections.ArrayDeque<EditorSnapshot>()
    private val redoStack = kotlin.collections.ArrayDeque<EditorSnapshot>()
    private val maxHistorySize = 100

    private var lastTypingSnapshotTime = 0L
    private val typingDebounceMs = 500L

    var canUndo by mutableStateOf(false)
        private set
    var canRedo by mutableStateOf(false)
        private set

    private fun syncHistoryState() {
        canUndo = undoStack.isNotEmpty()
        canRedo = redoStack.isNotEmpty()
    }

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

    /**
     * Initiates or extends a multi-block selection.
     * Called when the user's selection reaches the boundary of a block (start or end).
     *
     * @param fromBlockId The block where the selection boundary was hit.
     * @param direction -1 for extending upward (previous block), +1 for extending downward (next block).
     */
    fun extendSelectionToAdjacentBlock(fromBlockId: String, direction: Int) {
        val fromIndex = blocks.indexOfFirst { it.blockId == fromBlockId }
        if (fromIndex < 0) return
        val targetIndex = fromIndex + direction
        if (targetIndex < 0 || targetIndex >= blocks.size) return

        val targetBlock = blocks[targetIndex]

        if (!multiBlockSelection.isActive) {
            // Start a new multi-block selection
            val fromBlock = blocks[fromIndex]
            val anchorOffset: Int
            val focusOffset: Int
            if (direction < 0) {
                // Extending upward: anchor is at end of current block, focus at start of previous
                anchorOffset = fromBlock.rawText.length
                focusOffset = 0
            } else {
                // Extending downward: anchor is at start of current block, focus at end of next
                anchorOffset = 0
                focusOffset = targetBlock.rawText.length
            }
            multiBlockSelection = MultiBlockSelection(
                anchorBlockId = fromBlockId,
                anchorOffset = anchorOffset,
                focusBlockId = targetBlock.blockId,
                focusOffset = focusOffset,
            )
            if (BuildConfig.DEBUG) android.util.Log.i("OtsoState", "extendSelection NEW: from=$fromBlockId(dir=$direction) → anchor=$fromBlockId offset=$anchorOffset, focus=${targetBlock.blockId} offset=$focusOffset")
        } else {
            // Extend existing multi-block selection
            val focusOffset = if (direction < 0) 0 else targetBlock.rawText.length
            multiBlockSelection = multiBlockSelection.copy(
                focusBlockId = targetBlock.blockId,
                focusOffset = focusOffset,
            )
            if (BuildConfig.DEBUG) android.util.Log.i("OtsoState", "extendSelection EXTEND: focus → ${targetBlock.blockId} offset=$focusOffset")
        }
        // Do NOT change activeBlockId here: moving focus to the target block causes the IME
        // to attach to a new InputConnection, which resets the selection to collapsed [0,0],
        // then clearMultiBlockSelection() fires immediately, erasing the selection we just set.
        // The multi-block overlay is driven by multiBlockSelection state alone — no focus move needed.
    }

    /**
     * Clears the multi-block selection state.
     * Called when the user taps or makes a new single-block selection.
     */
    fun clearMultiBlockSelection() {
        if (multiBlockSelection.isActive) {
            if (BuildConfig.DEBUG) android.util.Log.i("OtsoState", "clearMultiBlockSelection: was anchor=${multiBlockSelection.anchorBlockId}, focus=${multiBlockSelection.focusBlockId}")
            multiBlockSelection = MultiBlockSelection.None
        }
    }

    fun selectAll() {
        if (blocks.isEmpty()) return
        val first = blocks.first()
        val last = blocks.last()
        if (blocks.size == 1) {
            selection = TextRange(0, first.rawText.length)
            if (BuildConfig.DEBUG) android.util.Log.i("OtsoState", "selectAll: single block, selection=${selection}")
            return
        }
        multiBlockSelection = MultiBlockSelection(
            anchorBlockId = first.blockId,
            anchorOffset = 0,
            focusBlockId = last.blockId,
            focusOffset = last.rawText.length,
        )
        // selection represents the range within the ACTIVE block
        val activeBlock = blocks.firstOrNull { it.blockId == activeBlockId } ?: first
        selection = TextRange(0, activeBlock.rawText.length)
        if (BuildConfig.DEBUG) android.util.Log.i("OtsoState", "selectAll: ${blocks.size} blocks, multiBlock anchor=${first.blockId} focus=${last.blockId}, activeBlock selection=${selection}")
    }

    /**
     * Returns whether a given block is fully or partially within the current multi-block selection.
     * Returns a Pair of (startOffset, endOffset) for the selected range within this block,
     * or null if the block is not part of the selection.
     */
    fun getMultiBlockSelectionRange(blockId: String): Pair<Int, Int>? {
        if (!multiBlockSelection.isActive) return null

        val anchorIndex = blocks.indexOfFirst { it.blockId == multiBlockSelection.anchorBlockId }
        val focusIndex = blocks.indexOfFirst { it.blockId == multiBlockSelection.focusBlockId }
        val blockIndex = blocks.indexOfFirst { it.blockId == blockId }
        if (anchorIndex < 0 || focusIndex < 0 || blockIndex < 0) return null

        val startIndex = minOf(anchorIndex, focusIndex)
        val endIndex = maxOf(anchorIndex, focusIndex)

        if (blockIndex < startIndex || blockIndex > endIndex) return null

        val block = blocks[blockIndex]
        val isAnchorFirst = anchorIndex <= focusIndex

        return when {
            blockIndex == startIndex && blockIndex == endIndex -> {
                // Single block in range (shouldn't happen for multi-block, but handle gracefully)
                val s = if (isAnchorFirst) multiBlockSelection.anchorOffset else multiBlockSelection.focusOffset
                val e = if (isAnchorFirst) multiBlockSelection.focusOffset else multiBlockSelection.anchorOffset
                minOf(s, e) to maxOf(s, e)
            }
            blockIndex == startIndex -> {
                // First block in range
                val offset = if (isAnchorFirst) multiBlockSelection.anchorOffset else multiBlockSelection.focusOffset
                offset to block.rawText.length
            }
            blockIndex == endIndex -> {
                // Last block in range
                val offset = if (isAnchorFirst) multiBlockSelection.focusOffset else multiBlockSelection.anchorOffset
                0 to offset
            }
            else -> {
                // Middle block — fully selected
                0 to block.rawText.length
            }
        }
    }

    /**
     * Returns the full selected text across multiple blocks, joined by newlines.
     */
    fun getMultiBlockSelectedText(): String {
        if (!multiBlockSelection.isActive) return ""

        val anchorIndex = blocks.indexOfFirst { it.blockId == multiBlockSelection.anchorBlockId }
        val focusIndex = blocks.indexOfFirst { it.blockId == multiBlockSelection.focusBlockId }
        if (anchorIndex < 0 || focusIndex < 0) return ""

        val startIndex = minOf(anchorIndex, focusIndex)
        val endIndex = maxOf(anchorIndex, focusIndex)
        val isAnchorFirst = anchorIndex <= focusIndex

        return buildString {
            for (i in startIndex..endIndex) {
                val block = blocks[i]
                val range = getMultiBlockSelectionRange(block.blockId) ?: continue
                if (isNotEmpty()) append('\n')
                append(block.rawText.substring(range.first.coerceIn(0, block.rawText.length), range.second.coerceIn(0, block.rawText.length)))
            }
        }
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
            TextRange(length)
        }
    }

    fun updateBlock(blockId: String, newTfv: TextFieldValue) {
        val index = blocks.indexOfFirst { it.blockId == blockId }
        if (index < 0) return

        val current = blocks[index]
        val oldText = current.rawText
        val newText = newTfv.text
        
        // Snapshot only on text changes, ignore pure selection updates to avoid history pollution
        if (oldText != newText) {
            saveSnapshot(isTyping = true)
        }
        activeBlockId = blockId
        selection = newTfv.selection

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
            val cursor = selection.start.coerceIn(0, textLength)
            if (cursor == 0) return false
            (cursor - 1) to cursor
        } else {
            selection.min.coerceIn(0, textLength) to selection.max.coerceIn(0, textLength)
        }
        if (start >= end) return false

        // Optimization: Single pass check instead of filter + isFullyCovered
        // This avoids creating a temporary list on every toolbar update.
        return isFullyCoveredOptimized(block.spans, style, start, end)
    }

    private fun isFullyCoveredOptimized(spans: List<TextSpan>, style: SpanStyleType, start: Int, end: Int): Boolean {
        // Simple but high-performance coverage check
        var coveredUntil = start
        // We assume spans are somewhat sorted or we just check all that match the style
        for (span in spans) {
            if (span.style != style) continue
            if (span.startOffset <= coveredUntil && span.endOffset > coveredUntil) {
                coveredUntil = span.endOffset
            }
            if (coveredUntil >= end) return true
        }
        return false
    }

    /**
     * Toggles an inline style over the current selection.
     *
     * If the selection is fully covered by [style], style is removed from that range.
     * Otherwise the style is added across the whole range.
     */
    fun toggleStyle(style: SpanStyleType) {
        saveSnapshot()
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
        saveSnapshot()
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
        saveSnapshot()
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

    fun insertLinkAtSelection() {
        val text = block.rawText
        val len = text.length
        val start = selection.min.coerceIn(0, len)
        val end = selection.max.coerceIn(0, len)
        val selectedText = text.substring(start, end)

        val insertText: String
        val cursorPos: Int
        if (selectedText.isNotEmpty()) {
            insertText = "[$selectedText]()"
            cursorPos = start + selectedText.length + 3
        } else {
            insertText = "[]()"
            cursorPos = start + 1
        }

        val newSpans = block.spans.atomicReplaceSpans(start, end, insertText.length)
        val newText = buildString {
            append(text, 0, start)
            append(insertText)
            append(text, end, text.length)
        }
        block = block.copy(rawText = newText, spans = newSpans)
        selection = TextRange(cursorPos)
    }

    /**
     * Replaces the current selection with [insertText], or inserts at cursor when selection is empty.
     * Span offsets are adjusted for delete+insert so existing formatting stays aligned.
     */
    fun insertTextAtSelection(insertText: String) {
        if (insertText.isEmpty()) return
        saveSnapshot()

        val text = block.rawText
        val selectedRange = normalizedSelection()
        val start = selectedRange?.first ?: selection.min.coerceIn(0, text.length)
        val end = selectedRange?.second ?: selection.max.coerceIn(0, text.length)

        val newSpans = block.spans.atomicReplaceSpans(start, end, insertText.length)
        val newText = buildString {
            append(text, 0, start)
            append(insertText)
            append(text, end, text.length)
        }
        val caret = start + insertText.length

        block = block.copy(
            rawText = newText,
            spans = newSpans,
        )
        selection = TextRange(caret)
    }

    /**
     * Replaces the whole editor text from an external domain action (e.g. Find/Replace).
     * Existing inline spans are dropped to avoid stale offsets after bulk transforms.
     */
    fun updateText(newText: String, newCursorOffset: Int? = null) {
        saveSnapshot()
        block = block.copy(
            rawText = newText,
            spans = emptyList(),
        )
        val cursor = (newCursorOffset ?: selection.end).coerceIn(0, newText.length)
        selection = TextRange(cursor)
    }

    /**
     * Resets the state to a fresh [newBlock], typically after external ViewModel edits.
     * Preserves the current selection if no [newSelection] is explicitly provided.
     */
    fun reset(newBlock: ContentBlock, newSelection: TextRange? = null) {
        val targetSelection = newSelection ?: this.selection
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
        val flatCursor = targetSelection.end.coerceIn(0, newBlock.rawText.length)
        var cursorOffset = 0
        var targetBlockFound = blocks.last()
        var relCursor = targetBlockFound.rawText.length
        
        for (b in blocks) {
            val blockEnd = cursorOffset + b.rawText.length
            if (flatCursor <= blockEnd) {
                targetBlockFound = b
                relCursor = (flatCursor - cursorOffset).coerceIn(0, b.rawText.length)
                break
            }
            cursorOffset += b.rawText.length + 1
        }
        
        activeBlockId = targetBlockFound.blockId
        selection = TextRange(relCursor)
        ensureAtLeastOneBlock()
    }

    /**
     * Splits a block at [cursorPosition], inserting a new paragraph block after it.
     * The current block keeps content before cursor; the new block receives content after cursor.
     */
    fun splitBlockAtCursor(blockId: String, cursorPosition: Int) {
        saveSnapshot()
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
        if (BuildConfig.DEBUG) android.util.Log.i("OtsoStructural", "Block split: index=$index, newId=${newBlock.blockId}")
    }

    /**
     * Merges the block identified by [blockId] into its previous block.
     * No-op when the target is the first block.
     */
    fun mergeBlockWithPrevious(blockId: String) {
        saveSnapshot()
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

    fun undo() {
        if (undoStack.isEmpty()) return
        val currentSnapshot = EditorSnapshot(blocks.toList(), selection, activeBlockId)
        redoStack.addLast(currentSnapshot)
        
        val snapshot = undoStack.removeLast()
        applySnapshot(snapshot)
        syncHistoryState()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val currentSnapshot = EditorSnapshot(blocks.toList(), selection, activeBlockId)
        undoStack.addLast(currentSnapshot)
        
        val snapshot = redoStack.removeLast()
        applySnapshot(snapshot)
        syncHistoryState()
    }

    private fun saveSnapshot(isTyping: Boolean = false) {
        val now = System.currentTimeMillis()
        if (isTyping) {
            if (now - lastTypingSnapshotTime < typingDebounceMs) {
                // Coalesce: do not save a new snapshot, extend the typing window
                lastTypingSnapshotTime = now
                return
            }
            lastTypingSnapshotTime = now
        } else {
            // Structural change breaks the typing coalesce window
            lastTypingSnapshotTime = 0L
        }

        val snapshot = EditorSnapshot(blocks.toList(), selection, activeBlockId)
        undoStack.addLast(snapshot)
        if (undoStack.size > maxHistorySize) {
            undoStack.removeFirst()
        }
        redoStack.clear()
        syncHistoryState()
    }

    private fun applySnapshot(snapshot: EditorSnapshot) {
        blocks.clear()
        blocks.addAll(snapshot.blocks)
        activeBlockId = snapshot.activeBlockId
        selection = snapshot.selection
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
        // Optimization: Only search within the current line to prevent O(N) regex scanning on every keystroke
        val lineStart = text.lastIndexOf('\n', cursor - 1).let { if (it < 0) 0 else it + 1 }
        val searchChunk = text.substring(lineStart, cursor)
        
        for (pattern in inlineMarkdownPatterns) {
            val match = pattern.regex.find(searchChunk) ?: continue
            val tokenGroup = match.groups[1] ?: continue
            val contentGroup = match.groups[2] ?: continue
            
            // Adjust indices back to global text coordinates
            val tokenStart = lineStart + tokenGroup.range.first
            val tokenEndExclusive = lineStart + tokenGroup.range.last + 1
            val contentStart = lineStart + contentGroup.range.first
            val contentEndExclusive = lineStart + contentGroup.range.last + 1
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
