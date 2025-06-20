package com.rodolfo.itaxcix.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodolfo.itaxcix.data.remote.dto.auth.ResendCodeRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.VerifyCodeRegisterRequestDTO
import com.rodolfo.itaxcix.domain.model.ResendCodeRegisterResult
import com.rodolfo.itaxcix.domain.model.VerifyCodeRegisterResult
import com.rodolfo.itaxcix.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerifyCodeRegisterViewModel @Inject constructor(
    private val userRepository: UserRepository
): ViewModel() {

    private val _verifyCodeRegisterState = MutableStateFlow<VerifyCodeRegisterState>(VerifyCodeRegisterState.Initial)
    val verifyCodeRegisterState: StateFlow<VerifyCodeRegisterState> = _verifyCodeRegisterState.asStateFlow()

    private val _code = MutableStateFlow("")
    private val _userId = MutableStateFlow(0)

    val code: StateFlow<String> = _code
    val userId: StateFlow<Int> = _userId.asStateFlow()

    private val _codeError = MutableStateFlow<String?>(null)
    val codeError: StateFlow<String?> = _codeError.asStateFlow()

    fun updateCode(value: String) {
        _code.value = value
        resetStateIfError()
    }

    fun updateUserId(value: Int) {
        _userId.value = value
        resetStateIfError()
    }

    private fun resetStateIfError() {
        if (_verifyCodeRegisterState.value is VerifyCodeRegisterState.Error) {
            _verifyCodeRegisterState.value = VerifyCodeRegisterState.Initial
        }
    }

    fun verifyCode() {
        _verifyCodeRegisterState.value = VerifyCodeRegisterState.Initial

        val (isValid, errorMessage) = validateFields()
        if (!isValid) {
            _verifyCodeRegisterState.value = VerifyCodeRegisterState.Error(errorMessage ?: "Por favor, corrija los errores en el formulario")
            return
        }

        viewModelScope.launch {
            try {
                _verifyCodeRegisterState.value = VerifyCodeRegisterState.Loading

                val request = VerifyCodeRegisterRequestDTO(
                    userId = _userId.value,
                    code = _code.value
                )

                val response = userRepository.verifyCodeRegister(request)
                _verifyCodeRegisterState.value = VerifyCodeRegisterState.Success(response)
            } catch (e: Exception) {
                _verifyCodeRegisterState.value = VerifyCodeRegisterState.Error(e.message ?: "Error al verificar el código")
            }
        }
    }

    fun resendCode() {
        _verifyCodeRegisterState.value = VerifyCodeRegisterState.Initial

        viewModelScope.launch {
            try {
                _verifyCodeRegisterState.value = VerifyCodeRegisterState.Loading

                val request = ResendCodeRegisterRequestDTO(
                    userId = _userId.value
                )

                val response = userRepository.resendCodeRegister(request)
                _verifyCodeRegisterState.value = VerifyCodeRegisterState.ResendCodeSuccess(response)
            } catch (e: Exception) {
                _verifyCodeRegisterState.value = VerifyCodeRegisterState.Error(e.message ?: "Error al reenviar el código")
            }
        }
    }

    private fun validateFields(): Pair<Boolean, String?> {
        val errorMessages = mutableListOf<String>()
        var isValid = true

        if (_code.value.isBlank()) {
            _codeError.value = "El código es obligatorio"
            errorMessages.add("• El código es obligatorio")
            isValid = false
        } else {
            _codeError.value = null
        }

        return Pair(isValid, if (errorMessages.isNotEmpty()) errorMessages.joinToString("\n") else null)
    }

    fun onErrorShown() {
        if (_verifyCodeRegisterState.value is VerifyCodeRegisterState.Error) {
            _verifyCodeRegisterState.value = VerifyCodeRegisterState.Initial
        }
    }

    fun onSuccessShown() {
        if (_verifyCodeRegisterState.value is VerifyCodeRegisterState.Success) {
            _verifyCodeRegisterState.value = VerifyCodeRegisterState.Initial
        }
    }

    fun onResendCodeSuccessShown() {
        if (_verifyCodeRegisterState.value is VerifyCodeRegisterState.ResendCodeSuccess) {
            _verifyCodeRegisterState.value = VerifyCodeRegisterState.Initial
        }
    }

    sealed class VerifyCodeRegisterState {
        object Initial : VerifyCodeRegisterState()
        object Loading : VerifyCodeRegisterState()
        data class Success(val verifyCode: VerifyCodeRegisterResult) : VerifyCodeRegisterState()
        data class ResendCodeSuccess(val message: ResendCodeRegisterResult) : VerifyCodeRegisterState()
        data class Error(val message: String) : VerifyCodeRegisterState()
    }
}