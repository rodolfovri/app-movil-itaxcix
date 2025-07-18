package com.rodolfo.itaxcix.data.remote

import com.rodolfo.itaxcix.data.local.PreferencesManager
import com.rodolfo.itaxcix.data.remote.api.ApiConfig
import com.rodolfo.itaxcix.data.remote.dto.UserDTO
import com.rodolfo.itaxcix.data.remote.api.ApiService
import com.rodolfo.itaxcix.data.remote.dto.auth.CitizenRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.DriverRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.HelpCenterResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.driver.DriverStatusResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.common.GetProfilePhotoResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.LoginResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.RecoveryRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.RecoveryResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.RegisterDriverResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.RegisterResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.ResendCodeRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.ResendCodeRegisterResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.ResetPasswordRequestDTO
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
import com.rodolfo.itaxcix.data.remote.dto.auth.VerifyCodeRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.auth.VerifyCodeResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.citizen.CitizenToDriverRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.citizen.CitizenToDriverResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.driver.DriverToCitizenRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.driver.DriverToCitizenResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.citizen.ProfileInformationCitizenResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.common.ChangeEmailRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.common.ChangeEmailResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.common.ChangePhoneRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.common.ChangePhoneResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.common.RatingsCommentsResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.common.VerifyChangeEmailRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.common.VerifyChangeEmailResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.common.VerifyChangePhoneRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.common.VerifyChangePhoneResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.driver.ProfileInformationDriverResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.driver.VehicleAssociationRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.driver.VehicleAssociationResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.driver.VehicleDisassociationResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.travel.EmergencyNumberResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.travel.RegisterIncidentRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.travel.TravelRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.travel.RegisterIncidentResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.travel.TravelCancelResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.travel.TravelHistoryResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.travel.TravelRateRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.travel.TravelRateResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.travel.TravelRatingResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.travel.TravelRespondResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.travel.TravelResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.travel.TravelStartResponseDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import okio.IOException

