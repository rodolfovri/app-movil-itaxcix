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
        val firstName: String,
        val lastName: String,
        val availabilityStatus: Boolean? = null,
        val roles: List<String> = emptyList(),
        val permissions: List<String> = emptyList(),
        val rating: Double
    )
}