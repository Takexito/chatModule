package com.dev.podo.core.viewmodel

import androidx.lifecycle.viewModelScope
import com.dev.podo.core.datasource.Storage
import com.dev.podo.core.model.entities.User
import com.dev.podo.core.repository.CoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(private val coreRepository: CoreRepository) : BaseViewModel() {

    val user: User
        get() = Storage.user ?: throw NullPointerException("User is null")

    fun init(callback: () -> Unit) {
        fetchUser(callback)
    }

    private fun fetchUser(callback: () -> Unit) {
        launchOnFlow(coreRepository::fetchUser) { result ->
            result.getSuccessData()?.let {
                callback.invoke()
                initChatMessageService()
            }
        }
    }

    private fun initChatMessageService() {
        coreRepository.initChatMessageService(viewModelScope)
    }
}
