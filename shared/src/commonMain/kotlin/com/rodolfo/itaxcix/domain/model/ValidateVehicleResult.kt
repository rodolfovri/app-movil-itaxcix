package com.rodolfo.itaxcix.domain.model

data class ValidateVehicleResult (
    val message: String,
    val personId: Int,
    val vehicleId: Int
)