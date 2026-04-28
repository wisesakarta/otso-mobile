package com.otso.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.otso.app.ui.theme.SquircleShape
import com.otso.app.ui.theme.OtsoTypography
import com.otso.app.ui.theme.otsoClickable
import com.otso.app.ui.theme.otsoColors

data class TranslationLanguage(
    val tag: String,
    val label: String,
)

private val supportedLanguages = listOf(
    TranslationLanguage("auto", "Auto-detect"),
    TranslationLanguage("en", "English"),
    TranslationLanguage("id", "Indonesian"),
    TranslationLanguage("ja", "Japanese"),
    TranslationLanguage("ko", "Korean"),
    TranslationLanguage("zh", "Chinese"),
    TranslationLanguage("es", "Spanish"),
    TranslationLanguage("fr", "French"),
    TranslationLanguage("de", "German"),
    TranslationLanguage("ar", "Arabic"),
    TranslationLanguage("pt", "Portuguese"),
    TranslationLanguage("ru", "Russian"),
)

private val sourceLanguages = supportedLanguages
private val targetLanguages = supportedLanguages.filterNot { it.tag == "auto" }

@Composable
fun OtsoTranslateDialog(
    sourceTag: String,
    targetTag: String,
    hasSelection: Boolean,
    onSourceChange: (String) -> Unit,
    onTargetChange: (String) -> Unit,
    onCancel: () -> Unit,
    onTranslate: (Boolean) -> Unit,
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    val dialogShape = SquircleShape(20.dp)
    var selectionOnly by remember(hasSelection) { mutableStateOf(hasSelection) }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCancel,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .width(340.dp)
                    .background(colors.surface, dialogShape)
                    .border(1.dp, colors.edge, dialogShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    )
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Translate",
                    style = OtsoTypography.uiTitle,
                    color = colors.ink,
                )

                LanguageSelector(
                    label = "From",
                    selectedTag = sourceTag,
                    options = sourceLanguages,
                    onSelect = onSourceChange,
                )
                LanguageSelector(
                    label = "To",
                    selectedTag = targetTag,
                    options = targetLanguages,
                    onSelect = onTargetChange,
                )

                SelectionOption(
                    label = "Selected text only",
                    selected = selectionOnly && hasSelection,
                    enabled = hasSelection,
                    onSelect = { selectionOnly = true },
                )
                SelectionOption(
                    label = "Entire document",
                    selected = !selectionOnly || !hasSelection,
                    enabled = true,
                    onSelect = { selectionOnly = false },
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .background(colors.background, SquircleShape(10.dp))
                            .border(1.dp, colors.edge, SquircleShape(10.dp))
                            .otsoClickable(onClick = onCancel),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Cancel",
                            style = OtsoTypography.uiLabelMedium,
                            color = colors.muted,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .background(colors.accent, SquircleShape(10.dp))
                            .otsoClickable(onClick = { onTranslate(selectionOnly && hasSelection) }),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Translate",
                            style = OtsoTypography.uiLabelMedium,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionOption(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSelect,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = if (enabled) onSelect else null,
        )
        Text(
            text = label,
            style = OtsoTypography.uiLabelMedium,
            color = if (enabled) colors.ink else colors.muted.copy(alpha = 0.5f),
        )
    }
}

@Composable
private fun LanguageSelector(
    label: String,
    selectedTag: String,
    options: List<TranslationLanguage>,
    onSelect: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    val selectorShape = SquircleShape(10.dp)
    val selected = options.firstOrNull { it.tag == selectedTag } ?: options.first()
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = OtsoTypography.uiCaption,
            color = colors.muted,
        )

        Box {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(colors.background, selectorShape)
                    .border(1.dp, colors.edge, selectorShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { expanded = true },
                    )
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = selected.label,
                    style = OtsoTypography.uiLabelMedium,
                    color = colors.ink,
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(colors.surface, selectorShape)
                    .border(1.dp, colors.edge, selectorShape),
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.label,
                                style = OtsoTypography.uiLabel,
                                color = colors.ink,
                            )
                        },
                        onClick = {
                            expanded = false
                            onSelect(option.tag)
                        },
                    )
                }
            }
        }
    }
}
