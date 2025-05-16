package com.rodolfo.itaxcix.domain.model

data class DriverStatusResult (
    val isDriverAvailable: Boolean,
    val lastDriverStatusUpdate: String? = null
)