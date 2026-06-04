package com.powergrid.exemployee.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.powergrid.exemployee.common.UiState
import com.powergrid.exemployee.domain.model.Employee
import com.powergrid.exemployee.ui.components.ErrorMessage
import com.powergrid.exemployee.ui.components.LoadingIndicator
import com.powergrid.exemployee.ui.components.ProfileAvatar

import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.res.painterResource
import com.powergrid.exemployee.R

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.powergrid.exemployee.ui.theme.dynamic

@Composable fun HomeScreen(
    authToken: String,
    supportsGlass: Boolean = false,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.employee.collectAsStateWithLifecycle()

    LaunchedEffect(authToken) { viewModel.loadEmployee(authToken) }

    when (val s = state) {
        is UiState.Idle, is UiState.Loading -> LoadingIndicator()
        is UiState.Error -> ErrorMessage(s.message)
        is UiState.Success -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
            ) {
                EmployeeCard(s.data, supportsGlass)
            }
        }
    }
}

@Composable private fun EmployeeCard(employee: Employee, supportsGlass: Boolean) {
    val rotationY = remember(employee.employeeId) { Animatable(-180f) }
    val elevation = remember(employee.employeeId) { Animatable(10f) }
    val cardShape = RoundedCornerShape(18.dp)
    val goldBrush = Brush.linearGradient(listOf(Color(0xFFE6C176), Color(0xFFC7983C)))
    val glassGoldBrush = Brush.linearGradient(
        listOf(
            Color(0xFFF7DEA2).copy(alpha = 0.84f),
            Color(0xFFE6C176).copy(alpha = 0.72f),
            Color(0xFFC7983C).copy(alpha = 0.80f)
        )
    )

    LaunchedEffect(employee.employeeId) {
        rotationY.snapTo(-180f)
        elevation.snapTo(10f)
        rotationY.animateTo(0f, tween(durationMillis = 900, easing = FastOutSlowInEasing))
        elevation.animateTo(2f, tween(durationMillis = 120, easing = FastOutSlowInEasing))
        elevation.animateTo(5f, tween(durationMillis = 90, easing = FastOutSlowInEasing))
        elevation.animateTo(2f, tween(durationMillis = 140, easing = FastOutSlowInEasing))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .graphicsLayer {
                this.rotationY = rotationY.value
                cameraDistance = 12f * density
            },
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.value.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(
            modifier = Modifier
                .background(if (supportsGlass) glassGoldBrush else goldBrush)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = if (supportsGlass) 0.34f else 0f),
                    shape = cardShape
                )
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProfileAvatar(
                photoUrl = employee.photoUrl,
                size = 110.dp,
                shadowElevation = 0.dp
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = employee.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium.dynamic(),
                color = Color.Black,
            )
            Text(
                text = employee.designation,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF333333),
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color(0x33000000), thickness = 1.dp)
            Spacer(Modifier.height(16.dp))

            // Info grid
            InfoRow("Employee ID", employee.employeeId)
            InfoRow("Date of Birth", employee.dob)
            InfoRow("Phone", employee.phone)
            InfoRow("Posting Region", employee.postingRegion)
            InfoRow("Email", employee.email)
        }
    }
}

@Composable private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF444444),
            modifier = Modifier.width(120.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium.dynamic(),
            color = Color.Black,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}
