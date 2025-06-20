package com.rodolfo.itaxcix.feature.citizen.viewModelCitizen

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodolfo.itaxcix.data.local.PreferencesManager
import com.rodolfo.itaxcix.data.local.UserData
import com.rodolfo.itaxcix.data.remote.websocket.CitizenWebSocketService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CitizenHomeViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val citizenWebSocketService: CitizenWebSocketService
) : ViewModel() {

    // Estado de carga
    private val _isLoading = mutableStateOf(true)
    val isLoading = _isLoading

    val userData: StateFlow<UserData?> = preferencesManager.userData
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Lista de conductores disponibles
    val availableDrivers = citizenWebSocketService.availableDrivers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    init {
        setupWebSocketConnection()
        setupLoadingTimeout()
        observeDriversChanges()
    }

    private fun setupWebSocketConnection() {
        viewModelScope.launch {
            try {
                citizenWebSocketService.connect()

                // Callback para saber cuando se han recibido los conductores
                citizenWebSocketService.onInitialDriversReceived = {
                    _isLoading.value = false
                }

            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    private fun setupLoadingTimeout() {
        viewModelScope.launch {
            delay(10000L) // Esperar 10 segundos antes de detener el indicador de carga
            if (_isLoading.value) {
                _isLoading.value = false
            }
        }
    }

    private fun observeDriversChanges() {
        viewModelScope.launch {
            availableDrivers.collect { drivers ->
                // Si recibimos conductores, ya no estamos cargando
                if (drivers.isNotEmpty() && _isLoading.value) {
                    _isLoading.value = false
                }
            }
        }
    }
}