package com.rodolfo.itaxcix.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordResponseDTO (
    val message: String = ""
)