package com.rodolfo.itaxcix.domain.model

data class DriverToCitizenResult (
    val userId: Int,
    val status: String,
    val message: String,
    val citizenProfileId: Int
)