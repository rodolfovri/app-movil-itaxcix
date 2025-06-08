package com.rodolfo.itaxcix.data.remote.api

import com.rodolfo.itaxcix.data.remote.dto.UserDTO
import com.rodolfo.itaxcix.data.remote.dto.CitizenRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.DriverAvailabilityResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.DriverRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.DriverStatusResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.GetProfilePhotoResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.LoginResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.RecoveryResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.RegisterDriverResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.RegisterResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.ResendCodeRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.ResendCodeRegisterResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.ResetPasswordResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.ToggleDriverAvailabilityResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.UploadProfilePhotoResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.ValidateBiometricRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.ValidateBiometricResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.ValidateDocumentRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.ValidateDocumentResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.ValidateVehicleRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.ValidateVehicleResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.VerifyCodeRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.VerifyCodeRegisterResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.VerifyCodeResponseDTO

interface ApiService {
    suspend fun getUsers(): List<UserDTO>
    suspend fun getUserById(id: String): UserDTO
    suspend fun validateDocument(document: ValidateDocumentRequestDTO): ValidateDocumentResponseDTO
    suspend fun validateVehicle(vehicle: ValidateVehicleRequestDTO): ValidateVehicleResponseDTO
    suspend fun validateBiometric(biometric: ValidateBiometricRequestDTO): ValidateBiometricResponseDTO
    suspend fun registerCitizen(citizen: CitizenRegisterRequestDTO): RegisterResponseDTO
    suspend fun registerDriver(driver: DriverRegisterRequestDTO): RegisterDriverResponseDTO
    suspend fun verifyCodeRegister(verifyCode: VerifyCodeRegisterRequestDTO): VerifyCodeRegisterResponseDTO
    suspend fun resendCodeRegister(resendCode: ResendCodeRegisterRequestDTO): ResendCodeRegisterResponseDTO
    suspend fun login(documentValue: String, password: String): LoginResponseDTO
    suspend fun recovery(contactTypeId: Int, contact: String): RecoveryResponseDTO
    suspend fun verifyCode(userId: Int, code: String): VerifyCodeResponseDTO
    suspend fun resetPassword(userId: Int, newPassword: String, repeatPassword: String, token: String): ResetPasswordResponseDTO
    suspend fun getDriverStatus(driverId: Int): DriverStatusResponseDTO
    suspend fun toggleDriverAvailability(driverId: Int): ToggleDriverAvailabilityResponseDTO
    suspend fun getProfilePhoto(userId: Int): GetProfilePhotoResponseDTO
    suspend fun uploadProfilePhoto(userId: Int, base64Image: String): UploadProfilePhotoResponseDTO
}