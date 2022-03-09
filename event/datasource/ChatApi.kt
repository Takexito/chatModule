package com.dev.podo.event.datasource

import com.dev.podo.common.utils.exceptions.AccessTokenNullException
import com.dev.podo.core.datasource.Storage
import com.dev.podo.core.model.dto.MessageDto
import com.dev.podo.core.model.dto.MessageTitleDto
import com.dev.podo.core.model.dto.ResponseWrapper
import com.dev.podo.core.model.dto.ResponseWrapperList
import com.dev.podo.event.model.dto.chat.ChatDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ChatApi {

    @GET("api/chats/{chat}")
    suspend fun fetchChat(
        @Path("chat") chatId: Int,
        @Header("Authorization") token: String = Storage.userToken
            ?: throw AccessTokenNullException(),
    ): Response<ResponseWrapper<ChatDto.Data>>

    @GET("api/me/chats")
    suspend fun fetchChats(
        @Header("Authorization") token: String = Storage.userToken
            ?: throw AccessTokenNullException(),
    ): Response<ResponseWrapperList<ChatDto.Data>>

    @POST("api/chats/{chat}/message")
    suspend fun sendMessage(
        @Path("chat") chatId: Int,
        @Body message: MessageDto,
        @Header("Authorization") token: String = Storage.userToken
            ?: throw AccessTokenNullException(),
    ): Response<ResponseWrapper<ChatDto.Message>>

    @Multipart
    @POST("api/chats/{chat}/attachment")
    suspend fun sendAttachment(
        @Path("chat") chatId: Int,
        @Part message: MultipartBody.Part,
        @Header("Authorization") token: String = Storage.userToken
            ?: throw AccessTokenNullException(),
    ): Response<ResponseWrapper<ChatDto.Message>>

    @POST("api/chats/{chat}/continue")
    suspend fun continueChat(
        @Path("chat") chatId: Int,
        @Header("Authorization") token: String = Storage.userToken
            ?: throw AccessTokenNullException(),
    ): Response<ResponseWrapper<MessageTitleDto>>

    @POST("api/chats/{chat}/leave")
    suspend fun leaveChat(
        @Path("chat") chatId: Int,
        @Header("Authorization") token: String = Storage.userToken
            ?: throw AccessTokenNullException(),
    ): Response<ResponseWrapper<MessageTitleDto>>

    @GET("api/chats/{chat}/messages")
    suspend fun fetchChatMessages(
        @Path("chat") chatId: Int,
        @Query("page") page: Int,
        @Header("Authorization") token: String = Storage.userToken
            ?: throw AccessTokenNullException(),
    ): Response<ResponseWrapperList<ChatDto.Message>>
}
