package com.rodolfo.itaxcix.data.repository

import com.rodolfo.itaxcix.data.remote.dto.UserDTO
import com.rodolfo.itaxcix.data.remote.api.ApiService
import com.rodolfo.itaxcix.data.remote.dto.CitizenRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.DriverRegisterRequestDTO
import com.rodolfo.itaxcix.domain.model.LoginResult
import com.rodolfo.itaxcix.domain.model.RecoveryResult
import com.rodolfo.itaxcix.domain.model.RegisterDriverResult
import com.rodolfo.itaxcix.domain.model.RegisterResult
import com.rodolfo.itaxcix.domain.model.ResetPasswordResult
import com.rodolfo.itaxcix.domain.model.User
import com.rodolfo.itaxcix.domain.model.VerifyCodeResult
import com.rodolfo.itaxcix.domain.repository.UserRepository

class UserRepositoryImpl(private val apiService: ApiService) : UserRepository {
    override suspend fun getUsers(): List<User> {
        return apiService.getUsers().map { it.toDomain() }
    }

    override suspend fun getUserById(id: String): User {
        return apiService.getUserById(id).toDomain()
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

    override suspend fun login(username: String, password: String): LoginResult {
        val response = apiService.login(username, password)
        val user = User(
            id = response.user.user.id.toString(),
            nickname = response.user.user.alias,
            name = "",
            email = "",
            phone = "",
            address = "",
            city = "",
            country = "",
            rol = response.user.user.roles.toString()
        )
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
            name = name,
            email = email,
            phone = phone,
            address = address,
            city = city,
            country = country
        )
    }
}