package com.dev.podo.event.model.dto.chat

import com.dev.podo.auth.model.dto.UserDto
import com.dev.podo.chat.model.MessageType
import com.dev.podo.common.model.dto.MediaDto
import com.dev.podo.common.model.entities.Media
import com.dev.podo.common.utils.toLocalDate
import com.dev.podo.core.datasource.Storage
import com.dev.podo.core.model.dto.Dto
import com.dev.podo.core.model.dto.asEntityArray
import com.dev.podo.event.model.entities.ChatEvent
import com.dev.podo.home.model.dto.HomeDto
import com.squareup.moshi.Json

class ChatDto {
    data class Data(
        @Json(name = "id") var id: Int,
        @Json(name = "messages") var messages: List<Message>,
        @Json(name = "users") var users: List<UserDto>?,
        @Json(name = "event") var event: HomeDto.EventData?,
        @Json(name = "my_status") var status: String,
        @Json(name = "is_closed") var isClosed: Boolean,
        @Json(name = "closed_at") var closedAt: String?,
        @Json(name = "deleted_at") var deletedAt: String?,
        @Json(name = "created_at") var createdAt: String?,
        @Json(name = "updated_at") var updatedAt: String?
    ) : Dto<ChatEvent.ChatBlock> {
        override fun asEntity(): ChatEvent.ChatBlock {
            // TODO: fix on back
            // if user delete chat user is null
            val firstUser = users?.getOrNull(0)
            val secondUser = users?.getOrNull(1)

            val selfUser = if (firstUser?.id == Storage.user?.id) firstUser else secondUser
            val otherUser = if (firstUser?.id == selfUser?.id) secondUser else firstUser

            val state = when {
                otherUser == null -> ChatEvent.ChatState.TIMEOUT
                isClosed -> ChatEvent.ChatState.DECLINE
                otherUser?.chatStatus?.contains("invited") ?: false -> ChatEvent.ChatState.AWAIT
                selfUser?.chatStatus?.contains("asked") ?: false -> ChatEvent.ChatState.END_AWAIT
                otherUser?.chatStatus?.contains("asked") ?: false -> ChatEvent.ChatState.END
                selfUser?.chatStatus?.contains("present") ?: false -> ChatEvent.ChatState.ACTIVE
                else -> ChatEvent.ChatState.TIMEOUT
            }
            val users = users?.map {
                it.asEntity()
            }?.toList()
            // TODO: check if correct image by order
            return ChatEvent.ChatBlock(
                eventId = event?.id,
                userName = otherUser?.name ?: "",
                eventImage = otherUser?.media?.first()?.url?.thumb ?: "",
                eventTitle = event?.title ?: "",
                messages = messages.asEntityArray(),
                state = state,
                id = id,
                users = users ?: emptyList()
            )
        }
    }

    data class WSMessage(
        @Json(name = "message") var message: Message,
        @Json(name = "event") var event: String?,
    )

    data class ShortUser(
        @Json(name = "name") var name: String,
        @Json(name = "avatar") var avatarList: List<Media>,
    )

    // TODO: fix on back
    // if user delete chat user_id at message is null
    data class Message(
        @Json(name = "id") var id: Int,
        @Json(name = "user_id") var userId: Int?,
        @Json(name = "message") var message: String?,
        @Json(name = "type") var type: String?,
        @Json(name = "event") var eventName: String?,
        @Json(name = "event_id") var eventId: Int?,
        @Json(name = "user") var user: ShortUser?,
        @Json(name = "created_at") var createdAt: String,
        @Json(name = "updated_at") var updatedAt: String,
        @Json(name = "code") var code: String?,
        @Json(name = "attachments") var media: List<MediaDto?> = listOf(),
    ) : Dto<ChatEvent.Message> {
        override fun asEntity(): ChatEvent.Message {
            // TODO: FIXME
            val selfUserId = Storage.user?.id?.toInt()
            return ChatEvent.Message(
                id = id,
                eventId = eventId ?: -1,
                isSelf = userId == selfUserId,
                date = createdAt.toLocalDate(),
                isRead = true,
                text = message ?: "Вложение",
                type = parseMessageType(),
                code = code ?: "",
                userId = userId,
                media = media.asEntity()
            )
        }

        fun List<MediaDto?>.asEntity(): List<Media> {
            val list = arrayListOf<Media>()
            forEach {
                it?.asEntity()?.let { it1 -> list.add(it1) }
            }
            return list
        }

        private fun parseMessageType(): MessageType {
            return when (type) {
                "attachment" -> MessageType.PHOTO
                "system" -> MessageType.SYSTEM
                else -> MessageType.ORDINARY
            }
        }
    }
}
