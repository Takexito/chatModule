package com.dev.podo.core.model.dto

import com.dev.podo.core.model.entities.MessageTitle
import com.squareup.moshi.Json

data class MessageTitleDto(
    @Json(name = "message") val message: String?,
    @Json(name = "title") val title: String?,
) : Dto<MessageTitle> {
    override fun asEntity(): MessageTitle {
        return MessageTitle(message ?: "", title ?: "")
    }
}
