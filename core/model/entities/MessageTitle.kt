package com.dev.podo.core.model.entities

data class MessageTitle(
    val message: String,
    val title: String,
) {
    override fun toString(): String {
        return "title: $title \nmessage: $message"
    }
}
