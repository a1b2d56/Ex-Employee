package com.powergrid.exemployee.ui.noticeboard

import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.powergrid.exemployee.ui.theme.dynamic
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.powergrid.exemployee.common.UiState
import com.powergrid.exemployee.domain.model.Notice
import com.powergrid.exemployee.ui.components.ErrorMessage
import com.powergrid.exemployee.ui.components.LoadingIndicator
import com.powergrid.exemployee.ui.components.StatusChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun NoticeboardScreen(
    authToken: String,
    viewModel: NoticeboardViewModel = hiltViewModel(),
) {
    val state by viewModel.notices.collectAsStateWithLifecycle()

    LaunchedEffect(authToken) { viewModel.load(authToken) }

    when (val s = state) {
        is UiState.Idle, is UiState.Loading -> LoadingIndicator()
        is UiState.Error -> ErrorMessage(s.message, onRetry = { viewModel.load(authToken) })
        is UiState.Success -> {
            PullToRefreshBox(
                isRefreshing = false,
                onRefresh = { viewModel.load(authToken) },
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = s.data,
                        key = { it.id },
                    ) { notice ->
                        NoticeItem(
                            notice = notice,
                            modifier = Modifier.animateItem(
                                placementSpec = spring(),
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable private fun NoticeItem(notice: Notice, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (MaterialTheme.colorScheme.surfaceContainerLow.alpha < 1f) 0.dp else 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = notice.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold.dynamic(),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )

                if (notice.urgent) {
                    Spacer(Modifier.width(8.dp))
                    StatusChip(
                        label = "Urgent",
                        backgroundColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = notice.date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = notice.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
