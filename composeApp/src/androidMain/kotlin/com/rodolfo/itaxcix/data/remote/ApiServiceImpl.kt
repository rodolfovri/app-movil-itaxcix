package com.rodolfo.itaxcix.data.remote

import com.rodolfo.itaxcix.data.local.PreferencesManager
import com.rodolfo.itaxcix.data.remote.api.ApiConfig
import com.rodolfo.itaxcix.data.remote.dto.UserDTO
import com.rodolfo.itaxcix.data.remote.api.ApiService
import com.rodolfo.itaxcix.data.remote.dto.CitizenRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.DriverAvailabilityRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.DriverAvailabilityResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.DriverRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.DriverStatusResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.LoginResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.RecoveryRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.RecoveryResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.RegisterDriverResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.RegisterResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.ResendCodeRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.ResendCodeRegisterResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.ResetPasswordRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.ResetPasswordResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.ToggleDriverAvailabilityResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.ValidateBiometricRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.ValidateBiometricResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.ValidateDocumentRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.ValidateDocumentResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.ValidateVehicleRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.ValidateVehicleResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.VerifyCodeRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.VerifyCodeRegisterResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.VerifyCodeRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.VerifyCodeResponseDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.headers
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