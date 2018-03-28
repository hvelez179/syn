/*
 *
 *  SystemMonitorActivity.kt
 *  teva_common
 *
 *  Copyright © 2018 Teva. All rights reserved.
 *
 */

package com.teva.common.utilities

import org.threeten.bp.Duration

/**
 * Data types and classes implementing this interface can send their system monitor activities through the SystemMonitorMessage.
 */
interface SystemMonitorActivity {
    /**
     * Source or category for the activity.
     */
    val source: String

    /**
     * Display name of the activity.
     */
    val activityName: String

    /**
     * Additional label associated with the activity.
     */
    val activityLabel: String?

    /**
     * Timing metrics for the activity - usually the duration.
     */
    val timing: Duration?

    /**
     * Flag indicating whether the activity should only include timing metrics.
     */
    val timingOnly: Boolean
}