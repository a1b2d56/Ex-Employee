package com.powergrid.exemployee.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.powergrid.exemployee.common.UiState
import com.powergrid.exemployee.R
import com.powergrid.exemployee.ui.components.AppSnackbar
import com.powergrid.exemployee.ui.components.AppLogoBadge
import com.powergrid.exemployee.security.RootDetector

@Composable
fun LoginScreen(
    onLoginSuccess: (token: String) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val captchaState by viewModel.captcha.collectAsStateWithLifecycle()
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()
    val otpSentState by viewModel.otpSent.collectAsStateWithLifecycle()
    val otpVerifyState by viewModel.otpVerify.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var captchaAnswer by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    
    var passwordVisible by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    val isRooted = remember { RootDetector.isDeviceRooted() }
    var showRootWarning by remember { mutableStateOf(isRooted) }

    LaunchedEffect(Unit) {
        viewModel.loadCaptcha()
    }

    LaunchedEffect(loginState, otpSentState, otpVerifyState) {
        when {
            loginState is UiState.Success -> {
                onLoginSuccess((loginState as UiState.Success).data)
                viewModel.resetLogin()
            }
            loginState is UiState.Error -> {
                snackbarHostState.showSnackbar((loginState as UiState.Error).message)
                viewModel.resetLogin()
            }
            otpSentState is UiState.Success -> {
                snackbarHostState.showSnackbar("OTP Sent Successfully")
                viewModel.resetOtpSent()
            }
            otpSentState is UiState.Error -> {
                snackbarHostState.showSnackbar((otpSentState as UiState.Error).message)
                viewModel.resetOtpSent()
            }
            otpVerifyState is UiState.Success -> {
                onLoginSuccess((otpVerifyState as UiState.Success).data)
                viewModel.resetOtpVerify()
            }
            otpVerifyState is UiState.Error -> {
                snackbarHostState.showSnackbar((otpVerifyState as UiState.Error).message)
                viewModel.resetOtpVerify()
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                AppSnackbar(data = data)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (loginState is UiState.Loading || otpSentState is UiState.Loading || otpVerifyState is UiState.Loading || captchaState is UiState.Loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(48.dp))
            AppLogoBadge()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Ex-Employee",
                style = MaterialTheme.typography.headlineMedium
            )
            
            // Root Warning Pill
            if (showRootWarning) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.92f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Rooted device detected. Running in secure restricted mode.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { showRootWarning = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Modern Segmented Pill tab Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Password Login", "OTP Login").forEachIndexed { index, label ->
                    val isSelected = selectedTab == index
                    val tabBgColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        label = "TabBgAnimation"
                    )
                    val tabTextColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "TabTextColorAnimation"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(tabBgColor)
                            .clickable { selectedTab = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = tabTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                }, label = "Login Tab Transition"
            ) { tab ->
                Column(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Employee ID / Username") },
                        leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (tab == 0) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        painter = painterResource(id = if (passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility),
                                        contentDescription = "Toggle Password"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Captcha Section
                    if (captchaState is UiState.Success) {
                        val captchaData = (captchaState as? UiState.Success)?.data
                        if (captchaData != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Captcha Text
                                val primaryColor = MaterialTheme.colorScheme.primary
                                val stripeBrush = remember {
                                    Brush.linearGradient(
                                        colors = listOf(
                                            primaryColor.copy(alpha = 0.1f),
                                            primaryColor.copy(alpha = 0.3f)
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(40f, 40f),
                                        tileMode = TileMode.Repeated
                                    )
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .background(
                                            brush = stripeBrush, 
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = primaryColor.copy(alpha=0.3f),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = captchaData.question,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor,
                                        modifier = Modifier.graphicsLayer {
                                            rotationZ = -2f
                                        }
                                    )
                                    
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val canvasWidth = size.width
                                        val canvasHeight = size.height
                                        drawLine(
                                            color = primaryColor.copy(alpha = 0.2f),
                                            start = Offset(0f, canvasHeight * 0.2f),
                                            end = Offset(canvasWidth, canvasHeight * 0.8f),
                                            strokeWidth = 2f
                                        )
                                        drawLine(
                                            color = primaryColor.copy(alpha = 0.2f),
                                            start = Offset(canvasWidth * 0.2f, 0f),
                                            end = Offset(canvasWidth * 0.8f, canvasHeight),
                                            strokeWidth = 2f
                                        )
                                    }
                                }
                                
                                // Refresh Button
                                IconButton(onClick = { viewModel.loadCaptcha() }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Refresh,
                                        contentDescription = "Refresh Captcha",
                                        tint = primaryColor
                                    )
                                }
                
                                // Captcha Input
                                OutlinedTextField(
                                    value = captchaAnswer,
                                    onValueChange = { captchaAnswer = it },
                                    label = { Text("Answer") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    if (tab == 0) {
                        Button(
                            onClick = {
                                val token = (captchaState as? UiState.Success)?.data?.token ?: ""
                                viewModel.loginPassword(username, password, token, captchaAnswer.toIntOrNull() ?: 0)
                            },
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("Sign In")
                        }
                    } else {
                        Button(
                            onClick = {
                                val token = (captchaState as? UiState.Success)?.data?.token ?: ""
                                viewModel.sendOtp(username, token, captchaAnswer.toIntOrNull() ?: 0)
                            },
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("Send OTP")
                        }
                        
                        // Show OTP field if OTP is sent (simulated by checking if we have tried to send)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = otp,
                            onValueChange = { otp = it },
                            label = { Text("Enter OTP") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.verifyOtp(username, otp) },
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("Verify OTP")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = { onLoginSuccess("dev_bypass_token") },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("⚠ Dev Bypass (Remove before release)")
            }
        }
    }
}
