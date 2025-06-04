package com.rodolfo.itaxcix.data.repository

import android.util.Log
import com.rodolfo.itaxcix.data.local.PreferencesManager
import com.rodolfo.itaxcix.data.local.UserData
import com.rodolfo.itaxcix.data.remote.dto.UserDTO
import com.rodolfo.itaxcix.data.remote.api.ApiService
import com.rodolfo.itaxcix.data.remote.dto.CitizenRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.DriverRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.ValidateBiometricRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.ValidateDocumentRequestDTO
import com.rodolfo.itaxcix.domain.model.LoginResult
import com.rodolfo.itaxcix.domain.model.RecoveryResult
import com.rodolfo.itaxcix.domain.model.RegisterDriverResult
import com.rodolfo.itaxcix.domain.model.RegisterResult
import com.rodolfo.itaxcix.domain.model.ResetPasswordResult
import com.rodolfo.itaxcix.domain.model.User
import com.rodolfo.itaxcix.domain.model.ValidateBiometricResult
import com.rodolfo.itaxcix.domain.model.ValidateDocumentResult
import com.rodolfo.itaxcix.domain.model.VerifyCodeResult
import com.rodolfo.itaxcix.domain.repository.DriverRepository
import com.rodolfo.itaxcix.domain.repository.UserRepository

class UserRepositoryImpl(
    private val apiService: ApiService,
    private val driverRepository: DriverRepository,
    private val preferencesManager: PreferencesManager
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
            userId = response.userId,
            personId = response.personId
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

    override suspend fun login(documentValue: String, password: String): LoginResult {
        val response = apiService.login(documentValue, password)

        val token = response.data.token
        val userId = response.data.userId.toString()
        val document = response.data.documentValue
        val roles = response.data.roles
        val permissions = response.data.permissions

        var user = User(
            id = userId,
            nickname = "",
            document = document,
            name = "",
            email = "",
            phone = "",
            address = "",
            city = "",
            country = "",
            rol = roles,
            authToken = token
        )

        preferencesManager.saveUserData(
            UserData(
                id = userId.toInt(),
                name = "",
                nickname = "",
                document = document,
                email = "",
                phone = "",
                address = "",
                city = "",
                country = "",
                roles = roles,
                permissions = permissions,
                status = "UNAVAILABLE",
                isDriverAvailable = false,
                lastDriverStatusUpdate = null,
                authToken = token
            )
        )

        // Obtener el estado del conductor si el usuario tiene el rol de conductor
        if (roles.contains("Conductor")) {
            try {
                val driverStatus = driverRepository.getDriverStatus(userId.toInt())

                // Actualizar el usuario con el estado del conductor
                user = user.copy(
                    isDriverAvailable = driverStatus.isDriverAvailable,
                    lastDriverStatusUpdate = driverStatus.lastDriverStatusUpdate
                )

                // También actualizar UserData en las preferencias
                val currentUserData = preferencesManager.userData.value
                currentUserData?.let {
                    preferencesManager.saveUserData(
                        it.copy(
                            isDriverAvailable = driverStatus.isDriverAvailable,
                            lastDriverStatusUpdate = driverStatus.lastDriverStatusUpdate
                        )
                    )
                }
            } catch (e: Exception) {
                // Manejar el error pero continuar con el inicio de sesión
                Log.e("UserRepositoryImpl", "Error al obtener estado del conductor: ${e.message}")
            }
        }


        return LoginResult(message =  response.message, user = user)
    }

    override suspend fun recovery(contactTypeId: Int, contact: String): RecoveryResult {
        val response = apiService.recovery(contactTypeId, contact)
        return RecoveryResult(
            message = response.message
        )
    }

    override suspend fun verifyCode(code: String, contactTypeId: Int, contact: String): VerifyCodeResult {
        val response = apiService.verifyCode(code, contactTypeId, contact)
        return VerifyCodeResult(
            message = response.message,
            userId = response.userId,
        )
    }

    override suspend fun resetPassword(userId: String, newPassword: String): ResetPasswordResult {
        val response = apiService.resetPassword(userId, newPassword)
        return ResetPasswordResult(
            message = response.message
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