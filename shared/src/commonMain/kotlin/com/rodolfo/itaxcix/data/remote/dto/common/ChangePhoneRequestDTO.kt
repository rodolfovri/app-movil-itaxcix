package com.rodolfo.itaxcix.data.remote.dto.common

import kotlinx.serialization.Serializable

@Serializable
data class ChangePhoneRequestDTO (
    val userId: Int,
    val phone: String
)