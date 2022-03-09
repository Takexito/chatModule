package com.dev.podo.core.model.entities.statistics

data class Statistics(
    val totalPromptsCount: Int?,
    val acceptedPromptsCount: Int?,
    val totalChatsCount: Int?,
    val presentChatsCount: Int?,
    val chatMessagesCount: Int?,
    val totalEventsCount: Int?,
    val delayedEventCount: Int?,
    val podoNowEventCount: Int?,
    val totalActiveEventCount: Int?,
    val delayedActiveEventCount: Int?,
    val podoNowActiveEventCount: Int?
)