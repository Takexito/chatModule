package com.dev.podo.core.model.dto.statistics

import com.dev.podo.core.model.dto.Dto
import com.dev.podo.core.model.entities.MessageTitle
import com.dev.podo.core.model.entities.statistics.Statistics
import com.squareup.moshi.Json

data class StatisticsDto(
    @Json(name = "total_prompts_count") val totalPromptsCount: Int?,
    @Json(name = "accepted_prompts_count") val acceptedPromptsCount: Int?,
    @Json(name = "total_chats_count") val totalChatsCount: Int?,
    @Json(name = "present_chats_count") val presentChatsCount: Int?,
    @Json(name = "chat_messages") val chatMessagesCount: Int?,
    @Json(name = "total_events_count") val totalEventsCount: Int?,
    @Json(name = "regular_events_count") val delayedEventCount: Int?,
    @Json(name = "now_events_count") val podoNowEventCount: Int?,
    @Json(name = "total_active_events_count") val totalActiveEventCount: Int?,
    @Json(name = "regular_active_events_count") val delayedActiveEventCount: Int?,
    @Json(name = "now_active_events_count") val podoNowActiveEventCount: Int?,
) : Dto<Statistics> {
    override fun asEntity(): Statistics {
        return Statistics(
            totalPromptsCount,
            acceptedPromptsCount,
            totalChatsCount,
            presentChatsCount,
            chatMessagesCount,
            totalEventsCount,
            delayedEventCount,
            podoNowEventCount,
            totalActiveEventCount,
            delayedActiveEventCount,
            podoNowActiveEventCount
        )
    }
}
