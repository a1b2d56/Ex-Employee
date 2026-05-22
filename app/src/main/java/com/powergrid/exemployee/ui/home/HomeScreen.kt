package com.powergrid.exemployee.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.powergrid.exemployee.ui.theme.dynamic

@Composable fun HomeScreen(
    authToken: String,
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
                EmployeeCard(s.data)
            }
        }
    }
}

@Composable private fun EmployeeCard(employee: Employee) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (MaterialTheme.colorScheme.surfaceContainerLow.alpha < 1f) 0.dp else 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProfileAvatar(
                photoUrl = employee.photoUrl,
                size = 100.dp,
                shadowElevation = if (MaterialTheme.colorScheme.surfaceContainerLow.alpha < 1f) 0.dp else 4.dp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = employee.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold.dynamic(),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = employee.department,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(16.dp))

            // Info grid
            InfoRow("Employee ID", employee.employeeId)
            InfoRow("Age", employee.age.toString())
            InfoRow("Date of Birth", employee.dob)
            InfoRow("Email", employee.email)
            InfoRow("Phone", employee.phone)
        }
    }
}

@Composable private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium.dynamic(),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}
