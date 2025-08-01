package com.rodolfo.itaxcix.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class VerifyCodeRegisterResponseDTO (
    val message: String,
    val data: VerifyCodeRegisterData
) {
    @Serializable
    data class VerifyCodeRegisterData(
        val message: String
    )
}