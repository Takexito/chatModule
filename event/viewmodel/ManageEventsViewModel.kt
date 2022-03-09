package com.dev.podo.event.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.core.viewmodel.BaseViewModel
import com.dev.podo.event.model.entities.ChatEvent
import com.dev.podo.event.repository.ChatRepository
import com.dev.podo.event.repository.EventRepository
import com.dev.podo.home.model.entities.HomeEventModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ManageEventsViewModel @Inject constructor(
    private val repository: EventRepository,
    private val chatRepository: ChatRepository
) : BaseViewModel() {

    private val _eventResponse: MutableLiveData<ResultState<List<HomeEventModel>>> =
        MutableLiveData()
    val eventResponse: LiveData<ResultState<List<HomeEventModel>>>
        get() = _eventResponse

    private val _chats: MutableLiveData<ResultState<List<ChatEvent.ChatBlock>>> = MutableLiveData()
    val chats: LiveData<ResultState<List<ChatEvent.ChatBlock>>>
        get() = _chats

    private fun fetchUserEvents() {
        launchOnFlow(repository::fetchUserEvents) { response ->
            _eventResponse.postValue(response)
        }
    }

    init {
        fetchUserEvents()
        fetchUserChats()
    }

    fun fetchUserChats() {
        launchOnFlow(chatRepository::fetchChats) { response ->
            _chats.postValue(response)
        }
    }
}
