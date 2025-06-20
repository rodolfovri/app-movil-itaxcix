package com.rodolfo.itaxcix.feature.citizen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodolfo.itaxcix.data.remote.dto.auth.ValidateDocumentRequestDTO
import com.rodolfo.itaxcix.domain.model.ValidateDocumentResult
import com.rodolfo.itaxcix.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterValidationViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    // Estados para el formulario
    private val _document = MutableStateFlow("")
    private val _documentError = MutableStateFlow<String?>(null)
    private val _validationState = MutableStateFlow<ValidationState>(ValidationState.Initial)

    // Exponer estados
    val document: StateFlow<String> = _document.asStateFlow()
    val documentError: StateFlow<String?> = _documentError.asStateFlow()
    val validationState: StateFlow<ValidationState> = _validationState.asStateFlow()

    fun updateDocument(value: String) {
        _document.value = value
        validateDocument()
    }

    // Validación según tipo de documento con mensajes agrupados
    private fun validateDocument(): Pair<Boolean, String?> {
        val errorMessages = mutableListOf<String>()
        var isValid = true

        if (_document.value.isBlank()) {
            _documentError.value = "El número de documento es obligatorio"
            errorMessages.add("• El número de documento es obligatorio")
            isValid = false
        } else if (_document.value.contains(" ")) {
            _documentError.value = "El documento no puede contener espacios"
            errorMessages.add("• El documento no puede contener espacios")
            isValid = false
        } else if (!_document.value.matches(Regex("^[0-9]{8}$"))) {
            _documentError.value = "El DNI debe tener 8 dígitos numéricos"
            errorMessages.add("• El DNI debe tener 8 dígitos numéricos")
            isValid = false
        } else {
            _documentError.value = null
        }

        return Pair(isValid, if (errorMessages.isNotEmpty()) errorMessages.joinToString("\n") else null)
    }

    // Validar y continuar usando el nuevo enfoque con mensajes agrupados
    fun validate() {
        _validationState.value = ValidationState.Initial

        val (isValid, errorMessage) = validateDocument()
        if (!isValid) {
            _validationState.value = ValidationState.Error(errorMessage ?: "Documento inválido")
            return
        }

        viewModelScope.launch {
            try {
                _validationState.value = ValidationState.Loading

                val documentTypeId = 1 // Asignar un ID de tipo de documento fijo
                val request = ValidateDocumentRequestDTO(
                    documentTypeId = documentTypeId,
                    documentValue = _document.value
                )

                val response = userRepository.validateDocument(request)
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
        data object Initial : ValidationState()
        data object Loading : ValidationState()
        data class Success(val document: ValidateDocumentResult) : ValidationState()
        data class Error(val message: String) : ValidationState()
    }
}