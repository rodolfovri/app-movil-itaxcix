package com.rodolfo.itaxcix.domain.repository

import com.rodolfo.itaxcix.data.remote.dto.CitizenRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.DriverRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.ResendCodeRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.ResendCodeRegisterResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.ValidateBiometricRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.ValidateDocumentRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.ValidateVehicleRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.VerifyCodeRegisterRequestDTO
import com.rodolfo.itaxcix.domain.model.GetProfilePhotoResult
import com.rodolfo.itaxcix.domain.model.LoginResult
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
import com.rodolfo.itaxcix.domain.model.VerifyCodeRegisterResult
import com.rodolfo.itaxcix.domain.model.VerifyCodeResult

interface UserRepository {
    suspend fun getUsers(): List<User>
    suspend fun getUserById(id: String): User
    suspend fun validateDocument(document: ValidateDocumentRequestDTO): ValidateDocumentResult
    suspend fun validateVehicle(vehicle: ValidateVehicleRequestDTO): ValidateVehicleResult
    suspend fun validateBiometric(biometric: ValidateBiometricRequestDTO): ValidateBiometricResult
    suspend fun registerCitizen(user: CitizenRegisterRequestDTO): RegisterResult
    suspend fun registerDriver(user: DriverRegisterRequestDTO): RegisterDriverResult
    suspend fun verifyCodeRegister(verifyCode: VerifyCodeRegisterRequestDTO): VerifyCodeRegisterResult
    suspend fun resendCodeRegister(resendCode: ResendCodeRegisterRequestDTO): ResendCodeRegisterResult
    suspend fun login(documentValue: String, password: String): LoginResult
    suspend fun recovery(contactTypeId: Int, contact: String): RecoveryResult
    suspend fun verifyCode(userId: Int, code: String): VerifyCodeResult
    suspend fun resetPassword(userId: Int, newPassword: String, repeatPassword: String, token: String): ResetPasswordResult
    suspend fun getProfilePhoto(userId: Int): GetProfilePhotoResult
    suspend fun uploadProfilePhoto(userId: Int, base64Image: String): UploadProfilePhotoResult
}