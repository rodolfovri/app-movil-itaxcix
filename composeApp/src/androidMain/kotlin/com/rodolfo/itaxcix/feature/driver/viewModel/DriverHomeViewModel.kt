package com.rodolfo.itaxcix.feature.driver.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodolfo.itaxcix.data.local.PreferencesManager
import com.rodolfo.itaxcix.data.local.UserData
import com.rodolfo.itaxcix.data.remote.dto.websockets.TripRequestMessage
import com.rodolfo.itaxcix.data.remote.websocket.DriverWebSocketService
import com.rodolfo.itaxcix.domain.model.DriverAvailabilityResult
import com.rodolfo.itaxcix.domain.model.TravelRespondResult
import com.rodolfo.itaxcix.domain.repository.DriverRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriverHomeViewModel @Inject constructor(
    private val driverRepository: DriverRepository,
    private val preferencesManager: PreferencesManager,
    val driverWebSocketService: DriverWebSocketService
) : ViewModel() {

    private var locationUpdateJob: Job? = null
    private val _navigateToTrip = MutableSharedFlow<TripRequestMessage>()
    val navigateToTrip = _navigateToTrip.asSharedFlow()

    val userData: StateFlow<UserData?> = preferencesManager.userData
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _driverHomeState = MutableStateFlow<DriverHomeUiState>(DriverHomeUiState.Initial)
    val driverHomeState: StateFlow<DriverHomeUiState> = _driverHomeState.asStateFlow()

    // Cambia el estado de disponibilidad del conductor
    fun toggleDriverAvailability() {
        _driverHomeState.value = DriverHomeUiState.Loading

        viewModelScope.launch {
            try {
                val userId = userData.value?.id ?: 0
                val driverStatus = driverRepository.toggleDriverAvailability(userId)

                userData.value?.let {
                    preferencesManager.saveUserData(
                        it.copy(
                            isTucActive = driverStatus.hasActiveTuc
                        )
                    )
                }

                driverWebSocketService.connect()

                if (driverStatus.hasActiveTuc) {
                    startLocationUpdates()
                } else {
                    stopLocationUpdates()
                }

                _driverHomeState.value = DriverHomeUiState.Success(driverStatus)
            } catch (e: Exception) {
                _driverHomeState.value = DriverHomeUiState.Error(e.message ?: "Error")
            }
        }
    }

    fun respondToTrip(travelId: Int, accept: Boolean) {
        viewModelScope.launch {
            try {
                _driverHomeState.value = DriverHomeUiState.Loading
                val result = driverRepository.travelRespond(travelId, accept)

                val updateRequests = driverWebSocketService._tripRequests.value
                    .filter { it.data.tripId != travelId }
                driverWebSocketService._tripRequests.value = updateRequests

                _driverHomeState.value = DriverHomeUiState.RespondSuccess(result)
            } catch (e: Exception) {
                _driverHomeState.value = DriverHomeUiState.Error(e.message ?: "Error al responder al viaje")
            }
        }
    }

    fun saveUserLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val currentUserData = userData.value ?: return@launch
            val updatedUserData = currentUserData.copy(
                latitude = latitude,
                longitude = longitude
            )
            preferencesManager.saveUserData(updatedUserData)

            // Enviar al WebSocket si el conductor está activo
            if (currentUserData.isTucActive == true) {
                driverWebSocketService.updateLocation(latitude, longitude)
            }
        }
    }

    fun startLocationUpdates() {
        // Cancelar job existente si hay uno
        locationUpdateJob?.cancel()

        locationUpdateJob = viewModelScope.launch {
            while (userData.value?.isTucActive == true) {
                userData.value?.let { driver ->
                    driver.latitude?.let { lat ->
                        driver.longitude?.let { lng ->
                            driverWebSocketService.updateLocation(lat, lng)
                            Log.d("DriverHomeViewModel", "Ubicación actualizada: $lat, $lng")
                        }
                    }
                }
                delay(15000) // Actualizar cada 15 segundos
            }
        }
    }

    fun stopLocationUpdates() {
        locationUpdateJob?.cancel()
        locationUpdateJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }


    sealed class DriverHomeUiState {
        object Initial : DriverHomeUiState()
        object Loading : DriverHomeUiState()
        data class Success(val userData: DriverAvailabilityResult) : DriverHomeUiState()
        data class RespondSuccess(val travel: TravelRespondResult) : DriverHomeUiState()
        data class Error(val message: String) : DriverHomeUiState()
    }
}