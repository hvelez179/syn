/*
 *
 *  AnalyticsServiceImpl.kt
 *  teva_common
 *
 *  Copyright © 2018 Teva. All rights reserved.
 *
 */

package com.teva.common.services.analytics

import android.content.Context
import com.teva.utilities.services.DependencyProvider
import com.google.android.gms.analytics.Tracker
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.HitBuilders
import org.threeten.bp.Duration


/**
 * This class supports tracking of analytics information for the app.
 */
class AnalyticsServiceImpl(dependencyProvider: DependencyProvider) : AnalyticsService {

    private val googleAnalytics: GoogleAnalytics
    private val tracker: Tracker
    private val trackingID = "UA-108060185-1"
    private val LOCAL_DISPATCH_PERIOD = 20 // dispatch period in minutes

    private class Event(val category: String = "",
                        val action: String = "",
                        val label: String = "",
                        val value: Long = 0)

    init {
        val context = dependencyProvider.resolve<Context>()
        googleAnalytics = GoogleAnalytics.getInstance(context)
        googleAnalytics.setLocalDispatchPeriod(LOCAL_DISPATCH_PERIOD)
        tracker = googleAnalytics.newTracker(trackingID)
        tracker.enableExceptionReporting(true)
    }

    override fun enterScreen(screen: String) {
        tracker.setScreenName(screen)
        val screenViewBuilder = HitBuilders.ScreenViewBuilder().build()
        tracker.send(screenViewBuilder)
    }

    override fun leaveScreen(screen: String) {
        val trackerScreenName = tracker.get("ScreenName")
        if(trackerScreenName != null && trackerScreenName.equals(screen)) {
            tracker.setScreenName(null)
            val screenViewBuilder = HitBuilders.ScreenViewBuilder().build()
            tracker.send(screenViewBuilder)
        }
    }

    override fun event(event: String, source: String, label: String?) {
        tracker.send(HitBuilders.EventBuilder()
                .setAction(event)
                .setCategory(source)
                .setLabel(label)
                .setValue(0)
                .build())
    }

    override fun timing(timing: String, source: String, interval: Duration) {
        tracker.send(HitBuilders.TimingBuilder()
                .setCategory(source)
                .setVariable(timing)
                .setLabel("")
                .setValue(interval.seconds * 1000)
                .build())
    }
}