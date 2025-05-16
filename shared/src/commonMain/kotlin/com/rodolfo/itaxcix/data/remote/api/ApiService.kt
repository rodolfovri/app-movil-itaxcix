package com.rodolfo.itaxcix.data.remote.api

import com.rodolfo.itaxcix.data.remote.dto.UserDTO
import com.rodolfo.itaxcix.data.remote.dto.CitizenRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.DriverAvailabilityResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.DriverRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.DriverStatusResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.LoginResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.RecoveryResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.RegisterDriverResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.RegisterResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.ResetPasswordResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.VerifyCodeResponseDTO

interface ApiService {
    suspend fun getUsers(): List<UserDTO>
    suspend fun getUserById(id: String): UserDTO
    suspend fun registerCitizen(citizen: CitizenRegisterRequestDTO): RegisterResponseDTO
    suspend fun registerDriver(driver: DriverRegisterRequestDTO): RegisterDriverResponseDTO
    suspend fun login(username: String, password: String): LoginResponseDTO
    suspend fun recovery(contactTypeId: Int, contact: String): RecoveryResponseDTO
    suspend fun verifyCode(code: String, contactTypeId: Int, contact: String): VerifyCodeResponseDTO
    suspend fun resetPassword(userId: String, newPassword: String): ResetPasswordResponseDTO
    suspend fun getDriverStatus(driverId: Int): DriverStatusResponseDTO
    suspend fun driverActivateAvailability(driverId: Int): DriverAvailabilityResponseDTO
    suspend fun driverDeactivateAvailability(driverId: Int): DriverAvailabilityResponseDTO
}