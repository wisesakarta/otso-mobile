package com.otso.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import com.otso.app.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otso.app.ui.components.OtsoBackButton
import com.otso.app.ui.theme.otsoColors
import com.otso.app.ui.theme.otsoSpacing
import com.otso.app.ui.theme.otsoTypography
import com.otso.app.ui.theme.technicalGrain

/**
 * AboutScreen — The Product Manifesto.
 * Replicates desktop parity with Industrial Editorial standards.
 * Geometry: Absolute 0.dp (RectangleShape).
 * Typography: Left-aligned technical layout.
 */
@Composable
fun AboutScreen(
    onBackClick: () -> Unit
) {
    val otsoColors = MaterialTheme.colorScheme.otsoColors
    val otsoTypography = MaterialTheme.colorScheme.otsoTypography
    val otsoSpacing = MaterialTheme.colorScheme.otsoSpacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(otsoColors.background)
            .technicalGrain(alpha = 0.03f)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // 1. Navigation Header (Manual Pixel Precision)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            OtsoBackButton(
                onClick = onBackClick,
                color = otsoColors.ink
            )
        }

        // 2. Fragmented Content (Editorial Layout)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = otsoSpacing.editorialMargin)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Logo Branding — Adaptive & Centered
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                val logoRes = if (otsoColors.isDarkMode) R.drawable.ic_otso_dark else R.drawable.ic_otso_light
                Image(
                    painter = painterResource(id = logoRes),
                    contentDescription = "Otso Logo",
                    modifier = Modifier.height(120.dp) // 2.5x of 48dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Version Control (uiTechnical)
                Text(
                    text = "v1.0.0-rc.1",
                    style = otsoTypography.uiTechnical,
                    color = otsoColors.muted
                )
            }

            Spacer(modifier = Modifier.height(80.dp))

            // The Doctrine: "Clarity. Function. Detail."
            Text(
                text = "Clarity. Function. Detail.",
                style = otsoTypography.uiTitleLarge,
                color = otsoColors.ink,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // The Craftsman Credit
            Text(
                text = "Crafted with discipline by wisesakarta",
                style = otsoTypography.uiLabelMedium,
                color = otsoColors.muted,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))

            // 3. Developer Signature (Natural Integration)
            Text(
                text = "Technical Standard",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = otsoTypography.uiTechnical.copy(
                    fontSize = 12.sp
                ),
                color = otsoColors.ink.copy(alpha = 0.4f)
            )
        }

    }
}
