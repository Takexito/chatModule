package com.dev.podo.event.repository

import androidx.paging.Pager
import androidx.paging.PagingData
import com.dev.podo.common.model.entities.tag.TagGroup
import com.dev.podo.common.utils.Constants
import com.dev.podo.core.datasource.Storage
import com.dev.podo.core.model.entities.MessageTitle
import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.core.repository.BaseRepository
import com.dev.podo.event.datasource.EventPromptDataSource
import com.dev.podo.event.model.dto.CreateEventDto
import com.dev.podo.event.model.entities.prompt.Prompt
import com.dev.podo.home.model.entities.HomeEventModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class EventRepositoryImpl @Inject constructor(private val eventApi: EventApi) : EventRepository,
    BaseRepository() {
    override suspend fun fetchTags(): Flow<ResultState<List<TagGroup>>> {
        return fetchDataList { eventApi.fetchEventTags() }
    }

    override suspend fun createEvent(event: CreateEventDto): Flow<ResultState<HomeEventModel>> {
        return fetchData { eventApi.createEvent(Storage.userToken ?: "", event) }
    }

    override suspend fun fetchUserEvents(): Flow<ResultState<List<HomeEventModel>>> {
        return fetchDataList { eventApi.fetchUserEvents() }
    }

    override suspend fun archiveEvent(eventId: Long): Flow<ResultState<MessageTitle>> {
        return fetchDataWithoutWrapper { eventApi.archiveEvent(eventId) }
    }

    override suspend fun fetchPagedPromptList(eventId: Long): Flow<PagingData<Prompt>> {
        return Pager(
            config = Constants.defaultPagingConfig(),
            pagingSourceFactory = { EventPromptDataSource(eventApi, eventId, exceptionHandler) }
        ).flow
    }

    override suspend fun acceptPrompt(
        promptId: Long,
    ): Flow<ResultState<MessageTitle>> {
        return fetchDataWithoutWrapper { eventApi.acceptPrompt(promptId = promptId) }
    }

    override suspend fun declinePrompt(
        promptId: Long,
    ): Flow<ResultState<MessageTitle>> {
        return fetchDataWithoutWrapper { eventApi.declinePrompt(promptId = promptId) }

    }
}
