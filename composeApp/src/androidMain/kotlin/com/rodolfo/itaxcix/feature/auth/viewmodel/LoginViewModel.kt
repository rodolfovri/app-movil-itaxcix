package com.rodolfo.itaxcix.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodolfo.itaxcix.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel(){

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    // Datos del formulario
    private val _username = MutableStateFlow("")
    private val _password = MutableStateFlow("")

    // Exponer los estados como StateFlow
    val username: StateFlow<String> = _username
    val password: StateFlow<String> = _password

    // Estados para validación de campos
    private val _usernameError = MutableStateFlow<String?>(null)
    private val _passwordError = MutableStateFlow<String?>(null)

    // Exponer los estados de error como StateFlow
    val usernameError: StateFlow<String?> = _usernameError.asStateFlow()
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    // Método para actualizar los campos
    fun updateUsername(value: String) {
        _username.value = value
        resetStateIfError()
    }

    fun updatePassword(value: String) {
        _password.value = value
        resetStateIfError()
    }

    // Método para restablecer el estado si hay un error
    private fun resetStateIfError() {
        if (_loginState.value is LoginState.Error) {
            _loginState.value = LoginState.Initial
        }
    }

    // Método para validar los campos
    private fun validateFields(): Pair<Boolean, String?> {
        val errorMessages = mutableListOf<String>()
        var isValid = true

        if (_username.value.isBlank()) {
            _usernameError.value = "El nombre de usuario no puede estar vacío"
            errorMessages.add("• El nombre de usuario no puede estar vacío")
            isValid = false
        } else if (_username.value.contains(" ")) {
            _usernameError.value = "El nombre de usuario no puede contener espacios"
            errorMessages.add("• El nombre de usuario no puede contener espacios")
            isValid = false
        } else {
            _usernameError.value = null
        }

        if (_password.value.isBlank()) {
            _passwordError.value = "La contraseña no puede estar vacía"
            errorMessages.add("• La contraseña no puede estar vacía")
            isValid = false
        } else {
            _passwordError.value = null
        }

        return Pair(isValid, if (errorMessages.isNotEmpty()) errorMessages.joinToString("\n") else null)
    }

    // Método para iniciar sesión
    fun login() {
        _loginState.value = LoginState.Loading

        val (isValid, errorMessage) = validateFields()
        if (!isValid) {
            _loginState.value = LoginState.Error(errorMessage ?: "Por favor, completa todos los campos")
            return
        }

        val username = _username.value
        val password = _password.value

        viewModelScope.launch {
            try {
                val result = userRepository.login(username, password)
                _loginState.value = LoginState.Success(result.message, result)
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Error desconocido")
            }
        }
    }


    // Método para restablecer el estado de error
    fun onErrorShown() {
        if (_loginState.value is LoginState.Error) {
            _loginState.value = LoginState.Initial
        }
    }

    // Método para restablecer el estado de éxito
    fun onSuccessShown() {
        if (_loginState.value is LoginState.Success) {
            _loginState.value = LoginState.Initial
        }
    }


    // Estados para la pantalla de login
    sealed class LoginState {
        object Initial : LoginState()
        object Loading : LoginState()
        data class Success(val message: String, val user: Any) : LoginState() // Cambia Any por tu modelo de usuario
        data class Error(val message: String) : LoginState()
    }
}