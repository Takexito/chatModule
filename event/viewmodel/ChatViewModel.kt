package com.dev.podo.event.viewmodel

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.dev.podo.common.model.entities.PagedListWrapper
import com.dev.podo.core.model.entities.MessageTitle
import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.core.viewmodel.BaseViewModel
import com.dev.podo.event.model.entities.ChatEvent
import com.dev.podo.event.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class ChatViewModel @Inject constructor(private val repository: ChatRepository) : BaseViewModel() {

    private val _sendMassageFlow: MutableSharedFlow<ResultState<ChatEvent.Message>?> =
        MutableSharedFlow()
    val sendMassageFlow: SharedFlow<ResultState<ChatEvent.Message>?>
        get() = _sendMassageFlow.asSharedFlow()

    private val _sendAttachmentFlow: MutableSharedFlow<ResultState<ChatEvent.Message>?> =
        MutableSharedFlow()
    val sendAttachmentFlow: SharedFlow<ResultState<ChatEvent.Message>?>
        get() = _sendAttachmentFlow.asSharedFlow()

    private val _chatInfoFlow: MutableStateFlow<ResultState<ChatEvent.ChatBlock>> =
        MutableStateFlow(ResultState.InProgress)
    val chatInfoFlow: StateFlow<ResultState<ChatEvent.ChatBlock>>
        get() = _chatInfoFlow.asStateFlow()

    val newMessageFlow: SharedFlow<ChatEvent.Message?>
        get() = repository.getAllMessageFlow().shareIn(viewModelScope, SharingStarted.Eagerly)

    val newMessageAtChatFlow: SharedFlow<ChatEvent.Message?>
        get() = repository.getMessageFlow(chatId).shareIn(viewModelScope, SharingStarted.Eagerly)

    private val _chatContinueFlow: MutableSharedFlow<ResultState<MessageTitle>> =
        MutableSharedFlow()
    val chatContinueFlow: SharedFlow<ResultState<MessageTitle>>
        get() = _chatContinueFlow.asSharedFlow()

    private val _chatDeleteFlow: MutableSharedFlow<ResultState<MessageTitle>> =
        MutableSharedFlow()
    val chatDeleteFlow: SharedFlow<ResultState<MessageTitle>>
        get() = _chatDeleteFlow.asSharedFlow()

    private var _chatMessages: MutableStateFlow<ResultState<PagedListWrapper<ChatEvent.Message>>?> =
        MutableStateFlow(null)
    val chatMessages: StateFlow<ResultState<PagedListWrapper<ChatEvent.Message>>?>
        get() = _chatMessages.asStateFlow()

    var isChatContinues = false

    var chatId: Int = -1
        get() {
            require(field != -1)
            return field
        }

    var eventId: Int = -1
        get() {
            require(field != -1)
            return field
        }

    var uploadImages: ArrayList<Uri> = arrayListOf()

    fun fetchMessages(page: Int) {
        viewModelScope.launch {
            repository.fetchPagedChatMessages(chatId, page).collect { data ->
                _chatMessages.emit(data)
            }
        }
    }

    fun getChatInfo() {
        viewModelScope.launch {
            _chatInfoFlow.emit(ResultState.InProgress)
            val chatBlockList = repository.getChatInfo(chatId)
            chatBlockList.collect { _chatInfoFlow.emit(it) }
        }
    }

    fun sendMessage(message: ChatEvent.Message) {
        viewModelScope.launch {
            _sendMassageFlow.emit(ResultState.InProgress)
            repository.sendMessage(chatId, message).collect {
                _sendMassageFlow.emit(it)
            }
        }
    }

    fun sendAttachment(uri: Uri) {
        viewModelScope.launch {
            val newMessage = ChatEvent.Message.newSendAttachment(chatId, 0, uri)
            _sendAttachmentFlow.emit(ResultState.InProgress)
            repository.sendMessage(chatId, newMessage).collect {
                _sendAttachmentFlow.emit(it)
            }
        }
    }

    fun continueChat() {
        viewModelScope.launch {
            _chatContinueFlow.emit(ResultState.InProgress)
            repository.continueChat(chatId).collect {
                _chatContinueFlow.emit(it)
            }
        }
    }

    fun deleteChat() {
        viewModelScope.launch {
            _chatDeleteFlow.emit(ResultState.InProgress)
            repository.deleteChat(chatId).collect {
                _chatDeleteFlow.emit(it)
            }
        }
    }
}
