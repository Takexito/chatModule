package com.dev.podo.core.repository

import com.dev.podo.common.model.entities.PagedListWrapper
import com.dev.podo.core.model.dto.*
import com.dev.podo.core.model.entities.MessageTitle
import com.dev.podo.core.model.entities.ResultState
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import retrofit2.Response

open class BaseRepository {

    @Inject
    lateinit var exceptionHandler: ExceptionHandler

    suspend fun <T, K : Dto<T>> safeApiCallWithoutWrapper(
        call: suspend () -> Response<K>
    ): ResultState<T> {
        return try {
            val response = call.invoke()
            if (!response.isSuccessful) {
                throw HttpException(response)
                // ResultState.Error(NullPointerException("Response body is null by request url:$requestUrl"))
            }
            if (response.body() != null) ResultState.Success(response.body()!!.asEntity())
            else {
                val requestUrl = response.raw().request.url
                ResultState.Error(NullPointerException("Response body is null by request url:$requestUrl"))
            }
        } catch (exception: Exception) {
            ResultState.Error(exceptionHandler.handle(exception))
        }
    }

    suspend fun <T, K : Dto<T>> safeApiCallListWithoutWrapper(
        call: suspend () -> Response<List<K>>
    ): ResultState<List<T>> {
        return try {
            val response = call.invoke()
            if (response.body() != null) {
                ResultState.Success(response.body()!!.asEntity())
            } else {
                val requestUrl = response.raw().request.url
                ResultState.Error(NullPointerException("Response body is null by request url:$requestUrl"))
            }
        } catch (exception: Exception) {
            ResultState.Error(exceptionHandler.handle(exception))
        }
    }

    suspend fun <E, D : Dto<E>, W : ResponseWrapper<D>> safeApiCall(
        call: suspend () -> Response<W>
    ): ResultState<E> {
        try {
            val response = call.invoke()
            if (response.isSuccessful) {
                if (response.body() == null) {
                    val requestUrl = response.raw().request.url
                    return ResultState.Error(NullPointerException("Response body is null by request url:$requestUrl"))
                }
                if (response.body()!!.data == null) {
                    throw NullPointerException("data is null")
                }
                return ResultState.Success(response.body()!!.data!!.asEntity())
            }
            throw HttpException(response)
        } catch (exception: Exception) {
            return ResultState.Error(exceptionHandler.handle(exception))
        }
    }

    suspend fun safeApiCallMessage(
        call: suspend () -> Response<ResponseWrapper<MessageTitleDto>>
    ): ResultState<MessageTitle> {
        try {
            val response = call.invoke()
            if (response.isSuccessful) {
                if (response.body() == null) {
                    val requestUrl = response.raw().request.url
                    return ResultState.Error(NullPointerException("Response body is null by request url:$requestUrl"))
                }
                val data = MessageTitleDto(response.body()!!.message, response.body()!!.title)
                return ResultState.Success(data.asEntity())
            }
            throw HttpException(response)
        } catch (exception: Exception) {
            return ResultState.Error(exceptionHandler.handle(exception))
        }
    }

    suspend fun <T, D : Dto<T>, W : ResponseWrapperList<D>> safeApiCallList(
        call: suspend () -> Response<W>
    ): ResultState<List<T>> {
        try {
            val response = call.invoke()
            if (response.isSuccessful) {
                response.body()?.let {
                    val list = arrayListOf<T>()
                    response.body()!!.data.forEach { dto ->
                        dto?.asEntity()?.let { list.add(it) }
                    }
                    return ResultState.Success(list)
                }
                val requestUrl = response.raw().request.url
                return ResultState.Error(NullPointerException("Response body is null by request url:$requestUrl"))
            }
            throw HttpException(response)
        } catch (exception: Exception) {
            return ResultState.Error(exceptionHandler.handle(exception))
        }
    }

    suspend fun <T, D : Dto<T>, W : ResponseWrapperList<D>> safeApiCallWrapedList(
        call: suspend () -> Response<W>
    ): ResultState<PagedListWrapper<T>> {
        try {
            val response = call.invoke()
            if (response.isSuccessful) {
                response.body()?.run {
                    val list = arrayListOf<T>()
                    data.forEach { dto ->
                        dto?.asEntity()?.let { list.add(it) }
                    }
                    val wrappedResponse = PagedListWrapper(
                        data = list,
                        title = title,
                        message = message,
                        links = links,
                        meta = meta,
                        code = code
                    )
                    return ResultState.Success(wrappedResponse)
                }
                val requestUrl = response.raw().request.url
                return ResultState.Error(NullPointerException("Response body is null by request url:$requestUrl"))
            }
            throw HttpException(response)
        } catch (exception: Exception) {
            return ResultState.Error(exceptionHandler.handle(exception))
        }
    }

