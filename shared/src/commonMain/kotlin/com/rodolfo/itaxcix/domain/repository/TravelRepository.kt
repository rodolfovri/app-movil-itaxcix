package com.rodolfo.itaxcix.domain.repository

import com.rodolfo.itaxcix.data.remote.dto.travel.RegisterIncidentRequestDTO
import com.rodolfo.itaxcix.domain.model.RegisterIncidentResult
import com.rodolfo.itaxcix.domain.model.TravelCancelResult
import com.rodolfo.itaxcix.domain.model.TravelCompleteResult

interface TravelRepository {
    suspend fun travelCancel(travelId: Int): TravelCancelResult
    suspend fun travelComplete(travelId: Int): TravelCompleteResult
    suspend fun registerIncident(incident: RegisterIncidentRequestDTO): RegisterIncidentResult
}