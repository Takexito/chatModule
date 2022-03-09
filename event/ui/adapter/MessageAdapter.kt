package com.dev.podo.event.ui.adapter

import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.dev.podo.R
import com.dev.podo.chat.model.MessageType
import com.dev.podo.chat.util.ChatMessagesDateHelper
import com.dev.podo.common.utils.i18n.ServerCodesParser
import com.dev.podo.common.utils.px
import com.dev.podo.core.model.entities.User
import com.dev.podo.core.ui.adapter.OnRecyclerItemClick
import com.dev.podo.core.ui.adapter.ViewBindingGetter
import com.dev.podo.databinding.EventSentMessageItemBinding
import com.dev.podo.databinding.EventSentMessagePhotoItemBinding
import com.dev.podo.databinding.EventSystemMessageItemBinding
import com.dev.podo.event.model.entities.ChatEvent
import com.dev.podo.event.ui.chat.MessageDiffUtil
import com.stfalcon.imageviewer.StfalconImageViewer

private const val VIEW_TYPE_MESSAGE_SENT = 1
private const val VIEW_TYPE_MESSAGE_SYSTEM = 3
private const val VIEW_TYPE_MESSAGE_SENT_PHOTO = 4

class MessageAdapter(
    private val getSentMessageViewBinding: ViewBindingGetter<EventSentMessageItemBinding>,
    private val getSentPhotoMessageViewBinding: ViewBindingGetter<EventSentMessagePhotoItemBinding>,
    private val getSystemMessageViewBinding: ViewBindingGetter<EventSystemMessageItemBinding>,
    private val onRecyclerItemClick: OnRecyclerItemClick
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder<ViewBinding>>() {

    private val TAG = MessageAdapter::class.java.canonicalName
    var users = mutableListOf<User>()
    var data: MutableList<ChatEvent.Message> = mutableListOf()
    var sentMessageViewBinding: EventSentMessageItemBinding? = null
    var systemMessageViewBinging: EventSystemMessageItemBinding? = null
    var sentMessagePhotoViewBinding: EventSentMessagePhotoItemBinding? = null

    private var mRecyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    fun scrollToPosition(position: Int) {
        mRecyclerView?.scrollToPosition(position)
    }

    fun addItems(list: List<ChatEvent.Message>) {
        if (data.isEmpty()) {
            data.addAll(list)
            notifyItemRangeInserted(0, data.size)
        } else {
            val newData = data.replaceOrAddNewList(list).sortedBy {
                it.date
            }.reversed()
            val diff = DiffUtil.calculateDiff(MessageDiffUtil(data, newData))
            data.clear()
            data.addAll(newData)
            diff.dispatchUpdatesTo(this)
        }
    }

    fun submitUsers(list: List<User>) {
        users.clear()
        users.addAll(list)
    }

    fun replaceList(list: List<ChatEvent.Message>) {
        if (this.data.isEmpty()) {
            this.data.addAll(list)
            notifyItemRangeInserted(0, data.size)
        } else {
            val diff = DiffUtil.calculateDiff(MessageDiffUtil(this.data, list))
            diff.dispatchUpdatesTo(this)
        }
    }

    fun addItem(index: Int = data.size - 1, item: ChatEvent.Message): Boolean {
        if (data.size <= index) return false
        val messageList = data.toMutableList()
        val oldItem = messageList[index]
        if (oldItem == item) return false
        messageList.add(index, item)
        data = messageList
        notifyItemInserted(index)
        notifyItemChanged(index + 1)
        scrollToPosition(index)
        return true
    }

    abstract class MessageViewHolder<B : ViewBinding>(val viewBinding: B) :
        RecyclerView.ViewHolder(viewBinding.root) {
        abstract fun bind(position: Int)
    }

    inner class SentMessageViewHolder<B : ViewBinding>(viewBinding: B) :
        MessageViewHolder<B>(viewBinding) {
        override fun bind(position: Int) {
            val message = data[position]
            (viewBinding as EventSentMessageItemBinding).apply {
                root.clearConstraint(eventSentChatMessageCard.id, ConstraintSet.END)
                root.clearConstraint(eventSentChatMessageTime.id, ConstraintSet.END)
                root.clearConstraint(eventSentChatMessageCard.id, ConstraintSet.START)
                root.clearConstraint(eventSentChatMessageTime.id, ConstraintSet.START)
                if (message.isSelf){
                    root.connectConstraint(eventSentChatMessageCard.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 24.px)
                    root.connectConstraint(eventSentChatMessageTime.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 24.px)
                    eventSentChatMessageCard.setCardBackgroundColor(itemView.resources.getColor(R.color.light_green))
                } else {
                    root.connectConstraint(eventSentChatMessageCard.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 24.px)
                    root.connectConstraint(eventSentChatMessageTime.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 24.px)
                    eventSentChatMessageCard.setCardBackgroundColor(itemView.resources.getColor(R.color.label_quaternary))
                }

                eventSentChatMessageText.text = message.text
                ChatMessagesDateHelper.displayDateViews(
                    position,
                    eventSentChatDate,
                    eventSentChatMessageTime,
                    itemCount,
                    data
                )
            }
        }
    }

    inner class SentMessagePhotoViewHolder<B : ViewBinding>(viewBinding: B) :
        MessageViewHolder<B>(viewBinding) {
        override fun bind(position: Int) {
            val message = data[position]
            (viewBinding as EventSentMessagePhotoItemBinding).apply {
                root.clearConstraint(eventSentChatMessageCard.id, ConstraintSet.END)
                root.clearConstraint(eventSentChatMessageTime.id, ConstraintSet.END)
                if (message.isSelf){
                    root.connectConstraint(eventSentChatMessageCard.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 24.px)
                    root.connectConstraint(eventSentChatMessageTime.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 24.px)
                    eventSentChatMessageCard.setCardBackgroundColor(itemView.resources.getColor(R.color.light_green))
                } else {
                    root.connectConstraint(eventSentChatMessageCard.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 24.px)
                    root.connectConstraint(eventSentChatMessageTime.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 24.px)
                    eventSentChatMessageCard.setCardBackgroundColor(itemView.resources.getColor(R.color.label_quaternary))
                }

                Glide
                    .with(eventSentChatMessageImage)
                    .load(message.media[0]?.url?.fullSize)
                    .into(eventSentChatMessageImage)

                eventSentChatMessageImage.scaleType = ImageView.ScaleType.CENTER_CROP

                ChatMessagesDateHelper.displayDateViews(
                    position,
                    eventSentChatDate,
                    eventSentChatMessageTime,
                    itemCount,
                    data
                )

                eventSentChatMessageImage.setOnClickListener {
                    StfalconImageViewer.Builder(root.context, listOf(message.media[0])) { view, image ->
                        Glide
                            .with(view)
                            .load(image?.url?.fullSize)
                            .into(view)
                    }
                        .withTransitionFrom(eventSentChatMessageImage)
                        .show(true)
                }
            }
        }
    }

    inner class SystemMessageViewHolder<B : ViewBinding>(viewBinding: B) :
        MessageViewHolder<B>(viewBinding) {
        override fun bind(position: Int) {
            val message = data[position]
            (viewBinding as EventSystemMessageItemBinding).apply {
                val user = users.firstOrNull { it.id == message.userId?.toLong() }
                eventSystemChatMessageText.text = ServerCodesParser.systemMessageParse(
                    itemView.context,
                    message.code,
                    user?.sex,
                    user?.name,
                )
                ChatMessagesDateHelper.displayDateViews(
                    position,
                    eventSystemChatDate,
                    null,
                    itemCount,
                    data
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = data[position]

        return when (message.type) {
            MessageType.PHOTO -> {
                VIEW_TYPE_MESSAGE_SENT_PHOTO
            }
            MessageType.ORDINARY -> {
                VIEW_TYPE_MESSAGE_SENT
//                if (message.isSelf) VIEW_TYPE_MESSAGE_SENT else VIEW_TYPE_MESSAGE_RECEIVED
            }
            MessageType.SYSTEM -> {
                VIEW_TYPE_MESSAGE_SYSTEM
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MessageViewHolder<ViewBinding> {

        return when (viewType) {
            VIEW_TYPE_MESSAGE_SENT -> {
                sentMessageViewBinding = getSentMessageViewBinding.invoke(parent)
                SentMessageViewHolder(sentMessageViewBinding!!)
            }
            VIEW_TYPE_MESSAGE_SYSTEM -> {
                systemMessageViewBinging = getSystemMessageViewBinding.invoke(parent)
                SystemMessageViewHolder(systemMessageViewBinging!!)
            }
            VIEW_TYPE_MESSAGE_SENT_PHOTO -> {
                sentMessagePhotoViewBinding = getSentPhotoMessageViewBinding.invoke(parent)
                SentMessagePhotoViewHolder(sentMessagePhotoViewBinding!!)
            }
            else -> {
                // TODO: add error view
                sentMessageViewBinding = getSentMessageViewBinding.invoke(parent)
                SentMessageViewHolder(sentMessageViewBinding!!)
            }
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder<ViewBinding>, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return data.size
    }

}

fun <T> MutableList<T>.replaceOrAdd(newList: List<T>) {
    newList.forEach { newMessage ->
        val oldMessage = find { it == newMessage }
        if (oldMessage != null) {
            val index = indexOf(oldMessage)
            if (index == -1) return
            this[index] = newMessage
        } else {
            this.add(0, newMessage)
        }
    }
}

fun <T> MutableList<T>.replaceOrAddNewList(newList: List<T>): List<T> {
    val result = arrayListOf<T>()
    result.addAll(this)
    newList.reversed().forEach { newMessage ->
        val oldMessage = find { it == newMessage }
        if (oldMessage != null) {
            val index = result.indexOf(oldMessage)
            if (index != -1) result[index] = newMessage
        } else {
            result.add(0, newMessage)
        }
    }
    return result
}

fun ConstraintLayout.clearConstraint(viewId: Int, anchor: Int? = null){
    val constraintSet = ConstraintSet()
    constraintSet.clone(this)
    if (anchor == null) constraintSet.clear(viewId)
    else constraintSet.clear(viewId, anchor)
    constraintSet.applyTo(this)
}

fun ConstraintLayout.connectConstraint(startID: Int, startSide: Int, endID: Int, endSide: Int , margin: Int? = null){
    val constraintSet = ConstraintSet()
    constraintSet.clone(this)
    if (margin == null) constraintSet.connect(startID, startSide, endID, endSide)
    else constraintSet.connect(startID, startSide, endID, endSide, margin)
    constraintSet.applyTo(this)
}