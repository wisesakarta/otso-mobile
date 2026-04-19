package com.otso.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otso.app.ui.theme.OtsoSpacing
import com.otso.app.ui.theme.OtsoTypography
import com.otso.app.ui.theme.otsoClickable
import com.otso.app.ui.theme.otsoColors
import com.otso.app.ui.theme.StaggeredItem
import com.otso.app.viewmodel.EditorUiState

@Composable
fun OtsoTabSwitcherSheet(
    uiState: EditorUiState,
    onTabSwitch: (Int) -> Unit,
    onNewTab: () -> Unit,
    onCloseTab: (Int) -> Unit,
) {
    val otsoColors = MaterialTheme.colorScheme.otsoColors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(otsoColors.background),
    ) {
        // Simplified header
        StaggeredItem(index = 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${uiState.tabs.size}",
                    style = OtsoTypography.uiTechnical,
                    color = otsoColors.muted.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f),
                )
                // Tombol new tab tetap ada
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .otsoClickable(onClick = onNewTab),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Icon(
                        imageVector = OtsoIcons.Plus,
                        contentDescription = "New Tab",
                        modifier = Modifier.size(18.dp),
                        tint = otsoColors.accent,
                    )
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            itemsIndexed(uiState.tabs, key = { _, tab -> tab.id }) { index, tab ->
                val isActive = index == uiState.activeIndex

                StaggeredItem(index = index + 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .otsoClickable(onClick = { onTabSwitch(index) })
                            .background(if (isActive) otsoColors.accent.copy(alpha = 0.08f) else Color.Transparent)
                            .drawBehind {
                                drawLine(
                                    color = otsoColors.edge.copy(alpha = 0.1f),
                                    start = Offset(0f, size.height),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = 1.dp.toPx(),
                                )
                                if (isActive) {
                                    drawLine(
                                        color = otsoColors.accent,
                                        start = Offset(0f, 0f),
                                        end = Offset(0f, size.height),
                                        strokeWidth = 2.dp.toPx(),
                                    )
                                }
                            }
                            .padding(horizontal = OtsoSpacing.globalMargin),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tab.title,
                                style = if (isActive) OtsoTypography.uiLabelMedium else OtsoTypography.uiLabel,
                                color = if (isActive) {
                                    otsoColors.accent
                                } else {
                                    if (tab.isModified) otsoColors.ink else otsoColors.ink.copy(alpha = 0.65f)
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )

                            Text(
                                text = tab.content.take(80).replace("\n", " "),
                                style = OtsoTypography.uiCaption.copy(fontSize = 11.sp),
                                color = otsoColors.ink.copy(alpha = 0.45f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .otsoClickable { onCloseTab(index) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = OtsoIcons.X,
                                    contentDescription = "Close Tab",
                                    modifier = Modifier.size(16.dp),
                                    tint = otsoColors.accent
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
