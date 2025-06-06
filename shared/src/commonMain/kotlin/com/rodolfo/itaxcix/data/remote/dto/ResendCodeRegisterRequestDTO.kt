package com.rodolfo.itaxcix.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ResendCodeRegisterRequestDTO (
    val userId: Int
)