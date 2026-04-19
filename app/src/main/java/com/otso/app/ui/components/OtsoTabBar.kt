package com.otso.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import com.otso.app.R
import com.otso.app.ui.theme.OtsoSpacing
import com.otso.app.ui.theme.OtsoTypography
import com.otso.app.ui.theme.otsoColors
import com.otso.app.viewmodel.EditorUiState

@Composable
fun OtsoTabBar(
    uiState: EditorUiState,
    menuProgress: Float, // DNA: Injected physical progress from sheet
    onMenuClick: () -> Unit,
    onSwipeDown: () -> Unit,
    onRenameStart: (Int) -> Unit,
    onRenameUpdate: (String) -> Unit,
    onRenameCancel: () -> Unit,
    onRenameFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    var isMenuPressed by remember { mutableStateOf(false) } 
    val otsoColors = MaterialTheme.colorScheme.otsoColors
    val focusRequester = remember { FocusRequester() }
    val activeTab = uiState.tabs.getOrNull(uiState.activeIndex)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(OtsoSpacing.chromeBandH)
            .background(otsoColors.background)
            .drawBehind {
                drawLine(
                    color = otsoColors.edge.copy(alpha = 0.5f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(horizontal = OtsoSpacing.globalMargin),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { dragAccumulator = 0f },
                    ) { _, dragAmount ->
                        dragAccumulator += dragAmount
                        if (dragAccumulator > 50f) {
                            onSwipeDown()
                            dragAccumulator = 0f
                        }
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // KIRI: In-app logo — identity anchor, NON-TAPPABLE
            val logoRes = if (otsoColors.isDarkMode) R.drawable.ic_otso_dark else R.drawable.ic_otso_light
            Image(
                painter = painterResource(id = logoRes),
                contentDescription = null, // dekoratif, bukan aksi
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit,
            )

            // TENGAH: Judul tab aktif + modified dot
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { 
                            if (uiState.editingTabIndex == null) {
                                onRenameStart(uiState.activeIndex)
                            }
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val isEditing = uiState.editingTabIndex == uiState.activeIndex

                // DNA: Blur-Shift Transition (Lucas Yule / Emil Engineering)
                val blurRadius by animateFloatAsState(
                    targetValue = if (isEditing) 0f else 4f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "rename_blur"
                )
                val renameSlide by animateDpAsState(
                    targetValue = if (isEditing) 0.dp else 4.dp,
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
                    label = "rename_slide"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            // Apply blur if supported (API 31+) or just alpha fallback
                            renderEffect = if (isEditing && blurRadius > 0.1f) {
                                android.graphics.RenderEffect.createBlurEffect(
                                    blurRadius, blurRadius, android.graphics.Shader.TileMode.CLAMP
                                ).asComposeRenderEffect()
                            } else null
                            translationY = if (isEditing) renameSlide.toPx() else 0f
                            alpha = 1f
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (isEditing) {
                        var textFieldValue by remember(uiState.editingTabIndex) {
                            mutableStateOf(
                                androidx.compose.ui.text.input.TextFieldValue(
                                    text = uiState.editingTabName,
                                    selection = androidx.compose.ui.text.TextRange(uiState.editingTabName.length)
                                )
                            )
                        }
                        var hasBeenFocused by remember(uiState.editingTabIndex) { mutableStateOf(false) }
                        
                        LaunchedEffect(Unit) { focusRequester.requestFocus() }
                        
                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = { 
                                textFieldValue = it
                                onRenameUpdate(it.text)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        hasBeenFocused = true
                                    } else if (hasBeenFocused) {
                                        onRenameFinish()
                                    }
                                },
                            textStyle = OtsoTypography.uiLabelMedium.copy(color = otsoColors.ink),
                            cursorBrush = SolidColor(Color(0XFF001AE2)), // Blueprint Blue
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = { onRenameFinish() }
                            )
                        )
                    } else {
                        Text(
                            text = activeTab?.title ?: "Untitled",
                            style = OtsoTypography.uiLabelMedium,
                            color = if (activeTab?.isModified == true)
                                otsoColors.ink  // full opacity = modified
                            else
                                otsoColors.ink.copy(alpha = 0.65f), // slightly muted = clean
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                        )
                    }
                }
            }

            // KANAN: Counter (Only visible if tabs > 1)
            if (uiState.tabs.size > 1) {
                Text(
                    text = "${uiState.activeIndex + 1}/${uiState.tabs.size}",
                    style = OtsoTypography.uiCaption, // 11sp
                    color = otsoColors.ink.copy(alpha = 0.35f),
                    modifier = Modifier.padding(end = 8.dp),
                )
            }

            // KANAN: Menu trigger — The Renaissance Morph (3-Line Animated Icon)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer {
                        val scale = if (isMenuPressed) 0.95f else 1f
                        scaleX = scale
                        scaleY = scale
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isMenuPressed = true
                                tryAwaitRelease()
                                isMenuPressed = false
                                onMenuClick()
                            }
                        )
                    },
                contentAlignment = Alignment.CenterEnd,
            ) {
                // DNA: Using menuProgress to drive icon morph (Physical Coupling)
                AnimatedMenuIcon(
                    progress = menuProgress,
                    color = otsoColors.ink.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun AnimatedMenuIcon(
    progress: Float, // 0f = Hamburger, 1f = X
    color: Color,
) {
    // DNA: Map linear progress to transformation values
    // Using simple mapping for perfect coupling.
    val rotationTop = progress * 45f
    val rotationBottom = progress * -45f
    val alphaMid = 1f - progress
    val scaleMid = 1f - progress

    Box(
        modifier = Modifier.size(18.dp, 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // TOP BAR
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.8.dp)
                .align(Alignment.TopCenter)
                .graphicsLayer {
                    translationY = progress * 5.dp.toPx()
                    rotationZ = rotationTop
                }
                .background(color)
        )
        // MID BAR
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.8.dp)
                .align(Alignment.Center)
                .graphicsLayer {
                    alpha = alphaMid
                    scaleX = scaleMid
                }
                .background(color)
        )
        // BOTTOM BAR
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.8.dp)
                .align(Alignment.BottomCenter)
                .graphicsLayer {
                    translationY = -progress * 5.dp.toPx()
                    rotationZ = rotationBottom
                }
                .background(color)
        )
    }
}
