package com.rodolfo.itaxcix.data.remote.dto.common

import kotlinx.serialization.Serializable

@Serializable
data class ChangeEmailRequestDTO (
    val userId: Int,
    val email: String
)