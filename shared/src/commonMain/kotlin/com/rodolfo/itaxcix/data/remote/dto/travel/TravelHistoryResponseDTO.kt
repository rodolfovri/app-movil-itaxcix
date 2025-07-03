package com.rodolfo.itaxcix.data.remote.dto.travel

import kotlinx.serialization.Serializable

@Serializable
data class TravelHistoryResponseDTO (
    val message: String,
    val data: TravelHistoryData
) {
    @Serializable
    data class TravelHistoryData(
        val items: List<TravelHistoryItem>,
        val meta: TravelHistoryMeta
    ) {
        @Serializable
        data class TravelHistoryItem(
            val id: Int,
            val startDate: String,
            val origin: String,
            val destination: String,
            val status: String
        )

        @Serializable
        data class TravelHistoryMeta(
            val total: Int,
            val perPage: Int,
            val currentPage: Int,
            val lastPage: Int
        )
    }
}