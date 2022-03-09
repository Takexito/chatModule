package com.dev.podo.event.repository

import androidx.paging.PagingData
import com.dev.podo.common.model.entities.tag.TagGroup
import com.dev.podo.core.model.entities.MessageTitle
import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.event.model.dto.CreateEventDto
import com.dev.podo.event.model.entities.ChatEvent
import com.dev.podo.event.model.entities.prompt.Prompt
import com.dev.podo.home.model.entities.HomeEventModel
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    suspend fun fetchTags(): Flow<ResultState<List<TagGroup>>>
    suspend fun createEvent(event: CreateEventDto): Flow<ResultState<HomeEventModel>>
    suspend fun fetchUserEvents(): Flow<ResultState<List<HomeEventModel>>>
    suspend fun archiveEvent(eventId: Long): Flow<ResultState<MessageTitle>>
    suspend fun fetchPagedPromptList(eventId: Long): Flow<PagingData<Prompt>>
    suspend fun acceptPrompt(promptId: Long): Flow<ResultState<MessageTitle>>
    suspend fun declinePrompt(promptId: Long): Flow<ResultState<MessageTitle>>
}
