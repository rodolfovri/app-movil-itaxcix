package com.rodolfo.itaxcix.feature.citizen.history.citizenHistoryViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodolfo.itaxcix.data.local.PreferencesManager
import com.rodolfo.itaxcix.domain.model.TravelHistoryResult
import com.rodolfo.itaxcix.domain.model.TravelRatingResult
import com.rodolfo.itaxcix.domain.repository.TravelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CitizenHistoryViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val travelRepository: TravelRepository
): ViewModel() {

    val userData = preferencesManager.userData

    private val _historyState = MutableStateFlow<HistoryState>(HistoryState.Loading)
    val historyState: StateFlow<HistoryState> = _historyState

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage

    fun loadTravelHistory(page: Int = 1) {
        viewModelScope.launch {
            try {
                _historyState.value = HistoryState.Loading
                _currentPage.value = page

                println("Loading travel history, Page: $page")

                val userId = userData.value?.id ?: return@launch
                val result = travelRepository.travelHistory(userId, page)
                println("Travel history loaded - Current page: ${result.data.meta.currentPage}, Total: ${result.data.meta.total}")

                _historyState.value = HistoryState.Success(result)
            } catch (e: Exception) {
                _historyState.value = HistoryState.Error(e.message ?: "Ha ocurrido un error")
            }
        }
    }

    fun goToNextPage() {
        val currentState = _historyState.value
        if (currentState is HistoryState.Success) {
            val meta = currentState.data.data.meta
            val nextPage = _currentPage.value + 1
            if (nextPage <= meta.lastPage) {
                loadTravelHistory(nextPage)
            }
        }
    }

    fun goToPreviousPage() {
        val previousPage = _currentPage.value - 1
        if (previousPage >= 1) {
            loadTravelHistory(previousPage)
        }
    }

    fun goToPage(page: Int) {
        val currentState = _historyState.value
        if (currentState is HistoryState.Success) {
            val meta = currentState.data.data.meta
            if (page in 1..meta.lastPage) {
                loadTravelHistory(page)
            }
        }
    }

    sealed class HistoryState {
        data object Loading : HistoryState()
        data class Success(val data: TravelHistoryResult) : HistoryState()
        data class Error(val message: String) : HistoryState()
    }

    private val _travelRatingState = MutableStateFlow<TravelRatingState>(TravelRatingState.Initial)
    val travelRatingState: StateFlow<TravelRatingState> = _travelRatingState

    fun loadTravelRating(travelId: Int) {
        viewModelScope.launch {
            try {
                _travelRatingState.value = TravelRatingState.Loading
                val result = travelRepository.travelRating(travelId)
                _travelRatingState.value = TravelRatingState.Success(result)
            } catch (e: Exception) {
                _travelRatingState.value = TravelRatingState.Error(e.message ?: "Error al cargar las calificaciones")
            }
        }
    }

    fun onTravelRatingErrorShown() {
        if (_travelRatingState.value is TravelRatingState.Error) {
            _travelRatingState.value = TravelRatingState.Initial
        }
    }

    sealed class TravelRatingState {
        data object Initial : TravelRatingState()
        data object Loading : TravelRatingState()
        data class Success(val data: TravelRatingResult) : TravelRatingState()
        data class Error(val message: String) : TravelRatingState()
    }
}