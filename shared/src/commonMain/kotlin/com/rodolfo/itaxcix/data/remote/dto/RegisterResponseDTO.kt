package com.rodolfo.itaxcix.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponseDTO (
    val message: String = "",
    val userId: String = "",
    val personId: String = "",
)