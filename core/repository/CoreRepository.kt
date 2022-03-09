package com.dev.podo.core.repository

import com.dev.podo.common.model.entities.LocalMedia
import com.dev.podo.common.model.entities.Media
import com.dev.podo.core.model.entities.MessageTitle
import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.core.model.entities.User
import java.io.InputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface CoreRepository {
    suspend fun fetchUser(): Flow<ResultState<User>>
    fun initChatMessageService(viewModelScope: CoroutineScope)
    suspend fun updateUser(user: User): Flow<ResultState<User?>>
    suspend fun uploadMedia(filesArray: ArrayList<InputStream>): Flow<ResultState<List<Media?>>>
    suspend fun uploadMedia(file: InputStream, order: Int): Flow<ResultState<List<Media>>>
    suspend fun updateUserMediaOrder(mediaList: List<LocalMedia>): Flow<ResultState<List<Media>>>
    suspend fun updateUserCity(cityId: Long): Flow<ResultState<User>>
    suspend fun deleteMedia(mediaId: Long): Flow<ResultState<MessageTitle>>
    suspend fun fetchPublicUser(userId: Long): Flow<ResultState<User>>
    suspend fun deleteUser(): Flow<ResultState<MessageTitle>>
}
