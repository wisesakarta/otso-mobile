package com.otso.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            .background(otsoColors.background)
            .padding(bottom = 32.dp),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Sessions",
                style = OtsoTypography.uiTitle,
                color = otsoColors.ink,
            )
            
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .otsoClickable(onClick = onNewTab),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = OtsoIcons.Plus,
                    contentDescription = "New",
                    modifier = Modifier.size(20.dp),
                    tint = otsoColors.accent
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
        ) {
            itemsIndexed(uiState.tabs, key = { _, tab -> tab.id }) { index, tab ->
                val isActive = index == uiState.activeIndex
                
                StaggeredItem(index = index) {
                    TabItem(
                        title = tab.title,
                        isActive = isActive,
                        isModified = tab.isModified,
                        onClick = { onTabSwitch(index) },
                        onClose = { onCloseTab(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TabItem(
    title: String,
    isActive: Boolean,
    isModified: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .otsoClickable(onClick = onClick)
            .background(if (isActive) colors.accent.copy(alpha = 0.04f) else Color.Transparent)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isModified) {
                Box(
                    modifier = Modifier.size(6.dp).background(colors.accent, androidx.compose.foundation.shape.CircleShape)
                )
            }

            Text(
                text = title,
                style = OtsoTypography.uiLabel,
                color = if (isActive) colors.accent else colors.ink,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedContent(
                    targetState = isActive,
                    transitionSpec = {
                        (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                            scaleIn(initialScale = 0.95f, animationSpec = spring(stiffness = Spring.StiffnessMedium)))
                            .togetherWith(
                                fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                                    scaleOut(targetScale = 0.95f, animationSpec = spring(stiffness = Spring.StiffnessMedium))
                            )
                    },
                    label = "tab_close_visibility",
                ) { visible ->
                    if (visible) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .otsoClickable(onClick = onClose),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = OtsoIcons.X,
                                contentDescription = "Close",
                                modifier = Modifier.size(16.dp),
                                tint = colors.muted.copy(alpha = 0.4f)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}
