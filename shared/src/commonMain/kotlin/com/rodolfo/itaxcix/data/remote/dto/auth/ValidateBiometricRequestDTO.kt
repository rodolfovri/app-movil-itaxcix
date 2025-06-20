package com.rodolfo.itaxcix.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class ValidateBiometricRequestDTO (
    val personId: Int,
    val imageBase64: String
)