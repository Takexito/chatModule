package com.dev.podo.event.repository

import android.net.Uri
import androidx.core.net.toFile
import com.dev.podo.chat.model.MessageType
import com.dev.podo.common.model.entities.PagedListWrapper
import com.dev.podo.core.model.dto.MessageDto
import com.dev.podo.core.model.entities.MessageTitle
import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.core.repository.BaseRepository
import com.dev.podo.core.ui.MainApplication
import com.dev.podo.event.MessageServicePool
import com.dev.podo.event.datasource.ChatApi
import com.dev.podo.event.datasource.ChatMessageDataSource
import com.dev.podo.event.datasource.ChatStorage
import com.dev.podo.event.model.entities.ChatEvent
import kotlinx.coroutines.flow.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject


class ChatRepositoryImpl @Inject constructor(
    private val chatApi: ChatApi,
    private val chatMessageDataSource: ChatMessageDataSource
) : ChatRepository, BaseRepository() {

    private val messageServicePool = MessageServicePool(chatMessageDataSource)

    override suspend fun getChatInfo(chatId: Int): Flow<ResultState<ChatEvent.ChatBlock>> {
        return fetchData { chatApi.fetchChat(chatId) }
    }

    override suspend fun sendMessage(
        chatId: Int,
        message: ChatEvent.Message
    ): Flow<ResultState<ChatEvent.Message>> {
        val result: ResultState<ChatEvent.Message> = if (message.type == MessageType.PHOTO) {
            val inputStream =
                MainApplication.context?.contentResolver?.openInputStream(Uri.parse(message.media[0]?.url?.fullSize))
            val body =
                inputStream?.readBytes()?.toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", "image1", body!!)
            safeApiCall { chatApi.sendAttachment(chatId, part) }
        } else {
            val messageDto = MessageDto(message.text)
            safeApiCall { chatApi.sendMessage(chatId, messageDto) }
        }
        return flow { emit(result) }
    }

    override suspend fun continueChat(chatId: Int): Flow<ResultState<MessageTitle>> {
        return fetchMessage { chatApi.continueChat(chatId) }
    }

    override suspend fun deleteChat(chatId: Int): Flow<ResultState<MessageTitle>> {
        return fetchMessage { chatApi.leaveChat(chatId) }
    }

    override suspend fun fetchPagedChatMessages(
        chatId: Int,
        page: Int
    ): Flow<ResultState<PagedListWrapper<ChatEvent.Message>>> {
        return fetchDataPagedList { chatApi.fetchChatMessages(chatId, page) }
    }

    override suspend fun fetchChats(): Flow<ResultState<List<ChatEvent.ChatBlock>>> {
        return flow {
            if (ChatStorage.chatList.isEmpty()) emit(ResultState.InProgress) else {
                emit(ResultState.Success(ChatStorage.chatList))
            }
            val result = safeApiCallList { chatApi.fetchChats() }
            result.getSuccessData()?.let { list ->
                ChatStorage.updateChat(list)
            }
            emit(result)
        }
    }

    override fun getAllMessageFlow(): Flow<ChatEvent.Message?> {
        return messageServicePool.allMessageFlow
    }

    override fun getMessageFlow(chatId: Int): Flow<ChatEvent.Message?> {
        return messageServicePool.getMessageFlow(chatId)
    }
}
