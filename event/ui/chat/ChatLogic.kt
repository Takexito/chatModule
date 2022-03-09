package com.dev.podo.event.ui.chat

import android.net.Uri
import android.util.Log
import com.dev.podo.common.ui.ProgressBarHelper
import com.dev.podo.common.utils.collectOnLifecycle
import com.dev.podo.common.utils.presentError
import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.event.model.entities.ChatEvent

class ChatLogic(val chatFragment: ChatFragment) {
    fun initCollectors() {
        collectChatInfo()
        collectNewMessage()
        collectChatContinueResponse()
        collectChatDeleteResponse()
    }

    private fun collectChatInfo() {
        chatFragment.collectOnLifecycle(flow = chatFragment.viewModel.chatInfoFlow) { result ->
            when (result) {
                is ResultState.Error -> {
                    chatFragment.presentError(result.exception)
                }
                ResultState.InProgress -> {
                }
                is ResultState.Success -> {
                    val chatBlock = result.data
                    chatFragment.adapters.messageAdapter.submitUsers(chatBlock.users)
                    chatFragment.selectViewByChatStatus(chatBlock)
                    chatFragment.initToolBar(chatBlock)
                    collectMessages()
                }
            }
        }
    }

    private fun collectMessages() {
        chatFragment.collectOnLifecycle(flow = chatFragment.viewModel.chatMessages) { result ->
            result?.let {
                chatFragment.adapters.stateAdapter.updateState(it)
            }
            when (result) {
                is ResultState.Error -> {
                    chatFragment.presentError(result.exception)
                }
                ResultState.InProgress -> {
                }
                is ResultState.Success -> {
                    result.data.let { resultData ->
                        chatFragment.endlessScrollListener?.lastPage =
                            resultData.meta?.lastPage ?: 1
                        resultData.data?.let { list ->
                            chatFragment.adapters.messageAdapter.addItems(list)
                        }
                    }
                }
            }
        }
    }

    private fun collectNewMessage() {
        chatFragment.collectOnLifecycle(flow = chatFragment.viewModel.newMessageAtChatFlow) { result ->
            result?.let {
                Log.i(this.javaClass.name, "collect: $it")
                chatFragment.adapters.messageAdapter.addItem(0, it)
            }
        }
    }

    fun collectSendMessageFlow() {
        chatFragment.collectOnLifecycle(flow = chatFragment.viewModel.sendMassageFlow) { result ->
            when (result) {
                is ResultState.Error -> {
                    chatFragment.sendErrorView()
                    chatFragment.presentError(
                        result.exception,
                        chatFragment.viewBinding.eventChatMessageInputEdit
                    )
                }
                ResultState.InProgress -> {
                    chatFragment.sendProgressView()
                }
                is ResultState.Success -> {
                    chatFragment.sendSuccessView()
                    showLastSentMessages(result.data)
                }
            }
        }
    }

    fun collectSendAttachmentMessageFlow() {
        chatFragment.collectOnLifecycle(flow = chatFragment.viewModel.sendAttachmentFlow) { result ->
            when (result) {
                is ResultState.Error -> {
//                    chatFragment.sendErrorView()
                    chatFragment.presentError(
                        result.exception,
                        chatFragment.viewBinding.eventChatMessageInputEdit
                    )
                }
                ResultState.InProgress -> {
                    chatFragment.sendProgressView()
                }
                is ResultState.Success -> {
                    chatFragment.sendSuccessView()
                    showLastSentMessages(result.data)
                }
            }
        }
    }

    fun sendMessage() {
        val text = chatFragment.viewBinding.eventChatMessageInputEdit.text.toString().trim()
        if (text.isBlank()) return
        val lastMessageId = chatFragment.adapters.messageAdapter.data.lastOrNull()?.id
        lastMessageId?.let {
            val chatId = chatFragment.viewModel.chatId
            val message = ChatEvent.Message.newSendMessage(chatId, lastMessageId, text)
            chatFragment.viewModel.sendMessage(message)
        }
    }

    fun sendAttachment(uri: Uri) {
        chatFragment.viewModel.sendAttachment(uri)
    }

    private fun showLastSentMessages(message: ChatEvent.Message) {
        chatFragment.adapters.messageAdapter.addItem(0, message)
        chatFragment.viewBinding.eventChatMessageInputEdit.setText("")
    }

    private fun collectChatContinueResponse() {
        chatFragment.collectOnLifecycle(flow = chatFragment.viewModel.chatContinueFlow) { result ->
            ProgressBarHelper.showOnLoading(chatFragment.viewBinding.eventChatProgress.root, result)
            when (result) {
                is ResultState.Error -> {
                    chatFragment.presentError(result.exception)
                }
                ResultState.InProgress -> {
                }
                is ResultState.Success -> {
                    chatFragment.setEndAwaitLayout()
                    chatFragment.viewModel.getChatInfo()
                }
            }
        }
    }

    private fun collectChatDeleteResponse() {
        chatFragment.collectOnLifecycle(flow = chatFragment.viewModel.chatDeleteFlow) { result ->
            ProgressBarHelper.showOnLoading(chatFragment.viewBinding.eventChatProgress.root, result)
            when (result) {
                is ResultState.Error -> {
                    chatFragment.presentError(result.exception)
                }
                ResultState.InProgress -> {
                }
                is ResultState.Success -> {
                    chatFragment.navigateBack()
                }
            }
        }
    }
}
