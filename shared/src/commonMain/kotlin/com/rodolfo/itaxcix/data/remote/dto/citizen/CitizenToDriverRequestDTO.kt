package com.rodolfo.itaxcix.data.remote.dto.citizen

import kotlinx.serialization.Serializable

@Serializable
data class CitizenToDriverRequestDTO (
    val userId: Int,
    val plateValue: String
)