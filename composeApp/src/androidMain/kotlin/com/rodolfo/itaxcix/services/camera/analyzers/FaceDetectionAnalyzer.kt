package com.rodolfo.itaxcix.services.camera.analyzers

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark

class FaceDetectionAnalyzer(
    private val onFacesDetected: (List<Face>, FaceValidationState) -> Unit
) : ImageAnalysis.Analyzer {

    // Aumentamos la precisión de detección con más opciones
    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.2f) // Detecta rostros que ocupen al menos 20% del marco
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .enableTracking() // Habilita seguimiento de ID para cada rostro
            .build()
    )

    private var lastProcessingTimestamp = 0L
    private val processingInterval = 100L // Procesar cada 100ms para mejorar rendimiento

    data class FaceValidationState(
        val isFrontFacing: Boolean = false,
        val eyesOpen: Boolean = false,
        val isSmiling: Boolean = false,
        val allLandmarksDetected: Boolean = false,
        val isValidPose: Boolean = false
    )

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()

        // Control de velocidad de procesamiento para evitar sobrecarga
        if (currentTimestamp - lastProcessingTimestamp < processingInterval) {
            imageProxy.close()
            return
        }
        lastProcessingTimestamp = currentTimestamp

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            faceDetector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        val primaryFace = faces[0]

                        // Evaluación de calidad del rostro
                        val validationState = evaluateFaceQuality(primaryFace)

                        // Pasar tanto los rostros como el estado de validación
                        onFacesDetected(faces, validationState)
                    } else {
                        onFacesDetected(emptyList(), FaceValidationState())
                    }
                }
                .addOnFailureListener { e ->
                    // Manejo de errores mejorado
                    onFacesDetected(emptyList(), FaceValidationState())
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun evaluateFaceQuality(face: Face): FaceValidationState {
        // Verificar si tenemos todos los puntos de referencia faciales clave
        val requiredLandmarks = listOf(
            FaceLandmark.LEFT_EYE,
            FaceLandmark.RIGHT_EYE,
            FaceLandmark.NOSE_BASE,
            FaceLandmark.MOUTH_LEFT,
            FaceLandmark.MOUTH_RIGHT
        )

        val allLandmarksDetected = requiredLandmarks.all { landmarkType ->
            face.getLandmark(landmarkType) != null
        }

        // Verificar orientación de la cara (pose)
        val headEulerY = face.headEulerAngleY // Eje Y (izquierda/derecha)
        val headEulerZ = face.headEulerAngleZ // Eje Z (inclinación)

        // La cara está mirando hacia adelante si los ángulos son cercanos a 0
        val isValidPose = Math.abs(headEulerY) < 15 && Math.abs(headEulerZ) < 10

        // Verificar sonrisa y ojos abiertos para pruebas de vivacidad
        val isSmiling = face.smilingProbability != null && face.smilingProbability!! > 0.8f

        val leftEyeOpen = face.leftEyeOpenProbability != null &&
                face.leftEyeOpenProbability!! > 0.9f
        val rightEyeOpen = face.rightEyeOpenProbability != null &&
                face.rightEyeOpenProbability!! > 0.9f
        val eyesOpen = leftEyeOpen && rightEyeOpen

        return FaceValidationState(
            isFrontFacing = true, // Asumimos cámara frontal por defecto
            eyesOpen = eyesOpen,
            isSmiling = isSmiling,
            allLandmarksDetected = allLandmarksDetected,
            isValidPose = isValidPose
        )
    }
}