/*
 *
 *  AnalyticsService.kt
 *  teva_common
 *
 *  Copyright © 2018 Teva. All rights reserved.
 *
 */

package com.teva.common.services.analytics

import org.threeten.bp.Duration

/**
 * This interface needs to be implemented by classes which support tracking analytics information.
 */
interface AnalyticsService {
    /**
     * This method notifies the Analytics Service when the screen is entered.
     *
     * @param screen - This parameter is the screen to track as entered.
     */
    fun enterScreen(screen: String)

    /**
     * This method notifies the Analytics Service when the screen is exited.
     *
     * @param screen - This parameter is the screen to track as exited.
     */
    fun leaveScreen(screen: String)

    /**
     * This method adds an AnalyticsEvent to the Analytics Tracker.
     *
     * @param event - This parameter is the event to track.
     * @param source - This parameter is the source of the event.
     * @param label - This parameter is the label associated with the event being tracked.
     */
    fun event(event: String, source: String, label: String?)

    /**
     * This method adds an AnalyticsTiming to the Analytics Tracker, with a given time interval, and label.
     *
     * @param timing - This parameter is the AnalyticsTiming to track.
     * @param source - This parameter is the source of the AnalyticsTiming to track.
     * @param label - This parameter is the time interval to track.
     */
    fun timing( timing: String, source: String, interval: Duration)

}