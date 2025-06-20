package com.rodolfo.itaxcix.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodolfo.itaxcix.domain.model.RecoveryResult
import com.rodolfo.itaxcix.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecoveryViewModel @Inject constructor(
    private val userRepository: UserRepository,
): ViewModel() {

    private val _recoveryState = MutableStateFlow<RecoveryState>(RecoveryState.Initial)
    val recoveryState: StateFlow<RecoveryState> = _recoveryState.asStateFlow()

    private val _contact = MutableStateFlow("")
    private val _contactTypeId = MutableStateFlow(1)

    val contact: StateFlow<String> = _contact
    val contactTypeId: StateFlow<Int> = _contactTypeId

    private val _contactError = MutableStateFlow<String?>(null)

    val contactError: StateFlow<String?> = _contactError.asStateFlow()

    fun updateContact(value: String) {
        _contact.value = value
        resetStateIfError()
    }

    fun updateContactTypeId(value: Int) {
        _contactTypeId.value = value
        resetStateIfError()
    }

    fun recoverPassword() {
        _recoveryState.value = RecoveryState.Loading

        val (isValid, errorMessage) = validateFields()
        if (!isValid) {
            _recoveryState.value = RecoveryState.Error(errorMessage ?: "Por favor, corrija los errores en el formulario")
            return
        }

        viewModelScope.launch {
            try {
                _recoveryState.value = RecoveryState.Loading

                val contactValue = if(_contactTypeId.value == 2 && !_contact.value.startsWith("+51")) {
                    "+51${_contact.value}"
                } else {
                    _contact.value
                }

                val result = userRepository.recovery(
                    contactTypeId = _contactTypeId.value,
                    contact = contactValue
                )

                _recoveryState.value = RecoveryState.Success(result)
            } catch (e: Exception) {
                _recoveryState.value = RecoveryState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    private fun resetStateIfError() {
        if (_recoveryState.value is RecoveryState.Error) {
            _recoveryState.value = RecoveryState.Initial
        }
    }

    private fun validateFields(): Pair<Boolean, String?> {
        val errorMessages = mutableListOf<String>()
        var isValid = true

        if (_contact.value.isEmpty()) {
            _contactError.value = "El campo de contacto no puede estar vacío"
            errorMessages.add("• El campo de contacto no puede estar vacío")
            isValid = false
        } else if (_contact.value.contains(" ")) {
            _contactError.value = "El campo de contacto no puede contener espacios"
            errorMessages.add("• El campo de contacto no puede contener espacios")
            isValid = false
        } else {
            _contactError.value = null
        }

        return Pair(isValid, if (errorMessages.isNotEmpty()) errorMessages.joinToString("\n") else null)
    }

    fun onErrorShown() {
        if (_recoveryState.value is RecoveryState.Error) {
            _recoveryState.value = RecoveryState.Initial
        }
    }

    fun onSuccessShown() {
        if (_recoveryState.value is RecoveryState.Success) {
            _recoveryState.value = RecoveryState.Initial
        }
    }



    sealed class RecoveryState {
        data object Initial : RecoveryState()
        data object Loading : RecoveryState()
        data class Success(val response: RecoveryResult) : RecoveryState()
        data class Error(val message: String) : RecoveryState()
    }
}