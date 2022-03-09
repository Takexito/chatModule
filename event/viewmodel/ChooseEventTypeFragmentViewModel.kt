package com.dev.podo.event.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dev.podo.common.model.entities.EventType
import com.dev.podo.common.utils.exceptions.SubscriptionRestrictException
import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.core.viewmodel.BaseViewModel
import com.dev.podo.event.repository.EventRepository
import com.dev.podo.home.model.entities.HomeEventModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChooseEventTypeFragmentViewModel @Inject constructor(private val repository: EventRepository) :
    BaseViewModel() {

    private val _userEventsResponse: MutableLiveData<ResultState<List<HomeEventModel>>> = MutableLiveData()
    val userEventsResponse: LiveData<ResultState<List<HomeEventModel>>>
        get() = _userEventsResponse

    fun checkEventCreatePermission(eventType: EventType) {
        when (eventType) {
            EventType.LIVE_EVENT -> {
                getLiveEvents()
            }
            EventType.DELAYED_EVENT -> {
                getDelayedEvents()
            }
        }
    }

    private fun getLiveEvents() {
        launchOnFlow(repository::fetchUserEvents) { result ->
            if (result is ResultState.Success) {
                val events = result.data
                for (item in events) {
                    if (item.type == EventType.LIVE_EVENT) {
                        postSubscriptionError()
                        return@launchOnFlow
                    }
                }
            }
            _userEventsResponse.postValue(result)
        }
//        postSubscriptionError()
    }

    private fun getDelayedEvents() {
        launchOnFlow(repository::fetchUserEvents) { result ->
            if (result is ResultState.Success) {
                val events = result.data
                for (item in events) {
                    if (item.type == EventType.DELAYED_EVENT) {
                        postSubscriptionError()
                        return@launchOnFlow
                    }
                }
            }
            _userEventsResponse.postValue(result)
        }
    }

    private fun postSubscriptionError() {
        _userEventsResponse.postValue(
            ResultState.Error(
                SubscriptionRestrictException()
            )
        )
    }
}
