package com.rodolfo.itaxcix.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ValidateVehicleResponseDTO (
    val message: String,
    val data: ValidateVehicleDataDTO
) {
    @Serializable
    data class ValidateVehicleDataDTO(
        val personId: Int,
        val vehicleId: Int
    )
}