package com.rodolfo.itaxcix.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodolfo.itaxcix.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResetPasswordViewModel(
    private val userRepository: UserRepository
): ViewModel() {

    private val _resetPasswordState = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Initial)
    val resetPasswordState: StateFlow<ResetPasswordState> = _resetPasswordState.asStateFlow()

    private val _userId = MutableStateFlow("")
    private val _newPassword = MutableStateFlow("")

    val userId: StateFlow<String> = _userId
    val newPassword: StateFlow<String> = _newPassword

    private val _userIdError = MutableStateFlow<String?>(null)
    private val _newPasswordError = MutableStateFlow<String?>(null)

    val userIdError: StateFlow<String?> = _userIdError.asStateFlow()
    val newPasswordError: StateFlow<String?> = _newPasswordError.asStateFlow()

    fun updateUserId(value: String) {
        _userId.value = value
        resetStateIfError()
    }

    fun updateNewPassword(value: String) {
        _newPassword.value = value
        resetStateIfError()
    }

    fun resetPassword() {
        _resetPasswordState.value = ResetPasswordState.Loading

        val (isValid, errorMessage) = validateFields()
        if (!isValid) {
            _resetPasswordState.value = ResetPasswordState.Error(errorMessage ?: "Por favor, corrija los errores en el formulario")
            return
        }

        viewModelScope.launch {
            try {
                _resetPasswordState.value = ResetPasswordState.Loading

                val result = userRepository.resetPassword(
                    userId = _userId.value,
                    newPassword = _newPassword.value
                )

                _resetPasswordState.value = ResetPasswordState.Success(result.message)
            } catch (e: Exception) {
                _resetPasswordState.value = ResetPasswordState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    private fun resetStateIfError() {
        if (_resetPasswordState.value is ResetPasswordState.Error) {
            _resetPasswordState.value = ResetPasswordState.Initial
        }
    }

    private fun validateFields(): Pair<Boolean, String?> {
        val errorMessages = mutableListOf<String>()
        var isValid = true

        if (_userId.value.isEmpty()) {
            _userIdError.value = "El ID de usuario no puede estar vacío"
            errorMessages.add("• El ID de usuario no puede estar vacío")
            isValid = false
        } else {
            _userIdError.value = null
        }

        if (_newPassword.value.isBlank()) {
            _newPasswordError.value = "La nueva contraseña no puede estar vacía"
            errorMessages.add("• La nueva contraseña no puede estar vacía")
            isValid = false
        } else if (_newPassword.value.length < 8) {
            _newPasswordError.value = "La contraseña debe tener al menos 8 caracteres"
            errorMessages.add("• La contraseña debe tener al menos 8 caracteres")
            isValid = false
        } else if (!_newPassword.value.contains(Regex("[A-Z]"))) {
            _newPasswordError.value = "La contraseña debe contener al menos una letra mayúscula"
            errorMessages.add("• La contraseña debe contener al menos una letra mayúscula")
            isValid = false
        } else if (!_newPassword.value.contains(Regex("[a-z]"))) {
            _newPasswordError.value = "La contraseña debe contener al menos una letra minúscula"
            errorMessages.add("• La contraseña debe contener al menos una letra minúscula")
            isValid = false
        } else if (!_newPassword.value.contains(Regex("[0-9]"))) {
            _newPasswordError.value = "La contraseña debe contener al menos un número"
            errorMessages.add("• La contraseña debe contener al menos un número")
            isValid = false
        } else if (!_newPassword.value.contains(Regex("[^A-Za-z0-9]"))) {
            _newPasswordError.value = "La contraseña debe contener al menos un carácter especial (@, #, etc.)"
            errorMessages.add("• La contraseña debe contener al menos un carácter especial (@, #, etc.)")
            isValid = false
        } else {
            _newPasswordError.value = null
        }

        return Pair(isValid, if (errorMessages.isNotEmpty()) errorMessages.joinToString("\n") else null)
    }

    fun onErrorShown() {
        if (_resetPasswordState.value is ResetPasswordState.Error) {
            _resetPasswordState.value = ResetPasswordState.Initial
        }
    }

    fun onSuccessShown() {
        if (_resetPasswordState.value is ResetPasswordState.Success) {
            _resetPasswordState.value = ResetPasswordState.Initial
        }
    }

    sealed class ResetPasswordState {
        object Initial : ResetPasswordState()
        object Loading : ResetPasswordState()
        data class Success(val message: String) : ResetPasswordState()
        data class Error(val message: String) : ResetPasswordState()
    }
}