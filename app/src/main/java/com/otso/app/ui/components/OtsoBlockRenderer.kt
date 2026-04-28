package com.otso.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.sp
import com.otso.app.model.BlockType
import com.otso.app.model.ContentBlock
import com.otso.app.ui.mapper.toAnnotatedString
import com.otso.app.ui.theme.GeneralSans
import com.otso.app.ui.theme.JetBrainsMono
import com.otso.app.ui.theme.OtsoTypography
import com.otso.app.ui.theme.otsoColors

/**
 * Renders a single [ContentBlock] as a styled [Text] composable.
 *
 * Typography is driven by [block.type]; inline decoration is driven by
 * [block.spans] via [toAnnotatedString]. The component is fully stateless —
 * it owns no state and produces no side effects.
 */
@Composable
fun OtsoBlockRenderer(block: ContentBlock) {
    val colors = MaterialTheme.colorScheme.otsoColors
    val annotated = block.toAnnotatedString(colors)

    val textStyle: TextStyle = when (block.type) {
        BlockType.Heading1 -> OtsoTypography.editorBody.copy(
            fontFamily = GeneralSans,
            fontSize = 28.sp,
            lineHeight = 36.sp,
        )
        BlockType.Heading2 -> OtsoTypography.editorBody.copy(
            fontFamily = GeneralSans,
            fontSize = 22.sp,
            lineHeight = 30.sp,
        )
        BlockType.Paragraph -> OtsoTypography.editorBody
        BlockType.BulletList -> OtsoTypography.editorBody
        BlockType.CodeBlock -> OtsoTypography.editorBody.copy(
            fontFamily = JetBrainsMono,
        )
    }

    val displayText = if (block.type == BlockType.BulletList) {
        buildAnnotatedString {
            append("• ")
            append(annotated)
        }
    } else {
        annotated
    }

    Text(
        text = displayText,
        style = textStyle.copy(color = colors.ink),
    )
}
