package com.rodolfo.itaxcix.data.repository

import android.util.Log
import com.rodolfo.itaxcix.data.local.PreferencesManager
import com.rodolfo.itaxcix.data.local.UserData
import com.rodolfo.itaxcix.data.remote.dto.UserDTO
import com.rodolfo.itaxcix.data.remote.api.ApiService
import com.rodolfo.itaxcix.data.remote.dto.auth.CitizenRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.DriverRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.ResendCodeRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.ValidateBiometricRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.ValidateDocumentRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.ValidateVehicleRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.VerifyCodeRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.common.ChangeEmailRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.common.ChangePhoneRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.common.VerifyChangeEmailRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.common.VerifyChangePhoneRequestDTO
import com.rodolfo.itaxcix.data.remote.websocket.CitizenWebSocketService
import com.rodolfo.itaxcix.domain.model.ChangeEmailResult
import com.rodolfo.itaxcix.domain.model.ChangePhoneResult
import com.rodolfo.itaxcix.domain.model.GetProfilePhotoResult
import com.rodolfo.itaxcix.domain.model.HelpCenterResult
import com.rodolfo.itaxcix.domain.model.LoginResult
import com.rodolfo.itaxcix.domain.model.RatingsCommentsResult
import com.rodolfo.itaxcix.domain.model.RecoveryResult
import com.rodolfo.itaxcix.domain.model.RegisterDriverResult
import com.rodolfo.itaxcix.domain.model.RegisterResult
import com.rodolfo.itaxcix.domain.model.ResendCodeRegisterResult
import com.rodolfo.itaxcix.domain.model.ResetPasswordResult
import com.rodolfo.itaxcix.domain.model.UploadProfilePhotoResult
import com.rodolfo.itaxcix.domain.model.User
import com.rodolfo.itaxcix.domain.model.ValidateBiometricResult
import com.rodolfo.itaxcix.domain.model.ValidateDocumentResult
import com.rodolfo.itaxcix.domain.model.ValidateVehicleResult
import com.rodolfo.itaxcix.domain.model.VerifyChangeEmailResult
import com.rodolfo.itaxcix.domain.model.VerifyChangePhoneResult
import com.rodolfo.itaxcix.domain.model.VerifyCodeRegisterResult
import com.rodolfo.itaxcix.domain.model.VerifyCodeResult
import com.rodolfo.itaxcix.domain.repository.DriverRepository
import com.rodolfo.itaxcix.domain.repository.UserRepository

