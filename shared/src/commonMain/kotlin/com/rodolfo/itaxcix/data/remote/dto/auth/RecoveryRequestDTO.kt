package com.rodolfo.itaxcix.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class RecoveryRequestDTO (
    val contactTypeId: Int,
    val contactValue: String,
)