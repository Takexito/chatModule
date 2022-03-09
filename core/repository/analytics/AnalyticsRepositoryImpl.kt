package com.dev.podo.core.repository.analytics

import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.core.model.entities.statistics.Statistics
import com.dev.podo.core.repository.BaseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AnalyticsRepositoryImpl @Inject constructor(
    private val userApi: UserApi
) : BaseRepository(), AnalyticsRepository {
    override suspend fun getStatistics(): Flow<ResultState<Statistics>> {
        return fetchData { userApi.getStatistics() }
    }
}
