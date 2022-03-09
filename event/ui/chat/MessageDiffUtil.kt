package com.dev.podo.event.ui.chat

import androidx.recyclerview.widget.DiffUtil
import com.dev.podo.event.model.entities.ChatEvent

class MessageDiffUtil(
    private var oldMessage: List<ChatEvent.Message>,
    private var newMessage: List<ChatEvent.Message>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldMessage.size
    }

    override fun getNewListSize(): Int {
        return newMessage.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldMessage[oldItemPosition].id == newMessage[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldMessage[oldItemPosition] == newMessage[newItemPosition]
    }
}
