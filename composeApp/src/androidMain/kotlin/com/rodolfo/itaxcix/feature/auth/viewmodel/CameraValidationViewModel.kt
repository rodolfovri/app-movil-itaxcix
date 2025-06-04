package com.rodolfo.itaxcix.feature.auth.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.face.Face
import com.rodolfo.itaxcix.data.remote.dto.ValidateBiometricRequestDTO
import com.rodolfo.itaxcix.domain.model.ValidateBiometricResult
import com.rodolfo.itaxcix.domain.repository.UserRepository
import com.rodolfo.itaxcix.services.camera.analyzers.FaceDetectionAnalyzer
import com.rodolfo.itaxcix.utils.ImageUtils.compressAndConvertToBase64
import com.rodolfo.itaxcix.utils.ImageUtils.toBase64String
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CameraValidationViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    // Estado de validación
    private val _validationState = MutableStateFlow<ValidationState>(ValidationState.Waiting)
    val validationState: StateFlow<ValidationState> = _validationState.asStateFlow()

    // Progreso de validación
    private val _validationProgress = MutableStateFlow(0f)
    val validationProgress: StateFlow<Float> = _validationProgress.asStateFlow()

    // Estado de detección de rostro
    private val _faceDetected = MutableStateFlow(false)
    val faceDetected: StateFlow<Boolean> = _faceDetected.asStateFlow()

    private val _document = MutableStateFlow("")
    val document: StateFlow<String> = _document.asStateFlow()

    // ID de persona recibido de la validación de documento
    private val _personId = MutableStateFlow<Int?>(null)
    val personId: StateFlow<Int?> = _personId.asStateFlow()

    // Contador de frames con detección correcta
    private var successfulFrames = 0
    private val requiredFrames = 30 // Frames necesarios para validación completa

    // Establecer datos del documento
    fun setDocumentData(personId: Int?) {
        _personId.value = personId
    }

    // Método llamado cuando se detecta un rostro
    fun onFaceDetected(detected: Boolean) {
        // Solo procesar si NO estamos en validación biométrica
        if (_validationState.value !is ValidationState.BiometricValidating &&
            _validationState.value !is ValidationState.BiometricSuccess &&
            _validationState.value !is ValidationState.BiometricError) {

            _faceDetected.value = detected

            if (detected && _validationState.value == ValidationState.Waiting) {
                // Iniciar proceso de validación cuando se detecta un rostro
                _validationState.value = ValidationState.Validating
            }
        }
    }

    // Método para procesar el análisis facial completo
    fun processFaceAnalysis(faces: List<Face>, validationState: FaceDetectionAnalyzer.FaceValidationState) {
        // Solo procesar si NO estamos en validación biométrica
        if (_validationState.value is ValidationState.BiometricValidating ||
            _validationState.value is ValidationState.BiometricSuccess ||
            _validationState.value is ValidationState.BiometricError) {
            return
        }

        // Si no hay rostros o no estamos en estado de validación, reiniciar
        if (faces.isEmpty() || _validationState.value != ValidationState.Validating) {
            resetValidation()
            return
        }

        // Evaluar calidad de la detección
        val qualityCheck = validationState.isValidPose &&
                validationState.eyesOpen &&
                validationState.allLandmarksDetected

        if (qualityCheck) {
            // Incrementar contador de frames exitosos
            successfulFrames++

            // Actualizar progreso
            _validationProgress.value = successfulFrames.toFloat() / requiredFrames.toFloat()

            // Verificar si se completó la validación
            if (successfulFrames >= requiredFrames) {
                _validationState.value = ValidationState.Success
            }
        } else {
            // Reducir contador si la calidad no es buena (con límite inferior)
            if (successfulFrames > 0) successfulFrames--
            _validationProgress.value = successfulFrames.toFloat() / requiredFrames.toFloat()
        }
    }

    // Validar la imagen biométrica con el servidor
    fun validateBiometricImage(photoFile: File?) {
        if (photoFile == null) {
            _validationState.value = ValidationState.BiometricError("No se pudo obtener la imagen")
            Log.d("CameraValidationViewModel", "validateBiometricImage: photoFile is null")
            return
        }

        _validationState.value = ValidationState.BiometricValidating

        viewModelScope.launch {
            try {

                val compressedBase64 = photoFile.compressAndConvertToBase64(500, 60)
                val formattedBase64 = "data:image/jpg;base64,$compressedBase64"

                Log.d("CameraValidationViewModel", "validateBiometricImage: base64Image length = ${compressedBase64.length}")
                Log.d("CameraValidationViewModel", "validateBiometricImage: formattedBase64 length = ${formattedBase64.length}")
                Log.d("CameraValidationViewModel", "validateBiometricImage: personId = ${_personId.value}")

                val request = ValidateBiometricRequestDTO(
                    personId = _personId.value ?: 0,
                    imageBase64 = formattedBase64
                )

                val response = userRepository.validateBiometric(request)
                _validationState.value = ValidationState.BiometricSuccess(response)
            } catch (e: Exception) {
                _validationState.value = ValidationState.BiometricError(
                    e.message ?: "Error en la validación biométrica"
                )
            }
        }
    }

    fun onSuccessShown() {
        // Reiniciar el estado de validación después de mostrar el éxito
        resetValidationManually()
    }

    fun onErrorShown() {
        // Reiniciar el estado de validación después de mostrar el error
        resetValidationManually()
    }

    // Añadir esta función al final del CameraValidationViewModel
    fun resetValidationManually() {
        successfulFrames = 0
        _validationProgress.value = 0f
        _validationState.value = ValidationState.Waiting
        _faceDetected.value = false
    }

    // Reiniciar el proceso de validación
    private fun resetValidation() {
        if (_validationState.value != ValidationState.Success) {
            successfulFrames = 0
            _validationProgress.value = 0f
            _validationState.value = ValidationState.Waiting
        }
    }

    // Estados posibles durante la validación
    sealed class ValidationState {
        object Waiting : ValidationState()
        object Validating : ValidationState()
        object Success : ValidationState()
        object BiometricValidating : ValidationState()
        data class BiometricSuccess(val result: ValidateBiometricResult) : ValidationState()
        data class BiometricError(val message: String) : ValidationState()
    }
}