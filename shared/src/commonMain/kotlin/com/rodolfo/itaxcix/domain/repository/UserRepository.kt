package com.rodolfo.itaxcix.domain.repository

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
    suspend fun changeEmail(changeEmail: ChangeEmailRequestDTO): ChangeEmailResult
    suspend fun verifyChangeEmail(verifyChange: VerifyChangeEmailRequestDTO): VerifyChangeEmailResult
    suspend fun changePhone(changePhone: ChangePhoneRequestDTO): ChangePhoneResult
    suspend fun verifyChangePhone(verifyChange: VerifyChangePhoneRequestDTO): VerifyChangePhoneResult
    suspend fun helpCenter(): HelpCenterResult
    suspend fun getRatingCommentsUser(userId: Int): RatingsCommentsResult
    suspend fun getRatingCommentsDriver(driverId: Int): RatingsCommentsResult
}