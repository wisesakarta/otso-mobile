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
    val otsoSpacing = MaterialTheme.colorScheme.otsoSpacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(otsoColors.background)
            .technicalGrain(alpha = 0.03f)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // 1. Navigation Header
        StaggeredItem(index = 0) {
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
        }

        // 2. Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = otsoSpacing.editorialMargin)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Logo & Version
            StaggeredItem(index = 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    val logoRes = if (otsoColors.isDarkMode) R.drawable.ic_otso_dark else R.drawable.ic_otso_light
                    Image(
                        painter = painterResource(id = logoRes),
                        contentDescription = "Otso Logo",
                        modifier = Modifier.height(48.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val versionName = try {
                        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "Unknown"
                    } catch (e: Exception) {
                        "Unknown"
                    }
                    
                    Text(
                        text = "v$versionName",
                        style = otsoTypography.uiTechnical.copy(letterSpacing = 0.3.sp),
                        color = otsoColors.muted,
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            StaggeredItem(index = 2) {
                Text(
                    text = "Designed to fade away, so your words can stand out.",
                    style = otsoTypography.uiTitleLarge.copy(
                        lineHeight = 34.sp,
                        letterSpacing = (-0.25).sp,
                    ),
                    color = otsoColors.ink,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            StaggeredItem(index = 3) {
                Text(
                    text = "Built with care for those who value focus.",
                    style = otsoTypography.uiLabel.copy(
                        lineHeight = 21.sp,
                        letterSpacing = 0.1.sp,
                    ),
                    color = otsoColors.muted,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Utility Links
            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
            StaggeredItem(index = 4) {
                @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    UtilityChip(
                        icon = com.otso.app.ui.components.OtsoIcons.PaperPlaneTilt,
                        label = "Send Feedback",
                        onClick = { uriHandler.openUri("mailto:feedback@wisesakarta.com") }
                    )
                    UtilityChip(
                        icon = com.otso.app.ui.components.OtsoIcons.Code,
                        label = "Source Code & Roadmap",
                        onClick = { uriHandler.openUri("https://github.com/wisesakarta") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            StaggeredItem(index = 5) {
                Text(
                    text = "Technical Standard • wisesakarta",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = otsoTypography.uiTechnical.copy(fontSize = 12.sp),
                    color = otsoColors.ink.copy(alpha = 0.4f)
                )
            }
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
