package com.dev.podo.core.model.dto

import java.lang.Exception

class RequestException(
    val code: Int?,
    val title: String?,
    message: String?,
    throwable: Throwable? = null
) :
    Exception(message, throwable)
