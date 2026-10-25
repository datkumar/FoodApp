package com.example.tech.foodapp.ui.state

import android.content.Context
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

private const val CameraVMTag = "CAMERA_VM"

class CameraViewModel : ViewModel() {
    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest

    private var lensFacing = CameraSelector.LENS_FACING_FRONT

    private val squareResolutionSelector = ResolutionSelector.Builder().setResolutionStrategy(
        ResolutionStrategy(
            Size(1080, 1080),
            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
        )
    ).build()

    private val cameraPreviewUseCase = Preview.Builder()
        .setResolutionSelector(squareResolutionSelector)
        .build()
        .apply {
            setSurfaceProvider { newSurfaceRequest ->
                _surfaceRequest.update { newSurfaceRequest }
            }
        }

    private val imageCaptureUseCase = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .setResolutionSelector(squareResolutionSelector)
        .build()

    suspend fun bindToCamera(
        appContext: Context,
        lifecycleOwner: LifecycleOwner,
        startAsFront: Boolean
    ) {
        lensFacing = if (startAsFront) CameraSelector.LENS_FACING_FRONT
        else CameraSelector.LENS_FACING_BACK

        val processCameraProvider = ProcessCameraProvider.awaitInstance(appContext)
        rebind(processCameraProvider, lifecycleOwner)

        try {
            awaitCancellation()
        } finally {
            processCameraProvider.unbindAll()
        }
    }

    private fun rebind(provider: ProcessCameraProvider, lifecycleOwner: LifecycleOwner) {
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        try {
            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                cameraPreviewUseCase,
                imageCaptureUseCase
            )
        } catch (e: Exception) {
            Log.e(CameraVMTag, "Binding failed", e)
        }
    }

    fun flipCamera(appContext: Context, lifecycleOwner: LifecycleOwner) {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        viewModelScope.launch {
            val provider = ProcessCameraProvider.awaitInstance(appContext)
            rebind(provider, lifecycleOwner)
        }
    }

    fun capturePhoto(context: Context, onPhotoCaptured: (File) -> Unit) {
        val tempFile = File(context.cacheDir, "temp_pfp_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()
        imageCaptureUseCase.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    onPhotoCaptured(tempFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(context, "Capture Failed", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}