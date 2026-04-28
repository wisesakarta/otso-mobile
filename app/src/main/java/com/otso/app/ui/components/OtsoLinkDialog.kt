package com.otso.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.otso.app.ui.theme.SquircleShape
import com.otso.app.ui.theme.OtsoTypography
import com.otso.app.ui.theme.otsoClickable
import com.otso.app.ui.theme.otsoColors

@Composable
fun OtsoLinkDialog(
    url: String,
    onUrlChange: (String) -> Unit,
    onCancel: () -> Unit,
    onApply: () -> Unit,
) {
    val otsoColors = MaterialTheme.colorScheme.otsoColors
    val focusRequester = remember { FocusRequester() }
    val dialogShape = SquircleShape(20.dp)
    val inputShape = SquircleShape(10.dp)

    // Auto-focus when opened
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onCancel
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(320.dp)
                    .clip(dialogShape)
                    .background(otsoColors.surface, dialogShape)
                    .border(1.dp, otsoColors.edge, dialogShape)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {}
                    )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Insert Link",
                        style = OtsoTypography.uiTitle,
                        color = otsoColors.ink
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Enter the destination URL for the current selection.",
                        style = OtsoTypography.uiLabel,
                        color = otsoColors.muted
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // URL Input Field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(otsoColors.background, inputShape)
                            .border(1.dp, otsoColors.edge, inputShape)
                            .padding(12.dp)
                    ) {
                        BasicTextField(
                            value = url,
                            onValueChange = onUrlChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            textStyle = OtsoTypography.uiLabelMedium.copy(color = otsoColors.ink),
                            cursorBrush = SolidColor(otsoColors.accent),
                            singleLine = true,
                        )
                        if (url.isEmpty()) {
                            Text(
                                text = "https://example.com",
                                style = OtsoTypography.uiLabel,
                                color = otsoColors.muted.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .drawBehind {
                            drawLine(
                                color = otsoColors.edge,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawLine(
                                color = otsoColors.edge,
                                start = Offset(size.width / 2f, 0f),
                                end = Offset(size.width / 2f, size.height),
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

                    // APPLY
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(otsoColors.accent)
                            .otsoClickable { onApply() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Apply",
                            style = OtsoTypography.uiLabelMedium,
                            color = otsoColors.background
                        )
                    }
                }
            }
        }
    }
}
