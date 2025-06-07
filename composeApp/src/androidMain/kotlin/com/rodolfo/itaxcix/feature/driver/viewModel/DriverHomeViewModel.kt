package com.rodolfo.itaxcix.feature.driver.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodolfo.itaxcix.data.local.PreferencesManager
import com.rodolfo.itaxcix.data.local.UserData
import com.rodolfo.itaxcix.domain.model.DriverAvailabilityResult
import com.rodolfo.itaxcix.domain.repository.DriverRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriverHomeViewModel @Inject constructor(
    private val driverRepository: DriverRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val userData: StateFlow<UserData?> = preferencesManager.userData
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _driverHomeState = MutableStateFlow<DriverHomeUiState>(DriverHomeUiState.Initial)
    val driverHomeState: StateFlow<DriverHomeUiState> = _driverHomeState.asStateFlow()

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

                _driverHomeState.value = DriverHomeUiState.Success(driverStatus)
            } catch (e: Exception) {
                _driverHomeState.value = DriverHomeUiState.Error(e.message ?: "Error")
            }
        }
    }

    sealed class DriverHomeUiState {
        object Initial : DriverHomeUiState()
        object Loading : DriverHomeUiState()
        data class Success(val userData: DriverAvailabilityResult) : DriverHomeUiState()
        data class Error(val message: String) : DriverHomeUiState()
    }
}