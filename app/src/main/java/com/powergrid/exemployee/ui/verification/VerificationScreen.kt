package com.powergrid.exemployee.ui.verification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.powergrid.exemployee.common.UiState
import com.powergrid.exemployee.domain.model.VerificationDoc
import com.powergrid.exemployee.ui.components.ErrorMessage
import com.powergrid.exemployee.ui.components.LoadingIndicator
import com.powergrid.exemployee.ui.components.PullRefreshList
import com.powergrid.exemployee.ui.components.StatusChip

@Composable fun VerificationScreen(
    authToken: String,
    viewModel: VerificationViewModel = hiltViewModel(),
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
        ) { doc ->
            VerificationCard(doc)
        }
    }
}

private data class ChipConfig(val label: String, val color: Color)

private fun resolveChip(status: String): ChipConfig = when (status.lowercase()) {
    "verified" -> ChipConfig("✓ Verified", Color(0xFFC8F5D1))
    "rejected" -> ChipConfig("✗ Rejected", Color(0xFFFFE0E0))
    else -> ChipConfig("⏳ Pending", Color(0xFFFFF3CC))
}

@Composable private fun VerificationCard(doc: VerificationDoc) {
    val chip = resolveChip(doc.status)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = doc.docType,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                StatusChip(label = chip.label, backgroundColor = chip.color)
            }

            doc.verifiedOn?.let { date ->
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Verified on: $date",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            doc.remarks?.let { remarks ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = remarks,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
