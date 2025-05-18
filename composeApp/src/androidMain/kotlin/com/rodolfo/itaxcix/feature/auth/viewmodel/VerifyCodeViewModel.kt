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
class VerifyCodeViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel(){

    private val _verifyCodeState = MutableStateFlow<VerifyCodeState>(VerifyCodeState.Initial)
    val verifyCodeState: StateFlow<VerifyCodeState> = _verifyCodeState.asStateFlow()

    private val _code = MutableStateFlow("")
    private val _contact = MutableStateFlow("")
    private val _contactTypeId = MutableStateFlow(1)

    val code: StateFlow<String> = _code
    val contact: StateFlow<String> = _contact
    val contactTypeId: StateFlow<Int> = _contactTypeId

    private val _codeError = MutableStateFlow<String?>(null)
    private val _contactError = MutableStateFlow<String?>(null)
    private val _contactTypeError = MutableStateFlow<String?>(null)

    val codeError: StateFlow<String?> = _codeError.asStateFlow()
    val contactError: StateFlow<String?> = _contactError.asStateFlow()

    fun updateCode(value: String) {
        _code.value = value
        resetStateIfError()
    }

    fun updateContact(value: String) {
        _contact.value = value
        resetStateIfError()
    }

    fun updateContactTypeId(value: Int) {
        _contactTypeId.value = value
        resetStateIfError()
    }

    private fun resetStateIfError() {
        if (_verifyCodeState.value is VerifyCodeState.Error) {
            _verifyCodeState.value = VerifyCodeState.Initial
        }
    }

    fun verifyCode() {
        _verifyCodeState.value = VerifyCodeState.Loading

        val (isValid, errorMessage) = validateFields()
        if (!isValid) {
            _verifyCodeState.value = VerifyCodeState.Error(errorMessage ?: "Por favor, corrija los errores en el formulario")
            return
        }

        viewModelScope.launch {
            try {
                _verifyCodeState.value = VerifyCodeState.Loading

                val contactValue = if (_contactTypeId.value == 2 && !_contact.value.startsWith("+51")) {
                    "+51${_contact.value}"
                } else {
                    _contact.value
                }

                val result = userRepository.verifyCode(
                    code = _code.value,
                    contactTypeId = _contactTypeId.value,
                    contact = contactValue
                )

                _verifyCodeState.value = VerifyCodeState.Success(result.message, result.userId)
            } catch (e: Exception) {
                _verifyCodeState.value = VerifyCodeState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    private fun validateFields(): Pair<Boolean, String?> {
        val errorMessages = mutableListOf<String>()
        var isValid = true

        if (_code.value.isBlank()) {
            _codeError.value = "El código es requerido"
            errorMessages.add("• El código es requerido")
            isValid = false
        } else if (_code.value.contains(" ")) {
            _codeError.value = "El código no puede contener espacios"
            errorMessages.add("• El código no puede contener espacios")
            isValid = false
        } else {
            _codeError.value = null
        }

        if (_contact.value.isEmpty()) {
            _contactError.value = "El contacto es requerido"
            errorMessages.add("• El contacto es requerido")
            isValid = false
        } else {
            _contactError.value = null
        }

        if (_contactTypeId.value == 0) {
            _contactTypeError.value = "El tipo de contacto es requerido"
            errorMessages.add("• El tipo de contacto es requerido")
            isValid = false
        } else {
            _contactTypeError.value = null
        }

        return Pair(isValid, if (errorMessages.isNotEmpty()) errorMessages.joinToString("\n") else null)
    }

    fun onErrorShown() {
        if (_verifyCodeState.value is VerifyCodeState.Error) {
            _verifyCodeState.value = VerifyCodeState.Initial
        }
    }

    fun onSuccessShown() {
        if (_verifyCodeState.value is VerifyCodeState.Success) {
            _verifyCodeState.value = VerifyCodeState.Initial
        }
    }

    sealed class VerifyCodeState {
        object Initial : VerifyCodeState()
        object Loading : VerifyCodeState()
        data class Success(val message: String, val userId: String) : VerifyCodeState()
        data class Error(val message: String) : VerifyCodeState()
    }
}