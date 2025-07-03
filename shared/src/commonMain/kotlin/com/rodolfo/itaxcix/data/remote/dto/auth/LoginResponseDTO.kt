package com.rodolfo.itaxcix.data.remote.dto.auth

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
        val roles: List<RoleDTO>,
        val permissions: List<PermissionDTO>,
        val rating: Double,
        val availabilityStatus: Boolean? = null
    ) {
        @Serializable
        data class RoleDTO(
            val id: Int,
            val name: String
        )

        @Serializable
        data class PermissionDTO(
            val id: Int,
            val name: String
        )
    }
}