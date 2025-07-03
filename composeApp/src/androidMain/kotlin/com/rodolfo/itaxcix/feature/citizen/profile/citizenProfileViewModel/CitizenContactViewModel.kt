package com.rodolfo.itaxcix.feature.citizen.profile.citizenProfileViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodolfo.itaxcix.data.local.PreferencesManager
import com.rodolfo.itaxcix.data.remote.dto.common.ChangeEmailRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.common.ChangePhoneRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.common.VerifyChangeEmailRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.common.VerifyChangePhoneRequestDTO
import com.rodolfo.itaxcix.domain.model.ChangeEmailResult
import com.rodolfo.itaxcix.domain.model.ChangePhoneResult
import com.rodolfo.itaxcix.domain.model.VerifyChangeEmailResult
import com.rodolfo.itaxcix.domain.model.VerifyChangePhoneResult
import com.rodolfo.itaxcix.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CitizenContactViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val userData = preferencesManager.userData

    // Estados del formulario
    private val _newEmail = MutableStateFlow("")
    private val _verificationCode = MutableStateFlow("")

    val newEmail: StateFlow<String> = _newEmail.asStateFlow()
    val verificationCode: StateFlow<String> = _verificationCode.asStateFlow()

    // Estados de validación
    private val _emailError = MutableStateFlow<String?>(null)
    private val _codeError = MutableStateFlow<String?>(null)

    val emailError: StateFlow<String?> = _emailError.asStateFlow()
    val codeError: StateFlow<String?> = _codeError.asStateFlow()

    // Estados de la operación
    private val _changeEmailState = MutableStateFlow<ChangeEmailState>(ChangeEmailState.Initial)
    val changeEmailState: StateFlow<ChangeEmailState> = _changeEmailState.asStateFlow()

    private val _verifyEmailState = MutableStateFlow<VerifyEmailState>(VerifyEmailState.Initial)
    val verifyEmailState: StateFlow<VerifyEmailState> = _verifyEmailState.asStateFlow()

    // Funciones para actualizar campos
    fun updateNewEmail(value: String) {
        _newEmail.value = value
        validateEmail()
        resetChangeEmailStateIfError()
    }

    fun updateVerificationCode(value: String) {
        _verificationCode.value = value
        validateCode()
        resetVerifyEmailStateIfError()
    }

    // Validaciones
    private fun validateEmail() {
        if (_newEmail.value.isBlank()) {
            _emailError.value = "El correo electrónico no puede estar vacío"
        } else if (_newEmail.value.contains(" ")) {
            _emailError.value = "El correo electrónico no puede contener espacios"
        } else if (!_newEmail.value.matches(Regex("^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$"))) {
            _emailError.value = "Ingresa un correo electrónico válido"
        } else {
            _emailError.value = null
        }
    }

    private fun validateCode() {
        if (_verificationCode.value.isBlank()) {
            _codeError.value = "El código de verificación no puede estar vacío"
        } else if (_verificationCode.value.length != 6) {
            _codeError.value = "El código debe tener 6 dígitos"
        } else if (!_verificationCode.value.matches(Regex("^[0-9]{6}$"))) {
            _codeError.value = "El código solo puede contener números"
        } else {
            _codeError.value = null
        }
    }

    // Resetear estados si hay error
    private fun resetChangeEmailStateIfError() {
        if (_changeEmailState.value is ChangeEmailState.Error) {
            _changeEmailState.value = ChangeEmailState.Initial
        }
    }

    private fun resetVerifyEmailStateIfError() {
        if (_verifyEmailState.value is VerifyEmailState.Error) {
            _verifyEmailState.value = VerifyEmailState.Initial
        }
    }

    // Validación completa para cambio de email
    private fun validateChangeEmailFields(): Pair<Boolean, String?> {
        val errorMessages = mutableListOf<String>()
        var isValid = true

        if (_newEmail.value.isBlank()) {
            _emailError.value = "El correo electrónico no puede estar vacío"
            errorMessages.add("• El correo electrónico no puede estar vacío")
            isValid = false
        } else if (_newEmail.value.contains(" ")) {
            _emailError.value = "El correo electrónico no puede contener espacios"
            errorMessages.add("• El correo electrónico no puede contener espacios")
            isValid = false
        } else if (!_newEmail.value.matches(Regex("^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$"))) {
            _emailError.value = "Ingresa un correo electrónico válido"
            errorMessages.add("• Ingresa un correo electrónico válido")
            isValid = false
        } else {
            _emailError.value = null
        }

        return Pair(isValid, if (errorMessages.isNotEmpty()) errorMessages.joinToString("\n") else null)
    }

    // Validación completa para verificación
    private fun validateVerifyFields(): Pair<Boolean, String?> {
        val errorMessages = mutableListOf<String>()
        var isValid = true

        if (_verificationCode.value.isBlank()) {
            _codeError.value = "El código de verificación no puede estar vacío"
            errorMessages.add("• El código de verificación no puede estar vacío")
            isValid = false
        } else if (_verificationCode.value.length != 6) {
            _codeError.value = "El código debe tener 6 dígitos"
            errorMessages.add("• El código debe tener 6 dígitos")
            isValid = false
        } else if (!_verificationCode.value.matches(Regex("^[0-9]{6}$"))) {
            _codeError.value = "El código solo puede contener números"
            errorMessages.add("• El código solo puede contener números")
            isValid = false
        } else {
            _codeError.value = null
        }

        return Pair(isValid, if (errorMessages.isNotEmpty()) errorMessages.joinToString("\n") else null)
    }

    // Función para solicitar cambio de email
    fun requestChangeEmail() {
        _changeEmailState.value = ChangeEmailState.Initial

        val (isValid, errorMessage) = validateChangeEmailFields()
        if (!isValid) {
            _changeEmailState.value = ChangeEmailState.Error(errorMessage ?: "Por favor, corrige los errores antes de continuar.")
            return
        }

        viewModelScope.launch {
            try {
                _changeEmailState.value = ChangeEmailState.Loading

                val userId = userData.value?.id ?: return@launch

                val request = ChangeEmailRequestDTO(
                    userId = userId,
                    email = _newEmail.value
                )

                val result = userRepository.changeEmail(request)
                _changeEmailState.value = ChangeEmailState.Success(result)
            } catch (e: Exception) {
                _changeEmailState.value = ChangeEmailState.Error(e.message ?: "Error desconocido")
                Log.d("CITIZEN_CONTACT_VM", "Error al solicitar cambio de email: ${e.message}")
            }
        }
    }

    // Función para verificar código
    fun verifyEmailChange() {
        _verifyEmailState.value = VerifyEmailState.Initial

        val (isValid, errorMessage) = validateVerifyFields()
        if (!isValid) {
            _verifyEmailState.value = VerifyEmailState.Error(errorMessage ?: "Por favor, corrige los errores antes de continuar.")
            return
        }

        viewModelScope.launch {
            try {
                _verifyEmailState.value = VerifyEmailState.Loading

                val userId = userData.value?.id ?: throw Exception("Usuario no encontrado")

                val request = VerifyChangeEmailRequestDTO(
                    userId = userId,
                    code = _verificationCode.value
                )

                val result = userRepository.verifyChangeEmail(request)
                _verifyEmailState.value = VerifyEmailState.Success(result)

                // Actualizar el email en las preferencias locales
                val currentUserData = userData.value
                currentUserData?.let {
                    val updatedUserData = it.copy(email = _newEmail.value)
                    preferencesManager.saveUserData(updatedUserData)
                }

            } catch (e: Exception) {
                _verifyEmailState.value = VerifyEmailState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // Métodos para consumir eventos
    fun onChangeEmailErrorShown() {
        if (_changeEmailState.value is ChangeEmailState.Error) {
            _changeEmailState.value = ChangeEmailState.Initial
        }
    }

    fun onChangeEmailSuccessShown() {
        if (_changeEmailState.value is ChangeEmailState.Success) {
            _changeEmailState.value = ChangeEmailState.Initial
        }
    }

    fun onVerifyEmailErrorShown() {
        if (_verifyEmailState.value is VerifyEmailState.Error) {
            _verifyEmailState.value = VerifyEmailState.Initial
        }
    }

    fun onVerifyEmailSuccessShown() {
        if (_verifyEmailState.value is VerifyEmailState.Success) {
            _verifyEmailState.value = VerifyEmailState.Initial
        }
    }

    // Estados sellados
    sealed class ChangeEmailState {
        data object Initial : ChangeEmailState()
        data object Loading : ChangeEmailState()
        data class Success(val result: ChangeEmailResult) : ChangeEmailState()
        data class Error(val message: String) : ChangeEmailState()
    }

    sealed class VerifyEmailState {
        data object Initial : VerifyEmailState()
        data object Loading : VerifyEmailState()
        data class Success(val result: VerifyChangeEmailResult) : VerifyEmailState()
        data class Error(val message: String) : VerifyEmailState()
    }

    // Función para solicitar cambio de teléfono
    fun requestChangePhone() {
        _changePhoneState.value = ChangePhoneState.Initial

        val (isValid, errorMessage) = validateChangePhoneFields()
        if (!isValid) {
            _changePhoneState.value = ChangePhoneState.Error(errorMessage ?: "Por favor, corrige los errores antes de continuar.")
            return
        }

        viewModelScope.launch {
            try {
                _changePhoneState.value = ChangePhoneState.Loading

                val userId = userData.value?.id ?: return@launch
                val phonePrefix = "+51${_newPhone.value}"

                val request = ChangePhoneRequestDTO(
                    userId = userId,
                    phone = phonePrefix
                )

                val result = userRepository.changePhone(request)
                _changePhoneState.value = ChangePhoneState.Success(result)
            } catch (e: Exception) {
                _changePhoneState.value = ChangePhoneState.Error(e.message ?: "Error desconocido")
            }
        }
    }


    // Estados del formulario para teléfono
    private val _newPhone = MutableStateFlow("")
    private val _verificationCodePhone = MutableStateFlow("")

    val newPhone: StateFlow<String> = _newPhone.asStateFlow()
    val verificationCodePhone: StateFlow<String> = _verificationCodePhone.asStateFlow()

    // Estados de validación para teléfono
    private val _phoneError = MutableStateFlow<String?>(null)
    private val _codePhoneError = MutableStateFlow<String?>(null)

    val phoneError: StateFlow<String?> = _phoneError.asStateFlow()
    val codePhoneError: StateFlow<String?> = _codePhoneError.asStateFlow()

    // Estados de la operación para teléfono
    private val _changePhoneState = MutableStateFlow<ChangePhoneState>(ChangePhoneState.Initial)
    val changePhoneState: StateFlow<ChangePhoneState> = _changePhoneState.asStateFlow()

    private val _verifyPhoneState = MutableStateFlow<VerifyPhoneState>(VerifyPhoneState.Initial)
    val verifyPhoneState: StateFlow<VerifyPhoneState> = _verifyPhoneState.asStateFlow()

    // Funciones para actualizar campos de teléfono
    fun updateNewPhone(value: String) {
        _newPhone.value = value
        validatePhone()
        resetChangePhoneStateIfError()
    }

    fun updateVerificationCodePhone(value: String) {
        _verificationCodePhone.value = value
        validateCodePhone()
        resetVerifyPhoneStateIfError()
    }

    // Validaciones para teléfono
    private fun validatePhone() {
        if (_newPhone.value.isBlank()) {
            _phoneError.value = "El número de teléfono no puede estar vacío"
        } else if (_newPhone.value.contains(" ")) {
            _phoneError.value = "El número de teléfono no puede contener espacios"
        } else if (!_newPhone.value.matches(Regex("^9[0-9]{8}$"))) {
            _phoneError.value = "Ingresa un número válido que inicie con 9 y tenga 9 dígitos"
        } else {
            _phoneError.value = null
        }
    }

    private fun validateCodePhone() {
        if (_verificationCodePhone.value.isBlank()) {
            _codePhoneError.value = "El código de verificación no puede estar vacío"
        } else if (_verificationCodePhone.value.length != 6) {
            _codePhoneError.value = "El código debe tener 6 dígitos"
        } else if (!_verificationCodePhone.value.matches(Regex("^[0-9]{6}$"))) {
            _codePhoneError.value = "El código solo puede contener números"
        } else {
            _codePhoneError.value = null
        }
    }

    // Resetear estados si hay error para teléfono
    private fun resetChangePhoneStateIfError() {
        if (_changePhoneState.value is ChangePhoneState.Error) {
            _changePhoneState.value = ChangePhoneState.Initial
        }
    }

    private fun resetVerifyPhoneStateIfError() {
        if (_verifyPhoneState.value is VerifyPhoneState.Error) {
            _verifyPhoneState.value = VerifyPhoneState.Initial
        }
    }

    // Validación completa para cambio de teléfono
    private fun validateChangePhoneFields(): Pair<Boolean, String?> {
        val errorMessages = mutableListOf<String>()
        var isValid = true

        if (_newPhone.value.isBlank()) {
            _phoneError.value = "El número de teléfono no puede estar vacío"
            errorMessages.add("• El número de teléfono no puede estar vacío")
            isValid = false
        } else if (_newPhone.value.contains(" ")) {
            _phoneError.value = "El número de teléfono no puede contener espacios"
            errorMessages.add("• El número de teléfono no puede contener espacios")
            isValid = false
        } else if (!_newPhone.value.matches(Regex("^9[0-9]{8}$"))) {
            _phoneError.value = "Ingresa un número válido que inicie con 9 y tenga 9 dígitos"
            errorMessages.add("• Ingresa un número válido que inicie con 9 y tenga 9 dígitos")
            isValid = false
        } else {
            _phoneError.value = null
        }

        return Pair(isValid, if (errorMessages.isNotEmpty()) errorMessages.joinToString("\n") else null)
    }

    // Validación completa para verificación de teléfono
    private fun validateVerifyPhoneFields(): Pair<Boolean, String?> {
        val errorMessages = mutableListOf<String>()
        var isValid = true

        if (_verificationCodePhone.value.isBlank()) {
            _codePhoneError.value = "El código de verificación no puede estar vacío"
            errorMessages.add("• El código de verificación no puede estar vacío")
            isValid = false
        } else if (_verificationCodePhone.value.length != 6) {
            _codePhoneError.value = "El código debe tener 6 dígitos"
            errorMessages.add("• El código debe tener 6 dígitos")
            isValid = false
        } else if (!_verificationCodePhone.value.matches(Regex("^[0-9]{6}$"))) {
            _codePhoneError.value = "El código solo puede contener números"
            errorMessages.add("• El código solo puede contener números")
            isValid = false
        } else {
            _codePhoneError.value = null
        }

        return Pair(isValid, if (errorMessages.isNotEmpty()) errorMessages.joinToString("\n") else null)
    }

    // Función para verificar código de teléfono
    fun verifyPhoneChange() {
        _verifyPhoneState.value = VerifyPhoneState.Initial

        val (isValid, errorMessage) = validateVerifyPhoneFields()
        if (!isValid) {
            _verifyPhoneState.value = VerifyPhoneState.Error(errorMessage ?: "Por favor, corrige los errores antes de continuar.")
            return
        }

        viewModelScope.launch {
            try {
                _verifyPhoneState.value = VerifyPhoneState.Loading

                val userId = userData.value?.id ?: throw Exception("Usuario no encontrado")

                val request = VerifyChangePhoneRequestDTO(
                    userId = userId,
                    code = _verificationCodePhone.value
                )

                val result = userRepository.verifyChangePhone(request)
                _verifyPhoneState.value = VerifyPhoneState.Success(result)

                // Actualizar el teléfono en las preferencias locales
                val currentUserData = userData.value
                currentUserData?.let {
                    val updatedUserData = it.copy(phone = _newPhone.value)
                    preferencesManager.saveUserData(updatedUserData)
                }

            } catch (e: Exception) {
                _verifyPhoneState.value = VerifyPhoneState.Error(e.message ?: "Error desconocido")
            }
        }
    }


    // Métodos para consumir eventos de teléfono
    fun onChangePhoneErrorShown() {
        if (_changePhoneState.value is ChangePhoneState.Error) {
            _changePhoneState.value = ChangePhoneState.Initial
        }
    }

    fun onChangePhoneSuccessShown() {
        if (_changePhoneState.value is ChangePhoneState.Success) {
            _changePhoneState.value = ChangePhoneState.Initial
        }
    }

    fun onVerifyPhoneErrorShown() {
        if (_verifyPhoneState.value is VerifyPhoneState.Error) {
            _verifyPhoneState.value = VerifyPhoneState.Initial
        }
    }

    fun onVerifyPhoneSuccessShown() {
        if (_verifyPhoneState.value is VerifyPhoneState.Success) {
            _verifyPhoneState.value = VerifyPhoneState.Initial
        }
    }


    // Estados sellados para teléfono
    sealed class ChangePhoneState {
        data object Initial : ChangePhoneState()
        data object Loading : ChangePhoneState()
        data class Success(val result: ChangePhoneResult) : ChangePhoneState()
        data class Error(val message: String) : ChangePhoneState()
    }

    sealed class VerifyPhoneState {
        data object Initial : VerifyPhoneState()
        data object Loading : VerifyPhoneState()
        data class Success(val result: VerifyChangePhoneResult) : VerifyPhoneState()
        data class Error(val message: String) : VerifyPhoneState()
    }
}