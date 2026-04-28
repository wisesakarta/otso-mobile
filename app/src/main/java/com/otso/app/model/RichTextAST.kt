package com.otso.app.model

import androidx.compose.runtime.Stable
import java.util.UUID

// ─────────────────────────────────────────────
// Block-level structure
// ─────────────────────────────────────────────

/**
 * The semantic role of a content block within a document.
 * Maps directly to the block-level nodes of a standard rich text AST.
 */
enum class BlockType {
    Paragraph,
    Heading1,
    Heading2,
    BulletList,
    CodeBlock,
}

// ─────────────────────────────────────────────
// Inline (span-level) structure
// ─────────────────────────────────────────────

/**
 * The visual/semantic style applied to a contiguous run of characters within a block.
 * Highlight carries an optional [TextSpan.colorHex]; all others are binary.
 */
enum class SpanStyleType {
    Bold,
    Italic,
    Strikethrough,
    Highlight,
    Underline,
    Code,
}

/**
 * A style annotation over the character range [startOffset, endOffset) within
 * the parent [ContentBlock.rawText]. Offsets are zero-based, end-exclusive.
 *
 * @param startOffset  Index of the first styled character (inclusive).
 * @param endOffset    Index past the last styled character (exclusive).
 * @param style        The decoration applied to this run.
 * @param colorHex     Optional hex color string (e.g. "#F9EB73"). Only meaningful
 *                     when [style] is [SpanStyleType.Highlight].
 */
data class TextSpan(
    val startOffset: Int,
    val endOffset: Int,
    val style: SpanStyleType,
    val colorHex: String? = null,
) {
    init {
        require(startOffset >= 0) { "startOffset must be >= 0" }
        require(endOffset >= startOffset) { "endOffset must be >= startOffset" }
    }
}

// ─────────────────────────────────────────────
// Document node
// ─────────────────────────────────────────────

/**
 * A single structural unit of the document — the fundamental node of the AST.
 *
 * [rawText] contains the plain content with NO inline markdown syntax characters.
 * All decoration is expressed exclusively through [spans], making the raw string
 * safe to display, search, and export without stripping logic.
 *
 * @param blockId  Stable unique identifier for this block (survives reordering).
 * @param type     The semantic role of this block.
 * @param rawText  Plain text content, free of markdown syntax.
 * @param spans    Ordered list of inline style annotations over [rawText].
 */
@Stable
data class ContentBlock(
    val blockId: String = UUID.randomUUID().toString(),
    val type: BlockType = BlockType.Paragraph,
    val rawText: String = "",
    val spans: List<TextSpan> = emptyList(),
)
