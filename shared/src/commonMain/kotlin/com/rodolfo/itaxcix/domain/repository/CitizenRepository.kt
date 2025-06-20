package com.rodolfo.itaxcix.domain.repository

import com.rodolfo.itaxcix.data.remote.dto.travel.TravelRequestDTO
import com.rodolfo.itaxcix.domain.model.TravelResult

interface CitizenRepository {
    suspend fun travels(travel: TravelRequestDTO): TravelResult
}