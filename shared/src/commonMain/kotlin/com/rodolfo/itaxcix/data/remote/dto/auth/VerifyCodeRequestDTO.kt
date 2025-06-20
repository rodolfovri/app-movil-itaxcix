package com.rodolfo.itaxcix.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class VerifyCodeRequestDTO (
    val userId: Int,
    val code: String
)