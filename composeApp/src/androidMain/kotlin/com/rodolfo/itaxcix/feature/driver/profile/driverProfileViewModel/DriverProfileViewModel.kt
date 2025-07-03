package com.rodolfo.itaxcix.feature.driver.profile.driverProfileViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodolfo.itaxcix.data.local.PreferencesManager
import com.rodolfo.itaxcix.data.remote.dto.driver.DriverToCitizenRequestDTO
import com.rodolfo.itaxcix.domain.model.ProfileInformationDriverResult
import com.rodolfo.itaxcix.domain.model.UploadProfilePhotoResult
import com.rodolfo.itaxcix.domain.repository.DriverRepository
import com.rodolfo.itaxcix.domain.repository.UserRepository
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
    private val userRepository: UserRepository,
    private val driverRepository: DriverRepository
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
            _isLoading.value = false
        }
    }

    sealed class UploadState {
        data object Initial : UploadState()
        data object Loading : UploadState()
        data class Success(val message: UploadProfilePhotoResult) : UploadState()
        data class Error(val message: String) : UploadState()
    }

    /*
        Cargar información del perfil del conductor
        Esta función obtiene la información del perfil del conductor desde el repositorio
        y actualiza el estado del ViewModel con el resultado.
     */

    private val _profileInfoState = MutableStateFlow<ProfileInfoState>(ProfileInfoState.Loading)
    val profileInfoState: StateFlow<ProfileInfoState> = _profileInfoState

    fun loadProfileInformation() {
        viewModelScope.launch {
            _profileInfoState.value = ProfileInfoState.Loading
            try {
                val userId = userData.value?.id ?: return@launch
                val profileInfo = driverRepository.profileInformationDriver(userId)
                _profileInfoState.value = ProfileInfoState.Success(profileInfo)
            } catch (e: Exception) {
                _profileInfoState.value = ProfileInfoState.Error(e.message ?: "Error al cargar la información del perfil")
            }
        }
    }

    sealed class ProfileInfoState {
        data object Loading : ProfileInfoState()
        data class Success(val profileInfo: ProfileInformationDriverResult) : ProfileInfoState()
        data class Error(val message: String) : ProfileInfoState()
    }

    /*
        Convertir conductor a ciudadano
        Esta función permite al conductor solicitar la conversión a ciudadano.
        Se actualiza el estado del ViewModel según el resultado de la operación.
     */

    // Nuevo estado para driver to citizen
    private val _driverToCitizenState = MutableStateFlow<DriverToCitizenState>(DriverToCitizenState.Initial)
    val driverToCitizenState: StateFlow<DriverToCitizenState> = _driverToCitizenState

    fun convertToCitizen() {
        val userId = userData.value?.id
        if (userId == null) {
            _driverToCitizenState.value = DriverToCitizenState.Error("Usuario no encontrado")
            return
        }

        viewModelScope.launch {
            try {
                _driverToCitizenState.value = DriverToCitizenState.Loading

                val request = DriverToCitizenRequestDTO(userId = userId)
                val result = driverRepository.driverToCitizen(request)

                Log.d("DriverToCitizen", "Result: $result")

                _driverToCitizenState.value = DriverToCitizenState.Success

                // Mantener el estado de éxito por 2 segundos
                delay(2000)
                _driverToCitizenState.value = DriverToCitizenState.Initial

            } catch (e: Exception) {
                Log.e("DriverToCitizen", "Error converting to citizen: ${e.message}")
                _driverToCitizenState.value = DriverToCitizenState.Error(
                    e.message ?: "Error al solicitar conversión a ciudadano"
                )

                // Mantener el estado de error por 3 segundos
                delay(3000)
                _driverToCitizenState.value = DriverToCitizenState.Initial
            }
        }
    }

    fun resetDriverToCitizenState() {
        _driverToCitizenState.value = DriverToCitizenState.Initial
    }

    sealed class DriverToCitizenState {
        data object Initial : DriverToCitizenState()
        data object Loading : DriverToCitizenState()
        data object Success : DriverToCitizenState()
        data class Error(val message: String) : DriverToCitizenState()
    }
}