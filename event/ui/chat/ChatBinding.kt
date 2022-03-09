package com.dev.podo.event.ui.chat

import android.content.Context
import android.view.LayoutInflater
import com.dev.podo.core.ui.adapter.ViewBindingGetter
import com.dev.podo.databinding.*

class ChatBinding(val context: Context) {
    val getSentViewBinding: ViewBindingGetter<EventSentMessageItemBinding> = { parent ->
        EventSentMessageItemBinding.inflate(LayoutInflater.from(context), parent, false)
    }

    val getStateRecyclerViewBinding: ViewBindingGetter<ItemNetworkStateBinding> =
        { parent ->
            ItemNetworkStateBinding.inflate(LayoutInflater.from(context), parent, false)
        }

    val getReceiveViewBinding: ViewBindingGetter<EventReceiveMessageItemBinding> =
        { parent ->
            EventReceiveMessageItemBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        }

    val getReceivePhotoViewBinding: ViewBindingGetter<EventReceiveMessagePhotoItemBinding> =
        { parent ->
            EventReceiveMessagePhotoItemBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        }

    val getSentPhotoViewBinding: ViewBindingGetter<EventSentMessagePhotoItemBinding> =
        { parent ->
            EventSentMessagePhotoItemBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        }

    val getReceiveSystemViewBinding: ViewBindingGetter<EventSystemMessageItemBinding> =
        { parent ->
            EventSystemMessageItemBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        }
}
