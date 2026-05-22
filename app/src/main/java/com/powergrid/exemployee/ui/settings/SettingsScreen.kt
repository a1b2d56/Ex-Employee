package com.powergrid.exemployee.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import com.powergrid.exemployee.ui.components.SectionHeader
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.powergrid.exemployee.R
import com.powergrid.exemployee.common.AuthPrefs
import com.powergrid.exemployee.common.FontPrefs
import com.powergrid.exemployee.ui.theme.dynamic
import com.powergrid.exemployee.common.ThemePrefs
import com.powergrid.exemployee.security.BiometricResult
import com.powergrid.exemployee.ui.components.SignOutDialog
import com.powergrid.exemployee.security.BiometricHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authToken: String,
    onNavigateToAbout: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    var showSignOutDialog by remember { mutableStateOf(false) }
    var showThemeSheet by remember { mutableStateOf(false) }
    
    var isBiometricEnabled by remember { mutableStateOf(viewModel.hasBiometricSecret()) }
    var currentTheme by remember { mutableStateOf(ThemePrefs.getTheme(context)) }
    var currentFontScale by remember { mutableFloatStateOf(FontPrefs.getScale(context)) }
    var currentIsBold by remember { mutableStateOf(FontPrefs.isBold(context)) }

    val fontScales = listOf(0.85f, 1.0f, 1.15f, 1.30f)
    val fontScaleLabels = listOf("S", "M", "L", "XL")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Card 1: Account
        item {
            Column {
                SectionHeader("Account")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                Column {
                    // Row 1: Signed In Status
                    SettingsRow(
                        title = "Signed In",
                        subtitle = "Manage your session",
                        iconRes = R.drawable.ic_person_outline,
                        iconColor = MaterialTheme.colorScheme.primary
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 68.dp, end = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )

                    // Row 2: Biometric Login
                    if (viewModel.isBiometricAvailable() && activity != null) {
                        SettingsRow(
                            title = "Biometric Login",
                            subtitle = "",
                            iconRes = R.drawable.ic_fingerprint,
                            iconColor = MaterialTheme.colorScheme.primary,
                            trailing = {
                                Switch(
                                    checked = isBiometricEnabled,
                                    onCheckedChange = { enable ->
                                        if (enable) {
                                            viewModel.biometric.promptToEncryptAndStore(activity, authToken) { result ->
                                                if (result is BiometricResult.Success) {
                                                    isBiometricEnabled = true
                                                    AuthPrefs.setToken(context, null)
                                                }
                                            }
                                        } else {
                                            viewModel.biometric.clearSecret()
                                            AuthPrefs.setToken(context, authToken)
                                            isBiometricEnabled = false
                                        }
                                    }
                                )
                            }
                        )
                        HorizontalDivider(
                        modifier = Modifier.padding(start = 68.dp, end = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )
                    }

                    // Row 3: Sign Out
                    SettingsRow(
                        title = "Sign Out",
                        subtitle = "",
                        iconRes = R.drawable.ic_logout,
                        iconColor = MaterialTheme.colorScheme.error,
                        modifier = Modifier.clickable { showSignOutDialog = true }
                    )
                }
            }
            }
        }

        // Card 2: Appearance
        item {
            Column {
                SectionHeader("Appearance")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                Column {
                    // Row 1: Theme
                    SettingsRow(
                        title = "Theme",
                        subtitle = currentTheme.label,
                        iconRes = R.drawable.ic_palette,
                        iconColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { showThemeSheet = true }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 68.dp, end = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )

                    // Row 2: Font Size
                    SettingsRow(
                        title = "Font Size",
                        subtitle = fontScaleLabels.getOrNull(fontScales.indexOf(currentFontScale)) ?: "M",
                        iconRes = R.drawable.ic_font_size,
                        iconColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { 
                            val nextIdx = (fontScales.indexOf(currentFontScale) + 1) % fontScales.size
                            currentFontScale = fontScales[nextIdx]
                            FontPrefs.setScale(context, currentFontScale)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 68.dp, end = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )

                    // Row 3: Bold Text
                    SettingsRow(
                        title = "Bold Text",
                        subtitle = "",
                        iconRes = R.drawable.ic_format_bold,
                        iconColor = MaterialTheme.colorScheme.primary,
                        trailing = {
                            Switch(
                                checked = currentIsBold,
                                onCheckedChange = { bold ->
                                    currentIsBold = bold
                                    FontPrefs.setBold(context, bold)
                                }
                            )
                        }
                    )
                }
            }
            }
        }


    }

    if (showSignOutDialog) {
        SignOutDialog(
            onDismiss = { showSignOutDialog = false },
            onConfirm = {
                showSignOutDialog = false
                onSignOut()
            }
        )
    }

    if (showThemeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showThemeSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                item {
                    Text(
                        text = "Choose Theme",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold.dynamic()
                    )
                }
                items(ThemePrefs.AppTheme.entries.size) { index ->
                    val theme = ThemePrefs.AppTheme.entries[index]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                currentTheme = theme
                                ThemePrefs.setTheme(context, theme)
                                showThemeSheet = false
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        RadioButton(
                            selected = currentTheme == theme,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = theme.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    iconRes: Int,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rounded icon background container (identical to sidebar style)
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title, 
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold.dynamic()
            )
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(16.dp))
            trailing()
        }
    }
}
