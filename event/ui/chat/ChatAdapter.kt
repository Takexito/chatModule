package com.dev.podo.event.ui.chat

import androidx.recyclerview.widget.ConcatAdapter
import com.dev.podo.common.ui.adapters.CommonStateAdapter
import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.event.ui.adapter.MessageAdapter

class ChatAdapter(bindings: ChatBinding) {
    val messageAdapter by lazy {
        MessageAdapter(
            bindings.getSentViewBinding,
            bindings.getSentPhotoViewBinding,
            bindings.getReceiveSystemViewBinding
        ) {}
    }
    val stateAdapter by lazy {
        CommonStateAdapter(
            {},
            bindings.getStateRecyclerViewBinding,
            ::retryMessagesFetch,
            ResultState.InProgress
        )
    }

    val messageConcatAdapter by lazy {
        ConcatAdapter(messageAdapter, stateAdapter)
    }

    private fun retryMessagesFetch() {
        // TODO: 24.10.2021 Implement retry messages fetch
    }
}
