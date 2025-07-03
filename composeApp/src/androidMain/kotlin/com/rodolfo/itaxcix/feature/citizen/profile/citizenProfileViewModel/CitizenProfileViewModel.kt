package com.rodolfo.itaxcix.feature.citizen.profile.citizenProfileViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodolfo.itaxcix.data.local.PreferencesManager
import com.rodolfo.itaxcix.data.remote.dto.citizen.CitizenToDriverRequestDTO
import com.rodolfo.itaxcix.domain.model.CitizenToDriverResult
import com.rodolfo.itaxcix.domain.model.ProfileInformationCitizenResult
import com.rodolfo.itaxcix.domain.model.UploadProfilePhotoResult
import com.rodolfo.itaxcix.domain.repository.CitizenRepository
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
class CitizenProfileViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val userRepository: UserRepository,
    private val citizenRepository: CitizenRepository
) : ViewModel() {

    val userData = preferencesManager.userData

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Initial)
    val uploadState: StateFlow<UploadState> = _uploadState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

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
                Log.e("CitizenProfileViewModel", "Error al cargar foto: ${e.message}")
            } finally {
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
                delay(2000)
                onSuccessShown()
            } catch (e: Exception) {
                _uploadState.value = UploadState.Error(e.message ?: "Error al subir la foto")
                delay(2000)
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
        Cargar información del perfil del ciudadano.
        Esta función obtiene la información del perfil del ciudadano desde el repositorio
        y actualiza el estado del ViewModel con el resultado.
     */

    private val _profileInfoState = MutableStateFlow<ProfileInfoState>(ProfileInfoState.Loading)
    val profileInfoState: StateFlow<ProfileInfoState> = _profileInfoState

    fun loadProfileInformation() {
        viewModelScope.launch {
            _profileInfoState.value = ProfileInfoState.Loading
            try {
                val userId = userData.value?.id ?: return@launch
                val profileInfo = citizenRepository.profileInformationCitizen(userId)
                _profileInfoState.value = ProfileInfoState.Success(profileInfo)
            } catch (e: Exception) {
                _profileInfoState.value = ProfileInfoState.Error(e.message ?: "Error al cargar información del perfil")
            }
        }
    }

    sealed class ProfileInfoState {
        data object Loading : ProfileInfoState()
        data class Success(val profileInfo: ProfileInformationCitizenResult) : ProfileInfoState()
        data class Error(val message: String) : ProfileInfoState()
    }


    /*
        Convertir ciudadano a conductor.
        Esta función inicia el proceso de conversión de ciudadano a conductor y actualiza el estado del ViewModel
        con el resultado de la operación.
     */

    private val _convertToDriverState = MutableStateFlow<ConvertToDriverState>(ConvertToDriverState.Initial)
    val convertToDriverState: StateFlow<ConvertToDriverState> = _convertToDriverState

    private val _plateValue = MutableStateFlow("")
    val plateValue: StateFlow<String> = _plateValue

    private val _plateValueError = MutableStateFlow<String?>(null)
    val plateValueError: StateFlow<String?> = _plateValueError

    fun onPlateValueChange(newValue: String) {
        _plateValue.value = newValue
        validatePlateValue()
        resetConvertToDriverStateIfError()
    }

    private fun validatePlateValue() {
        val plate = _plateValue.value
        when {
            plate.isBlank() -> {
                _plateValueError.value = "La placa no puede estar vacía"
            }
            plate.contains(" ") -> {
                _plateValueError.value = "La placa no puede contener espacios"
            }
            plate.length != 6 -> {
                _plateValueError.value = "La placa debe tener exactamente 6 caracteres"
            }
            !plate.matches(Regex("^[A-Z0-9]{6}$")) -> {
                _plateValueError.value = "La placa debe tener solo letras y números"
            }
            else -> {
                _plateValueError.value = null
            }
        }
    }

    private fun validateFieldsForConversion(): Pair<Boolean, String?> {
        val errorMessages = mutableListOf<String>()
        var isValid = true

        if (_plateValue.value.isBlank()) {
            _plateValueError.value = "La placa no puede estar vacía"
            errorMessages.add("• La placa no puede estar vacía")
            isValid = false
        } else if (_plateValue.value.length != 6) {
            _plateValueError.value = "La placa debe tener exactamente 6 caracteres"
            errorMessages.add("• La placa debe tener exactamente 6 caracteres")
            isValid = false
        } else if (!_plateValue.value.matches(Regex("^[A-Z0-9]{6}$"))) {
            _plateValueError.value = "La placa debe tener solo letras y números"
            errorMessages.add("• La placa debe tener solo letras y números")
            isValid = false
        } else {
            _plateValueError.value = null
        }

        return Pair(isValid, if (errorMessages.isNotEmpty()) errorMessages.joinToString("\n") else null)
    }

    fun convertToDriver() {
        _convertToDriverState.value = ConvertToDriverState.Initial

        val (isValid, errorMessage) = validateFieldsForConversion()
        if (!isValid) {
            _convertToDriverState.value = ConvertToDriverState.Error(errorMessage ?: "Por favor, corrige los errores antes de continuar.")
            return
        }

        viewModelScope.launch {
            _convertToDriverState.value = ConvertToDriverState.Loading
            try {
                val userId = userData.value?.id ?: return@launch

                val request = CitizenToDriverRequestDTO(
                    userId = userId,
                    plateValue = _plateValue.value
                )

                val result = citizenRepository.citizenToDriver(request)
                _convertToDriverState.value = ConvertToDriverState.Success(result)
            } catch (e: Exception) {
                _convertToDriverState.value = ConvertToDriverState.Error(e.message ?: "Error al convertir a conductor")
            }
        }
    }

    fun onConvertToDriverErrorShown() {
        if (_convertToDriverState.value is ConvertToDriverState.Error) {
            _convertToDriverState.value = ConvertToDriverState.Initial
        }
    }

    fun onConvertToDriverSuccessShown() {
        if (_convertToDriverState.value is ConvertToDriverState.Success) {
            _convertToDriverState.value = ConvertToDriverState.Initial
        }
    }

    private fun resetConvertToDriverStateIfError() {
        if (_convertToDriverState.value is ConvertToDriverState.Error) {
            _convertToDriverState.value = ConvertToDriverState.Initial
        }
    }

    sealed class ConvertToDriverState {
        data object Initial : ConvertToDriverState()
        data object Loading : ConvertToDriverState()
        data class Success(val message: CitizenToDriverResult) : ConvertToDriverState()
        data class Error(val message: String) : ConvertToDriverState()
    }
}