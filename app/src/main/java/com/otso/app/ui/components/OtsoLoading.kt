package com.otso.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.otso.app.ui.theme.otsoColors

/**
 * DNA Component: The Mechanical Morph (Design Engineering)
 * A high-fidelity animated loader using the standardized Asterisk icon.
 * Features a smooth infinite rotation with a subtle PoV blur effect.
 */
@Composable
fun OtsoAsteriskLoader(
    modifier: Modifier = Modifier
) {
    val otsoColors = MaterialTheme.colorScheme.otsoColors
    val infiniteTransition = rememberInfiniteTransition(label = "loading_asterisk")
    
    // Physical RPM: 1.5 seconds for a full technical rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "asterisk_rotation"
    )

    Box(
        modifier = modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = OtsoIcons.Asterisk,
            contentDescription = "Loading...",
            modifier = Modifier
                .size(32.dp)
                .graphicsLayer { 
                    rotationZ = rotation
                    // DNA: Subtle Persistence of Vision (PoV) Blur on API 31+
                    if (android.os.Build.VERSION.SDK_INT >= 31) {
                        renderEffect = android.graphics.RenderEffect.createBlurEffect(
                            1f, 1f, android.graphics.Shader.TileMode.CLAMP
                        ).asComposeRenderEffect()
                    }
                },
            tint = otsoColors.accent
        )
    }
}
