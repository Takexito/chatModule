package com.dev.podo.event.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dev.podo.core.model.entities.MessageTitle
import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.core.viewmodel.BaseViewModel
import com.dev.podo.event.repository.EventRepository
import com.dev.podo.home.repository.HomeRepository
import com.dev.podo.home.viewmodel.DetailedEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EventEditViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val eventRepository: EventRepository
) :
    BaseViewModel() {

    private val _event = MutableLiveData<DetailedEvent>().apply {
        value = ResultState.InProgress
    }
    val event: LiveData<DetailedEvent> = _event

    private val _archiveEventResult = MutableLiveData<ResultState<MessageTitle>>()
    val archiveEventResult: LiveData<ResultState<MessageTitle>> = _archiveEventResult

    val eventId: Long
        get() = event.value?.getSuccessData()?.id
            ?: throw NullPointerException("Can't get Event Id")

    fun fetchDetailedEvent(id: Long, isTrashed: Boolean = false) {
        launchOnFlow(
            async = {
                if (isTrashed) {
                    return@launchOnFlow homeRepository.fetchTrashedDetailedEvent(id)
                }
                homeRepository.fetchDetailedEvent(id)
            },
            doOnCollect = { result ->
                _event.value = result
            }
        )
    }

    fun archiveEvent() {
        launchOnFlow(eventId, eventRepository::archiveEvent) { result ->
            _archiveEventResult.postValue(result)
        }
    }
}
