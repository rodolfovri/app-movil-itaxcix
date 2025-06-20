package com.rodolfo.itaxcix.feature.citizen.travel.travelViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodolfo.itaxcix.data.remote.dto.travel.RegisterIncidentRequestDTO
import com.rodolfo.itaxcix.domain.model.RegisterIncidentResult
import com.rodolfo.itaxcix.domain.model.TravelCancelResult
import com.rodolfo.itaxcix.domain.repository.TravelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CitizenTripViewModel @Inject constructor(
    private val travelRepository: TravelRepository
) : ViewModel() {

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

    // Estados para el registro de incidencias
    sealed class IncidentState {
        data object Initial : IncidentState()
        data object Loading : IncidentState()
        data class Success(val result: RegisterIncidentResult) : IncidentState()
        data class Error(val message: String) : IncidentState()
    }
}