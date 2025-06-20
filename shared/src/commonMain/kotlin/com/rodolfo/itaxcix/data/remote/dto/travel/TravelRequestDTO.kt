package com.rodolfo.itaxcix.data.remote.dto.travel

import kotlinx.serialization.Serializable

@Serializable
data class TravelRequestDTO (
    val citizenId: Int,
    val driverId: Int,
    val originLatitude: Double,
    val originLongitude: Double,
    val originDistrict: String,
    val originAddress: String,
    val destinationLatitude: Double,
    val destinationLongitude: Double,
    val destinationDistrict: String,
    val destinationAddress: String,
)