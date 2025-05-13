package com.rodolfo.itaxcix.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RecoveryRequestDTO (
    val contactTypeId: Int,
    val contact: String,
)