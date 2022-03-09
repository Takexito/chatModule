package com.dev.podo.core.ui

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.dev.podo.common.utils.Constants
import com.dev.podo.common.utils.services.SentryUtil
import com.dev.podo.core.services.AnalyticsService
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {

    companion object {
        var context: Context? = null
        lateinit var simpleCache: SimpleCache
        lateinit var leastRecentlyUsedCacheEvictor: LeastRecentlyUsedCacheEvictor
        lateinit var exoDatabaseProvider: StandaloneDatabaseProvider
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        initMediaPlayerCache()
        registerActivityLifecycleCallback()
        initServices()
    }

    private fun registerActivityLifecycleCallback() {
        registerActivityLifecycleCallbacks(object: ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityStarted(activity: Activity) {
                SentryUtil.registerNavigation(activity)
            }

            override fun onActivityResumed(activity: Activity) {}

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {}

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {}

        })
    }

    private fun initMediaPlayerCache() {
        leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(
            Constants.mediaPlayerCacheSize()
        )
        exoDatabaseProvider = StandaloneDatabaseProvider(this)
        simpleCache = SimpleCache(cacheDir, leastRecentlyUsedCacheEvictor, exoDatabaseProvider)
    }

    private fun initServices() {
        AnalyticsService.init(this, applicationContext)
    }
}
