package com.otso.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.otso.app.model.LineEnding
import com.otso.app.model.TextEncoding
import com.otso.app.ui.theme.OtsoSpacing
import com.otso.app.ui.theme.OtsoTypography
import com.otso.app.ui.theme.otsoColors

@Composable
fun OtsoStatusBar(
    encoding: TextEncoding,
    lineEnding: LineEnding,
    cursorLine: Int,
    cursorCol: Int,
    fontName: String,
    fontSizeSp: Int,
    modifier: Modifier = Modifier,
) {
    val otsoColors = MaterialTheme.colorScheme.otsoColors

    val encodingLabel = encoding.name.replace("_", "-")
    val lineEndingLabel = lineEnding.name

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp) // DNA Restoration: 24dp (Metric Strip)
            .background(otsoColors.surface)
            .drawBehind {
                // DNA Revision: Clinical 0.5dp framing border
                drawLine(
                    color = otsoColors.edge.copy(alpha = 0.1f), 
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 0.5.dp.toPx(),
                )
            }
            .padding(horizontal = OtsoSpacing.globalMargin),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // DNA Restoration: Encoding + Line Ending (Left)
        Text(
            text = "$encodingLabel · $lineEndingLabel",
            style = OtsoTypography.uiTechnical, // Standardized Mono Telemetry
            color = otsoColors.ink.copy(alpha = 0.5f),
            modifier = Modifier.weight(1f),
        )

        // DNA Restoration: Cursor Pos (Right)
        Text(
            text = "Ln $cursorLine, Col $cursorCol",
            style = OtsoTypography.uiTechnical, // Standardized Mono Telemetry
            color = otsoColors.ink.copy(alpha = 0.5f),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
        )
        
        // Font name and size removed from status bar per DNA brief (moved to Menu)
    }
}