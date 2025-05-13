package com.rodolfo.itaxcix.domain.repository

import com.rodolfo.itaxcix.data.remote.dto.CitizenRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.DriverRegisterRequestDTO
import com.rodolfo.itaxcix.domain.model.LoginResult
import com.rodolfo.itaxcix.domain.model.RecoveryResult
import com.rodolfo.itaxcix.domain.model.RegisterDriverResult
import com.rodolfo.itaxcix.domain.model.RegisterResult
import com.rodolfo.itaxcix.domain.model.ResetPasswordResult
import com.rodolfo.itaxcix.domain.model.User
import com.rodolfo.itaxcix.domain.model.VerifyCodeResult

interface UserRepository {
    suspend fun getUsers(): List<User>
    suspend fun getUserById(id: String): User
    suspend fun registerCitizen(user: CitizenRegisterRequestDTO): RegisterResult
    suspend fun registerDriver(user: DriverRegisterRequestDTO): RegisterDriverResult
    suspend fun login(username: String, password: String): LoginResult
    suspend fun recovery(contactTypeId: Int, contact: String): RecoveryResult
    suspend fun verifyCode(code: String, contactTypeId: Int, contact: String): VerifyCodeResult
    suspend fun resetPassword(userId: String, newPassword: String): ResetPasswordResult
}