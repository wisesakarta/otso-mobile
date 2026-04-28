package com.otso.app.logic

import com.otso.app.model.TextSpan

/**
 * Adjusts all span boundaries after a single text mutation.
 *
 * [insertIndex] — the left edge of the change (inclusive).
 * [delta]       — characters added (positive) or removed (negative).
 *
 * Insertion rules (delta > 0):
 *   - Span entirely before insertIndex    → unchanged.
 *   - Insertion at or before span.start   → shift both boundaries right.
 *   - Insertion strictly inside span      → extend end only (typed char joins the span).
 *   - Insertion at or after span.end      → unchanged.
 *
 * Deletion rules (delta < 0), deleted range = [insertIndex, insertIndex + |delta|):
 *   - Span entirely before deleted range  → unchanged.
 *   - Span entirely after deleted range   → shift both boundaries left.
 *   - Span start inside deleted range     → clamp start to insertIndex.
 *   - Span end inside deleted range       → clamp end to insertIndex.
 *   - Resulting zero-length span          → discarded.
 */
fun List<TextSpan>.shiftOffsets(insertIndex: Int, delta: Int): List<TextSpan> {
    if (delta == 0) return this

    return if (delta > 0) {
        map { span ->
            when {
                insertIndex <= span.startOffset ->
                    span.copy(
                        startOffset = span.startOffset + delta,
                        endOffset   = span.endOffset + delta,
                    )
                insertIndex < span.endOffset ->
                    span.copy(endOffset = span.endOffset + delta)
                else -> span
            }
        }
    } else {
        val deletionEnd = insertIndex - delta  // insertIndex + abs(delta)
        mapNotNull { span ->
            val newStart = when {
                span.startOffset <= insertIndex -> span.startOffset
                span.startOffset >= deletionEnd -> span.startOffset + delta
                else                            -> insertIndex
            }
            val newEnd = when {
                span.endOffset <= insertIndex -> span.endOffset
                span.endOffset >= deletionEnd -> span.endOffset + delta
                else                          -> insertIndex
            }
            if (newEnd <= newStart) null
            else span.copy(startOffset = newStart, endOffset = newEnd)
        }
    }
}
