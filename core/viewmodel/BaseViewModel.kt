package com.dev.podo.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.podo.core.model.entities.ResultState
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

open class BaseViewModel : ViewModel() {

    /**
     *  Launch Coroutine with [CoroutineContext] = [Dispatchers.IO] by [viewModelScope]
     *  @param async [suspend] function what will be launch
     *  @param doOnCollect [suspend] function with [CoroutineScope], what invoked on [Flow.collect]
     *  (by default set [CoroutineContext] = [Dispatchers.Main] , you can change it by invoke [withContext] in body of [doOnCollect])
     */
    fun <T> launchOnFlow(
        async: suspend () -> Flow<ResultState<T>>,
        doOnCollect: suspend CoroutineScope.(ResultState<T>) -> Unit
    ): Job {

        return viewModelScope.launch(Dispatchers.IO) {
            async().collect { result ->
                withContext(Dispatchers.Main) {
                    doOnCollect(result)
                }
            }
        }
    }

    /**
     *  Launch Coroutine with [CoroutineContext] = [Dispatchers.IO] by [viewModelScope]
     *  @param model parameter [async] function
     *  @param async [suspend] function what will be launch
     *  @param doOnCollect [suspend] function with [CoroutineScope], what invoked on [Flow.collect]
     *  (by default set [CoroutineContext] = [Dispatchers.Main] , you can change it by invoke [withContext] in body of [doOnCollect])
     */
    fun <T, K> launchOnFlow(
        model: K,
        async: suspend (K) -> Flow<ResultState<T>>,
        doOnCollect: suspend (ResultState<T>) -> Unit
    ): Job {
        return viewModelScope.launch(Dispatchers.IO) {
            async(model).collect { result ->
                withContext(Dispatchers.Main) {
                    doOnCollect(result)
                }
            }
        }
    }
}
