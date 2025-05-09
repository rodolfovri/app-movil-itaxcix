package com.rodolfo.itaxcix.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CitizenRegisterRequestDTO (
    val documentTypeId: Int,
    val document: String,
    val alias: String,
    val password: String,
    val contactTypeId: Int,
    val contact: String,
)