class ApiServiceImpl(
    private val client: HttpClient,
    private val preferencesManager: PreferencesManager
) : ApiService {
    
    private val baseUrl = ApiConfig.BASE_URL

    override suspend fun getUsers(): List<UserDTO> {
        return safeApiCall {
            client.get("$baseUrl/users").body()
        }
    }

    override suspend fun getUserById(id: String): UserDTO {
        return safeApiCall {
            client.get("$baseUrl/users/$id").body()
        }
    }

    override suspend fun validateDocument(document: ValidateDocumentRequestDTO): ValidateDocumentResponseDTO {
        return safeApiCall {
            val response = client.post("$baseUrl/auth/validation/document") {
                contentType(ContentType.Application.Json)
                setBody(document)
            }
            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun validateVehicle(vehicle: ValidateVehicleRequestDTO): ValidateVehicleResponseDTO {
        return safeApiCall {
            val response = client.post("$baseUrl/auth/validation/vehicle") {
                contentType(ContentType.Application.Json)
                setBody(vehicle)
            }
            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun validateBiometric(biometric: ValidateBiometricRequestDTO): ValidateBiometricResponseDTO {
        return safeApiCall {
            val response = client.post("$baseUrl/auth/validation/biometric") {
                contentType(ContentType.Application.Json)
                setBody(biometric)
            }
            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun registerCitizen(citizen: CitizenRegisterRequestDTO): RegisterResponseDTO {
        return safeApiCall {
            val response = client.post("$baseUrl/auth/registration") {
                contentType(ContentType.Application.Json)
                setBody(citizen)
            }
            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun registerDriver(driver: DriverRegisterRequestDTO): RegisterDriverResponseDTO {
        return safeApiCall {
            val response = client.post("$baseUrl/auth/register/driver") {
                contentType(ContentType.Application.Json)
                setBody(driver)
            }
            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun verifyCodeRegister(verifyCode: VerifyCodeRegisterRequestDTO): VerifyCodeRegisterResponseDTO {
        return safeApiCall {
            val response = client.post("$baseUrl/auth/registration/verify-code") {
                contentType(ContentType.Application.Json)
                setBody(verifyCode)
            }
            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun resendCodeRegister(resendCode: ResendCodeRegisterRequestDTO): ResendCodeRegisterResponseDTO {
        return safeApiCall {
            val response = client.post("$baseUrl/auth/registration/resend-code") {
                contentType(ContentType.Application.Json)
                setBody(resendCode)
            }
            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun login(documentValue: String, password: String): LoginResponseDTO {
        return safeApiCall {
            val response = client.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("documentValue" to documentValue, "password" to password))
            }
            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun recovery(contactTypeId: Int, contact: String): RecoveryResponseDTO {
        return safeApiCall {
            val recoveryRequest = RecoveryRequestDTO(contactTypeId, contact)
            val response = client.post("$baseUrl/auth/recovery/start") {
                contentType(ContentType.Application.Json)
                setBody(recoveryRequest)
            }
            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun verifyCode(userId: Int, code: String): VerifyCodeResponseDTO {
        return safeApiCall {
            val verifyCodeRequest = VerifyCodeRequestDTO(userId, code)
            val response = client.post("$baseUrl/auth/recovery/verify-code") {
                contentType(ContentType.Application.Json)
                setBody(verifyCodeRequest)
            }
            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun resetPassword(userId: Int, newPassword: String, repeatPassword: String, token: String): ResetPasswordResponseDTO {
        return safeApiCall {
            val resetPasswordRequest = ResetPasswordRequestDTO(userId, newPassword, repeatPassword)
            val response = client.post("$baseUrl/auth/recovery/change-password") {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $token")
                }
                setBody(resetPasswordRequest)
            }
            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun getDriverStatus(driverId: Int): DriverStatusResponseDTO {
        return safeApiCall {
            val response = client.get("$baseUrl/driver/status/$driverId") {
                addAuthToken()
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun toggleDriverAvailability(driverId: Int): ToggleDriverAvailabilityResponseDTO {
        return safeApiCall {
            val response = client.get("$baseUrl/drivers/$driverId/has-active-tuc") {
                addAuthToken()
                contentType(ContentType.Application.Json)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun getProfilePhoto(userId: Int): GetProfilePhotoResponseDTO {
        return safeApiCall {
            val response = client.get("$baseUrl/users/$userId/profile-photo") {
                addAuthToken()
                contentType(ContentType.Application.Json)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun uploadProfilePhoto(userId: Int, base64Image: String): UploadProfilePhotoResponseDTO {
        return safeApiCall {
            val response = client.post("$baseUrl/users/$userId/profile-photo") {
                addAuthToken()
                contentType(ContentType.Application.Json)
                setBody(mapOf("base64Image" to base64Image))
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun travels(travel: TravelRequestDTO): TravelResponseDTO {
        return safeApiCall {
            val response = client.post("$baseUrl/travels") {
                addAuthToken()
                contentType(ContentType.Application.Json)
                setBody(travel)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun travelRespond(travelId: Int, accept: Boolean): TravelRespondResponseDTO {
        return safeApiCall {
            val response = client.patch("$baseUrl/travels/$travelId/respond") {
                addAuthToken()
                contentType(ContentType.Application.Json)
                setBody(mapOf("accepted" to accept))
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun travelStart(travelId: Int): TravelStartResponseDTO {
        return safeApiCall {
            val response = client.patch("$baseUrl/travels/$travelId/start") {
                addAuthToken()
                contentType(ContentType.Application.Json)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun travelCancel(travelId: Int): TravelCancelResponseDTO {
        return safeApiCall {
            val response = client.patch("$baseUrl/travels/$travelId/cancel") {
                addAuthToken()
                contentType(ContentType.Application.Json)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun travelComplete(travelId: Int): TravelCancelResponseDTO {
        return safeApiCall {
            val response = client.patch("$baseUrl/travels/$travelId/complete") {
                addAuthToken()
                contentType(ContentType.Application.Json)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun registerIncident(incident: RegisterIncidentRequestDTO): RegisterIncidentResponseDTO {
        return safeApiCall {
            val response = client.post("$baseUrl/incidents/register") {
                addAuthToken()
                contentType(ContentType.Application.Json)
                setBody(incident)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun travelRate(travelId: Int, travelRate: TravelRateRequestDTO): TravelRateResponseDTO {
        return safeApiCall {
            val response = client.post("$baseUrl/travels/$travelId/rate") {
                addAuthToken()
                contentType(ContentType.Application.Json)
                setBody(travelRate)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun profileInformationCitizen(userId: Int): ProfileInformationCitizenResponseDTO {
        return safeApiCall {
            val response = client.get("$baseUrl/profile/citizen/$userId") {
                addAuthToken()
                contentType(ContentType.Application.Json)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun profileInformationDriver(userId: Int): ProfileInformationDriverResponseDTO {
        return safeApiCall {
            val response = client.get("$baseUrl/profile/driver/$userId") {
                addAuthToken()
                contentType(ContentType.Application.Json)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun travelHistory(userId: Int, page: Int): TravelHistoryResponseDTO {
        return safeApiCall {
            val response = client.get("$baseUrl/users/$userId/travels/history") {
                addAuthToken()
                contentType(ContentType.Application.Json)
                parameter("page", page)
                parameter("perPage", 10)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun changeEmail(changeEmail: ChangeEmailRequestDTO): ChangeEmailResponseDTO {
        return safeApiCall {
            val response = client.post("$baseUrl/profile/change-email") {
                addAuthToken()
                contentType(ContentType.Application.Json)
                setBody(changeEmail)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun verifyChangeEmail(verifyChange: VerifyChangeEmailRequestDTO): VerifyChangeEmailResponseDTO {
        return safeApiCall {
            val response = client.post("$baseUrl/profile/verify-email") {
                addAuthToken()
                contentType(ContentType.Application.Json)
                setBody(verifyChange)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun changePhone(changePhone: ChangePhoneRequestDTO): ChangePhoneResponseDTO {
        return safeApiCall {
            val response = client.post("$baseUrl/profile/change-phone") {
                addAuthToken()
                contentType(ContentType.Application.Json)
                setBody(changePhone)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun verifyChangePhone(verifyChange: VerifyChangePhoneRequestDTO): VerifyChangePhoneResponseDTO {
        return safeApiCall {
            val response = client.post("$baseUrl/profile/verify-phone") {
                addAuthToken()
                contentType(ContentType.Application.Json)
                setBody(verifyChange)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun travelRating(travelId: Int): TravelRatingResponseDTO {
        return safeApiCall {
            val response = client.get("$baseUrl/travels/$travelId/ratings") {
                addAuthToken()
                contentType(ContentType.Application.Json)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun helpCenter(): HelpCenterResponseDTO {
        return safeApiCall {
            val response = client.get("$baseUrl/help-center/public") {
                contentType(ContentType.Application.Json)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun emergencyNumber(): EmergencyNumberResponseDTO {
        return safeApiCall {
            val response = client.get("$baseUrl/emergency/number") {
                addAuthToken()
                contentType(ContentType.Application.Json)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun driverToCitizen(driverToCitizen: DriverToCitizenRequestDTO): DriverToCitizenResponseDTO {
        return safeApiCall {
            val response = client.post("$baseUrl/users/request-citizen-role") {
                addAuthToken()
                contentType(ContentType.Application.Json)
                setBody(driverToCitizen)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun getRatingCommentsUser(userId: Int): RatingsCommentsResponseDTO {
        return safeApiCall {
            val response = client.get("$baseUrl/users/$userId/ratings/comments") {
                addAuthToken()
                contentType(ContentType.Application.Json)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun getRatingCommentsDriver(driverId: Int): RatingsCommentsResponseDTO {
        return safeApiCall {
            val response = client.get("$baseUrl/users/$driverId/ratings/comments") {
                addAuthToken()
                contentType(ContentType.Application.Json)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun citizenToDriver(citizenToDriver: CitizenToDriverRequestDTO): CitizenToDriverResponseDTO {
        return safeApiCall {
            val response = client.post("$baseUrl/users/request-driver-role") {
                addAuthToken()
                contentType(ContentType.Application.Json)
                setBody(citizenToDriver)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun vehicleAssociation(userId: Int, vehicle: VehicleAssociationRequestDTO): VehicleAssociationResponseDTO {
        return safeApiCall {
            val response = client.post("$baseUrl/users/$userId/vehicle/association") {
                addAuthToken()
                contentType(ContentType.Application.Json)
                setBody(vehicle)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    override suspend fun vehicleDissociation(userId: Int): VehicleDisassociationResponseDTO {
        return safeApiCall {
            val response = client.delete("$baseUrl/users/$userId/vehicle/association") {
                addAuthToken()
                contentType(ContentType.Application.Json)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorBody = response.bodyAsText()
                throw Exception(parseErrorMessage(errorBody))
            }
        }
    }

    private fun parseErrorMessage(json: String): String {
        return try {
            val jsonObject = Json.parseToJsonElement(json).jsonObject
            jsonObject["error"]?.jsonObject?.get("message")?.toString()?.removeSurrounding("\"")
                ?: "Error desconocido"
        } catch (e: Exception) {
            "Error inesperado del servidor"
        }
    }

    // Método de extensión para añadir headers de autenticación
    private fun HttpRequestBuilder.addAuthToken() {
        val token = preferencesManager.userData.value?.authToken
        if (token != null) {
            headers {
                append("Authorization", "Bearer $token")
            }
        } else {
            throw Exception("Token no proporcionado. Por favor inicie sesión nuevamente.")
        }
    }

    private suspend inline fun <reified T> safeApiCall(crossinline apiCall: suspend () -> T): T {
        try {
            return apiCall()
        } catch (e: IOException) {
            throw Exception("Tiempo de espera agotado. Por favor verifique su conexión a Internet o contacte al soporte técnico")
        } catch (e: SocketTimeoutException) {
            throw Exception("Tiempo de espera agotado. Por favor verifique su conexión a Internet o contacte al soporte técnico")
        } catch (e: ConnectTimeoutException) {
            throw Exception("No se pudo establecer conexión. Revise su red o contacte con el soporte técnico.")
        } catch (e: TimeoutCancellationException) {
            throw Exception("Tiempo de espera agotado. Por favor verifique su conexión a Internet o contacte al soporte técnico")
        }
    }
}