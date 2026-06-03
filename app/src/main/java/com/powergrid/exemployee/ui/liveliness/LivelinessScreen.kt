package com.powergrid.exemployee.ui.liveliness

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.powergrid.exemployee.ui.components.ErrorMessage
import com.powergrid.exemployee.ui.components.LoadingIndicator
import java.util.concurrent.Executors

@Composable
fun LivelinessScreen(
    authToken: String,
    viewModel: LivelinessViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(authToken) {
        viewModel.loadCardItems(authToken)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading -> {
                LoadingIndicator()
            }
            uiState.errorMessage != null -> {
                ErrorMessage(message = uiState.errorMessage ?: "Unknown error")
            }
            uiState.selectedCard == null -> {
                CardSelectionView(
                    statusMessage = uiState.statusMessage,
                    cardItems = uiState.cardItems,
                    onCardSelected = { card ->
                        viewModel.selectCard(card)
                    }
                )
            }
            else -> {
                CameraVerificationView(
                    uiState = uiState,
                    onFaceFrameDetected = { faces, imageProxy, onProcessed ->
                        viewModel.onFaceFrameDetected(faces, imageProxy, onProcessed)
                    },
                    onBackPress = {
                        viewModel.closeCompletionDialog()
                    }
                )
            }
        }

        if (uiState.showCompletionDialog) {
            VerificationSuccessDialog(
                onDismiss = {
                    viewModel.closeCompletionDialog()
                }
            )
        }
    }
}

@Composable
private fun CardSelectionView(
    statusMessage: String,
    cardItems: List<LivelinessCardItem>,
    onCardSelected: (LivelinessCardItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(cardItems) { card ->
                LivelinessCardRow(card = card, onClick = { onCardSelected(card) })
            }
        }
    }
}

@Composable
private fun LivelinessCardRow(
    card: LivelinessCardItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Blue outlined circle
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    
                    val relationColor = MaterialTheme.colorScheme.onPrimaryContainer
                    val relationBgColor = MaterialTheme.colorScheme.primaryContainer
                    
                    Surface(
                        color = if (card.relation.equals("Son", ignoreCase = true) || card.relation.equals("Daughter", ignoreCase = true)) MaterialTheme.colorScheme.surfaceVariant else relationBgColor,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = card.relation,
                            color = if (card.relation.equals("Son", ignoreCase = true) || card.relation.equals("Daughter", ignoreCase = true)) MaterialTheme.colorScheme.onSurfaceVariant else relationColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                
                Spacer(Modifier.height(4.dp))
                
                if (card.dob.isNotEmpty()) {
                    Text(
                        text = "DOB: ${card.dob}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
                if (card.age.isNotEmpty()) {
                    Text(
                        text = "Age: ${card.age}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
                
                Spacer(Modifier.height(8.dp))
                
                val statusColor = when (card.status) {
                    1 -> Color(0xFFF57F17) 
                    2 -> MaterialTheme.colorScheme.error
                    0 -> Color(0xFF4CAF50)
                    else -> Color.Transparent
                }
                val statusBgColor = when (card.status) {
                    1 -> Color(0xFFFFF9C4)
                    2 -> MaterialTheme.colorScheme.errorContainer
                    0 -> Color(0xFFE8F5E9)
                    else -> Color.Transparent
                }
                val statusText = when (card.status) {
                    1 -> "In process"
                    2 -> "Needs verification"
                    0 -> "Updated"
                    else -> ""
                }
                
                if (statusText.isNotEmpty()) {
                    Surface(
                        color = if (card.status == 2) statusBgColor else if (card.status == 0) statusBgColor else statusColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = statusText,
                            color = if (card.status == 2) MaterialTheme.colorScheme.onErrorContainer else statusColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraVerificationView(
    uiState: LivelinessUiState,
    onFaceFrameDetected: (List<Face>, ImageProxy, () -> Unit) -> Unit,
    onBackPress: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )
    
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    if (!hasCameraPermission) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Camera permission is required for face verification",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }
    
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val faceDetectorOptions = remember {
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
    }
    val faceDetector = remember { FaceDetection.getClient(faceDetectorOptions) }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            faceDetector.close()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setResolutionSelector(
                            ResolutionSelector.Builder()
                                .setResolutionStrategy(
                                    ResolutionStrategy(
                                        Size(640, 480),
                                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                                    )
                                )
                                .build()
                        )
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        
                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            faceDetector.process(image)
                                .addOnSuccessListener { faces ->
                                    onFaceFrameDetected(faces, imageProxy) {
                                        imageProxy.close()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("CameraVerificationView", "Face detection failure", e)
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }
                    
                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.e("CameraVerificationView", "Use case binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val ovalWidth = canvasWidth * 0.55f
            val ovalHeight = canvasHeight * 0.45f
            val ovalLeft = (canvasWidth - ovalWidth) / 2
            val ovalTop = (canvasHeight - ovalHeight) / 2.3f
            
            val path = Path().apply {
                addRect(rect = androidx.compose.ui.geometry.Rect(0f, 0f, canvasWidth, canvasHeight))
            }
            val ovalPath = Path().apply {
                addOval(oval = androidx.compose.ui.geometry.Rect(ovalLeft, ovalTop, ovalLeft + ovalWidth, ovalTop + ovalHeight))
            }
            
            drawPath(path = path, color = Color.Black.copy(alpha = 0.5f))
            drawPath(path = ovalPath, color = Color.Transparent, blendMode = BlendMode.Clear)
            
            val strokeColor = if (uiState.faceDetected) Color(0xFF4CAF50) else Color.White
            drawOval(
                color = strokeColor,
                topLeft = Offset(ovalLeft, ovalTop),
                size = androidx.compose.ui.geometry.Size(ovalWidth, ovalHeight),
                style = Stroke(width = 4.dp.toPx())
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Button(
                    onClick = onBackPress,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = CircleShape
                ) {
                    Text("Back")
                }
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.statusMessage,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    if (uiState.faceMatchScore != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Face Similarity Score: ${String.format(androidx.compose.ui.text.intl.Locale.current.platformLocale, "%.2f", uiState.faceMatchScore)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    if (uiState.blinkCount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Blinks Detected: ${uiState.blinkCount}/2",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VerificationSuccessDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(72.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Verification Successful",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Face verification has been completed successfully.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}
