package com.rodolfo.itaxcix.domain.model

data class HelpCenterResult (
    val message: String,
    val data: List<HelpCenterData>
) {
    data class HelpCenterData(
        val id: Int,
        val title: String,
        val subtitle: String,
        val answer: String,
        val active: Boolean
    )
}