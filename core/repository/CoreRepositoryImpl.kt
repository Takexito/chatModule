package com.dev.podo.core.repository

import com.dev.podo.auth.model.dto.asUpdateDto
import com.dev.podo.common.model.entities.LocalMedia
import com.dev.podo.common.model.entities.Media
import com.dev.podo.common.utils.exceptions.AccessTokenNullException
import com.dev.podo.common.utils.network.AppMediaType
import com.dev.podo.common.utils.network.ServerFilesHelper
import com.dev.podo.core.datasource.Storage
import com.dev.podo.core.model.entities.MessageTitle
import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.core.model.entities.User
import com.dev.podo.event.datasource.ChatMessageDataSource
import java.io.InputStream
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class CoreRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val chatMessageDataSource: ChatMessageDataSource
) : BaseRepository(), CoreRepository {

    private fun saveUser(user: User?) {
        if (user != null) Storage.user = user
    }

    override suspend fun fetchUser(): Flow<ResultState<User>> {
        return fetchDataWithAction({
            userApi.getUser(Storage.userToken ?: throw AccessTokenNullException())
        }, this::saveUser)
    }

    override fun initChatMessageService(viewModelScope: CoroutineScope) {
        chatMessageDataSource.init(viewModelScope)
    }

    override suspend fun updateUser(user: User): Flow<ResultState<User?>> {
        return fetchDataWithAction({
            userApi.updateProfile(Storage.userToken ?: "", user.asUpdateDto())
        }, this::saveUser)
    }

    override suspend fun uploadMedia(filesArray: ArrayList<InputStream>): Flow<ResultState<List<Media?>>> {
        val bodyParts = arrayListOf<MultipartBody.Part>()
        filesArray.forEach {
            bodyParts.add(
                ServerFilesHelper.createFileMultipartBodyPart(
                    it,
                    "user_image",
                    "files[]",
                    AppMediaType.Image.getType()
                )
            )
        }
        val result = fetchDataList {
            userApi.sendMedia(Storage.userToken ?: "", bodyParts)
        }
        filesArray.forEach { it.close() }
        return result
    }

    override suspend fun updateUserMediaOrder(mediaList: List<LocalMedia>): Flow<ResultState<List<Media>>> {
        return fetchDataList {
            userApi.updateMediaOrder(
                Storage.userToken ?: "",
                LocalMedia.toDto(mediaList)
            )
        }
    }

    override suspend fun updateUserCity(cityId: Long): Flow<ResultState<User>> {
        return fetchData { userApi.updateUserCity(Storage.userToken ?: "", cityId) }
    }

    override suspend fun deleteMedia(mediaId: Long): Flow<ResultState<MessageTitle>> {
        return fetchDataWithoutWrapper { userApi.deleteMedia(Storage.userToken ?: "", mediaId) }
    }

    override suspend fun fetchPublicUser(userId: Long): Flow<ResultState<User>> {
        return fetchData { userApi.getPublicProfile(userId = userId) }
    }

    override suspend fun uploadMedia(
        file: InputStream,
        order: Int
    ): Flow<ResultState<List<Media>>> {
        val orderPart = order.toString().toRequestBody(AppMediaType.Text.getType())
        val filePart = ServerFilesHelper.createFileMultipartBodyPart(
            file,
            "user_image",
            "file",
            AppMediaType.Image.getType()
        )
        val result = fetchDataList {
            userApi.sendMediaWithOrder(
                Storage.userToken ?: "",
                orderPart,
                filePart
            )
        }
        return result
    }

    override suspend fun deleteUser(): Flow<ResultState<MessageTitle>> {
        return fetchMessage { userApi.deleteUser() }
    }
}