    /**
     * Complex function to get cached data, fetch data by api call, and cached it response by one call
     *
     * 1. Emit cacheDataState
     *
     * 2. Make api call and emit it
     *
     * 3. Cache response from prev. step
     * @see [getDataState]
     * @see [safeApiCall]
     * @see [cacheDataByResultState]
     */

    suspend fun <E, D : Dto<E>, W : ResponseWrapper<D>> fetchData(
        call: suspend () -> Response<W>
    ): Flow<ResultState<E>> {
        return flow {
            emit(getDataState())
            val response = safeApiCall(call)
            emit(response)
            cacheDataByResultState(response)
        }
    }

    suspend fun <E, D : Dto<E>, W : ResponseWrapper<D>> fetchDataWithAction(
        call: suspend () -> Response<W>,
        action: (data: E?) -> Unit
    ): Flow<ResultState<E>> {
        return flow {
            emit(getDataState())
            val response = safeApiCall(call)
            action.invoke(response.getSuccessData())
            emit(response)
            cacheDataByResultState(response)
        }
    }

    suspend fun <E, D : Dto<E>, W : ResponseWrapperList<D>> fetchDataList(
        call: suspend () -> Response<W>
    ): Flow<ResultState<List<E>>> {
        return flow {
            emit(getDataState())
            val response = safeApiCallList(call)
            emit(response)
            cacheDataByResultState(response)
        }
    }

    suspend fun <E, D : Dto<E>, W : ResponseWrapperList<D>> fetchDataListWithAction(
        call: suspend () -> Response<W>,
        action: (data: List<E>?) -> Unit
    ): Flow<ResultState<List<E>>> {
        return flow {
            emit(getDataState())
            val response = safeApiCallList(call)
            action.invoke(response.getSuccessData())
            emit(response)
            cacheDataByResultState(response)
        }
    }

    suspend fun <E, D : Dto<E>, W : ResponseWrapperList<D>> fetchDataPagedList(
        call: suspend () -> Response<W>
    ): Flow<ResultState<PagedListWrapper<E>>> {
        return flow {
            emit(getDataState())
            val response = safeApiCallWrapedList(call)
            emit(response)
            cacheDataByResultState(response)
        }
    }

    suspend fun fetchMessage(
        call: suspend () -> Response<ResponseWrapper<MessageTitleDto>>
    ): Flow<ResultState<MessageTitle>> {
        return flow {
            emit(getDataState())
            val response = safeApiCallMessage(call)
            emit(response)
            cacheDataByResultState(response)
        }
    }

    /**
     * Complex function to get cached data, fetch data by api call, and cached it response by one call
     *
     * 1. Emit cacheDataState
     *
     * 2. Make api call and emit it
     *
     * 3. Cache response from prev. step
     * @see [getDataState]
     * @see [safeApiCall]
     * @see [cacheDataByResultState]
     */

    suspend fun <T, K : Dto<T>> fetchDataWithoutWrapper(
        call: suspend () -> Response<K>
    ): Flow<ResultState<T>> {
        return flow {
            emit(getDataState())
            val response = safeApiCallWithoutWrapper(call)
            emit(response)
            cacheDataByResultState(response)
        }
    }

    suspend fun <T, K : Dto<T>> fetchDataListWithoutWrapper(
        call: suspend () -> Response<List<K>>
    ): Flow<ResultState<List<T>>> {
        return flow {
            emit(getDataState())
            val response = safeApiCallListWithoutWrapper(call)
            emit(response)
            cacheDataByResultState(response)
        }
    }

    /**
     * If has cached data at database return: [ResultState.Success]
     *
     * else return: [ResultState.InProgress]
     */
    suspend fun <T> getDataState(): ResultState<T> {
        return ResultState.InProgress
    }

    suspend fun <T> cacheData(data: T) {
        // TODO: 24.10.2021 implement caching
    }

    suspend fun <T> cacheDataByResultState(resultState: ResultState<T>) {
        resultState.getSuccessData()?.let { cacheData(it) }
    }
}
