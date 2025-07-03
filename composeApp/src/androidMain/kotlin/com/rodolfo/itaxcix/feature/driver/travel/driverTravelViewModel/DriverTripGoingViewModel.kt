package com.rodolfo.itaxcix.feature.driver.travel.driverTravelViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodolfo.itaxcix.data.local.PreferencesManager
import com.rodolfo.itaxcix.domain.model.TravelCancelResult
import com.rodolfo.itaxcix.domain.model.TravelCompleteResult
import com.rodolfo.itaxcix.domain.model.TravelRateResult
import com.rodolfo.itaxcix.domain.repository.TravelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriverTripGoingViewModel @Inject constructor(
    private val travelRepository: TravelRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val userData = preferencesManager.userData

    private val _driverTripGoingState = MutableStateFlow<DriverTripOngoingUiState>(DriverTripOngoingUiState.Initial)
    val driverTripGoingState: StateFlow<DriverTripOngoingUiState> = _driverTripGoingState.asStateFlow()

    fun completeTrip(travelId: Int) {
        viewModelScope.launch {
            _driverTripGoingState.value = DriverTripOngoingUiState.Loading
            try {
                val result = travelRepository.travelComplete(travelId)
                _driverTripGoingState.value = DriverTripOngoingUiState.AcceptSuccess(result)
            } catch (e: Exception) {
                _driverTripGoingState.value = DriverTripOngoingUiState.Error(e.message ?: "Error al completar el viaje")
            }
        }
    }

    fun cancelTrip(travelId: Int) {
        viewModelScope.launch {
            _driverTripGoingState.value = DriverTripOngoingUiState.Loading
            try {
                val result = travelRepository.travelCancel(travelId)
                _driverTripGoingState.value = DriverTripOngoingUiState.CancelSuccess(result)
            } catch (e: Exception) {
                _driverTripGoingState.value = DriverTripOngoingUiState.Error(e.message ?: "Error al cancelar el viaje")
            }
        }
    }

    fun onErrorShown() {
        if (_driverTripGoingState.value is DriverTripOngoingUiState.Error) {
            _driverTripGoingState.value = DriverTripOngoingUiState.Initial
        }
    }

    fun onAcceptSuccessShown() {
        if (_driverTripGoingState.value is DriverTripOngoingUiState.AcceptSuccess) {
            _driverTripGoingState.value = DriverTripOngoingUiState.Initial
        }
    }

    fun onCancelSuccessShown() {
        if (_driverTripGoingState.value is DriverTripOngoingUiState.CancelSuccess) {
            _driverTripGoingState.value = DriverTripOngoingUiState.Initial
        }
    }

    sealed class DriverTripOngoingUiState {
        data object Initial : DriverTripOngoingUiState()
        data object Loading : DriverTripOngoingUiState()
        data class AcceptSuccess(val acceptSuccess: TravelCompleteResult) : DriverTripOngoingUiState()
        data class CancelSuccess(val cancelSuccess: TravelCancelResult) : DriverTripOngoingUiState()
        data class Error(val message: String) : DriverTripOngoingUiState()
    }

    // Estados para la calificación del conductor
    private val _rateState = MutableStateFlow<RateState>(RateState.Initial)
    val rateState: StateFlow<RateState> = _rateState.asStateFlow()

    // Estados para los campos del formulario de calificación
    private val _rating = MutableStateFlow(0)
    val rating: StateFlow<Int> = _rating.asStateFlow()

    private val _ratingComment = MutableStateFlow("")
    val ratingComment: StateFlow<String> = _ratingComment.asStateFlow()

    // Estados para los errores de validación
    private val _ratingError = MutableStateFlow<String?>(null)
    val ratingError: StateFlow<String?> = _ratingError.asStateFlow()

    private val _ratingCommentError = MutableStateFlow<String?>(null)
    val ratingCommentError: StateFlow<String?> = _ratingCommentError.asStateFlow()

    // Métodos para actualizar los campos de calificación
    fun updateRating(value: Int) {
        _rating.value = value
        validateRating()
        resetRateStateIfError()
    }

    fun updateRatingComment(value: String) {
        _ratingComment.value = value
        validateRatingComment()
        resetRateStateIfError()
    }

    private fun resetRateStateIfError() {
        if (_rateState.value is RateState.Error) {
            _rateState.value = RateState.Initial
        }
    }

    // Validaciones para calificación
    private fun validateRating() {
        if (_rating.value == 0) {
            _ratingError.value = "Debes seleccionar una calificación"
        } else {
            _ratingError.value = null
        }
    }

    private fun validateRatingComment() {
        when {
            _ratingComment.value.isBlank() -> {
                _ratingCommentError.value = "Debes ingresar un comentario"
            }
            _ratingComment.value.trim().length < 5 -> {
                _ratingCommentError.value = "El comentario debe tener al menos 5 caracteres"
            }
            else -> {
                _ratingCommentError.value = null
            }
        }
    }

    // Validación completa para calificación
    private fun validateRatingFields(): Pair<Boolean, String?> {
        validateRating()
        validateRatingComment()

        val hasRatingError = _ratingError.value != null
        val hasCommentError = _ratingCommentError.value != null

        val isValid = !hasRatingError && !hasCommentError

        val errorMessage = when {
            hasRatingError && hasCommentError -> "Debes seleccionar una calificación y agregar un comentario"
            hasRatingError -> "Debes seleccionar una calificación"
            hasCommentError -> "Debes agregar un comentario"
            else -> null
        }

        return Pair(isValid, errorMessage)
    }

    fun rateTrip(tripId: Int) {
        val (isValid, errorMessage) = validateRatingFields()
        if (!isValid) {
            _rateState.value = RateState.Error(errorMessage ?: "Por favor, completa todos los campos requeridos")
            return
        }

        viewModelScope.launch {
            _rateState.value = RateState.Loading
            try {
                val travelRateRequest = com.rodolfo.itaxcix.data.remote.dto.travel.TravelRateRequestDTO(
                    travelId = tripId,
                    raterId = userData.value?.id ?: return@launch,
                    score = _rating.value,
                    comment = _ratingComment.value
                )
                val result = travelRepository.travelRate(tripId, travelRateRequest)
                _rateState.value = RateState.Success(result)
            } catch (e: Exception) {
                _rateState.value = RateState.Error(e.message ?: "Error al calificar el viaje")
            }
        }
    }

    fun onRateErrorShown() {
        if (_rateState.value is RateState.Error) {
            _rateState.value = RateState.Initial
        }
    }

    fun onRateSuccessShown() {
        if (_rateState.value is RateState.Success) {
            _rateState.value = RateState.Initial
        }
    }


    sealed class RateState {
        data object Initial : RateState()
        data object Loading : RateState()
        data class Success(val result: TravelRateResult) : RateState()
        data class Error(val message: String) : RateState()
    }
}