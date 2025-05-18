package com.rodolfo.itaxcix.feature.driver.viewModel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class RegisterValidationViewModel @Inject constructor() : ViewModel() {

    // Estados para el formulario
    private val _documentTypeId = MutableStateFlow(1) // DNI por defecto
    private val _document = MutableStateFlow("")
    private val _plate = MutableStateFlow("")
    private val _documentError = MutableStateFlow<String?>(null)
    private val _plateError = MutableStateFlow<String?>(null)
    private val _validationState = MutableStateFlow<ValidationState>(ValidationState.Initial)

    // Exponer estados
    val documentTypeId: StateFlow<Int> = _documentTypeId
    val document: StateFlow<String> = _document
    val plate: StateFlow<String> = _plate
    val plateError: StateFlow<String?> = _plateError
    val documentError: StateFlow<String?> = _documentError
    val validationState: StateFlow<ValidationState> = _validationState

    // Actualizar tipo de documento según selección
    fun updateDocumentType(selectedOption: String) {
        _documentTypeId.value = when(selectedOption) {
            "DNI" -> 1
            "Pasaporte" -> 2
            "Carnet de Extranjería" -> 3
            "RUC" -> 4
            else -> 1 // DNI por defecto
        }
        validateFields() // Revalidar al cambiar el tipo
    }

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
        } else {
            when (_documentTypeId.value) {
                1 -> { // DNI
                    if (!_document.value.matches(Regex("^[0-9]{8}$"))) {
                        _documentError.value = "El DNI debe tener 8 dígitos numéricos"
                        errorMessages.add("• El DNI debe tener 8 dígitos numéricos")
                        isValid = false
                    } else {
                        _documentError.value = null
                    }
                }
                2 -> { // Pasaporte
                    if (!_document.value.matches(Regex("^[A-Z0-9]{6,12}$"))) {
                        _documentError.value = "El pasaporte debe tener entre 6 y 12 caracteres alfanuméricos"
                        errorMessages.add("• El pasaporte debe tener entre 6 y 12 caracteres alfanuméricos")
                        isValid = false
                    } else {
                        _documentError.value = null
                    }
                }
                3 -> { // Carnet de Extranjería
                    if (!_document.value.matches(Regex("^[0-9]{9}$"))) {
                        _documentError.value = "El carnet de extranjería debe tener 9 dígitos"
                        errorMessages.add("• El carnet de extranjería debe tener 9 dígitos")
                        isValid = false
                    } else {
                        _documentError.value = null
                    }
                }
                4 -> { // RUC
                    if (!_document.value.matches(Regex("^[0-9]{11}$"))) {
                        _documentError.value = "El RUC debe tener 11 dígitos numéricos"
                        errorMessages.add("• El RUC debe tener 11 dígitos numéricos")
                        isValid = false
                    } else {
                        _documentError.value = null
                    }
                }
            }
        }

        // Validación de la placa
        if (_plate.value.isBlank()) {
            _plateError.value = "La placa no puede estar vacía"
            errorMessages.add("• La placa no puede estar vacía")
            isValid = false
        } else if (!_plate.value.matches(Regex("^[A-Z0-9]{6,7}$"))) {
            _plateError.value = "Formato de placa inválido"
            errorMessages.add("• Formato de placa inválido")
            isValid = false
        } else {
            _plateError.value = null
        }

        return Pair(isValid, if (errorMessages.isNotEmpty()) errorMessages.joinToString("\n") else null)
    }

    // Validar usando el nuevo enfoque con mensajes agrupados
    fun validate() {
        val (isValid, errorMessage) = validateFields()
        if (isValid) {
            _validationState.value = ValidationState.Success(
                _documentTypeId.value,
                _document.value,
                _plate.value
            )
        } else {
            _validationState.value = ValidationState.Error(errorMessage ?: "Datos inválidos")
        }
    }

    // Navegación exitosa
    fun onSuccessNavigated() {
        _validationState.value = ValidationState.Initial
    }

    fun onErrorShown() {
        _validationState.value = ValidationState.Initial
    }

    // Estados para la pantalla
    sealed class ValidationState {
        object Initial : ValidationState()
        data class Success(val documentTypeId: Int, val document: String, val plate: String) : ValidationState()
        data class Error(val message: String) : ValidationState()
    }
}