package com.rodolfo.itaxcix.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponseDTO (
    val message: String = "",
    val data : RegisterDataDTO
) {
    @Serializable
    data class RegisterDataDTO(
        val userId: Int
    )
}