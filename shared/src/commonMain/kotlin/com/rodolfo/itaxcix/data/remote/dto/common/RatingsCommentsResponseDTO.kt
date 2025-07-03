package com.rodolfo.itaxcix.data.remote.dto.common

import kotlinx.serialization.Serializable

@Serializable
data class RatingsCommentsResponseDTO (
    val message: String,
    val data: RatingsData
) {
    @Serializable
    data class RatingsData(
        val averageRating: Double,
        val totalRatings: Int,
        val comments: List<CommentInfo>,
        val meta: MetaInfo
    ) {
        @Serializable
        data class CommentInfo(
            val id: Int,
            val travelId: Int,
            val raterName: String,
            val score: Int,
            val comment: String,
            val createdAt: String
        )

        @Serializable
        data class MetaInfo(
            val total: Int,
            val perPage: Int,
            val currentPage: Int,
            val lastPage: Int,
            val search: String? = null,
            val filters: String? = null,
            val sortBy: String? = null,
            val sortDirection: String? = null
        )
    }
}