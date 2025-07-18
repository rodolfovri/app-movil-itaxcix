package com.rodolfo.itaxcix.data.remote.dto.driver

import kotlinx.serialization.Serializable

@Serializable
data class VehicleAssociationResponseDTO (
    val data: VehicleAssociationData
) {
    @Serializable
    data class VehicleAssociationData(
        val userId: Int,
        val vehicleId: Int,
        val plateValue: String,
        val vehicleCreated: Boolean,
        val tucsUpdated: Int,
        val message: String
    )
}