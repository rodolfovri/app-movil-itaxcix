package com.rodolfo.itaxcix.feature.driver.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodolfo.itaxcix.data.remote.dto.CitizenRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.DriverRegisterRequestDTO
import com.rodolfo.itaxcix.domain.model.RegisterDriverResult
import com.rodolfo.itaxcix.domain.model.RegisterResult
import com.rodolfo.itaxcix.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterDriverViewModel @Inject constructor(
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
    private val _personId = MutableStateFlow<Int?>(null)
    private val _vehicleId = MutableStateFlow<Int?>(null)

    // Exponer los estados como StateFlow
    val documentTypeId: StateFlow<Int> = _documentTypeId
    val document: StateFlow<String> = _document
    val password: StateFlow<String> = _password
    val contactTypeId: StateFlow<Int> = _contactTypeId
    val contact: StateFlow<String> = _contact
    val personId: StateFlow<Int?> = _personId
    val vehicleId: StateFlow<Int?> = _vehicleId

    // Estados para validación de campos
    private val _passwordError = MutableStateFlow<String?>(null)
    private val _contactError = MutableStateFlow<String?>(null)

    // Exponer los estados de error como StateFlow
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()
    val contactError: StateFlow<String?> = _contactError.asStateFlow()

    // Método para actualizar los campos
    fun updateDocumentTypeId(value: Int) {
        _documentTypeId.value = value
        resetStateIfError()
    }

    fun updateDocument(value: String) {
        _document.value = value
        resetStateIfError()
    }

    fun updatePassword(value: String) {
        _password.value = value
        validatePassword()
        resetStateIfError()
    }

    fun updateContactTypeId(value: Int) {
        _contactTypeId.value = value
        resetStateIfError()
    }

    fun updateContact(value: String) {
        _contact.value = value
        validateContact()
        resetStateIfError()
    }

    fun updatePersonId(value: Int?) {
        _personId.value = value
        resetStateIfError()
    }

    fun updateVehicleId(value: Int?) {
        _vehicleId.value = value
        resetStateIfError()
    }

    // Método para resetear el estado si hay un error activo
    private fun resetStateIfError() {
        if (_registerState.value is RegisterState.Error) {
            _registerState.value = RegisterState.Initial
        }
    }

    private fun validatePassword() {
        if (_password.value.isBlank()) {
            _passwordError.value = "La contraseña no puede estar vacía"
        } else if (_password.value.contains(" ")) {
            _passwordError.value = "La contraseña no puede contener espacios"
        } else if (_password.value.length < 8) {
            _passwordError.value = "La contraseña debe tener al menos 8 caracteres"
        } else if (!_password.value.contains(Regex("[A-Z]"))) {
            _passwordError.value = "La contraseña debe contener al menos una letra mayúscula"
        } else if (!_password.value.contains(Regex("[a-z]"))) {
            _passwordError.value = "La contraseña debe contener al menos una letra minúscula"
        } else if (!_password.value.contains(Regex("[0-9]"))) {
            _passwordError.value = "La contraseña debe contener al menos un número"
        } else if (!_password.value.contains(Regex("[^A-Za-z0-9]"))) {
            _passwordError.value = "La contraseña debe contener al menos un carácter especial"
        } else {
            _passwordError.value = null
        }
    }

    private fun validateContact() {
        if (_contact.value.isBlank()) {
            _contactError.value = "El contacto no puede estar vacío"
        } else if (_contact.value.contains(" ")) {
            _contactError.value = "El contacto no puede contener espacios"
        } else if (_contactTypeId.value == 1 &&
            !_contact.value.matches(Regex("^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$"))) {
            _contactError.value = "El contacto debe ser un correo electrónico válido"
        } else if (_contactTypeId.value == 2 && !_contact.value.matches(Regex("^[0-9]{9}$"))) {
            _contactError.value = "El contacto debe ser un número de teléfono válido"
        } else {
            _contactError.value = null
        }
    }

    // Método para validar campos antes de registrar
    private fun validateFields(): Pair<Boolean, String?> {
        val errorMessages = mutableListOf<String>()
        var isValid = true

        if (_password.value.isBlank()) {
            _passwordError.value = "La contraseña no puede estar vacía"
            errorMessages.add("• La contraseña no puede estar vacía")
            isValid = false
        } else if (_password.value.contains(" ")) {
            _passwordError.value = "La contraseña no puede contener espacios"
            errorMessages.add("• La contraseña no puede contener espacios")
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
        } else if (_contact.value.contains(" ")) {
            _contactError.value = "El contacto no puede contener espacios"
            errorMessages.add("• El contacto no puede contener espacios")
            isValid = false
        } else if (_contactTypeId.value == 1 &&
            !_contact.value.matches(Regex("^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$"))) {
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

                val request = CitizenRegisterRequestDTO(
                    password = _password.value,
                    contactTypeId = _contactTypeId.value,
                    contactValue = contactValue,
                    personId = _personId.value ?: 0,
                    vehicleId = _vehicleId.value ?: 0,
                )

                val response = userRepository.registerCitizen(request)
                _registerState.value = RegisterState.Success(response)
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
        data class Success(val user: RegisterResult) : RegisterState()
        data class Error(val message: String) : RegisterState()
    }
}