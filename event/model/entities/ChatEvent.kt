package com.dev.podo.event.model.entities

import android.net.Uri
import com.dev.podo.chat.model.MessageType
import com.dev.podo.common.model.entities.Media
import com.dev.podo.common.model.entities.Sex
import com.dev.podo.core.model.entities.User
import java.util.*
import kotlin.collections.ArrayList

class ChatEvent {
    data class ChatBlock(
        val eventId: Long?,
        val userName: String,
        val eventTitle: String,
        val eventImage: String,
        val messages: ArrayList<Message>,
        var state: ChatState,
        val id: Int = 0,
        val users: List<User>
    ){
        companion object{
            fun emptyChat(): ChatBlock {
                return ChatBlock(
                    id = 0,
                    eventId = 0,
                    userName = "",
                    eventImage = "",
                    eventTitle = "",
                    state = ChatState.ACTIVE,
                    users = listOf(),
                    messages = arrayListOf()
                )
            }
        }
    }

    data class Message(
        val id: Int,
        val isSelf: Boolean,
        val isRead: Boolean,
        var text: String,
        val eventId: Int,
        val date: Date,
        val type: MessageType,
        val code: String? = "",
        val userId: Int? = null,
        var state: State = State.SEND,
        val media: List<Media?> = emptyList()
    ) {

        enum class State{
            SENDING, ERROR, SEND
        }

        override fun equals(other: Any?): Boolean {
            return if (other is Message) id == other.id
            else super.equals(other)
        }

        fun isSame(other: Message): Boolean {
            return if (text == other.text) date == other.date
            else false
        }

        override fun hashCode(): Int {
            return id
        }

        companion object {
            fun newSendMessage(eventId: Int, lastMessageId: Int, message: String): Message {
                return Message(
                    id = lastMessageId + 1,
                    isSelf = true,
                    isRead = false,
                    text = message,
                    date = Date(),
                    eventId = eventId,
                    type = MessageType.ORDINARY,
                )
            }

            fun newSendAttachment(eventId: Int, lastMessageId: Int, attachment: Uri): Message {
                return Message(
                    id = lastMessageId + 1,
                    isSelf = true,
                    isRead = false,
                    text = "",
                    media = listOf(Media(0, 0, Media.Url(attachment.toString(), null))),
                    date = Date(),
                    eventId = eventId,
                    type = MessageType.PHOTO,
                )
            }

            fun emptyMessage(id: Int = 1, message: String = "test message", eventId: Int = 0): Message {
                return Message(
                    id = id,
                    isSelf = false,
                    isRead = false,
                    text = message,
                    date = Date(),
                    eventId = eventId,
                    type = MessageType.ORDINARY
                )
            }
        }
    }

    data class UserChat(
        var id: Long? = null,
        var name: String? = null,
        var phone: String? = null,
        var birthDate: String? = null,
        var age: Int? = null,
        var sex: Sex? = null,
        var cityId: Int? = null,
        var city: String? = null,
        var description: String? = null,
        var registered: Boolean? = false,
        var approved: Boolean? = null,
        var isPlus: Boolean? = null,
        val media: List<Media>? = null,
        val chatState: ChatState = ChatState.TIMEOUT
    )

    enum class ChatState {
        AWAIT, DECLINE, TIMEOUT, ACTIVE, END, END_AWAIT
    }
}
