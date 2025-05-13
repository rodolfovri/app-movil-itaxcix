package com.rodolfo.itaxcix.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VerifyCodeRequestDTO (
    val code: String,
    val contactTypeId: Int,
    val contact: String,
)