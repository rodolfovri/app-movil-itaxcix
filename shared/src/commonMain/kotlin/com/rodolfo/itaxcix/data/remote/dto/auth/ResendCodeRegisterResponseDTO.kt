package com.rodolfo.itaxcix.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class ResendCodeRegisterResponseDTO (
    val message: String
)