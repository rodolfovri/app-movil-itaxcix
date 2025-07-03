package com.rodolfo.itaxcix.feature.auth.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodolfo.itaxcix.domain.model.HelpCenterResult
import com.rodolfo.itaxcix.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HelpCenterViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _helpCenterState = MutableStateFlow<HelpCenterState>(HelpCenterState.Loading)
    val helpCenterState: StateFlow<HelpCenterState> = _helpCenterState

    private val _expandedItems = MutableStateFlow<Set<Int>>(emptySet())
    val expandedItems: StateFlow<Set<Int>> = _expandedItems

    init {
        loadHelpCenter()
    }

    private fun loadHelpCenter() {
        viewModelScope.launch {
            _helpCenterState.value = HelpCenterState.Loading
            try {
                val helpCenterResult = userRepository.helpCenter()
                _helpCenterState.value = HelpCenterState.Success(helpCenterResult)
            } catch (e: Exception) {
                Log.e("HelpCenterViewModel", "Error al cargar centro de ayuda: ${e.message}")
                _helpCenterState.value = HelpCenterState.Error(e.message ?: "Error al cargar el centro de ayuda")
            }
        }
    }

    fun toggleExpanded(itemId: Int) {
        val currentExpanded = _expandedItems.value.toMutableSet()
        if (currentExpanded.contains(itemId)) {
            currentExpanded.remove(itemId)
        } else {
            currentExpanded.add(itemId)
        }
        _expandedItems.value = currentExpanded
    }

    fun retryLoadHelpCenter() {
        loadHelpCenter()
    }

    sealed class HelpCenterState {
        data object Loading : HelpCenterState()
        data class Success(val helpCenter: HelpCenterResult) : HelpCenterState()
        data class Error(val message: String) : HelpCenterState()
    }
}