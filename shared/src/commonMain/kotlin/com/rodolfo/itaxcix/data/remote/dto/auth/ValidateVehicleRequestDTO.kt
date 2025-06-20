package com.rodolfo.itaxcix.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class ValidateVehicleRequestDTO (
    val documentTypeId: Int,
    val documentValue: String,
    val plateValue: String
)