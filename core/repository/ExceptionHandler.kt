package com.dev.podo.core.repository

import android.util.Log
import com.dev.podo.core.model.dto.ErrorDto
import com.dev.podo.core.model.dto.RequestException
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import retrofit2.HttpException

class ExceptionHandler @Inject constructor(val moshi: Moshi) {
    fun handle(exception: Exception): Exception {
        return when (exception) {
            is HttpException -> {
                Log.e(this.javaClass.canonicalName, "was exception on request", exception)
                try {
                    val error = parseServerError(exception)
                    RequestException(exception.code(), error?.title, error?.message, exception)
                } catch (e: JsonEncodingException) {
                    handle(e)
                }
            }
            is UnknownHostException -> {
                Log.e(this.javaClass.canonicalName, "was exception on request", exception)
                Exception("Упс все сломалось", exception)
            }
            is JsonEncodingException, is JsonDataException -> {
                Log.e(this.javaClass.canonicalName, "was exception on request", exception)
                Exception("Упс все сломалось", exception)
            }
            is SocketTimeoutException -> {
                Log.e(this.javaClass.canonicalName, "was exception on request", exception)
                Exception("Упс, у вас слишком большие фотографии", exception)
            }
            is IOException -> {
                Log.e(this.javaClass.canonicalName, "was exception on request", exception)
                Exception("Упс все сломалось", exception)
            }
            is IllegalArgumentException -> {
                Log.e(this.javaClass.canonicalName, "was exception on request", exception)
                Exception("Упс все сломалось", exception)
            }
            else -> {
                Log.e(this.javaClass.canonicalName, "was exception on request", exception)
                Exception("Упс все сломалось", exception)
            }
        }
    }

    @Throws(JsonEncodingException::class)
    fun parseServerError(exception: HttpException): ErrorDto? {
        val json = exception.response()?.errorBody()?.string() ?: ""
        val jsonAdapter: JsonAdapter<ErrorDto> = moshi.adapter(ErrorDto::class.java)
        return jsonAdapter.fromJson(json)
    }
}
