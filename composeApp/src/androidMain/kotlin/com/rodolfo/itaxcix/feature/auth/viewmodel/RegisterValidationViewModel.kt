package com.rodolfo.itaxcix.feature.citizen.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RegisterValidationViewModel : ViewModel() {

    // Estados para el formulario
    private val _documentTypeId = MutableStateFlow(1) // DNI por defecto
    private val _document = MutableStateFlow("")
    private val _documentError = MutableStateFlow<String?>(null)
    private val _validationState = MutableStateFlow<ValidationState>(ValidationState.Initial)

    // Exponer estados
    val documentTypeId: StateFlow<Int> = _documentTypeId
    val document: StateFlow<String> = _document
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
        validateDocument() // Revalidar al cambiar el tipo
    }

    fun updateDocument(value: String) {
        _document.value = value
        validateDocument()
    }

    // Validación según tipo de documento
    private fun validateDocument(): Boolean {
        when (_documentTypeId.value) {
            1 -> { // DNI
                if (!_document.value.matches(Regex("^[0-9]{8}$"))) {
                    _documentError.value = "El DNI debe tener 8 dígitos numéricos"
                    return false
                }
            }
            2 -> { // Pasaporte
                if (!_document.value.matches(Regex("^[A-Z0-9]{6,12}$"))) {
                    _documentError.value = "El pasaporte debe tener entre 6 y 12 caracteres alfanuméricos"
                    return false
                }
            }
            3 -> { // Carnet de Extranjería
                if (!_document.value.matches(Regex("^[0-9]{9}$"))) {
                    _documentError.value = "El carnet de extranjería debe tener 9 dígitos"
                    return false
                }
            }
            4 -> { // RUC
                if (!_document.value.matches(Regex("^[0-9]{11}$"))) {
                    _documentError.value = "El RUC debe tener 11 dígitos numéricos"
                    return false
                }
            }
        }
        _documentError.value = null
        return true
    }

    // Validar y continuar
    fun validate() {
        if (validateDocument()) {
            _validationState.value = ValidationState.Success(_documentTypeId.value, _document.value)
        } else {
            _validationState.value = ValidationState.Error(_documentError.value ?: "Documento inválido")
        }
    }

    // Navegación exitosa
    fun onSuccessNavigated() {
        _validationState.value = ValidationState.Initial
    }


    // Estados para la pantalla
    sealed class ValidationState {
        object Initial : ValidationState()
        data class Success(val documentTypeId: Int, val document: String) : ValidationState()
        data class Error(val message: String) : ValidationState()
    }
}