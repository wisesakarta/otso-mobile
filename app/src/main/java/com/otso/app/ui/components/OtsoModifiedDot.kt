package com.otso.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.otso.app.ui.theme.otsoColors

/**
 * OtsoModifiedDot - A premium "Glow Dot" indicator for unsaved changes.
 * Consists of a solid core and a soft radial glow to match the Renaissance aesthetic.
 */
@Composable
fun OtsoModifiedDot(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.otsoColors.accent
) {
    Box(
        modifier = modifier
            .size(7.dp) // Compact footprint for the sharp asterisk
            .drawBehind {
                val center = center
                val radius = 2.5.dp.toPx()
                
                // THE RIGID ASTERISK (3 lines crossing at 60 degrees)
                val strokeWidth = 1.2.dp.toPx()
                
                // Line 1: Vertical
                drawLine(
                    color = color,
                    start = Offset(center.x, center.y - radius),
                    end = Offset(center.x, center.y + radius),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Butt
                )
                
                // Line 2: 60 degrees
                val dx60 = radius * Math.cos(Math.toRadians(30.0)).toFloat()
                val dy60 = radius * Math.sin(Math.toRadians(30.0)).toFloat()
                drawLine(
                    color = color,
                    start = Offset(center.x - dx60, center.y - dy60),
                    end = Offset(center.x + dx60, center.y + dy60),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Butt
                )

                // Line 3: 120 degrees
                drawLine(
                    color = color,
                    start = Offset(center.x - dx60, center.y + dy60),
                    end = Offset(center.x + dx60, center.y - dy60),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Butt
                )
            }
    )
}
