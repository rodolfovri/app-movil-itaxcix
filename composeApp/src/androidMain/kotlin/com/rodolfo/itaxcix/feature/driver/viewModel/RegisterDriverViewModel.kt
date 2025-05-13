package com.rodolfo.itaxcix.feature.driver.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodolfo.itaxcix.data.remote.dto.DriverRegisterRequestDTO
import com.rodolfo.itaxcix.domain.model.RegisterDriverResult
import com.rodolfo.itaxcix.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterDriverViewModel(
    private val userRepository: UserRepository,
): ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Initial)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    // Datos del formulario
    private val _documentTypeId = MutableStateFlow(0)
    private val _document = MutableStateFlow("")
    private val _alias = MutableStateFlow("")
    private val _password = MutableStateFlow("")
    private val _contactTypeId = MutableStateFlow(1)
    private val _contact = MutableStateFlow("")
    private val _licensePlate = MutableStateFlow("")

    // Exponer los estados como StateFlow
    val documentTypeId: StateFlow<Int> = _documentTypeId
    val document: StateFlow<String> = _document
    val alias: StateFlow<String> = _alias
    val password: StateFlow<String> = _password
    val contactTypeId: StateFlow<Int> = _contactTypeId
    val contact: StateFlow<String> = _contact
    val licensePlate: StateFlow<String> = _licensePlate

    // Estados para validación de campos
    private val _aliasError = MutableStateFlow<String?>(null)
    private val _passwordError = MutableStateFlow<String?>(null)
    private val _contactError = MutableStateFlow<String?>(null)
    private val _licensePlateError = MutableStateFlow<String?>(null)

    // Exponer los estados de error como StateFlow
    val aliasError: StateFlow<String?> = _aliasError.asStateFlow()
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()
    val contactError: StateFlow<String?> = _contactError.asStateFlow()
    val licensePlateError: StateFlow<String?> = _licensePlateError.asStateFlow()

    // Método para actualizar los campos
    fun updateDocumentTypeId(value: Int) {
        _documentTypeId.value = value
        resetStateIfError()
    }

    fun updateDocument(value: String) {
        _document.value = value
        resetStateIfError()
    }

    fun updateAlias(value: String) {
        _alias.value = value
        resetStateIfError()
    }

    fun updatePassword(value: String) {
        _password.value = value
        resetStateIfError()
    }

    fun updateContactTypeId(value: Int) {
        _contactTypeId.value = value
        resetStateIfError()
    }

    fun updateContact(value: String) {
        _contact.value = value
        resetStateIfError()
    }

    fun updateLicensePlate(value: String) {
        _licensePlate.value = value
        resetStateIfError()
    }

    // Método para resetear el estado si hay un error activo
    private fun resetStateIfError() {
        if (_registerState.value is RegisterState.Error) {
            _registerState.value = RegisterState.Initial
        }
    }

    // Método para validar campos antes de registrar
    private fun validateFields(): Pair<Boolean, String?> {
        val errorMessages = mutableListOf<String>()
        var isValid = true

        if (_alias.value.isBlank()) {
            _aliasError.value = "Alias no puede estar vacío"
            errorMessages.add("• Alias no puede estar vacío")
            isValid = false
        } else if (_alias.value.length < 3) {
            _aliasError.value = "El alias debe tener al menos 3 caracteres"
            errorMessages.add("• El alias debe tener al menos 3 caracteres")
            isValid = false
        } else if (_alias.value.length > 20) {
            _aliasError.value = "El alias no puede tener más de 20 caracteres"
            errorMessages.add("• El alias no puede tener más de 20 caracteres")
            isValid = false
        } else {
            _aliasError.value = null
        }

        if (_password.value.isBlank()) {
            _passwordError.value = "La contraseña no puede estar vacía"
            errorMessages.add("• La contraseña no puede estar vacía")
            isValid = false
        } else if (_password.value.length < 8) {
            _passwordError.value = "La contraseña debe tener al menos 8 caracteres"
            errorMessages.add("• La contraseña debe tener al menos 8 caracteres")
            isValid = false
        } else if (!_password.value.contains(Regex("[A-Z]"))) {
            _passwordError.value = "La contraseña debe contener al menos una letra mayúscula"
            errorMessages.add("• La contraseña debe contener al menos una letra mayúscula")
            isValid = false
        } else if (!_password.value.contains(Regex("[a-z]"))) {
            _passwordError.value = "La contraseña debe contener al menos una letra minúscula"
            errorMessages.add("• La contraseña debe contener al menos una letra minúscula")
            isValid = false
        } else if (!_password.value.contains(Regex("[0-9]"))) {
            _passwordError.value = "La contraseña debe contener al menos un número"
            errorMessages.add("• La contraseña debe contener al menos un número")
            isValid = false
        } else if (!_password.value.contains(Regex("[^A-Za-z0-9]"))) {
            _passwordError.value = "La contraseña debe contener al menos un carácter especial (@, #, etc.)"
            errorMessages.add("• La contraseña debe contener al menos un carácter especial (@, #, etc.)")
            isValid = false
        } else {
            _passwordError.value = null
        }

        if (_contact.value.isBlank()) {
            _contactError.value = "El contacto no puede estar vacío"
            errorMessages.add("• El contacto no puede estar vacío")
            isValid = false
        } else if (_contactTypeId.value == 1 && !_contact.value.contains("@")) {
            _contactError.value = "El contacto debe ser un correo electrónico válido"
            errorMessages.add("• El contacto debe ser un correo electrónico válido")
            isValid = false
        } else if (_contactTypeId.value == 2 && !_contact.value.matches(Regex("^[0-9]{9}$"))) {
            _contactError.value = "El contacto debe ser un número de teléfono válido"
            errorMessages.add("• El contacto debe ser un número de teléfono válido")
            isValid = false
        } else {
            _contactError.value = null
        }

        if (_licensePlate.value.isBlank()) {
            _licensePlateError.value = "La placa no puede estar vacía"
            errorMessages.add("• La placa no puede estar vacía")
            isValid = false
        } else if (!_licensePlate.value.matches(Regex("^[A-Z0-9]{6,7}$"))) {
            _licensePlateError.value = "Formato de placa inválido"
            errorMessages.add("• Formato de placa inválido")
            isValid = false
        } else {
            _licensePlateError.value = null
        }

        return Pair(isValid, if (errorMessages.isNotEmpty()) errorMessages.joinToString("\n") else null)
    }

    fun registerDriver() {
        _registerState.value = RegisterState.Initial

        val (isValid, errorMessage) = validateFields()
        if (!isValid) {
            _registerState.value = RegisterState.Error(errorMessage ?: "Por favor, corrige los errores antes de continuar.")
            return
        }

        viewModelScope.launch {
            try {
                _registerState.value = RegisterState.Loading

                val contactValue = if(_contactTypeId.value == 2 && !_contact.value.startsWith("+51")) {
                    "+51${_contact.value}"
                } else {
                    _contact.value
                }

                val request = DriverRegisterRequestDTO(
                    documentTypeId = _documentTypeId.value,
                    document = _document.value,
                    alias = _alias.value,
                    password = _password.value,
                    contactTypeId = _contactTypeId.value,
                    contact = contactValue,
                    licensePlate = _licensePlate.value
                )

                val user = userRepository.registerDriver(request)
                _registerState.value = RegisterState.Success(user)
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // Método para consumir eventos una vez procesados
    fun onErrorShown() {
        if (_registerState.value is RegisterState.Error) {
            _registerState.value = RegisterState.Initial
        }
    }

    // Método para consumir eventos de éxito
    fun onSuccessShown() {
        if (_registerState.value is RegisterState.Success) {
            _registerState.value = RegisterState.Initial
        }
    }

    sealed class RegisterState {
        object Initial : RegisterState()
        object Loading : RegisterState()
        data class Success(val user: RegisterDriverResult) : RegisterState()
        data class Error(val message: String) : RegisterState()
        data class ValidationError(val message: String) : RegisterState()
    }
}