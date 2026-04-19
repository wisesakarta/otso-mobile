package com.otso.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.otso.app.ui.theme.otsoClickable

/**
 * OtsoBackButton — Specialized Industrial Navigation Trigger.
 * Constraints: Zero-Rounding, No Ripple, Phosphor Iconography.
 */
@Composable
fun OtsoBackButton(
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .otsoClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = OtsoIcons.ArrowLeft,
            contentDescription = "Back",
            modifier = Modifier.size(22.dp),
            tint = color
        )
    }
}
