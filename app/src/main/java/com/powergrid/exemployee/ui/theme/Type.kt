package com.powergrid.exemployee.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Builds a [Typography] with larger-than-default sizes for readability.
 * Android's system font-scale is applied on top automatically.
 */
fun appTypography(isBold: Boolean = false): Typography {
    val bodyWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
    val titleWeight = if (isBold) FontWeight.Bold else FontWeight.Medium
    val labelWeight = if (isBold) FontWeight.Bold else FontWeight.Medium

    return Typography(
        displayLarge = TextStyle(fontSize = 57.sp, fontWeight = FontWeight.Normal),
        displayMedium = TextStyle(fontSize = 45.sp, fontWeight = FontWeight.Normal),
        displaySmall = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Normal),

        headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = titleWeight),
        headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = titleWeight),
        headlineSmall = TextStyle(fontSize = 24.sp, fontWeight = titleWeight),

        titleLarge = TextStyle(fontSize = 22.sp, fontWeight = titleWeight),
        titleMedium = TextStyle(fontSize = 18.sp, fontWeight = titleWeight),
        titleSmall = TextStyle(fontSize = 15.sp, fontWeight = labelWeight),

        bodyLarge = TextStyle(fontSize = 17.sp, fontWeight = bodyWeight, lineHeight = 24.sp),
        bodyMedium = TextStyle(fontSize = 15.sp, fontWeight = bodyWeight, lineHeight = 22.sp),
        bodySmall = TextStyle(fontSize = 13.sp, fontWeight = bodyWeight, lineHeight = 18.sp),

        labelLarge = TextStyle(fontSize = 15.sp, fontWeight = labelWeight),
        labelMedium = TextStyle(fontSize = 13.sp, fontWeight = labelWeight),
        labelSmall = TextStyle(fontSize = 11.sp, fontWeight = labelWeight),
    )
}
