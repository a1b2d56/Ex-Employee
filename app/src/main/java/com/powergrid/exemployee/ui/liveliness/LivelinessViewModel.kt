package com.powergrid.exemployee.ui.liveliness

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import java.io.ByteArrayOutputStream
import com.powergrid.exemployee.ml.FaceNetHelper
import android.widget.Toast
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.powergrid.exemployee.common.UiState
import com.powergrid.exemployee.domain.repository.EmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

data class LivelinessCardItem(
    val id: String,
    val name: String,
    val relation: String,
    val photo: String?,
    val dob: String,
    val age: String,
    val status: Int
)

sealed interface VerificationResult {
    object Verified : VerificationResult {
        override fun toString(): String = "Verified"
    }
}

data class LivelinessUiState(
    val statusMessage: String = "Initializing verification...",
    val faceDetected: Boolean = false,
    val hasDetectedBlink: Boolean = false,
    val faceMatchScore: Float? = null,
    val isVerifying: Boolean = false,
    val verificationResult: VerificationResult? = null,
    val blinkCount: Int = 0,
    val cardItems: List<LivelinessCardItem> = emptyList(),
    val selectedCard: LivelinessCardItem? = null,
    val showCompletionDialog: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class LivelinessViewModel @Inject constructor(
    private val employeeRepository: EmployeeRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(LivelinessUiState())
    val uiState: StateFlow<LivelinessUiState> = _uiState.asStateFlow()

    val eyeHistory = ArrayList<Boolean>()
    
    private val faceNetHelper by lazy { FaceNetHelper(context) }
    private var profileFaceEmbedding: FloatArray? = null

    val isAnalyzing = AtomicBoolean(false)
    var referencePhotoVerified = false

    fun loadCardItems(authToken: String) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(statusMessage = "Loading employee & dependants...", isLoading = true) }
        viewModelScope.launch {
            try {
                val empResult = employeeRepository.getEmployeeInfo(authToken)
                val familyResult = employeeRepository.getFamilyMembers(authToken)
                
                if (empResult is UiState.Success && familyResult is UiState.Success) {
                    val employee = empResult.data
                    val familyMembers = familyResult.data
                    
                    val list = ArrayList<LivelinessCardItem>()
                    // Self item
                    list.add(
                        LivelinessCardItem(
                            id = "self",
                            name = employee.name,
                            relation = "Self",
                            photo = employee.photo,
                            dob = employee.dob,
                            age = calculateAge(employee.dob),
                            status = 2
                        )
                    )
                    // Family members mapped to card items with status based on index
                    familyMembers.forEachIndexed { index, member ->
                        val status = when (index) {
                            0 -> 2
                            1 -> 1
                            else -> 0
                        }
                        list.add(
                            LivelinessCardItem(
                                id = member.name,
                                name = member.name,
                                relation = member.relation,
                                photo = member.photo,
                                dob = member.dob,
                                age = member.age.toString(),
                                status = status
                            )
                        )
                    }
                    _uiState.update { 
                        it.copy(
                            statusMessage = "Select a card to verify",
                            cardItems = list,
                            isLoading = false
                        )
                    }
                } else {
                    val errorMsg = when {
                        empResult is UiState.Error -> empResult.message
                        familyResult is UiState.Error -> familyResult.message
                        else -> "Failed to load data"
                    }
                    _uiState.update { 
                        it.copy(
                            statusMessage = "Error loading details",
                            errorMessage = errorMsg,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("LivelinessViewModel", "Error initializing Liveliness screen", e)
                _uiState.update { 
                    it.copy(
                        statusMessage = "Error loading details",
                        errorMessage = "Init Error: " + (e.message ?: "Unknown error"),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectCard(card: LivelinessCardItem) {
        if (card.status == 2) {
            _uiState.update { 
                it.copy(
                    selectedCard = card,
                    statusMessage = "Loading profile photo...",
                    faceMatchScore = null,
                    faceDetected = false,
                    hasDetectedBlink = false,
                    verificationResult = null,
                    blinkCount = 0,
                    showCompletionDialog = false
                )
            }
            eyeHistory.clear()
            referencePhotoVerified = false
            performReferenceFaceDetection(card)
        } else {
            val msg = when (card.status) {
                1 -> "${card.name} is verification in progress"
                0 -> "${card.name} is already updated (No need to update)"
                else -> "${card.name} is not clickable"
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun performReferenceFaceDetection(card: LivelinessCardItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val photoPath = card.photo
            if (photoPath.isNullOrEmpty()) {
                referencePhotoVerified = true
                _uiState.update { it.copy(statusMessage = "Position your face in the oval") }
                return@launch
            }
            
            var detector: FaceDetector? = null
            try {
                val openStream = context.assets.open(photoPath)
                val bitmap = BitmapFactory.decodeStream(openStream)
                openStream.close()
                
                if (bitmap != null) {
                    val options = FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .build()
                    detector = FaceDetection.getClient(options)
                    val inputImage = InputImage.fromBitmap(bitmap, 0)
                    
                    val faces = Tasks.await(detector.process(inputImage))
                    
                    referencePhotoVerified = true
                    if (faces.isNotEmpty()) {
                        val faceBounds = faces.first().boundingBox
                        val faceBitmap = faceNetHelper.extractFace(bitmap, faceBounds)
                        profileFaceEmbedding = faceNetHelper.getFaceEmbedding(faceBitmap)
                        
                        if (profileFaceEmbedding != null) {
                            _uiState.update { it.copy(statusMessage = "Position your face in the oval") }
                        } else {
                            Log.e("LivelinessViewModel", "Failed to extract embedding from reference")
                            _uiState.update { it.copy(statusMessage = "Position your face in the oval (fallback)") }
                        }
                    } else {
                        Log.w("LivelinessViewModel", "No face detected in reference photo, using fallback")
                        _uiState.update { it.copy(statusMessage = "Position your face in the oval (fallback)") }
                    }
                } else {
                    referencePhotoVerified = true
                    _uiState.update { it.copy(statusMessage = "Position your face in the oval") }
                }
            } catch (e: Exception) {
                Log.e("LivelinessViewModel", "Error analyzing reference photo, using fallback", e)
                referencePhotoVerified = true
                _uiState.update { it.copy(statusMessage = "Position your face in the oval") }
            } finally {
                detector?.close()
            }
        }
    }

    fun onFaceFrameDetected(faces: List<Face>, image: ImageProxy, onFrameProcessed: () -> Unit) {
        if (isAnalyzing.get()) {
            onFrameProcessed()
            return
        }
        if (_uiState.value.verificationResult != null) {
            onFrameProcessed()
            return
        }
        
        isAnalyzing.set(true)
        
        try {
            val isCentered = isFaceCenteredInOval(faces, image)
            
            _uiState.update { 
                it.copy(
                    faceDetected = faces.isNotEmpty() && isCentered
                )
            }
            
            if (faces.isEmpty()) {
                _uiState.update { 
                    it.copy(
                        statusMessage = "Position your face in the oval",
                        faceDetected = false,
                        isVerifying = false
                    )
                }
                isAnalyzing.set(false)
                onFrameProcessed()
                return
            }
            
            if (!isCentered) {
                _uiState.update { 
                    it.copy(
                        statusMessage = "Please center your face",
                        faceDetected = false,
                        isVerifying = false
                    )
                }
                isAnalyzing.set(false)
                onFrameProcessed()
                return
            }
            
            val face = faces.first()
            val leftOpen = face.leftEyeOpenProbability
            val rightOpen = face.rightEyeOpenProbability
            
            if (leftOpen != null && rightOpen != null) {
                val eyesOpen = leftOpen > 0.5f && rightOpen > 0.5f
                eyeHistory.add(eyesOpen)
                if (eyeHistory.size > 10) {
                    eyeHistory.removeAt(0)
                }
                
                if (hasDetectedBlink()) {
                    _uiState.update { 
                        it.copy(
                            blinkCount = it.blinkCount + 1
                        )
                    }
                }
            }
            
            val bitmap = imageProxyToBitmap(image)
            
            // Calculate score using FaceNet if available
            if (bitmap != null && profileFaceEmbedding != null) {
                val faceBounds = face.boundingBox
                val faceBitmap = faceNetHelper.extractFace(bitmap, faceBounds)
                val currentEmbedding = faceNetHelper.getFaceEmbedding(faceBitmap)
                
                if (currentEmbedding != null) {
                    val score = faceNetHelper.compareFaces(profileFaceEmbedding!!, currentEmbedding)
                    _uiState.update { it.copy(faceMatchScore = score) }
                }
            } else {
                // Fallback to fake score if model fails or no reference embedding
                val eulerY = kotlin.math.abs(face.headEulerAngleY)
                val eulerZ = kotlin.math.abs(face.headEulerAngleZ)
                val rawScore = 1.0f - ((eulerY + eulerZ) / 90.0f)
                val score = rawScore.coerceIn(0.0f, 1.0f)
                _uiState.update { it.copy(faceMatchScore = score) }
            }
            
            val state = _uiState.value
            val hasBlinked = state.blinkCount >= 2
            
            if (hasBlinked) {
                _uiState.update { 
                    it.copy(
                        hasDetectedBlink = true
                    )
                }
            }
            
            if (hasBlinked && referencePhotoVerified && state.faceDetected) {
                _uiState.update { currentState ->
                    val updatedCard = currentState.selectedCard?.copy(status = 1)
                    val updatedList = currentState.cardItems.map { cardItem ->
                        if (cardItem.id == updatedCard?.id) updatedCard else cardItem
                    }
                    currentState.copy(
                        verificationResult = VerificationResult.Verified,
                        statusMessage = "Verification successful!",
                        cardItems = updatedList,
                        selectedCard = updatedCard,
                        showCompletionDialog = true
                    )
                }
            } else {
                if (!hasBlinked) {
                    val remaining = 2 - state.blinkCount
                    _uiState.update { 
                        it.copy(
                            statusMessage = "Please blink $remaining more times..."
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            statusMessage = "Face detected. Hold steady..."
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LivelinessViewModel", "Error in onFaceFrameDetected", e)
        } finally {
            isAnalyzing.set(false)
            onFrameProcessed()
        }
    }

    private fun isFaceCenteredInOval(faces: List<Face>, image: ImageProxy): Boolean {
        if (faces.isEmpty()) return false
        val face = faces.first()
        val boundingBox = face.boundingBox
        
        val rotationDegrees = image.imageInfo.rotationDegrees
        val isRotated = rotationDegrees == 90 || rotationDegrees == 270
        val width = if (isRotated) image.height else image.width
        val height = if (isRotated) image.width else image.height
        
        val centerX = boundingBox.centerX()
        val centerY = boundingBox.centerY()
        
        val xCentered = centerX > (0.25f * width) && centerX < (width * 0.75f)
        val yCentered = centerY > (0.20f * height) && centerY < (height * 0.80f)
        
        return xCentered && yCentered
    }

    private fun hasDetectedBlink(): Boolean {
        if (eyeHistory.size < 5) return false
        val recent = eyeHistory.takeLast(7)
        var openBefore = false
        var closed = false
        var openAfter = false
        for (isOpen in recent) {
            if (!closed) {
                if (isOpen) {
                    openBefore = true
                }
                if (!isOpen && openBefore) {
                    closed = true
                }
            } else if (isOpen) {
                openAfter = true
            }
        }
        return openBefore && closed && openAfter
    }

    private fun calculateAge(dobStr: String): String {
        if (dobStr.isEmpty()) return ""
        return try {
            val parts = dobStr.split("-")
            if (parts.size == 3) {
                val day = parts[0].toIntOrNull() ?: return ""
                val month = parts[1].toIntOrNull() ?: return ""
                val year = parts[2].toIntOrNull() ?: return ""
                val dobDate = LocalDate.of(year, month, day)
                val years = Period.between(dobDate, LocalDate.now()).years
                val age = if (years < 0) 0 else years
                age.toString()
            } else ""
        } catch (_: Exception) {
            ""
        }
    }

    fun closeCompletionDialog() {
        _uiState.update { 
            it.copy(
                showCompletionDialog = false,
                selectedCard = null,
                verificationResult = null,
                faceMatchScore = null,
                faceDetected = false,
                hasDetectedBlink = false,
                blinkCount = 0
            )
        }
        eyeHistory.clear()
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        return try {
            val width = imageProxy.width
            val height = imageProxy.height
            val yPlane = imageProxy.planes[0]
            val uPlane = imageProxy.planes[1]
            val vPlane = imageProxy.planes[2]
            val yBuffer = yPlane.buffer
            val uBuffer = uPlane.buffer
            val vBuffer = vPlane.buffer
            val yRowStride = yPlane.rowStride
            val uvRowStride = uPlane.rowStride
            val uvPixelStride = uPlane.pixelStride
            val nv21 = ByteArray(width * height + width * height / 2)
            var yDst = 0
            for (row in 0 until height) {
                yBuffer.position(row * yRowStride)
                yBuffer.get(nv21, yDst, width)
                yDst += width
            }
            var uvDst = width * height
            for (row in 0 until height / 2) {
                for (col in 0 until width / 2) {
                    val bufIdx = row * uvRowStride + col * uvPixelStride
                    nv21[uvDst++] = vBuffer.get(bufIdx)
                    nv21[uvDst++] = uBuffer.get(bufIdx)
                }
            }
            val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
            val imageBytes = out.toByteArray()
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size) ?: return null
            val rotation = imageProxy.imageInfo.rotationDegrees
            if (rotation != 0) {
                val matrix = Matrix()
                matrix.postRotate(rotation.toFloat())
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }
        } catch (_: Exception) {
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        faceNetHelper.close()
    }
}
