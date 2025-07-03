package com.rodolfo.itaxcix.domain.repository

import com.rodolfo.itaxcix.data.remote.dto.travel.RegisterIncidentRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.travel.TravelRateRequestDTO
import com.rodolfo.itaxcix.domain.model.EmergencyNumberResult
import com.rodolfo.itaxcix.domain.model.RegisterIncidentResult
import com.rodolfo.itaxcix.domain.model.TravelCancelResult
import com.rodolfo.itaxcix.domain.model.TravelCompleteResult
import com.rodolfo.itaxcix.domain.model.TravelHistoryResult
import com.rodolfo.itaxcix.domain.model.TravelRateResult
import com.rodolfo.itaxcix.domain.model.TravelRatingResult

interface TravelRepository {
    suspend fun travelCancel(travelId: Int): TravelCancelResult
    suspend fun travelComplete(travelId: Int): TravelCompleteResult
    suspend fun registerIncident(incident: RegisterIncidentRequestDTO): RegisterIncidentResult
    suspend fun travelHistory(userId: Int, page: Int = 1): TravelHistoryResult
    suspend fun travelRate(travelId: Int, travelRate: TravelRateRequestDTO): TravelRateResult
    suspend fun travelRating(travelId: Int): TravelRatingResult
    suspend fun emergencyNumber(): EmergencyNumberResult
}