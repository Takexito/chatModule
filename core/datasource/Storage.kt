package com.dev.podo.core.datasource

import com.dev.podo.core.model.entities.User
import com.dev.podo.home.model.entities.HomeEventModel

object Storage {
    const val TOKEN_KEY = "access_token"
    const val BROADCAST_TOKEN_KEY = "broadcast_token"
    const val USER_DATA_KEY = "user_data"

    const val USER_PREFERENCE_KEY = "com.dev.podo.USER_PREFERENCE"

    var userToken: String? = null
        set(value) {
            field = "Bearer $value"
        }

    var broadcastToken: String? = null

    var user: User? = null

    var podoNowList: ArrayList<HomeEventModel> = arrayListOf()

    fun clearData() {
        userToken = null
        user = null
        podoNowList = arrayListOf()
    }
}
