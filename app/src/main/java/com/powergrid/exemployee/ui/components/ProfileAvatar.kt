package com.powergrid.exemployee.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.powergrid.exemployee.R

@Composable
fun ProfileAvatar(
    photoUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
    borderWidth: Dp = 2.dp,
    shadowElevation: Dp = 6.dp,
    borderColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
    placeholderTint: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = if (MaterialTheme.colorScheme.surfaceContainerLow.alpha < 1f) 0.dp else shadowElevation,
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(borderWidth, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUrl.isNullOrEmpty()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Compute a proportional placeholder icon size (approx. 60% of avatar size)
            val iconSize = size * 0.6f
            Icon(
                painter = painterResource(id = R.drawable.ic_person_placeholder),
                contentDescription = "Profile Placeholder",
                modifier = Modifier.size(iconSize),
                tint = placeholderTint
            )
        }
    }
}
