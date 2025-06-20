package com.rodolfo.itaxcix.feature.driver.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodolfo.itaxcix.data.remote.dto.auth.ValidateVehicleRequestDTO
import com.rodolfo.itaxcix.domain.model.ValidateVehicleResult
import com.rodolfo.itaxcix.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterValidationViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    // Estados para el formulario
    private val _documentTypeId = MutableStateFlow(1) // DNI por defecto
    private val _document = MutableStateFlow("")
    private val _plate = MutableStateFlow("")
    private val _documentError = MutableStateFlow<String?>(null)
    private val _plateError = MutableStateFlow<String?>(null)
    private val _validationState = MutableStateFlow<ValidationState>(ValidationState.Initial)

    // Exponer estados
    val document: StateFlow<String> = _document
    val plate: StateFlow<String> = _plate
    val plateError: StateFlow<String?> = _plateError
    val documentError: StateFlow<String?> = _documentError
    val validationState: StateFlow<ValidationState> = _validationState

    fun updateDocument(value: String) {
        _document.value = value
        validateFields()
    }

    fun updatePlate(value: String) {
        _plate.value = value
        validateFields()
    }

    // Método de validación unificado que devuelve Pair<Boolean, String?>
    private fun validateFields(): Pair<Boolean, String?> {
        val errorMessages = mutableListOf<String>()
        var isValid = true

        // Validación del documento
        if (_document.value.isBlank()) {
            _documentError.value = "El documento no puede estar vacío"
            errorMessages.add("• El documento no puede estar vacío")
            isValid = false
        } else if (_document.value.contains(" ")) {
            _documentError.value = "El documento no puede contener espacios"
            errorMessages.add("• El documento no puede contener espacios")
            isValid = false
        } else if (!_document.value.matches(Regex("^[0-9]{8}$"))) {
            _documentError.value = "Formato inválido. El DNI debe tener exactamente 8 dígitos"
            errorMessages.add("• Formato inválido. El DNI debe tener exactamente 8 dígitos")
            isValid = false
        } else {
            _documentError.value = null
        }

        // Validación de la placa
        if (_plate.value.isBlank()) {
            _plateError.value = "La placa no puede estar vacía"
            errorMessages.add("• La placa no puede estar vacía")
            isValid = false
        } else if (!_plate.value.matches(Regex("^[A-Z0-9]{6}$"))) {
            _plateError.value = "Formato de placa inválido. Debe tener exactamente 6 caracteres alfanuméricos, ejemplo: ABC123"
            errorMessages.add("• Formato de placa inválido. Debe tener exactamente 6 caracteres alfanuméricos, ejemplo: ABC123")
            isValid = false
        } else {
            _plateError.value = null
        }

        return Pair(isValid, if (errorMessages.isNotEmpty()) errorMessages.joinToString("\n") else null)
    }

    // Validar usando el nuevo enfoque con mensajes agrupados
    fun validate() {
        _validationState.value = ValidationState.Initial

        val (isValid, errorMessage) = validateFields()
        if (!isValid) {
            _validationState.value = ValidationState.Error(errorMessage ?: "Por favor corrige los errores antes de continuar")
            return
        }

        viewModelScope.launch {
            try {
                _validationState.value = ValidationState.Loading

                val documentTypeId = 1
                val request = ValidateVehicleRequestDTO(
                    documentTypeId = documentTypeId,
                    documentValue = _document.value,
                    plateValue = _plate.value
                )

                val response = userRepository.validateVehicle(request)
                _validationState.value = ValidationState.Success(response)
            } catch (e: Exception) {
                _validationState.value =
                    ValidationState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // Navegación exitosa
    fun onSuccessNavigated() {
        if (_validationState.value is ValidationState.Success) {
            _validationState.value = ValidationState.Initial
        }
    }

    fun onErrorShown() {
        if (_validationState.value is ValidationState.Error) {
            _validationState.value = ValidationState.Initial
        }
    }

    // Estados para la pantalla
    sealed class ValidationState {
        object Initial : ValidationState()
        object Loading : ValidationState()
        data class Success(val vehicle: ValidateVehicleResult) : ValidationState()
        data class Error(val message: String) : ValidationState()
    }
}