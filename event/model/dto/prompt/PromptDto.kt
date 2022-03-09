package com.dev.podo.event.model.dto.prompt

import com.dev.podo.auth.model.dto.UserDto
import com.dev.podo.core.model.dto.Dto
import com.dev.podo.event.model.entities.prompt.Prompt
import com.squareup.moshi.Json

data class PromptDto(
    @Json(name = "id") val id: Long?,
    @Json(name = "message") val message: String? = "",
    @Json(name = "status") val status: String?,
    @Json(name = "user") val user: UserDto?,
    @Json(name = "user_id") val userId: Long?,
    @Json(name = "event_id") val eventId: Long?,

) : Dto<Prompt> {
    override fun asEntity(): Prompt {
        return Prompt(
            id, message, status, user?.asEntity(), userId, eventId
        )
    }
}
