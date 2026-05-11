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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otso.app.ui.components.OtsoBackButton
import com.otso.app.ui.theme.otsoColors
import com.otso.app.ui.theme.otsoSpacing
import com.otso.app.ui.theme.otsoTypography
import com.otso.app.ui.theme.technicalGrain
import com.otso.app.ui.theme.StaggeredItem
import com.otso.app.ui.theme.otsoClickable
import com.otso.app.ui.theme.SquircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.em
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight

/**
 * AboutScreen — The Product Manifesto.
 * Replicates desktop parity with Industrial Editorial standards.
 * Geometry: Squircle-driven surfaces, editorial alignment.
 * Typography: Left-aligned technical layout.
 */
@Composable
fun AboutScreen(
    onBackClick: () -> Unit
) {
    val otsoColors = MaterialTheme.colorScheme.otsoColors
    val otsoTypography = MaterialTheme.colorScheme.otsoTypography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(otsoColors.background)
            .technicalGrain(alpha = 0.02f) // Subtler grain for Muji feel
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // 1. Subtle Navigation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            OtsoBackButton(
                onClick = onBackClick,
                color = otsoColors.ink.copy(alpha = 0.4f) // Modest/Low-ego back button
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.15f))

            // 2. Hero (Sharp Squircle - The Identity)
            StaggeredItem(index = 0) {
                val logoRes = if (otsoColors.isDarkMode) com.otso.app.BrandAssets.logoDark else com.otso.app.BrandAssets.logoLight
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(com.otso.app.ui.theme.SquircleShape(12.dp))
                        .background(if (otsoColors.isDarkMode) otsoColors.surface else otsoColors.edge.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = logoRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            // 3. Manifesto (Plain & Honest)
            StaggeredItem(index = 1) {
                Text(
                    text = androidx.compose.ui.res.stringResource(id = com.otso.app.R.string.about_manifesto),
                    style = otsoTypography.uiLabel.copy(
                        lineHeight = 24.sp,
                        letterSpacing = 0.01.em,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ),
                    color = otsoColors.ink.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 4. Functional Footer (Muji Minimalist)
            Text(
                text = "Technical Standard v2.2.0",
                style = otsoTypography.uiTechnical.copy(fontSize = 11.sp, letterSpacing = 0.02.em),
                color = otsoColors.muted.copy(alpha = 0.3f),
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}

@Composable
private fun UtilityChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    val typography = MaterialTheme.colorScheme.otsoTypography
    Row(
        modifier = Modifier
            .wrapContentWidth()
            .height(40.dp)
            .background(colors.surface, SquircleShape(12.dp))
            .border(1.dp, colors.ink.copy(alpha = 0.06f), SquircleShape(12.dp))
            .otsoClickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = colors.ink
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = typography.uiLabel.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                fontSize = 13.sp
            ),
            color = colors.ink
        )
    }
}
