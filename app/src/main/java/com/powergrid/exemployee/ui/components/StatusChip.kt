package com.powergrid.exemployee.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight

@Composable fun StatusChip(
    label: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    contentColor: Color? = null,
) {
    val isDark = !MaterialTheme.colorScheme.surface.luminance().let { it > 0.5f }
    
    val resolvedBg: Color
    val resolvedText: Color
    
    if (contentColor != null) {
        resolvedBg = backgroundColor
        resolvedText = contentColor
    } else {
        when (backgroundColor) {
            Color(0xFFC8F5D1) -> { // Active / Verified
                if (isDark) {
                    resolvedBg = Color(0xFF1B5E20).copy(alpha = 0.25f)
                    resolvedText = Color(0xFF81C784)
                } else {
                    resolvedBg = Color(0xFFE8F5E9)
                    resolvedText = Color(0xFF2E7D32)
                }
            }
            Color(0xFFFFE0E0) -> { // Inactive / Rejected
                if (isDark) {
                    resolvedBg = Color(0xFFB71C1C).copy(alpha = 0.25f)
                    resolvedText = Color(0xFFE57373)
                } else {
                    resolvedBg = Color(0xFFFFEBEE)
                    resolvedText = Color(0xFFC62828)
                }
            }
            Color(0xFFFFF3CC) -> { // Pending
                if (isDark) {
                    resolvedBg = Color(0xFFF57F17).copy(alpha = 0.20f)
                    resolvedText = Color(0xFFFFD54F)
                } else {
                    resolvedBg = Color(0xFFFFFDE7)
                    resolvedText = Color(0xFFF57F17)
                }
            }
            else -> {
                resolvedBg = backgroundColor
                val lum = backgroundColor.luminance()
                resolvedText = if (lum > 0.5f) {
                    Color(
                        red = backgroundColor.red * 0.3f,
                        green = backgroundColor.green * 0.3f,
                        blue = backgroundColor.blue * 0.3f,
                        alpha = 1.0f
                    )
                } else {
                    Color(
                        red = (backgroundColor.red * 0.3f) + 0.7f,
                        green = (backgroundColor.green * 0.3f) + 0.7f,
                        blue = (backgroundColor.blue * 0.3f) + 0.7f,
                        alpha = 1.0f
                    )
                }
            }
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = resolvedBg,
        contentColor = resolvedText,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}
