package com.otso.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.otso.app.ui.theme.OtsoTypography
import com.otso.app.ui.theme.otsoClickable
import com.otso.app.ui.theme.otsoColors

@Composable
fun OtsoUnsavedDialog(
    fileName: String,
    onCancel: () -> Unit,
    onDiscard: () -> Unit,
    onSave: () -> Unit,
) {
    val otsoColors = MaterialTheme.colorScheme.otsoColors

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            usePlatformDefaultWidth = false, // Allow full-screen for our custom scrim
            decorFitsSystemWindows = true
        )
    ) {
        // FULL SCREEN OVERLAY (SCRIM)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f)) // Block interaction with 45% black scrim
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onCancel // Clicking outside cancels
                ),
            contentAlignment = Alignment.Center
        ) {
            // DIALOG BOX (0dp Corner Radius)
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .background(otsoColors.surface, RectangleShape)
                    .border(1.dp, otsoColors.edge, RectangleShape)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {} // Consume clicks inside
                    )
            ) {
                // Content Padding
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Unsaved modifications",
                        style = OtsoTypography.uiTitle,
                        color = otsoColors.ink
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "This file has unsaved changes. All data will be lost if you discard it.",
                        style = OtsoTypography.uiLabel,
                        color = otsoColors.muted,
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = fileName,
                            style = OtsoTypography.uiTechnical,
                            color = otsoColors.muted.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OtsoModifiedDot()
                    }
                }

                // ACTION BUTTONS ROW (Mathematical Border Locking)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .drawBehind {
                            // Top horizontal divider
                            drawLine(
                                color = otsoColors.edge,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx()
                            )
                            // Vertical divider 1 (between Cancel and Discard)
                            drawLine(
                                color = otsoColors.edge,
                                start = Offset(size.width / 3f, 0f),
                                end = Offset(size.width / 3f, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                            // Vertical divider 2 (between Discard and Save)
                            drawLine(
                                color = otsoColors.edge,
                                start = Offset(2 * size.width / 3f, 0f),
                                end = Offset(2 * size.width / 3f, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                ) {
                    // CANCEL
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .otsoClickable { onCancel() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            style = OtsoTypography.uiLabelMedium,
                            color = otsoColors.muted
                        )
                    }

                    // DISCARD
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .otsoClickable { onDiscard() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Discard",
                            style = OtsoTypography.uiLabelMedium,
                            color = otsoColors.ink
                        )
                    }

                    // SAVE (Blueprint Blue Solid)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(otsoColors.accent)
                            .otsoClickable { onSave() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Save",
                            style = OtsoTypography.uiLabelMedium,
                            color = otsoColors.background
                        )
                    }
                }
            }
        }
    }
}
