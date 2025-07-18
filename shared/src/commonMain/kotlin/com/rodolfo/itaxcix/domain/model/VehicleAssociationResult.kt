package com.rodolfo.itaxcix.domain.model

data class VehicleAssociationResult (
    val userId: Int,
    val vehicleId: Int,
    val plateValue: String,
    val vehicleCreated: Boolean,
    val tucsUpdated: Int,
    val message: String
)