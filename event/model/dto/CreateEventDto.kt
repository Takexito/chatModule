package com.dev.podo.event.model.dto

import com.dev.podo.R
import com.squareup.moshi.Json

enum class EventType(val titleRes: Int) {
    EVENT(titleRes = R.string.DELAYED_EVENT),
    NOW(titleRes = R.string.LIVE_EVENT)
}

data class CreateEventDto(
    val title: String? = null,
    val description: String? = null,
    val type: String? = null,
    @Json(name = "start_at")
    val startAt: String? = null,
    val preferred: String? = null,
    val place: String? = null,
    val tags: List<Int>? = arrayListOf(),
    val club: String? = null,
    val video: String? = null
)
