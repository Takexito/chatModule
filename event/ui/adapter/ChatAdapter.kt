package com.dev.podo.event.ui.adapter

import com.bumptech.glide.Glide
import com.dev.podo.R
import com.dev.podo.common.utils.time
import com.dev.podo.core.ui.adapter.BaseAdapter
import com.dev.podo.core.ui.adapter.OnRecyclerItemClick
import com.dev.podo.core.ui.adapter.ViewBindingGetter
import com.dev.podo.databinding.EventChatItemBinding
import com.dev.podo.event.model.entities.ChatEvent

class ChatAdapter(
    onItemClick: OnRecyclerItemClick,
    getViewBinding: ViewBindingGetter<EventChatItemBinding>
) : BaseAdapter<EventChatItemBinding, ChatEvent.ChatBlock>(onItemClick, getViewBinding) {

    override fun bind(viewBinding: EventChatItemBinding, position: Int) {
        val model = data[position]
        viewBinding.apply {
            root.setOnClickListener { onItemClick?.invoke(position) }
            chatUserName.text = model.userName
            chatEventTitle.text = model.eventTitle

            if(model.messages.size < 1) return@apply

            val lastMessage = model.messages.first()
            val messageDate = lastMessage.date
            chatMessageTime.text = messageDate.time()

            // Todo: to string
            chatMessageLast.text = if (lastMessage.isSelf) "Вы: ${lastMessage.text}"
            else lastMessage.text

            // Todo: with theme
            if (!lastMessage.isRead) chatMessageLast.setTextColor(root.resources.getColor(R.color.dark_green))

            Glide
                .with(root.context)
                .load(model.eventImage)
                .centerCrop()
//                .placeholder(R.drawable.podo_bench_image_short)
                .into(chatEventImage)
        }
    }

    fun newMessage(message: ChatEvent.Message){
        val chat = data.find { it.eventId?.toInt() == message.eventId }
        chat?.messages?.add(0, message)
        notifyItemChanged(0)
    }
}
