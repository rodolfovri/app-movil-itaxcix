package com.rodolfo.itaxcix.data.remote.api

import com.rodolfo.itaxcix.data.remote.dto.UserDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.CitizenRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.DriverRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.driver.DriverStatusResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.common.GetProfilePhotoResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.LoginResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.RecoveryResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.RegisterDriverResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.RegisterResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.ResendCodeRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.ResendCodeRegisterResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.ResetPasswordResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.driver.ToggleDriverAvailabilityResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.common.UploadProfilePhotoResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.ValidateBiometricRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.ValidateBiometricResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.ValidateDocumentRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.ValidateDocumentResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.ValidateVehicleRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.ValidateVehicleResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.VerifyCodeRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.VerifyCodeRegisterResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.VerifyCodeResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.travel.RegisterIncidentRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.travel.TravelRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.travel.RegisterIncidentResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.travel.TravelCancelResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.travel.TravelRespondResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.travel.TravelResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.travel.TravelStartResponseDTO

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
    suspend fun travels(travel: TravelRequestDTO): TravelResponseDTO
    suspend fun travelRespond(travelId: Int, accept: Boolean): TravelRespondResponseDTO
    suspend fun travelStart(travelId: Int): TravelStartResponseDTO
    suspend fun travelCancel(travelId: Int): TravelCancelResponseDTO
    suspend fun travelComplete(travelId: Int): TravelCancelResponseDTO
    suspend fun registerIncident(incident: RegisterIncidentRequestDTO): RegisterIncidentResponseDTO
}