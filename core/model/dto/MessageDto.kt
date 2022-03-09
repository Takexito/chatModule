package com.dev.podo.core.model.dto

import com.dev.podo.core.model.entities.Message
import com.squareup.moshi.Json

data class MessageDto(
    @Json(name = "message") val message: String?,
    @Json(name = "id") val id: Long? = null,
) : Dto<Message> {
    override fun asEntity(): Message {
        return Message(message ?: "", id ?: 0)
    }
}
