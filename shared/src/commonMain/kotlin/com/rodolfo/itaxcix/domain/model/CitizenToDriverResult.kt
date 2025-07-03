package com.rodolfo.itaxcix.domain.model

data class CitizenToDriverResult (
    val userId: Int,
    val status: String,
    val message: String,
    val driverProfileId: Int
)