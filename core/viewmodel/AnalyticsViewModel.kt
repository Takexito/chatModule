package com.dev.podo.core.viewmodel

import android.util.Log
import com.dev.podo.common.utils.services.SentryUtil
import com.dev.podo.core.datasource.Storage
import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.core.model.entities.statistics.Statistics
import com.dev.podo.core.repository.analytics.AnalyticsRepository
import com.dev.podo.core.services.EventCreateButtonClickEvent
import com.dev.podo.core.services.MetricaEvent
import com.dev.podo.core.services.PodoPlusSubscribeSuccessEvent
import com.dev.podo.event.ui.create_event.CreateEventFragment
import com.dev.podo.podoplus.model.SubscriptionCode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : BaseViewModel() {

    fun reportEventSubscribeSucceedEvent(subscriptionCode: SubscriptionCode) {
        createEventAnalyticEvent(
            PodoPlusSubscribeSuccessEvent(subscriptionCode)
        )
    }

    fun reportEventCreateButtonClickEvent(place: CreateEventFragment.NavigationPlace) {
        createEventAnalyticEvent(
            EventCreateButtonClickEvent(place)
        )
    }

    private fun createEventAnalyticEvent(event: MetricaEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            analyticsRepository.getStatistics().collect { result ->
                if (result is ResultState.Success) {
                    val statistics = result.getSuccessData()
                    statistics?.let { stats ->
                        sendMetric(event, stats)
                        return@collect
                    }
                    SentryUtil.captureException(Exception("Statistics is null"))
                }
            }
        }
    }

    private fun sendMetric(
        event: MetricaEvent,
        stats: Statistics,
    ) {
        when (event) {
            is EventCreateButtonClickEvent -> {
                event.reportEvent(
                    stats.totalEventsCount,
                    stats.totalPromptsCount,
                    Storage.user?.hasSubscription,
                    stats.totalActiveEventCount
                )
            }
            is PodoPlusSubscribeSuccessEvent -> {
                event.reportEvent(
                    stats.totalEventsCount,
                    stats.totalPromptsCount
                )
            }
        }
    }
}