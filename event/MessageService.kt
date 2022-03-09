package com.dev.podo.event

import com.dev.podo.event.datasource.ChatMessageDataSource
import com.dev.podo.event.datasource.ChatStorage
import com.dev.podo.event.model.entities.ChatEvent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*


/**
 * Mark feature at developing process
 *
 * You mustn't use these feature
 */
annotation class Developing

/**
 * Mark feature at developing process
 *
 * These feature can be change
 */
annotation class Experimental

@Developing
class MessageService(
    val chatId: Int,
) {
    val _messageFlow: MutableSharedFlow<ChatEvent.Message> = MutableSharedFlow()
    private val messageSet: HashSet<ChatEvent.Message> = hashSetOf()
    private val chat: ChatEvent.ChatBlock?
        get() = ChatStorage.chatList.find { chat -> chat.id == chatId }

    private fun findMessageAtSet(message: ChatEvent.Message): ChatEvent.Message? {
        return messageSet.find { it.text == message.text }
    }

    private fun getMessageList(): List<ChatEvent.Message> {
        val list = chat?.messages ?: arrayListOf()
        list.addAll(messageSet)
        return list
    }

    suspend fun add(message: ChatEvent.Message): ChatEvent.Message {
        message.state = ChatEvent.Message.State.SENDING
        messageSet.add(message)
        _messageFlow.emit(message)
        return message
    }

    suspend fun confirmMessage(message: ChatEvent.Message): ChatEvent.Message? {
        findMessageAtSet(message)?.let { localMessage ->
            chat?.messages?.add(message)
            messageSet.remove(localMessage)
            _messageFlow.emit(message)
            return message
        }
        return null
    }

    suspend fun errorState(message: ChatEvent.Message): ChatEvent.Message? {
        findMessageAtSet(message)?.let { localMessage ->
            localMessage.state = ChatEvent.Message.State.ERROR
            _messageFlow.emit(localMessage)
            return localMessage
        }
        return null
    }
}

@Experimental
class MessageServicePool constructor(
    private val chatMessageDataSource: ChatMessageDataSource
) {

    private var _messageFlow: Flow<ChatEvent.Message?> = flowOf()

    @FlowPreview
    val allMessageFlow: Flow<ChatEvent.Message?> =
        flowOf(_messageFlow, chatMessageDataSource.messageFlow).flattenMerge()

    private val messageServicePool: HashMap<Int, MessageService> = hashMapOf()

    @Developing
    @FlowPreview
    private fun addToPool(service: MessageService) {
        messageServicePool[service.chatId] = service
        _messageFlow = flowOf(service._messageFlow, _messageFlow).flattenMerge()
    }

    @FlowPreview
    fun getMessageFlow(chatId: Int): Flow<ChatEvent.Message?> {
        val chat = ChatStorage.chatList.find { it.id == chatId }
        val eventId = chat?.eventId?.toInt() ?: -1
        val flow = chatMessageDataSource.messageFlow.map { message ->
            if (message?.eventId == eventId) {
                if (message.isSelf) null else message
            } else null
        }
        val serviceFlow = getService(chatId)._messageFlow
        return flowOf(serviceFlow, flow).flattenMerge()
    }

    @Developing
    @FlowPreview
    fun getService(chatId: Int): MessageService {
        var messageService: MessageService? = messageServicePool[chatId]
        if (messageService == null) {
            messageService = MessageService(chatId)
            addToPool(messageService)
        }
        return messageService
    }
}