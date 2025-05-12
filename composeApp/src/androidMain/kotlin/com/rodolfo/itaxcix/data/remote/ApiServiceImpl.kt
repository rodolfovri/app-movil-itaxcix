package com.rodolfo.itaxcix.data.remote

import android.util.Log
import com.rodolfo.itaxcix.data.remote.dto.UserDTO
import com.rodolfo.itaxcix.data.remote.api.ApiService
import com.rodolfo.itaxcix.data.remote.dto.CitizenRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.DriverRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.LoginResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.RegisterDriverResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.RegisterResponseDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class ApiServiceImpl(private val client: HttpClient) : ApiService {

    private val baseUrl = "https://149.130.161.148/api/v1"

    override suspend fun getUsers(): List<UserDTO> {
        return client.get("$baseUrl/users").body()
    }

    override suspend fun getUserById(id: String): UserDTO {
        return client.get("$baseUrl/users/$id").body()
    }

    override suspend fun registerCitizen(citizen: CitizenRegisterRequestDTO): RegisterResponseDTO {
        val response = client.post("$baseUrl/auth/register/citizen") {
            contentType(ContentType.Application.Json)
            setBody(citizen)
        }
        if (response.status.value in 200..299) {
            return response.body()
        } else {
            val errorBody = response.bodyAsText()
            throw Exception(parseErrorMessage(errorBody))
        }
    }

    override suspend fun registerDriver(driver: DriverRegisterRequestDTO): RegisterDriverResponseDTO {
        val response = client.post("$baseUrl/auth/register/driver") {
            contentType(ContentType.Application.Json)
            setBody(driver)
        }
       if (response.status.value in 200..299) {
            return response.body()
        } else {
            val errorBody = response.bodyAsText()
            throw Exception(parseErrorMessage(errorBody))
        }
    }

    override suspend fun login(username: String, password: String): LoginResponseDTO {
        val response = client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("username" to username, "password" to password))
        }
        if (response.status.value in 200..299) {
            return response.body()
        } else {
            val errorBody = response.bodyAsText()
            throw Exception(parseErrorMessage(errorBody))
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

}