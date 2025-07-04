package com.rodolfo.itaxcix.domain.model

data class VehicleAssociationResult (
    val userId: Int,
    val vehicleId: Int,
    val plateValue: String,
    val vehicleCreated: Boolean,
    val tucsUpdate: Boolean,
    val message: String
)