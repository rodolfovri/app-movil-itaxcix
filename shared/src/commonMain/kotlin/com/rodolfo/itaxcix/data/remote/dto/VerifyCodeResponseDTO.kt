package com.rodolfo.itaxcix.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VerifyCodeResponseDTO (
    val message: String = "",
    val userId: String = ""
)