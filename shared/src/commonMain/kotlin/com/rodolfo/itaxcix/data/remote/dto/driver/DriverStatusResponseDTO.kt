package com.rodolfo.itaxcix.data.remote.dto.driver

import kotlinx.serialization.Serializable

@Serializable
data class DriverStatusResponseDTO (
    val available: Boolean,
    val lastUpdated: String? = null
)