package com.rodolfo.itaxcix.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class DriverRegisterRequestDTO (
    val documentTypeId: Int,
    val document: String,
    val alias: String,
    val password: String,
    val contactTypeId: Int,
    val contact: String,
    val licensePlate: String
)