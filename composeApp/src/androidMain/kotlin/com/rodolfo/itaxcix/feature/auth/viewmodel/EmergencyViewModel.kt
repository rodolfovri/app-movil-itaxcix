package com.rodolfo.itaxcix.feature.auth.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodolfo.itaxcix.domain.repository.TravelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmergencyViewModel @Inject constructor(
    private val travelRepository: TravelRepository
) : ViewModel() {

    private val _emergencyState = MutableStateFlow<EmergencyState>(EmergencyState.Initial)
    val emergencyState: StateFlow<EmergencyState> = _emergencyState.asStateFlow()

    private val _emergencyNumber = MutableStateFlow("")
    val emergencyNumber: StateFlow<String> = _emergencyNumber.asStateFlow()

    fun getEmergencyNumber() {
        _emergencyState.value = EmergencyState.Loading

        viewModelScope.launch {
            try {
                val result = travelRepository.emergencyNumber()
                _emergencyNumber.value = result.number
                _emergencyState.value = EmergencyState.Success(result.number)
                Log.d("EmergencyViewModel", "Número de emergencia obtenido: ${result.number}")
            } catch (e: Exception) {
                _emergencyState.value = EmergencyState.Error(e.message ?: "Error al obtener número de emergencia")
                Log.d("EmergencyViewModel", "Error al obtener número de emergencia: ${e.message}")
            }
        }
    }

    fun onEmergencySuccessShown() {
        if (_emergencyState.value is EmergencyState.Success) {
            _emergencyState.value = EmergencyState.Initial
        }
    }

    fun onEmergencyErrorShown() {
        if (_emergencyState.value is EmergencyState.Error) {
            _emergencyState.value = EmergencyState.Initial
        }
    }

    sealed class EmergencyState {
        data object Initial : EmergencyState()
        data object Loading : EmergencyState()
        data class Success(val number: String) : EmergencyState()
        data class Error(val message: String) : EmergencyState()
    }
}