package com.rodolfo.itaxcix.feature.citizen.travel.travelViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodolfo.itaxcix.data.local.PreferencesManager
import com.rodolfo.itaxcix.data.remote.dto.travel.RegisterIncidentRequestDTO
import com.rodolfo.itaxcix.data.remote.websocket.CitizenWebSocketService
import com.rodolfo.itaxcix.domain.model.RegisterIncidentResult
import com.rodolfo.itaxcix.domain.model.TravelCancelResult
import com.rodolfo.itaxcix.domain.model.TravelRateResult
import com.rodolfo.itaxcix.domain.repository.TravelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CitizenTripViewModel @Inject constructor(
    private val travelRepository: TravelRepository,
    val citizenWebSocketService: CitizenWebSocketService,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val userData = preferencesManager.userData

    /*
        En esta parte se maneja para cancelar un viaje.
     */

    private val _cancelState = MutableStateFlow<CancelState>(CancelState.Initial)
    val cancelState: StateFlow<CancelState> = _cancelState.asStateFlow()

    fun cancelTrip(tripId: Int) {
        viewModelScope.launch {
            _cancelState.value = CancelState.Loading
            try {
                val result = travelRepository.travelCancel(tripId)
                _cancelState.value = CancelState.Success(result)
            } catch (e: Exception) {
                _cancelState.value = CancelState.Error(e.message ?: "Error al cancelar el viaje")
            }
        }
    }

    sealed class CancelState {
        data object Initial : CancelState()
        data object Loading : CancelState()
        data class Success(val result: TravelCancelResult) : CancelState()
        data class Error(val message: String) : CancelState()
    }

    /*
        En esta parte se maneja para registrar una incidencia después de cancelar un viaje.
     */

    // Estados para el registro de incidencias
    private val _incidentState = MutableStateFlow<IncidentState>(IncidentState.Initial)
    val incidentState: StateFlow<IncidentState> = _incidentState.asStateFlow()

    // Estados para los campos del formulario
    private val _incidentType = MutableStateFlow("")
    val incidentType: StateFlow<String> = _incidentType.asStateFlow()

    private val _comment = MutableStateFlow("")
    val comment: StateFlow<String> = _comment.asStateFlow()

    // Estados para los errores de validación
    private val _incidentTypeError = MutableStateFlow<String?>(null)
    val incidentTypeError: StateFlow<String?> = _incidentTypeError.asStateFlow()

    private val _commentError = MutableStateFlow<String?>(null)
    val commentError: StateFlow<String?> = _commentError.asStateFlow()

    // Métodos para actualizar los campos
    fun updateIncidentType(value: String) {
        _incidentType.value = value
        validateIncidentType()
        resetIncidentStateIfError()
    }

    fun updateComment(value: String) {
        _comment.value = value
        validateComment() // Agregar validación del comment
        resetIncidentStateIfError()
    }

    private fun resetIncidentStateIfError() {
        if (_incidentState.value is IncidentState.Error) {
            _incidentState.value = IncidentState.Initial
        }
    }

    // CORREGIR: Validar ambos campos
    private fun validateFields(): Pair<Boolean, String?> {
        validateIncidentType()
        validateComment()

        val hasIncidentTypeError = _incidentTypeError.value != null
        val hasCommentError = _commentError.value != null

        val isValid = !hasIncidentTypeError && !hasCommentError

        val errorMessage = when {
            hasIncidentTypeError && hasCommentError -> "Debes seleccionar un tipo de incidencia y agregar un comentario"
            hasIncidentTypeError -> "Debes seleccionar un tipo de incidencia"
            hasCommentError -> "Debes agregar un comentario"
            else -> null
        }

        return Pair(isValid, errorMessage)
    }

    fun registerIncident(tripId: Int, driverId: Int) {
        val (isValid, errorMessage) = validateFields()
        if (!isValid) {
            _incidentState.value = IncidentState.Error(errorMessage ?: "Por favor, completa todos los campos requeridos")
            return
        }

        viewModelScope.launch {
            _incidentState.value = IncidentState.Loading
            try {
                val incidentRequest = RegisterIncidentRequestDTO(
                    userId = driverId,
                    travelId = tripId,
                    typeName = _incidentType.value,
                    comment = _comment.value
                )

                val result = travelRepository.registerIncident(incidentRequest)
                _incidentState.value = IncidentState.Success(result)
            } catch (e: Exception) {
                _incidentState.value = IncidentState.Error(e.message ?: "Error al registrar la incidencia")
            }
        }
    }

    // Validaciones
    private fun validateIncidentType() {
        if (_incidentType.value.isBlank()) {
            _incidentTypeError.value = "Debes seleccionar un tipo de incidencia"
        } else {
            _incidentTypeError.value = null
        }
    }

    // CORREGIR: Hacer la validación del comentario más específica
    private fun validateComment() {
        when {
            _comment.value.isBlank() -> {
                _commentError.value = "Debes ingresar un comentario"
            }
            _comment.value.trim().length < 5 -> {
                _commentError.value = "El comentario debe tener al menos 5 caracteres"
            }
            else -> {
                _commentError.value = null
            }
        }
    }

    // Métodos para consumir eventos
    fun onIncidentErrorShown() {
        if (_incidentState.value is IncidentState.Error) {
            _incidentState.value = IncidentState.Initial
        }
    }

    fun onIncidentSuccessShown() {
        if (_incidentState.value is IncidentState.Success) {
            _incidentState.value = IncidentState.Initial
        }
    }

    fun onCancelSuccessShown() {
        if (_cancelState.value is CancelState.Success) {
            _cancelState.value = CancelState.Initial
        }
    }

    // Estados para el registro de incidencias
    sealed class IncidentState {
        data object Initial : IncidentState()
        data object Loading : IncidentState()
        data class Success(val result: RegisterIncidentResult) : IncidentState()
        data class Error(val message: String) : IncidentState()
    }

    /*
        En esta parte se maneja para calificar un viaje.
     */

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

    // Métodos para actualizar los campos
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
                Log.d("CitizenTripViewModel", "Error al calificar el viaje: ${e.message}")
            }
        }
    }

    // Métodos para consumir eventos
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

    // Estados para la calificación
    sealed class RateState {
        data object Initial : RateState()
        data object Loading : RateState()
        data class Success(val result: TravelRateResult) : RateState()
        data class Error(val message: String) : RateState()
    }

}