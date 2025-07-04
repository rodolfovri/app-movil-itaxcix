package com.rodolfo.itaxcix.data.remote.dto.driver

import kotlinx.serialization.Serializable

@Serializable
data class VehicleAssociationRequestDTO (
    val plateValue: String
)