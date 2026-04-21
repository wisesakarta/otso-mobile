package com.otso.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otso.app.ui.theme.OtsoTypography
import com.otso.app.ui.theme.otsoColors
import kotlinx.coroutines.delay

/**
 * DNA Component: The Mechanical Morph (Design Engineering)
 * A high-fidelity ASCII loader that cycles through geometrical variants 
 * of the asterisk with a micro-blur crossfade for a "physical" feel.
 */
@Composable
fun OtsoAsteriskLoader(
    modifier: Modifier = Modifier
) {
    val frames = listOf("·", "+", "×", "*")
    var frameIndex by remember { mutableIntStateOf(0) }

    // Physical Coupling: High-RPM cycle with deliberate frame intervals
    LaunchedEffect(Unit) {
        while (true) {
            delay(120)
            frameIndex = (frameIndex + 1) % frames.size
        }
    }

    Box(
        modifier = modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = frames[frameIndex],
            transitionSpec = {
                // DNA: Blur-Crossfade Transition (Emil Engineering)
                // We use a custom slide + fade to simulate organic morphing
                (fadeIn(animationSpec = tween(120, easing = LinearEasing)) + 
                 scaleIn(initialScale = 0.8f, animationSpec = tween(120))) togetherWith
                (fadeOut(animationSpec = tween(120, easing = LinearEasing)) + 
                 scaleOut(targetScale = 1.2f, animationSpec = tween(120)))
            },
            label = "ascii_morph"
        ) { char ->
            Text(
                text = char,
                style = OtsoTypography.uiDisplayLarge.copy(
                    fontSize = 32.sp,
                    fontFamily = FontFamily.Monospace 
                ),
                color = androidx.compose.material3.MaterialTheme.colorScheme.otsoColors.accent,
                modifier = Modifier.graphicsLayer {
                    // APPLY BLUR EFFECT (API 31+) if supported
                    // This masks the imperfect character swap with a "soft" persistence of vision
                    renderEffect = if (android.os.Build.VERSION.SDK_INT >= 31) {
                         android.graphics.RenderEffect.createBlurEffect(
                            1.5f, 1.5f, android.graphics.Shader.TileMode.CLAMP
                        ).asComposeRenderEffect()
                    } else null
                }
            )
        }
    }
}
