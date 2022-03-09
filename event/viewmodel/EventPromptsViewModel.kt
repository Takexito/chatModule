package com.dev.podo.event.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dev.podo.core.model.entities.MessageTitle
import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.core.viewmodel.BaseViewModel
import com.dev.podo.event.model.entities.prompt.Prompt
import com.dev.podo.event.repository.EventRepository
import com.dev.podo.event.ui.adapter.prompt.PROMPT_ACCEPT
import com.dev.podo.event.ui.adapter.prompt.PROMPT_DECLINE
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

@HiltViewModel
class EventPromptsViewModel @Inject constructor(private val eventRepository: EventRepository) :
    BaseViewModel() {

    private var _promptFlow: Flow<PagingData<Prompt>> = flow {}
    val promptFlow: Flow<PagingData<Prompt>>
        get() = _promptFlow

    private val _promptActionData = MutableLiveData<PromptActionState>()
    val promptActionData: LiveData<PromptActionState>
        get() = _promptActionData

    fun fetchEventResponses(eventId: Long) {
        Log.e("LOGGING", "BEFORE REPO CALL")
        viewModelScope.launch {
            val result = eventRepository
                .fetchPagedPromptList(eventId)
                .cachedIn(viewModelScope)
            _promptFlow = result
        }
        Log.e("LOGGING", "AFTER REPO CALL")
    }

    fun performPromptAction(promptId: Long, actionType: Int, position: Int) {
        when (actionType) {
            PROMPT_DECLINE -> {
                launchOnFlow(promptId, eventRepository::declinePrompt) { result ->
                    _promptActionData.postValue(
                        PromptActionState(
                            promptId,
                            actionType,
                            position,
                            result
                        )
                    )
                }
            }
            PROMPT_ACCEPT -> {
                launchOnFlow(promptId, eventRepository::acceptPrompt) { result ->
                    _promptActionData.postValue(
                        PromptActionState(
                            promptId,
                            actionType,
                            position,
                            result
                        )
                    )
                }
            }
        }
    }
}

data class PromptActionState(
    val promptId: Long,
    val actionId: Int,
    val position: Int,
    val result: ResultState<MessageTitle>,
)
