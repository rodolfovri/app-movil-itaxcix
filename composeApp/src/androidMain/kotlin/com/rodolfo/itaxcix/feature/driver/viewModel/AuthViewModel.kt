package com.rodolfo.itaxcix.feature.driver.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodolfo.itaxcix.data.local.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    val preferencesManager: PreferencesManager
) : ViewModel(){

    private val _logoutState = MutableStateFlow<LogoutState>(LogoutState.Initial)
    val logoutState: StateFlow<LogoutState> = _logoutState.asStateFlow()

    fun logout() {
        _logoutState.value = LogoutState.Loading
        viewModelScope.launch {
            try {
                // Limpiar datos del usuario
                preferencesManager.clearUserData()
                _logoutState.value = LogoutState.Success
            } catch (e: Exception) {
                _logoutState.value = LogoutState.Error(e.message ?: "Error al cerrar sesión")
            }
        }
    }

    fun onSuccessShown() {
        _logoutState.value = LogoutState.Initial
    }

    sealed class LogoutState {
        object Initial : LogoutState()
        object Loading : LogoutState()
        object Success : LogoutState()
        data class Error(val message: String) : LogoutState()
    }
}