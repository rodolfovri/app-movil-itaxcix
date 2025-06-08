package com.rodolfo.itaxcix.feature.driver.profile.driverProfileViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodolfo.itaxcix.data.local.PreferencesManager
import com.rodolfo.itaxcix.domain.model.GetProfilePhotoResult
import com.rodolfo.itaxcix.domain.model.UploadProfilePhotoResult
import com.rodolfo.itaxcix.domain.repository.UserRepository
import com.rodolfo.itaxcix.utils.ImageUtils
import com.rodolfo.itaxcix.utils.ImageUtils.compressAndConvertToBase64
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DriverProfileViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val userRepository: UserRepository
) : ViewModel() {

    val userData = preferencesManager.userData

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Initial)
    val uploadState: StateFlow<UploadState> = _uploadState

    // Si necesitas cargar datos adicionales del perfil que no estén en preferencias:
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Nuevo estado para controlar la carga de la imagen de perfil
    private val _isLoadingProfileImage = MutableStateFlow(false)
    val isLoadingProfileImage: StateFlow<Boolean> = _isLoadingProfileImage

    init {
        loadAdditionalProfileData()
        loadProfileImage()
    }

    private fun loadProfileImage() {
        viewModelScope.launch {
            try {
                val userId = userData.value?.id ?: return@launch

                // Si ya tenemos la imagen en preferencias, no mostramos indicador
                val cachedImage = userData.value?.profileImage
                if (!cachedImage.isNullOrEmpty()) {
                    return@launch
                }

                // Activar indicador de carga
                _isLoadingProfileImage.value = true

                val result = userRepository.getProfilePhoto(userId)

                if (result.base64Image != null) {
                    val currentUserData = userData.value
                    currentUserData?.let {
                        val updatedUserData = it.copy(profileImage = result.base64Image)
                        preferencesManager.saveUserData(updatedUserData)
                    }
                }
            } catch (e: Exception) {
                Log.e("DriverProfileViewModel", "Error al cargar foto: ${e.message}")
            } finally {
                // Desactivar indicador de carga
                _isLoadingProfileImage.value = false
            }
        }
    }

    fun uploadProfilePhoto(file: File) {
        viewModelScope.launch {
            try {
                _uploadState.value = UploadState.Loading

                val compressedBase64 = file.compressAndConvertToBase64(500, 70)
                val formattedBase64 = "data:image/jpg;base64,$compressedBase64"
                val userId = userData.value?.id ?: return@launch

                val result = userRepository.uploadProfilePhoto(userId, formattedBase64)

                val currentUserData = userData.value
                currentUserData?.let {
                    val updatedUserData = it.copy(profileImage = formattedBase64)
                    preferencesManager.saveUserData(updatedUserData)
                }

                _uploadState.value = UploadState.Success(result)
                delay(2000) // Simula un pequeño retraso para mostrar el mensaje de éxito
                onSuccessShown()
            } catch (e: Exception) {
                _uploadState.value = UploadState.Error(e.message ?: "Error al subir la foto")
                delay(2000) // Simula un pequeño retraso para mostrar el mensaje de error
                onErrorShown()
            }
        }
    }

    fun onSuccessShown() {
        if (_uploadState.value is UploadState.Success) {
            _uploadState.value = UploadState.Initial
        }
    }

    fun onErrorShown() {
        if (_uploadState.value is UploadState.Error) {
            _uploadState.value = UploadState.Initial
        }
    }

    private fun loadAdditionalProfileData() {
        viewModelScope.launch {
            _isLoading.value = true
            // Aquí podrías hacer llamadas a la API para obtener datos actualizados
            // Como información del vehículo, histórico de viajes, etc.
            _isLoading.value = false
        }
    }

    // Estados para la subida de la foto
    sealed class UploadState {
        object Initial : UploadState()
        object Loading : UploadState()
        data class Success(val message: UploadProfilePhotoResult) : UploadState()
        data class getSuccess(val message: GetProfilePhotoResult) : UploadState()
        data class Error(val message: String) : UploadState()
    }
}