package com.rodolfo.itaxcix.feature.driver.travel.driverTravelViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodolfo.itaxcix.data.local.PreferencesManager
import com.rodolfo.itaxcix.domain.model.TravelCancelResult
import com.rodolfo.itaxcix.domain.model.TravelStartResult
import com.rodolfo.itaxcix.domain.repository.DriverRepository
import com.rodolfo.itaxcix.domain.repository.TravelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriverTripInProgressViewModel @Inject constructor(
    private val driverRepository: DriverRepository,
    private val travelRepository: TravelRepository
) : ViewModel() {

    private val _driverTripInProgressState = MutableStateFlow<DriverTripInProgressUiState>(DriverTripInProgressUiState.Initial)
    val driverTripInProgressState: StateFlow<DriverTripInProgressUiState> = _driverTripInProgressState.asStateFlow()

    fun startTrip(travelId: Int) {
        viewModelScope.launch {
            _driverTripInProgressState.value = DriverTripInProgressUiState.Loading
            try {
                val result = driverRepository.travelStart(travelId)
                _driverTripInProgressState.value = DriverTripInProgressUiState.AcceptSuccess(result)
            } catch (e: Exception) {
                _driverTripInProgressState.value = DriverTripInProgressUiState.Error(e.message ?: "Error al iniciar el viaje")
            }
        }
    }

    fun cancelTrip(travelId: Int) {
        viewModelScope.launch {
            _driverTripInProgressState.value = DriverTripInProgressUiState.Loading
            try {
                val result = travelRepository.travelCancel(travelId)
                _driverTripInProgressState.value = DriverTripInProgressUiState.CancelSuccess(result)
            } catch (e: Exception) {
                _driverTripInProgressState.value = DriverTripInProgressUiState.Error(e.message ?: "Error al cancelar el viaje")
            }
        }
    }

    fun onErrorShown() {
        if (_driverTripInProgressState.value is DriverTripInProgressUiState.Error) {
            _driverTripInProgressState.value = DriverTripInProgressUiState.Initial
        }
    }

    fun onAcceptSuccessShown() {
        if (_driverTripInProgressState.value is DriverTripInProgressUiState.AcceptSuccess) {
            _driverTripInProgressState.value = DriverTripInProgressUiState.Initial
        }
    }

    fun onCancelSuccessShown() {
        if (_driverTripInProgressState.value is DriverTripInProgressUiState.CancelSuccess) {
            _driverTripInProgressState.value = DriverTripInProgressUiState.Initial
        }
    }

    sealed interface DriverTripInProgressUiState {
        data object Initial : DriverTripInProgressUiState
        data object Loading : DriverTripInProgressUiState
        data class Error(val message: String) : DriverTripInProgressUiState
        data class AcceptSuccess(val acceptSuccess: TravelStartResult) : DriverTripInProgressUiState
        data class CancelSuccess(val cancelSuccess: TravelCancelResult) : DriverTripInProgressUiState
    }
}