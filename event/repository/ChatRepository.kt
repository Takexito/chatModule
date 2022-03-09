package com.dev.podo.event.repository

import com.dev.podo.common.model.entities.PagedListWrapper
import com.dev.podo.core.model.entities.MessageTitle
import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.event.model.entities.ChatEvent
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun getChatInfo(chatId: Int): Flow<ResultState<ChatEvent.ChatBlock>>
    suspend fun sendMessage(chatId: Int, message: ChatEvent.Message): Flow<ResultState<ChatEvent.Message>>
    suspend fun continueChat(chatId: Int): Flow<ResultState<MessageTitle>>
    suspend fun deleteChat(chatId: Int): Flow<ResultState<MessageTitle>>
    suspend fun fetchPagedChatMessages(chatId: Int, page: Int): Flow<ResultState<PagedListWrapper<ChatEvent.Message>>>
    suspend fun fetchChats(): Flow<ResultState<List<ChatEvent.ChatBlock>>>
    fun getAllMessageFlow(): Flow<ChatEvent.Message?>
    fun getMessageFlow(chatId: Int): Flow<ChatEvent.Message?>
}
