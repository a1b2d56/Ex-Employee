package com.powergrid.exemployee.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.powergrid.exemployee.R

@Composable
fun AppLogoBadge(
    modifier: Modifier = Modifier,
    fraction: Float = 0.5f
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_app_logo),
            contentDescription = "Power Grid Portal Logo",
            modifier = Modifier.fillMaxWidth(fraction),
            contentScale = ContentScale.FillWidth
        )
    }
}
