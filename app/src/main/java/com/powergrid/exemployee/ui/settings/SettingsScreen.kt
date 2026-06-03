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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import kotlin.math.roundToInt
import com.powergrid.exemployee.ui.components.SectionHeader
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.powergrid.exemployee.R
import com.powergrid.exemployee.common.AuthPrefs
import com.powergrid.exemployee.common.FontPrefs
import com.powergrid.exemployee.ui.theme.dynamic
import com.powergrid.exemployee.common.ThemePrefs
import com.powergrid.exemployee.security.BiometricResult
import com.powergrid.exemployee.ui.components.SignOutDialog
import com.powergrid.exemployee.ui.components.glassPanel
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authToken: String,
    onSignOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val view = LocalView.current
    val activity = context as? FragmentActivity

    var showSignOutDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    var showFontFamilyDialog by remember { mutableStateOf(false) }
    
    var isBiometricEnabled by remember { mutableStateOf(viewModel.hasBiometricSecret()) }
    var currentTheme by remember { mutableStateOf(ThemePrefs.getTheme(context)) }
    var currentFontScale by remember { mutableFloatStateOf(FontPrefs.getScale(context)) }
    var currentIsBold by remember { mutableStateOf(FontPrefs.isBold(context)) }
    var currentFontFamily by remember { mutableStateOf(FontPrefs.getFontFamily(context)) }

    val fontScales = listOf(0.85f, 1.0f, 1.15f, 1.30f)
    val fontScaleLabels = listOf("S", "M", "L", "XL")
    val fontFamilies = listOf(
        "default" to "System Default",
        "inter" to "Inter",
        "figtree" to "Figtree",
        "outfit" to "Outfit",
        "plus_jakarta_sans" to "Plus Jakarta Sans",
        "source_sans_3" to "Source Sans 3",
        "nunito_sans" to "Nunito Sans",
        "work_sans" to "Work Sans",
        "manrope" to "Manrope"
    )

    // Backdrop capture state for frosted glass on settings cards
    val backdrop = rememberLayerBackdrop()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .layerBackdrop(backdrop)
    ) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
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
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .glassPanel(
                            backdrop = backdrop,
                            shape = RoundedCornerShape(18.dp),
                            blurRadius = 12.dp,
                            fallbackColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
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
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .glassPanel(
                            backdrop = backdrop,
                            shape = RoundedCornerShape(18.dp),
                            blurRadius = 12.dp,
                            fallbackColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                Column {
                    // Row 1: Theme
                    SettingsRow(
                        title = "Theme",
                        subtitle = currentTheme.label,
                        iconRes = R.drawable.ic_palette,
                        iconColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { showThemeDialog = true }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 68.dp, end = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )

                    // Row 2: Font Size Slider
                    SettingsRow(
                         title = "Font Size",
                         subtitle = fontScaleLabels.getOrNull(fontScales.indexOf(currentFontScale)) ?: "M",
                         iconRes = R.drawable.ic_font_size,
                         iconColor = MaterialTheme.colorScheme.primary
                    )
                    Slider(
                        value = fontScales.indexOf(currentFontScale).toFloat(),
                        onValueChange = { newValue ->
                            val newIdx = newValue.roundToInt()
                            if (newIdx in fontScales.indices && fontScales[newIdx] != currentFontScale) {
                                currentFontScale = fontScales[newIdx]
                                FontPrefs.setScale(context, currentFontScale)
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            }
                        },
                        valueRange = 0f..(fontScales.size - 1).toFloat(),
                        steps = fontScales.size - 2,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 68.dp, end = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )

                    // Row 2.5: Font Family
                    SettingsRow(
                         title = "Font Family",
                         subtitle = fontFamilies.firstOrNull { it.first == currentFontFamily }?.second ?: "System Default",
                         iconRes = R.drawable.ic_font_family,
                         iconColor = MaterialTheme.colorScheme.primary,
                         modifier = Modifier.clickable { 
                             showFontFamilyDialog = true
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

    } // end Box(layerBackdrop)

    if (showSignOutDialog) {
        SignOutDialog(
            onDismiss = { showSignOutDialog = false },
            onConfirm = {
                showSignOutDialog = false
                onSignOut()
            }
        )
    }

    if (showThemeDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showThemeDialog = false }) {
                    Text("Close")
                }
            },
            title = {
                Text(
                    text = "Choose Theme",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold.dynamic()
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(ThemePrefs.AppTheme.entries.size) { index ->
                        val theme = ThemePrefs.AppTheme.entries[index]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentTheme = theme
                                    ThemePrefs.setTheme(context, theme)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp)
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
        )
    }

    if (showFontFamilyDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showFontFamilyDialog = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showFontFamilyDialog = false }) {
                    Text("Close")
                }
            },
            title = {
                Text(
                    text = "Choose Font Family",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold.dynamic()
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(fontFamilies.size) { index ->
                        val (key, label) = fontFamilies[index]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentFontFamily = key
                                    FontPrefs.setFontFamily(context, key)
                                    showFontFamilyDialog = false
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = currentFontFamily == key,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    iconRes: Int,
    modifier: Modifier = Modifier,
    iconColor: Color = MaterialTheme.colorScheme.primary,
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
