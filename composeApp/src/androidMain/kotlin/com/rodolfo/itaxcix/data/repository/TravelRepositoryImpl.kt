package com.rodolfo.itaxcix.data.repository

import com.rodolfo.itaxcix.data.remote.api.ApiService
import com.rodolfo.itaxcix.data.remote.dto.travel.RegisterIncidentRequestDTO
import com.rodolfo.itaxcix.domain.model.RegisterIncidentResult
import com.rodolfo.itaxcix.domain.model.TravelCancelResult
import com.rodolfo.itaxcix.domain.model.TravelCompleteResult
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
}