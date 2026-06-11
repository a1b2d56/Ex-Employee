package com.powergrid.exemployee.ui.dependants

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.powergrid.exemployee.R
import com.powergrid.exemployee.domain.model.Dependant
import com.powergrid.exemployee.domain.model.Employee
import com.powergrid.exemployee.domain.model.VerificationDoc
import com.powergrid.exemployee.ui.components.ErrorMessage
import com.powergrid.exemployee.ui.components.LoadingIndicator
import com.powergrid.exemployee.ui.components.ProfileAvatar
import com.powergrid.exemployee.ui.theme.dynamic

@Composable fun DependantsScreen(
    authToken: String,
    viewModel: DependantsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(authToken) { viewModel.load(authToken) }

    when {
        state.isLoading -> LoadingIndicator()
        state.errorMessage != null -> ErrorMessage(
            message = state.errorMessage!!,
            onRetry = { viewModel.load(authToken) },
        )
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Employee "Self" Card — compact, gold gradient, expandable
                state.employee?.let { employee ->
                    val selfStatus = if (state.allVerified) "Updated" else "In process"
                    EmployeeSelfCard(
                        employee = employee,
                        selfStatus = selfStatus,
                        isExpanded = state.expandedCardId == "self",
                        onToggle = { viewModel.toggleExpand("self") },
                        verificationDocs = state.verificationDocs,
                    )
                }

                // Dependant Cards — expandable
                state.dependants.forEach { dependant ->
                    ExpandableDependantCard(
                        dependant = dependant,
                        isExpanded = state.expandedCardId == dependant.id,
                        onToggle = { viewModel.toggleExpand(dependant.id) },
                        verificationDocs = state.verificationDocs,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Download Certificate — only when every dependant is verified
                if (state.allVerified) {
                    Spacer(Modifier.height(4.dp))
                    androidx.compose.material3.OutlinedButton(
                        onClick = { /* TODO: API call to download certificate */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Download Certificate",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold.dynamic()
                        )
                    }
                }
            }
        }
    }
}

// ─── Employee "Self" Card ───────────────────────────────────────────

@Composable private fun EmployeeSelfCard(
    employee: Employee,
    selfStatus: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    verificationDocs: List<VerificationDoc>,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            ),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(Color(0xFFE6C176), Color(0xFFC7983C))))
        ) {
            // ── Collapsed header (always visible) ──
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileAvatar(
                    photoUrl = employee.photoUrl,
                    size = 52.dp,
                    shadowElevation = 0.dp,
                    borderWidth = 1.5.dp
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = employee.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black,
                            modifier = Modifier.weight(1f),
                        )

                        // "Self" badge
                        Surface(
                            color = Color.White.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Self",
                                color = Color.Black,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(2.dp))

                    Text(
                        text = employee.designation,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF333333),
                    )

                    if (employee.dob.isNotEmpty()) {
                        Text(
                            text = "DOB: ${employee.dob}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF333333).copy(alpha = 0.8f),
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Status badge
                    StatusBadge(selfStatus)
                }
            }

            // ── Expanded content ──
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(),
                exit = shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                ) {
                    HorizontalDivider(color = Color(0x33000000), thickness = 1.dp)
                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold.dynamic(),
                        color = Color(0xFF5D4037),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    InfoRow("Employee ID", employee.employeeId)
                    InfoRow("Date of Birth", employee.dob)
                    InfoRow("Phone", employee.phone)
                    InfoRow("Email", employee.email)

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Document Status",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold.dynamic(),
                        color = Color(0xFF5D4037),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (verificationDocs.isEmpty()) {
                        Text(
                            text = "No documents available",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF333333).copy(alpha = 0.7f),
                        )
                    } else {
                        verificationDocs.forEach { doc ->
                            DocumentStatusRow(doc)
                        }
                    }
                }
            }
        }
    }
}

// ─── Expandable Dependant Card ──────────────────────────────────────

@Composable private fun ExpandableDependantCard(
    dependant: Dependant,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    verificationDocs: List<VerificationDoc>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            ),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        )
    ) {
        Column {
            // ── Collapsed header (always visible) ──
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileAvatar(
                    photoUrl = dependant.photoUrl,
                    size = 52.dp,
                    shadowElevation = 0.dp,
                    borderWidth = 1.5.dp
                )

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
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )

                        // Relation badge
                        val isChild = dependant.relation.equals("Son", ignoreCase = true) ||
                                dependant.relation.equals("Daughter", ignoreCase = true)
                        Surface(
                            color = if (isChild) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = dependant.relation,
                                color = if (isChild) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    if (dependant.dob.isNotEmpty()) {
                        Text(
                            text = "DOB: ${dependant.dob}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        )
                    }

                    if (dependant.age > 0) {
                        Text(
                            text = "Age: ${dependant.age}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Status badge — same style as Liveliness
                    StatusBadge(dependant.status)
                }
            }

            // ── Expanded content ──
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(),
                exit = shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 1.dp
                    )
                    Spacer(Modifier.height(12.dp))

                    // Details section
                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold.dynamic(),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    DetailRow("Name", dependant.name)
                    DetailRow("Relation", dependant.relation)
                    if (dependant.dob.isNotEmpty()) DetailRow("Date of Birth", dependant.dob)
                    if (dependant.age > 0) DetailRow("Age", "${dependant.age}")

                    Spacer(Modifier.height(16.dp))

                    // Document status section
                    Text(
                        text = "Document Status",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold.dynamic(),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (verificationDocs.isEmpty()) {
                        Text(
                            text = "No documents available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        verificationDocs.forEach { doc ->
                            DocumentStatusRow(doc)
                        }
                    }
                }
            }
        }
    }
}

// ─── Status Badge (Liveliness style) ────────────────────────────────

@Composable private fun StatusBadge(status: String) {
    val (statusColor, statusBgColor, statusText) = when (status) {
        "Needs verification" -> Triple(
            Color(0xFFBA1A1A),
            Color(0xFFFFDAD6),
            "Needs verification"
        )
        "In process" -> Triple(
            Color(0xFFF57F17),
            Color(0xFFFFF9C4),
            "In process"
        )
        "Updated", "No need to update" -> Triple(
            Color(0xFF4CAF50),
            Color(0xFFE8F5E9),
            "Updated"
        )
        else -> Triple(Color.Transparent, Color.Transparent, status)
    }

    if (statusText.isNotEmpty()) {
        Surface(
            color = statusBgColor,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = statusText,
                color = statusColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

// ─── Document Status Row ────────────────────────────────────────────

@Composable private fun DocumentStatusRow(doc: VerificationDoc) {
    val (statusColor, statusText, statusIcon) = when (doc.status) {
        "verified" -> Triple(Color(0xFF4CAF50), "Verified", R.drawable.ic_nav_home)
        "pending" -> Triple(Color(0xFFF57F17), "Pending", R.drawable.ic_nav_noticeboard)
        "rejected" -> Triple(Color(0xFFBA1A1A), "Rejected", R.drawable.ic_nav_noticeboard)
        else -> Triple(Color.Gray, doc.status, R.drawable.ic_nav_noticeboard)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = doc.docType,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        Surface(
            color = statusColor.copy(alpha = 0.12f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = statusText,
                color = statusColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

// ─── Detail Row ─────────────────────────────────────────────────────

@Composable private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
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

// ─── Gold card info row ─────────────────────────────────────────────

@Composable private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
