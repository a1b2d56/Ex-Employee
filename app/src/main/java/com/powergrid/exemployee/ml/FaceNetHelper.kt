package com.powergrid.exemployee.ml

import androidx.core.graphics.scale

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorOperator
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.support.tensorbuffer.TensorBufferFloat
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * FaceNet model wrapper for face recognition
 * Uses facenet.tflite model to generate face embeddings
 */
class FaceNetHelper(context: Context) {
    
    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null
    private val inputSize = 160 // FaceNet input size
    private val embeddingSize = 128 // FaceNet embedding size (model output)
    private val lock = Any()
    private var isClosed = false
    
    // Image Processor for standardization (Whitening)
    private val imageTensorProcessor = ImageProcessor.Builder()
        .add(ResizeOp(inputSize, inputSize, ResizeOp.ResizeMethod.BILINEAR))
        .add(StandardizeOp())
        .build()
    
    companion object {
        private const val TAG = "FaceNetHelper"
    }
    
    init {
        try {
            val model = FileUtil.loadMappedFile(context, "facenet.tflite")
            
            val interpreterOptions = Interpreter.Options()
            gpuDelegate = null
            
            interpreterOptions.numThreads = 4
            interpreterOptions.setUseXNNPACK(true)
            
            try {
                interpreter = Interpreter(model, interpreterOptions)
            } catch (e: Exception) {
                val cpuOptions = Interpreter.Options().apply {
                    numThreads = 4
                    setUseXNNPACK(false)
                }
                interpreter = Interpreter(model, cpuOptions)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load FaceNet model", e)
        }
    }
    
    fun getFaceEmbedding(faceBitmap: Bitmap): FloatArray? {
        synchronized(lock) {
            if (isClosed || interpreter == null) {
                Log.e(TAG, "Interpreter is closed or null")
                return null
            }
            
            try {
                val tensorImage = TensorImage.fromBitmap(faceBitmap)
                val processedImageBuffer = imageTensorProcessor.process(tensorImage).buffer
                
                val outputBuffer = Array(1) { FloatArray(embeddingSize) }
                
                interpreter?.run(processedImageBuffer, outputBuffer)
                
                return outputBuffer[0]
            } catch (e: Exception) {
                Log.e(TAG, "Error extracting face embedding", e)
                return null
            }
        }
    }
    
    fun compareFaces(embedding1: FloatArray, embedding2: FloatArray): Float {
        if (embedding1.size != embedding2.size) {
            Log.e(TAG, "Embedding size mismatch: ${embedding1.size} vs ${embedding2.size}")
            return 0f
        }
        
        var dotProduct = 0f
        var norm1 = 0f
        var norm2 = 0f
        
        for (i in embedding1.indices) {
            dotProduct += embedding1[i] * embedding2[i]
            norm1 += embedding1[i] * embedding1[i]
            norm2 += embedding2[i] * embedding2[i]
        }
        
        val norm1Sqrt = sqrt(norm1)
        val norm2Sqrt = sqrt(norm2)
        
        if (norm1Sqrt == 0f || norm2Sqrt == 0f) {
            Log.e(TAG, "Zero norm detected in embeddings")
            return 0f
        }
        
        val cosineSimilarity = dotProduct / (norm1Sqrt * norm2Sqrt)
        
        

        val normalizedScore = (cosineSimilarity + 1f) / 2f
        
        return normalizedScore
    }
    
    fun extractFace(fullBitmap: Bitmap, faceBounds: Rect): Bitmap {
        val padding = 0
        
        val left = (faceBounds.left - padding).coerceAtLeast(0)
        val top = (faceBounds.top - padding).coerceAtLeast(0)
        val right = (faceBounds.right + padding).coerceAtMost(fullBitmap.width)
        val bottom = (faceBounds.bottom + padding).coerceAtMost(fullBitmap.height)
        
        val width = right - left
        val height = bottom - top
        
        if (width <= 0 || height <= 0) {
            Log.w(TAG, "Invalid face bounds, using scaled full image")
            return fullBitmap.scale(inputSize, inputSize, true)
        }
        
        return Bitmap.createBitmap(fullBitmap, left, top, width, height)
    }
    
    fun close() {
        synchronized(lock) {
            if (!isClosed) {
                Log.d(TAG, "Closing FaceNet helper")
                interpreter?.close()
                gpuDelegate?.close()
                interpreter = null
                gpuDelegate = null
                isClosed = true
            }
        }
    }
    
    class StandardizeOp : TensorOperator {
        override fun apply(p0: TensorBuffer?): TensorBuffer {
            val pixels = p0!!.floatArray
            val mean = pixels.average().toFloat()
            var std = sqrt(pixels.map { pi -> (pi - mean).pow(2) }.sum() / pixels.size.toFloat())
            std = max(std, 1f / sqrt(pixels.size.toFloat()))
            for (i in pixels.indices) {
                pixels[i] = (pixels[i] - mean) / std
            }
            val output = TensorBufferFloat.createFixedSize(p0.shape, DataType.FLOAT32)
            output.loadArray(pixels)
            return output
        }
    }
}
