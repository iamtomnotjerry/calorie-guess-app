package com.example.caloriesense.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

@Composable
fun CameraScreen(
    onImageCaptured: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    // Sử dụng background executor để xử lý ảnh
    val cameraExecutor = remember { Dispatchers.Default.asExecutor() }

    LaunchedEffect(Unit) {
        val cameraProvider = context.getCameraProvider()
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (e: Exception) {
            Log.e("Camera", "Binding failed", e)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            onClick = {
                imageCapture.takePicture(
                    cameraExecutor,
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            // Xử lý ảnh ở background thread
                            val rotationDegrees = image.imageInfo.rotationDegrees
                            val bitmap = image.toBitmap().rotateBitmap(rotationDegrees)
                            image.close()
                            
                            // Trả kết quả về Main Thread để thực hiện Navigation
                            scope.launch {
                                withContext(Dispatchers.Main) {
                                    onImageCaptured(bitmap)
                                }
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("Camera", "Capture failed", exception)
                        }
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .size(80.dp)
                .background(Color.White.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = "Capture", modifier = Modifier.size(40.dp))
        }
    }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCancellableCoroutine { continuation ->
    val future = ProcessCameraProvider.getInstance(this)
    future.addListener({
        try {
            continuation.resume(future.get())
        } catch (e: Exception) {
            Log.e("Camera", "Error getting camera provider", e)
        }
    }, ContextCompat.getMainExecutor(this))
}

fun Bitmap.rotateBitmap(degrees: Int): Bitmap {
    if (degrees == 0) return this
    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
    val rotated = Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    if (rotated != this) {
        this.recycle() // Giải phóng bộ nhớ của bitmap cũ
    }
    return rotated
}
