package com.rodolfo.itaxcix.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class CitizenRegisterRequestDTO (
    val password: String,
    val contactTypeId: Int,
    val contactValue: String,
    val personId: Int,
    val vehicleId: Int? = null,
)