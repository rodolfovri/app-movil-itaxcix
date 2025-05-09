package com.rodolfo.itaxcix.feature.auth.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.rodolfo.itaxcix.domain.repository.UserRepository

class LoginViewModel(
    private val userRepository: UserRepository,
) : ViewModel(){

    private val _loginState = mutableStateOf<LoginState>(LoginState.Initial)
    val loginState: LoginState get() = _loginState.value

    // Datos del formulario
    private val _username = mutableStateOf("")
    private val _password = mutableStateOf("")

    // Exponer los estados como StateFlow
    val username: String get() = _username.value
    val password: String get() = _password.value

    // Método para actualizar los campos
    fun updateUsername(value: String) { _username.value = value }
    fun updatePassword(value: String) { _password.value = value }


    // Método para iniciar sesión
    suspend fun login(username: String, password: String) {
        _loginState.value = LoginState.Loading
        try {
            val user = userRepository.login(username, password)
            _loginState.value = LoginState.Success(user)
        } catch (e: Exception) {
            _loginState.value = LoginState.Error(e.message ?: "Error desconocido")
        }
    }

    // Estados para la pantalla de login
    sealed class LoginState {
        object Initial : LoginState()
        object Loading : LoginState()
        data class Success(val user: Any) : LoginState() // Cambia Any por tu modelo de usuario
        data class Error(val message: String) : LoginState()
    }
}