package com.dev.podo.event.datasource

import com.dev.podo.event.model.entities.ChatEvent

object ChatStorage {
    val chatList: ArrayList<ChatEvent.ChatBlock> = arrayListOf()
    fun updateChat(list: List<ChatEvent.ChatBlock>){
        chatList.clear()
        chatList.addAll(list)
    }
}