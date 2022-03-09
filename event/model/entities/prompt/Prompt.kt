package com.dev.podo.event.model.entities.prompt

import androidx.recyclerview.widget.DiffUtil
import com.dev.podo.core.model.entities.User

data class Prompt(
    val id: Long?,
    val message: String? = "",
    val status: String?,
    val user: User?,
    val userId: Long?,
    val eventId: Long?
) {
    companion object {
        // TODO:: complete fakeFactory with fake users
        fun fakeFactory(size: Int): ArrayList<Prompt> {
            val prompts = arrayListOf<Prompt>()
            for (i in 0 until size) {
                prompts.add(
                    Prompt(
                        i.toLong(),
                        "",
                        "",
                        null,
                        null,
                        null
                    )
                )
            }
            return prompts
        }
    }
}

object PromptComparator : DiffUtil.ItemCallback<Prompt>() {

    override fun areItemsTheSame(oldItem: Prompt, newItem: Prompt): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Prompt, newItem: Prompt): Boolean {
        return oldItem.message == newItem.message ||
            oldItem.status == newItem.status ||
            oldItem.userId == newItem.userId
    }
}
