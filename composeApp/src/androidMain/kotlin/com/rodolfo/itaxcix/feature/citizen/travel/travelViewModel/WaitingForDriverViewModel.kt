package com.rodolfo.itaxcix.feature.citizen.travel.travelViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodolfo.itaxcix.data.remote.websocket.CitizenWebSocketService
import com.rodolfo.itaxcix.domain.model.TravelCancelResult
import com.rodolfo.itaxcix.domain.repository.TravelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WaitingForDriverViewModel @Inject constructor(
    private val travelRepository: TravelRepository,
    private val citizenWebSocketService: CitizenWebSocketService
) : ViewModel() {

    private val _waitingState = MutableStateFlow<WaitingState>(WaitingState.Initial)
    val waitingState: StateFlow<WaitingState> = _waitingState.asStateFlow()

    private val _tripResponseState = MutableStateFlow<TripResponseState>(TripResponseState.Waiting)
    val tripResponseState: StateFlow<TripResponseState> = _tripResponseState.asStateFlow()

    init {
        viewModelScope.launch {
            citizenWebSocketService.tripResponse.collect { response ->
                response?.let {
                    if (it.data.accepted) {
                        _tripResponseState.value = TripResponseState.Accepted(
                            driverId = it.data.driverId,
                            driverName = it.data.driverName,
                            estimatedArrival = it.data.estimatedArrival
                        )
                        Log.d("WaitingForDriverViewModel", "Trip accepted by driver: ${it.data.driverName}, Estimated arrival: ${it.data.estimatedArrival} minutes")
                    } else {
                        _tripResponseState.value = TripResponseState.Rejected
                        Log.d("WaitingForDriverViewModel", "Trip rejected by driver")
                    }
                }
            }
        }
    }

    fun cancelTrip(travelId: Int) {
        viewModelScope.launch {
            _waitingState.value = WaitingState.Loading
            try {
                val result = travelRepository.travelCancel(travelId)
                _waitingState.value = WaitingState.Success(result)
            } catch (e: Exception) {
                _waitingState.value = WaitingState.Error(e.message ?: "Error al cancelar el viaje")
            }
        }
    }

    fun onErrorShown() {
        if (_waitingState.value is WaitingState.Error) {
            _waitingState.value = WaitingState.Initial
        }
    }

    fun onSuccessShown() {
        if (_waitingState.value is WaitingState.Success) {
            _waitingState.value = WaitingState.Initial
        }
    }

    fun resetTripResponseState() {
        citizenWebSocketService.resetTripResponseState()
        _tripResponseState.value = TripResponseState.Waiting
    }

    sealed class WaitingState {
        data object Initial : WaitingState()
        data object Loading : WaitingState()
        data class Success(val success: TravelCancelResult) : WaitingState()
        data class Error(val message: String) : WaitingState()
    }

    sealed class TripResponseState {
        data object Waiting : TripResponseState()
        data class Accepted(
            val driverId: Int,
            val driverName: String,
            val estimatedArrival: Double
        ) : TripResponseState()
        data object Rejected : TripResponseState()
    }
}