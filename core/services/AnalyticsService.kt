package com.dev.podo.core.services

import android.app.Application
import android.content.Context
import com.dev.podo.BuildConfig
import com.yandex.metrica.YandexMetrica

import com.yandex.metrica.YandexMetricaConfig




object AnalyticsService {

    fun init(application: Application, context: Context) {
        val config = YandexMetricaConfig.newConfigBuilder(BuildConfig.METRICA_API_KEY).build()
        YandexMetrica.activate(context, config)
        YandexMetrica.enableActivityAutoTracking(application)
    }

}