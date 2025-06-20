package com.rodolfo.itaxcix.data.remote.dto.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DirectionsResponse(
    val routes: List<Route>
) {
    @Serializable
    data class Route(
        @SerialName("overview_polyline")
        val overview_polyline: OverviewPolyline
    )

    @Serializable
    data class OverviewPolyline(
        val points: String
    )
}
