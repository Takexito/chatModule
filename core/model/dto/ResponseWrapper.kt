package com.dev.podo.core.model.dto

import com.squareup.moshi.Json

data class ResponseWrapper<D : Dto<*>>(
    @Json(name = "data") val data: D?,
    @Json(name = "title") val title: String?,
    @Json(name = "message") val message: String?,
    @Json(name = "code") val code: Int?
)

data class ResponseWrapperList<D : Dto<*>>(
    @Json(name = "data") val data: List<D?>,
    @Json(name = "title") val title: String?,
    @Json(name = "message") val message: String?,
    @Json(name = "links") val links: Links?,
    @Json(name = "meta") val meta: Meta?,
    @Json(name = "code") val code: Int?
)

data class Links(
    @Json(name = "first") val first: String?,
    @Json(name = "last") val last: String?,
    @Json(name = "prev") val prev: String?,
    @Json(name = "next") val next: String?,
)

data class Meta(
    @Json(name = "current_page") val currPage: Int,
    @Json(name = "from") val from: Int?,
    @Json(name = "last_page") val lastPage: Int,
    @Json(name = "links") val links: List<MetaLink>?,
    @Json(name = "path") val path: String,
    @Json(name = "per_page") val perPage: Int?,
    @Json(name = "to") val to: Int?,
    @Json(name = "total") val total: Int?
)

data class MetaLink(
    @Json(name = "url") val url: String?,
    @Json(name = "label") val label: String?,
    @Json(name = "active") val active: Boolean?
)

fun <T> List<T?>.toNotNullable(): List<T> {
    val list = arrayListOf<T>()
    forEach {
        if (it != null) list.add(it)
    }
    return list
}
