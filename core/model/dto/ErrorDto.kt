package com.dev.podo.core.model.dto

import com.squareup.moshi.Json

data class ErrorDto(
    @field:Json(name = "message") val message: String?,
    @field:Json(name = "title") val title: String?,
)
