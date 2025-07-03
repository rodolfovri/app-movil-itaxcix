package com.rodolfo.itaxcix.domain.model

data class RatingsCommentsResult (
    val message: String,
    val data: RatingsData
) {
    data class RatingsData(
        val averageRating: Double,
        val totalRatings: Int,
        val comments: List<CommentInfo>,
        val meta: MetaInfo
    ) {
        data class CommentInfo(
            val id: Int,
            val travelId: Int,
            val raterName: String,
            val score: Int,
            val comment: String,
            val createdAt: String
        )

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