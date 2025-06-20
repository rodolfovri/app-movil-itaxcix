package com.rodolfo.itaxcix.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class VerifyCodeRegisterRequestDTO (
    val userId: Int,
    val code: String
)