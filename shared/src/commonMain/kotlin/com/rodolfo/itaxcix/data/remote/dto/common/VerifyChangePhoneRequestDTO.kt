package com.rodolfo.itaxcix.data.remote.dto.common

import kotlinx.serialization.Serializable

@Serializable
data class VerifyChangePhoneRequestDTO (
    val userId: Int,
    val code: String
)