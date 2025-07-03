package com.rodolfo.itaxcix.feature.citizen.viewModelCitizen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodolfo.itaxcix.domain.model.RatingsCommentsResult
import com.rodolfo.itaxcix.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriverRatingsViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _ratingsState = MutableStateFlow<RatingsState>(RatingsState.Loading)
    val ratingsState: StateFlow<RatingsState> = _ratingsState

    fun loadDriverRatings(driverId: Int) {
        viewModelScope.launch {
            _ratingsState.value = RatingsState.Loading
            try {
                val ratingsInfo = userRepository.getRatingCommentsUser(driverId)
                _ratingsState.value = RatingsState.Success(ratingsInfo)
                Log.d("DriverRatingsViewModel", "Calificaciones cargadas: ${ratingsInfo.data.averageRating}")
            } catch (e: Exception) {
                _ratingsState.value = RatingsState.Error(e.message ?: "Error al cargar las calificaciones")
                Log.e("DriverRatingsViewModel", "Error cargando calificaciones: ${e.message}")
            }
        }
    }

    sealed class RatingsState {
        data object Loading : RatingsState()
        data class Success(val ratingsInfo: RatingsCommentsResult) : RatingsState()
        data class Error(val message: String) : RatingsState()
    }
}