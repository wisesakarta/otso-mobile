package com.otso.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.otso.app.model.BlockType
import com.otso.app.model.ContentBlock
import com.otso.app.model.SpanStyleType
import com.otso.app.model.TextSpan
import com.otso.app.ui.components.OtsoEditor
import com.otso.app.ui.theme.OtsoSpacing
import com.otso.app.ui.theme.OtsoTypography
import com.otso.app.ui.theme.otsoColors
import com.otso.app.viewmodel.RichTextState

// ─────────────────────────────────────────────────────────────────────────────
// Validation fixture
//
// "OtsoMobile AST Engine is Live"
//  0         1         2
//  0123456789012345678901234567890
//
// "OtsoMobile" → [0, 10)  Bold
// "AST Engine" → [11, 21) Highlight #F9EB73 (yellow)
//
// Typing BEFORE "OtsoMobile" must shift both spans right.
// Typing INSIDE  "OtsoMobile" must extend bold's end only.
// Deleting across a span boundary must shrink or discard the span.
// ─────────────────────────────────────────────────────────────────────────────
private val VALIDATION_BLOCK = ContentBlock(
    type = BlockType.Paragraph,
    rawText = "OtsoMobile AST Engine is Live",
    spans = listOf(
        TextSpan(startOffset = 0,  endOffset = 10, style = SpanStyleType.Bold),
        TextSpan(startOffset = 11, endOffset = 21, style = SpanStyleType.Highlight, colorHex = "#F9EB73"),
    ),
)

/**
 * Phase 3 — Interactive AST sandbox.
 *
 * Renders [VALIDATION_BLOCK] through [OtsoEditor]'s RichTextState overload.
 * Every keystroke exercises [RichTextState.onTextChange] → [shiftOffsets].
 * The live debug header shows the current span count so the user can verify
 * that spans shrink and disappear correctly as text is deleted.
 */
@Composable
fun AstPreviewScreen() {
    val colors = MaterialTheme.colorScheme.otsoColors
    val richTextState = remember { RichTextState(VALIDATION_BLOCK) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // ── Debug Header ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier.padding(
                horizontal = OtsoSpacing.globalMargin,
                vertical = 20.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Phase 3 — AST Interactive Validation",
                style = OtsoTypography.uiLabel.copy(color = colors.muted),
            )
            val block = richTextState.block
            Text(
                text = "spans: ${block.spans.size}  |  rawText.length: ${block.rawText.length}",
                style = OtsoTypography.uiCaption.copy(color = colors.muted),
            )
            block.spans.forEachIndexed { i, span ->
                Text(
                    text = "[$i] ${span.style.name}  [${span.startOffset}, ${span.endOffset})  " +
                            "\"${block.rawText.substring(span.startOffset.coerceIn(0, block.rawText.length), span.endOffset.coerceIn(0, block.rawText.length))}\"",
                    style = OtsoTypography.uiCaption.copy(color = colors.muted),
                )
            }
        }

        // ── Live Editor ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            OtsoEditor(
                richTextState = richTextState,
                scrollState = scrollState,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
