package com.rodolfo.itaxcix.services.camera

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor

class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var currentAnalyzer: ImageAnalysis.Analyzer? = null
    private var imageCapture: ImageCapture? = null

    fun startCamera(
        cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
        imageAnalyzer: ImageAnalysis.Analyzer? = null
    ) {
        this.currentAnalyzer = imageAnalyzer

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // Configurar analizador de imágenes si se proporciona
            this.imageAnalyzer = if (imageAnalyzer != null) {
                ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(ContextCompat.getMainExecutor(context), imageAnalyzer) }
            } else null

            // Configurar la captura de imágenes
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                cameraProvider?.unbindAll()

                val useCases = mutableListOf<UseCase>(preview)
                this.imageAnalyzer?.let { useCases.add(it) }
                imageCapture?.let { useCases.add(it) }

                camera = cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    *useCases.toTypedArray()
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun takePhoto(
        executor: Executor,
        onPhotoTaken: (File) -> Unit,
        onError: (ImageCaptureException) -> Unit
    ) {
        imageCapture?.let { imageCapture ->
            // Crear un archivo temporal para guardar la foto
            val photoFile = File(
                context.cacheDir,
                SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                    .format(System.currentTimeMillis()) + ".jpg"
            )

            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture.takePicture(
                outputOptions,
                executor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        onPhotoTaken(photoFile)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        onError(exception)
                    }
                }
            )
        }
    }

    fun switchCamera() {
        val currentCameraSelector = if (camera?.cameraInfo?.lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        startCamera(currentCameraSelector, currentAnalyzer)
    }

    fun stopCamera() {
        cameraProvider?.unbindAll()
    }
}