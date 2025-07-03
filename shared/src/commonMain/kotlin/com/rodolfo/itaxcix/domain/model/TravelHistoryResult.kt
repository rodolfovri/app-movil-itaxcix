package com.rodolfo.itaxcix.domain.model

data class TravelHistoryResult (
    val message: String,
    val data: TravelHistoryData
) {
    data class TravelHistoryData(
        val items: List<TravelHistoryItem>,
        val meta: TravelHistoryMeta
    ) {
        data class TravelHistoryItem(
            val id: Int,
            val startDate: String,
            val origin: String,
            val destination: String,
            val status: String
        )

        data class TravelHistoryMeta(
            val total: Int,
            val perPage: Int,
            val currentPage: Int,
            val lastPage: Int
        )
    }
}