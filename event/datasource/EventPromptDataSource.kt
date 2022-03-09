package com.dev.podo.event.datasource

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dev.podo.common.utils.DEFAULT_FIRST_PAGE_INDEX
import com.dev.podo.core.model.dto.toNotNullable
import com.dev.podo.core.repository.ExceptionHandler
import com.dev.podo.event.model.entities.prompt.Prompt
import retrofit2.HttpException

class EventPromptDataSource(
    val eventApi: EventApi,
    val eventId: Long,
    val exceptionHandler: ExceptionHandler,
) : PagingSource<Int, Prompt>() {

    override fun getRefreshKey(state: PagingState<Int, Prompt>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPosition) ?: return null
        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Prompt> {
        val page = params.key ?: DEFAULT_FIRST_PAGE_INDEX
        try {
            Log.e("LOGGING", "BEFORE API CALL")
            val response = eventApi.fetchPagedEventPrompts(
                eventId = eventId,
                page = page
            )

            if (!response.isSuccessful) throw HttpException(response)
            val data = response.body()?.data?.map {
                it?.asEntity()
            }?.toList()

            var prevPage: Int? = null
            var nextPage: Int? = null
            response.body()?.meta?.let {
                val lastPage = it.lastPage
                prevPage = (if (page > 1) page - 1 else null)
                nextPage = (if (page < lastPage && data?.isNotEmpty() == true) page + 1 else null)
            }
            Log.e("LOGGING", "AFTER API CALL, DATA: $response")
            return LoadResult.Page(
                data?.toNotNullable() ?: emptyList(),
                prevPage,
                nextPage
            )
        } catch (e: Exception) {
            Log.e("LOGGING", "ERROR ON PAGING DATA SOURCE: ${e.message}")
            return LoadResult.Error(exceptionHandler.handle(e))
        }
    }
}