class UserRepositoryImpl(
    private val apiService: ApiService,
    private val driverRepository: DriverRepository,
    private val preferencesManager: PreferencesManager,
    private val citizenWebSocketService: CitizenWebSocketService
) : UserRepository {
    override suspend fun getUsers(): List<User> {
        return apiService.getUsers().map { it.toDomain() }
    }

    override suspend fun getUserById(id: String): User {
        return apiService.getUserById(id).toDomain()
    }

    override suspend fun validateDocument(document: ValidateDocumentRequestDTO): ValidateDocumentResult {
        val response = apiService.validateDocument(document)
        return ValidateDocumentResult(
            message = response.message,
            personId = response.data?.personId
        )
    }

    override suspend fun validateVehicle(vehicle: ValidateVehicleRequestDTO): ValidateVehicleResult {
        val response = apiService.validateVehicle(vehicle)
        return ValidateVehicleResult(
            message = response.message,
            personId = response.data.personId,
            vehicleId = response.data.vehicleId
        )
    }

    override suspend fun validateBiometric(biometric: ValidateBiometricRequestDTO) : ValidateBiometricResult {
        val response = apiService.validateBiometric(biometric)
        return ValidateBiometricResult(
            message = response.message,
            personId = response.data?.personId
        )
    }

    override suspend fun registerCitizen(user: CitizenRegisterRequestDTO): RegisterResult {
        val response = apiService.registerCitizen(user)
        return RegisterResult(
            message = response.message,
            userId = response.data.userId,
        )
    }

    override suspend fun registerDriver(user: DriverRegisterRequestDTO): RegisterDriverResult {
        val response = apiService.registerDriver(user)
        return RegisterDriverResult(
            message = response.message,
            userId = response.userId,
            personId = response.personId,
            vehicleId = response.vehicleId
        )
    }

    override suspend fun verifyCodeRegister(verifyCode: VerifyCodeRegisterRequestDTO): VerifyCodeRegisterResult {
        val response = apiService.verifyCodeRegister(verifyCode)
        return VerifyCodeRegisterResult(
            message = response.message
        )
    }

    override suspend fun resendCodeRegister(resendCode: ResendCodeRegisterRequestDTO): ResendCodeRegisterResult {
        val response = apiService.resendCodeRegister(resendCode)
        return ResendCodeRegisterResult(
            message = response.message
        )
    }

    override suspend fun login(documentValue: String, password: String): LoginResult {
        val response = apiService.login(documentValue, password)

        val loginData = response.data

        // Guardar información básica del usuario
        val userData = UserData(
            id = loginData.userId,
            firstName = loginData.firstName,
            lastName = loginData.lastName,
            fullName = "${loginData.firstName} ${loginData.lastName}",
            isTucActive = null,
            rating = loginData.rating,
            nickname = "",
            document = loginData.documentValue,
            email = "",
            phone = "",
            address = "",
            city = "",
            country = "",
            roles = loginData.roles.map { it.name },
            permissions = loginData.permissions.map { it.name },
            status = "",
            isDriverAvailable = loginData.availabilityStatus ?: false,
            lastDriverStatusUpdate = null,
            authToken = loginData.token
        )

        // Guardar datos básicos primero
        preferencesManager.saveUserData(userData)

        // Cargar la foto de perfil inmediatamente después del login
        try {
            val photoResult = getProfilePhoto(loginData.userId)
            if (photoResult.base64Image != null) {
                val updatedUserData = userData.copy(
                    profileImage = photoResult.base64Image
                )
                preferencesManager.saveUserData(updatedUserData)
            }
        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "Error cargando foto de perfil: ${e.message}")
        }

        // Conectar WebSocket si es ciudadano
        if (loginData.roles.any { it.id == 1 }) { // El id 1 es para ciudadanos
            try {
                Log.d("UserRepositoryImpl", "Conectando al WebSocket para el ciudadano")
                citizenWebSocketService.connect()
            } catch (e: Exception) {
                Log.e("UserRepositoryImpl", "Error conectando al WebSocket: ${e.message}")
            }
        }

        return LoginResult(
            message = response.message,
            data = LoginResult.LoginData(
                token = loginData.token,
                userId = loginData.userId,
                documentValue = loginData.documentValue,
                firstName = loginData.firstName,
                lastName = loginData.lastName,
                roles = loginData.roles.map { LoginResult.LoginData.Role(it.id, it.name) },
                permissions = loginData.permissions.map { LoginResult.LoginData.Permission(it.id, it.name) },
                rating = loginData.rating,
                availabilityStatus = loginData.availabilityStatus
            )
        )
    }

    override suspend fun recovery(contactTypeId: Int, contact: String): RecoveryResult {
        val response = apiService.recovery(contactTypeId, contact)
        return RecoveryResult(
            message = response.message,
            userId = response.data.userId
        )
    }

    override suspend fun verifyCode(userId: Int, code: String): VerifyCodeResult {
        val response = apiService.verifyCode(userId, code)
        return VerifyCodeResult(
            message = response.message,
            token = response.data.token
        )
    }

    override suspend fun resetPassword(userId: Int, newPassword: String, repeatPassword: String, token: String): ResetPasswordResult {
        val response = apiService.resetPassword(userId, newPassword, repeatPassword, token)
        return ResetPasswordResult(
            message = response.message
        )
    }

    override suspend fun getProfilePhoto(userId: Int): GetProfilePhotoResult {
        val response = apiService.getProfilePhoto(userId)
        return GetProfilePhotoResult(
            message = response.message,
            base64Image = response.data.base64Image
        )
    }

    override suspend fun uploadProfilePhoto(userId: Int, base64Image: String): UploadProfilePhotoResult {
        val response = apiService.uploadProfilePhoto(userId, base64Image)
        return UploadProfilePhotoResult(
            message = response.message
        )
    }

    override suspend fun changeEmail(changeEmail: ChangeEmailRequestDTO): ChangeEmailResult {
        val response = apiService.changeEmail(changeEmail)
        return ChangeEmailResult(
            message = response.message
        )
    }

    override suspend fun verifyChangeEmail(verifyChange: VerifyChangeEmailRequestDTO): VerifyChangeEmailResult {
        val response = apiService.verifyChangeEmail(verifyChange)
        return VerifyChangeEmailResult(
            message = response.message
        )
    }

    override suspend fun changePhone(changePhone: ChangePhoneRequestDTO): ChangePhoneResult {
        val response = apiService.changePhone(changePhone)
        return ChangePhoneResult(
            message = response.message
        )
    }

    override suspend fun verifyChangePhone(verifyChange: VerifyChangePhoneRequestDTO): VerifyChangePhoneResult {
        val response = apiService.verifyChangePhone(verifyChange)
        return VerifyChangePhoneResult(
            message = response.message
        )
    }

    override suspend fun helpCenter(): HelpCenterResult {
        val response = apiService.helpCenter()
        return HelpCenterResult(
            message = response.message,
            data = response.data.map { HelpCenterResult.HelpCenterData(
                id = it.id,
                title = it.title,
                subtitle = it.subtitle,
                answer = it.answer,
                active = it.active
            ) }
        )
    }

    override suspend fun getRatingCommentsUser(userId: Int): RatingsCommentsResult {
        val response = apiService.getRatingCommentsUser(userId)
        return RatingsCommentsResult(
            message = response.message,
            data = RatingsCommentsResult.RatingsData(
                averageRating = response.data.averageRating,
                totalRatings = response.data.totalRatings,
                comments = response.data.comments.map { comment ->
                    RatingsCommentsResult.RatingsData.CommentInfo(
                        id = comment.id,
                        travelId = comment.travelId,
                        raterName = comment.raterName,
                        score = comment.score,
                        comment = comment.comment,
                        createdAt = comment.createdAt
                    )
                },
                meta = RatingsCommentsResult.RatingsData.MetaInfo(
                    total = response.data.meta.total,
                    perPage = response.data.meta.perPage,
                    currentPage = response.data.meta.currentPage,
                    lastPage = response.data.meta.lastPage,
                    search = response.data.meta.search,
                    filters = response.data.meta.filters,
                    sortBy = response.data.meta.sortBy,
                    sortDirection = response.data.meta.sortDirection
                )
            )
        )
    }

    // Funciones de extensión para mapeo
    private fun UserDTO.toDomain(): User {
        return User(
            id = id ?: "",
            nickname = nickname ?: "",
            document = document ?: "",
            name = name ?: "",
            email = email ?: "",
            phone = phone ?: "",
            address = address ?: "",
            city = city ?: "",
            country = country ?: ""
        )
    }

    private fun User.toDto(): UserDTO {
        return UserDTO(
            id = id,
            nickname = nickname,
            document = document,
            name = name,
            email = email,
            phone = phone,
            address = address,
            city = city,
            country = country
        )
    }
}