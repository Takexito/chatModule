package com.dev.podo.event.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dev.podo.R
import com.dev.podo.common.model.entities.Choice
import com.dev.podo.common.model.entities.tag.TagGroup
import com.dev.podo.common.respository.MediaRepository
import com.dev.podo.common.utils.exceptions.ResourceException
import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.core.viewmodel.BaseViewModel
import com.dev.podo.event.model.dto.EventType
import com.dev.podo.event.model.entities.Event
import com.dev.podo.event.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class CreateLiveEventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val mediaRepository: MediaRepository
) : BaseViewModel() {

    private val _tagGroupsResponse: MutableLiveData<ResultState<List<TagGroup>>> = MutableLiveData()
    val tagGroupsResponse: LiveData<ResultState<List<TagGroup>>>
        get() = _tagGroupsResponse

    private val _selectedTag: MutableSharedFlow<Choice?> = MutableSharedFlow()
    val selectedTag: SharedFlow<Choice?>
        get() = _selectedTag.asSharedFlow()

    private val _tagSections: MutableLiveData<MutableList<TagGroups>> = MutableLiveData()
    val tagSections: LiveData<MutableList<TagGroups>>
        get() = _tagSections

    val event = Event()
    private val _eventResponse: MutableSharedFlow<ResultState<*>> = MutableSharedFlow()
    val eventResponse: SharedFlow<ResultState<*>>
        get() = _eventResponse.asSharedFlow()

    var videoUri: Uri? = null

    init {
        event.type = EventType.NOW
        fetchTags()
    }

    fun updateTags(updatedTags: MutableList<TagGroups>) {
        _tagSections.postValue(updatedTags)
    }

    fun updateSelectedTag(item: Choice? = null) {
        viewModelScope.launch {
            _selectedTag.emit(item)
        }
    }

    private fun fetchTags() {
        launchOnFlow(eventRepository::fetchTags) { result ->
            _tagGroupsResponse.postValue(result)
        }
    }

    fun initiateEventCreation(contentResolver: ContentResolver) {
        viewModelScope.launch {
            if (isDataInvalid()) {
                return@launch
            }
            withContext(Dispatchers.IO) {
                _eventResponse.emit(ResultState.InProgress)
                uploadVideo(contentResolver)
            }
        }
    }

    private suspend fun isDataInvalid(): Boolean {
        if (videoUri == null) {
            _eventResponse.emit(
                ResultState.Error(
                    exception = ResourceException(
                        stringId = R.string.video_not_chosen
                    )
                )
            )
            return true
        }
        try {
            event.validate()
        } catch (exception: Exception) {
            _eventResponse.emit(
                ResultState.Error(exception)
            )
            return true
        }
        return false
    }

    private suspend fun uploadVideo(contentResolver: ContentResolver) {
        videoUri?.let { uri ->
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            inputStream?.let { stream ->
                mediaRepository.uploadMedia(
                    "podo_${
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        .format(Date())
                    }.mp4",
                    stream
                )
                    .collect { result ->
                        when (result) {
                            is ResultState.Success -> {
                                event.videoId = result.data.mediaId
                                createEvent()
                            }
                            is ResultState.Error -> {
                                _eventResponse.emit(
                                    ResultState.Error(
                                        exception = result.exception
                                    )
                                )
                            }
                        }
                    }
            }
        }
    }

    private suspend fun createEvent() {
        viewModelScope.launch {
            eventRepository.createEvent(
                event.asDto()
            ).collect { result ->
                when (result) {
                    is ResultState.Error -> {
                        _eventResponse.emit(
                            ResultState.Error(
                                exception = result.exception
                            )
                        )
                    }
                    is ResultState.Success -> {
                        _eventResponse.emit(
                            ResultState.Success(
                                data = result.data
                            )
                        )
                    }
                }
            }
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
}
