package com.rodolfo.itaxcix.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseDTO(
    val message: String,
    val user: LoginUserWrapperDTO
) {
    @Serializable
    data class LoginUserWrapperDTO(
        val token: String,
        val user: LoginUserDTO
    )

    @Serializable
    data class LoginUserDTO(
        val id: Int,
        val alias: String,
        val roles: List<String>
    )
}