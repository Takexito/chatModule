package com.dev.podo.core.repository.analytics

import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.core.model.entities.statistics.Statistics
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {

    suspend fun getStatistics(): Flow<ResultState<Statistics>>

}