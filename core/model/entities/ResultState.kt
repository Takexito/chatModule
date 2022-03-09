package com.dev.podo.core.model.entities

sealed class ResultState<out T> {
    data class Success<out T>(val data: T) : ResultState<T>()
    data class Error(val exception: Exception) : ResultState<Nothing>() {
        override fun toString(): String {
            return "ResultState Error with message: ${exception.message} \n${exception.stackTraceToString()}"
        }
    }
    object InProgress : ResultState<Nothing>()

    fun getSuccessData(): T? = (this as? Success)?.data
    fun <K> map(mapper: (T) -> K): ResultState<K> {
        return when (this) {
            is Error -> Error(exception)
            is InProgress -> InProgress
            is Success -> Success(mapper(data))
        }
    }
}
