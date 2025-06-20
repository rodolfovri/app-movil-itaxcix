package com.rodolfo.itaxcix.data.repository

import com.rodolfo.itaxcix.data.remote.api.ApiService
import com.rodolfo.itaxcix.data.remote.dto.travel.TravelRequestDTO
import com.rodolfo.itaxcix.domain.model.TravelResult
import com.rodolfo.itaxcix.domain.repository.CitizenRepository

class CitizenRepositoryImpl (private val apiService: ApiService) : CitizenRepository {

    override suspend fun travels(travel: TravelRequestDTO): TravelResult {
        val response = apiService.travels(travel)
        return TravelResult(
            message = response.message,
            travelId = response.data.travelId
        )
    }
}