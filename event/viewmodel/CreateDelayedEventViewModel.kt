package com.dev.podo.event.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dev.podo.common.model.entities.Choice
import com.dev.podo.common.model.entities.Section
import com.dev.podo.common.model.entities.tag.TagGroup
import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.core.viewmodel.BaseViewModel
import com.dev.podo.event.model.dto.EventType
import com.dev.podo.event.model.entities.Event
import com.dev.podo.event.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

typealias TagGroups = Section<Choice>

@HiltViewModel
class CreateDelayedEventViewModel @Inject constructor(private val eventRepository: EventRepository) :
    BaseViewModel() {

    private val _tagGroupsResponse: MutableLiveData<ResultState<List<TagGroup>>> = MutableLiveData()
    val tagGroupsResponse: LiveData<ResultState<List<TagGroup>>>
        get() = _tagGroupsResponse

    private val _tagSections: MutableLiveData<MutableList<TagGroups>> = MutableLiveData()
    val tagSections: LiveData<MutableList<TagGroups>>
        get() = _tagSections

    val event = Event(type = EventType.EVENT)
    private val _eventResponse: MutableLiveData<ResultState<*>> = MutableLiveData()
    val eventResponse: LiveData<ResultState<*>>
        get() = _eventResponse

    fun updateTags(updatedTags: MutableList<TagGroups>) {
        _tagSections.postValue(updatedTags)
    }

    private fun fetchTags() {
        launchOnFlow(eventRepository::fetchTags) { result ->
            _tagGroupsResponse.postValue(result)
        }
    }

    fun convertPreferred(position: Int): String {
        return when (position) {
            0 -> "any"
            1 -> "female"
            2 -> "male"
            else -> ""
        }
    }

    fun createEvent() {
        try {
            event.validate()
        } catch (exception: Exception) {
            _eventResponse.postValue(
                ResultState.Error(exception)
            )
            return
        }
        launchOnFlow(event.asDto(), eventRepository::createEvent) { result ->
            _eventResponse.postValue(result)
        }
    }

    init {
        event.type = EventType.EVENT
        fetchTags()
    }
}
