package com.rodolfo.itaxcix.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class ValidateBiometricResponseDTO (
    val message: String = "",
    val data: ValidateBiometricDataDTO? = null
) {
    @Serializable
    data class ValidateBiometricDataDTO(
        val personId: Int
    )
}