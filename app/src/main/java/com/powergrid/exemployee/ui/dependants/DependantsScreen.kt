package com.powergrid.exemployee.ui.dependants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.outlined.Person
import com.powergrid.exemployee.common.UiState
import com.powergrid.exemployee.domain.model.Dependant
import com.powergrid.exemployee.ui.components.ErrorMessage
import com.powergrid.exemployee.ui.components.LoadingIndicator
import com.powergrid.exemployee.ui.components.PullRefreshList
import com.powergrid.exemployee.ui.components.StatusChip

@Composable fun DependantsScreen(
    authToken: String,
    viewModel: DependantsViewModel = hiltViewModel(),
) {
    val state by viewModel.items.collectAsStateWithLifecycle()

    LaunchedEffect(authToken) { viewModel.load(authToken) }

    when (val s = state) {
        is UiState.Idle,
        is UiState.Loading -> LoadingIndicator()

        is UiState.Error -> ErrorMessage(
            message = s.message,
            onRetry = { viewModel.load(authToken) },
        )

        is UiState.Success -> PullRefreshList(
            items = s.data,
            isRefreshing = false,
            onRefresh = { viewModel.load(authToken) },
        ) { dependant ->
            DependantCard(dependant)
        }
    }
}

@Composable private fun DependantCard(dependant: Dependant) {
    val isActive = dependant.status.equals("active", ignoreCase = true)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (MaterialTheme.colorScheme.surfaceContainerLow.alpha < 1f) 0.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 4.dp,
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = dependant.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    StatusChip(
                        label = if (isActive) "Active" else "Inactive",
                        backgroundColor = if (isActive) Color(0xFFC8F5D1) else Color(0xFFFFE0E0),
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = dependant.relation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = "Age: ${dependant.age}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
