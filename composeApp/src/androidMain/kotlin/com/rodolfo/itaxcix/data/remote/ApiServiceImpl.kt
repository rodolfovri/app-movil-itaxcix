package com.rodolfo.itaxcix.data.remote

import com.rodolfo.itaxcix.data.remote.dto.UserDTO
import com.rodolfo.itaxcix.data.remote.api.ApiService
import com.rodolfo.itaxcix.data.remote.dto.CitizenRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.DriverRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.LoginResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.RecoveryRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.RecoveryResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.RegisterDriverResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.RegisterResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.ResetPasswordResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.VerifyCodeRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.VerifyCodeResponseDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import okio.IOException

class ApiServiceImpl(private val client: HttpClient) : ApiService {

    private val baseUrl = "https://149.130.161.148/api/v1"

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

    override suspend fun registerCitizen(citizen: CitizenRegisterRequestDTO): RegisterResponseDTO {
        return safeApiCall {
            val response = client.post("$baseUrl/auth/register/citizen") {
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

    override suspend fun login(username: String, password: String): LoginResponseDTO {
        return safeApiCall {
            val response = client.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("username" to username, "password" to password))
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
            val response = client.post("$baseUrl/auth/recovery") {
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

    override suspend fun verifyCode(code: String, contactTypeId: Int, contact: String): VerifyCodeResponseDTO {
        return safeApiCall {
            val verifyCodeRequest = VerifyCodeRequestDTO(code, contactTypeId, contact)
            val response = client.post("$baseUrl/auth/verify-code") {
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

    override suspend fun resetPassword(userId: String, newPassword: String): ResetPasswordResponseDTO {
        return safeApiCall {
            val response = client.post("$baseUrl/auth/reset-password") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("userId" to userId, "newPassword" to newPassword))
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