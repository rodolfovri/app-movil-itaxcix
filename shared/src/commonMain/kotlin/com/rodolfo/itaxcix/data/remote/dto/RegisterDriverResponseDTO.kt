package com.rodolfo.itaxcix.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
class RegisterDriverResponseDTO (
    val message: String = "",
    val userId: String = "",
    val personId: String = "",
    val vehicleId: String = ""
)