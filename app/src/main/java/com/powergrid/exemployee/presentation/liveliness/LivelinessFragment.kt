package com.powergrid.exemployee.presentation.liveliness

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.powergrid.exemployee.common.*
import com.powergrid.exemployee.databinding.FragmentLivelinessBinding
import com.powergrid.exemployee.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class LivelinessFragment : BaseFragment() {
    private var _b: FragmentLivelinessBinding? = null
    private val b get() = _b!!
    private val vm: LivelinessViewModel by viewModels()

    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraExecutor: ExecutorService
    private var isCameraRunning = false

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) startCamera() else toast("Camera permission required")
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentLivelinessBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)
        cameraExecutor = Executors.newSingleThreadExecutor()
        val token = (requireActivity() as MainActivity).authToken

        b.btnCapturePhoto.setOnClickListener {
            if (!isCameraRunning) {
                requestPermission.launch(Manifest.permission.CAMERA)
            } else {
                takePhoto()
            }
        }

        b.btnSubmitLiveliness.setOnClickListener { vm.submit(token) }

        collectFlow(vm.submitState) { state ->
            when (state) {
                is UiState.Loading -> { b.progress.visible(); b.btnSubmitLiveliness.isEnabled = false }
                is UiState.Success -> {
                    b.progress.gone(); b.btnSubmitLiveliness.isEnabled = true
                    Snackbar.make(b.root, state.data, Snackbar.LENGTH_LONG).show()
                    vm.reset()
                }
                is UiState.Error -> { b.progress.gone(); b.btnSubmitLiveliness.isEnabled = true; toast(state.message) }
                UiState.Idle     -> b.progress.gone()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(b.viewFinder.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                isCameraRunning = true
                b.viewFinder.visibility = View.VISIBLE
                b.ivCapturedPhoto.alpha = 0f
                b.tvCameraStatus.text = "Position your face in the frame"
                b.btnCapturePhoto.text = "Snap Photo"
            } catch(exc: Exception) {
                toast("Failed to start camera")
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        b.tvCameraStatus.text = "Capturing..."
        
        imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()), object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.capacity())
                buffer.get(bytes)
                val rawBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
                
                // Rotate to match device orientation for front camera
                val matrix = Matrix().apply { 
                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                    postScale(-1f, 1f, rawBitmap.width / 2f, rawBitmap.height / 2f) // Mirror front camera
                }
                val bitmap = Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
                
                b.ivCapturedPhoto.setImageBitmap(bitmap)
                b.ivCapturedPhoto.alpha = 1.0f
                b.viewFinder.visibility = View.INVISIBLE
                b.tvCameraStatus.text = "Photo captured successfully"
                b.btnCapturePhoto.text = "Retake Photo"
                vm.setCapturedPhoto(bitmap)
                
                isCameraRunning = false
                cameraProvider?.unbindAll()
                image.close()
            }

            override fun onError(exc: ImageCaptureException) {
                toast("Photo capture failed: ${exc.message}")
                b.tvCameraStatus.text = "Capture failed. Try again."
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _b = null
    }
}
