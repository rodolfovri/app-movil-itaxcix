package com.rodolfo.itaxcix.data.repository

import com.rodolfo.itaxcix.data.remote.api.ApiService
import com.rodolfo.itaxcix.data.remote.dto.travel.RegisterIncidentRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.travel.TravelRateRequestDTO
import com.rodolfo.itaxcix.domain.model.EmergencyNumberResult
import com.rodolfo.itaxcix.domain.model.RegisterIncidentResult
import com.rodolfo.itaxcix.domain.model.TravelCancelResult
import com.rodolfo.itaxcix.domain.model.TravelCompleteResult
import com.rodolfo.itaxcix.domain.model.TravelHistoryResult
import com.rodolfo.itaxcix.domain.model.TravelRateResult
import com.rodolfo.itaxcix.domain.model.TravelRatingResult
import com.rodolfo.itaxcix.domain.repository.TravelRepository

class TravelRepositoryImpl(private val apiService: ApiService) : TravelRepository {

    override suspend fun travelCancel(travelId: Int): TravelCancelResult {
        val response = apiService.travelCancel(travelId)
        return TravelCancelResult(
            message = response.message,
            travelId = response.data.travelId
        )
    }

    override suspend fun travelComplete(travelId: Int): TravelCompleteResult {
        val response = apiService.travelComplete(travelId)
        return TravelCompleteResult(
            message = response.message,
            travelId = response.data.travelId
        )
    }

    override suspend fun registerIncident(incident: RegisterIncidentRequestDTO): RegisterIncidentResult {
        val response = apiService.registerIncident(incident)
        return RegisterIncidentResult(
            incidentId = response.incidentId,
            message = response.message
        )
    }

    override suspend fun travelHistory(userId: Int, page: Int): TravelHistoryResult {
        val response = apiService.travelHistory(userId, page)
        return TravelHistoryResult(
            message = response.message,
            data = TravelHistoryResult.TravelHistoryData(
                items = response.data.items.map { item ->
                    TravelHistoryResult.TravelHistoryData.TravelHistoryItem(
                        id = item.id,
                        startDate = item.startDate,
                        origin = item.origin,
                        destination = item.destination,
                        status = item.status
                    )
                },
                meta = TravelHistoryResult.TravelHistoryData.TravelHistoryMeta(
                    total = response.data.meta.total,
                    perPage = response.data.meta.perPage,
                    currentPage = response.data.meta.currentPage,
                    lastPage = response.data.meta.lastPage
                )
            )
        )
    }

    override suspend fun travelRate(travelId: Int, travelRate: TravelRateRequestDTO): TravelRateResult {
        val response = apiService.travelRate(travelId, travelRate)
        return TravelRateResult(
            message = response.message
        )
    }

    override suspend fun travelRating(travelId: Int): TravelRatingResult {
        val response = apiService.travelRating(travelId)
        return TravelRatingResult(
            message = response.message,
            driverRating = response.data.driverRating?.let {
                TravelRatingResult.Rating(
                    id = it.id,
                    travelId = it.travelId,
                    ratedName = it.ratedName,
                    raterName = it.raterName,
                    score = it.score,
                    comment = it.comment,
                    createdAt = it.createdAt
                )
            },
            citizenRating = response.data.citizenRating?.let {
                TravelRatingResult.Rating(
                    id = it.id,
                    travelId = it.travelId,
                    ratedName = it.ratedName,
                    raterName = it.raterName,
                    score = it.score,
                    comment = it.comment,
                    createdAt = it.createdAt
                )
            }
        )
    }

    override suspend fun emergencyNumber(): EmergencyNumberResult {
        val response = apiService.emergencyNumber()
        return EmergencyNumberResult(
            message = response.message,
            number = response.data.number
        )
    }
}