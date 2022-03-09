package com.dev.podo.event.datasource

import com.dev.podo.event.model.entities.ChatEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow

interface ChatMessageDataSource {
    val messageFlow: SharedFlow<ChatEvent.Message?>
    fun init(coroutineScope: CoroutineScope)
}
