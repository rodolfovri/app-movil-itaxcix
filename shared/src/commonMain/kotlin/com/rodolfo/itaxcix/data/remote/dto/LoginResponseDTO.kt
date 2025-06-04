package com.rodolfo.itaxcix.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseDTO(
    val message: String,
    val data: LoginDataDTO
) {
    @Serializable
    data class LoginDataDTO(
        val token: String,
        val userId: Int,
        val documentValue: String,
        val roles: List<String> = emptyList(),
        val permissions: List<String> = emptyList()
    )
}