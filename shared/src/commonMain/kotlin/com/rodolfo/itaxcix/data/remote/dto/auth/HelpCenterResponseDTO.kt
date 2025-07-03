package com.rodolfo.itaxcix.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class HelpCenterResponseDTO(
    val message: String,
    val data: List<HelpCenterData>
) {
    @Serializable
    data class HelpCenterData(
        val id: Int,
        val title: String,
        val subtitle: String,
        val answer: String,
        val active: Boolean
    )
}