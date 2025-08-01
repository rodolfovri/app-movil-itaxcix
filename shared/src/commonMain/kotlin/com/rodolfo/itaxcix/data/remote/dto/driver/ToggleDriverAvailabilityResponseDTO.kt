package com.rodolfo.itaxcix.data.remote.dto.driver

import kotlinx.serialization.Serializable

@Serializable
data class ToggleDriverAvailabilityResponseDTO (
    val message: String,
    val data: ToggleDriverAvailabilityDataDTO
) {
    @Serializable
    data class ToggleDriverAvailabilityDataDTO(
        val driverId: Int,
        val hasActiveTuc: Boolean
    )
}