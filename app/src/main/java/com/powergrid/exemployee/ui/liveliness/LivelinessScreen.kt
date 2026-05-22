package com.powergrid.exemployee.ui.liveliness

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.powergrid.exemployee.common.UiState
import com.powergrid.exemployee.ui.components.LoadingIndicator
import com.powergrid.exemployee.ui.components.AppSnackbar

@Composable
fun LivelinessScreen(
    authToken: String,
    viewModel: LivelinessViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val submitState by viewModel.submitState.collectAsStateWithLifecycle()
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    var isCameraRunning by remember { mutableStateOf(false) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (granted) isCameraRunning = true
        }
    )

    val imageCapture = remember { ImageCapture.Builder().build() }

    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(submitState) {
        if (submitState is UiState.Success) {
            snackbarHostState.showSnackbar((submitState as UiState.Success).data)
            viewModel.reset()
            capturedBitmap = null
            viewModel.setCapturedPhoto(null)
            isCameraRunning = false
        } else if (submitState is UiState.Error) {
            snackbarHostState.showSnackbar((submitState as UiState.Error).message)
            viewModel.reset()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                AppSnackbar(data = data)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (submitState is UiState.Loading) {
                LoadingIndicator()
            } else if (capturedBitmap != null) {
                // Show captured image
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = "Captured Photo",
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(onClick = { 
                            capturedBitmap = null
                            viewModel.setCapturedPhoto(null)
                            isCameraRunning = true
                        }) {
                            Text("Retake")
                        }
                        Button(onClick = { viewModel.submit() }) {
                            Text("Submit Liveliness")
                        }
                    }
                }
            } else if (isCameraRunning && hasCameraPermission) {
                // Show CameraX Preview
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.surfaceProvider = previewView.surfaceProvider
                                }
                                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageCapture
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    Button(
                        onClick = {
                            imageCapture.takePicture(
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        val buffer = image.planes[0].buffer
                                        val bytes = ByteArray(buffer.capacity())
                                        buffer.get(bytes)
                                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
                                        val matrix = Matrix().apply {
                                            postRotate(image.imageInfo.rotationDegrees.toFloat())
                                            // Front camera mirror
                                            postScale(-1f, 1f)
                                        }
                                        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                                        capturedBitmap = rotated
                                        viewModel.setCapturedPhoto(rotated)
                                        isCameraRunning = false
                                        image.close()
                                    }
                                    override fun onError(exception: ImageCaptureException) {
                                        exception.printStackTrace()
                                    }
                                }
                            )
                        },
                        modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp)
                    ) {
                        Text("Capture Photo")
                    }
                }
            } else {
                // Show start state
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Please capture a clear, well-lit photograph of yourself and submit to confirm liveliness of your registered dependants.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    Button(onClick = {
                        if (hasCameraPermission) {
                            isCameraRunning = true
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }) {
                        Text("Start Camera")
                    }
                }
            }
        }
    }
}
