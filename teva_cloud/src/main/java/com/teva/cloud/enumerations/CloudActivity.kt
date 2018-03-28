/*
 *
 *  CloudActivity.kt
 *  teva_cloud
 *
 *  Copyright © 2018 Teva. All rights reserved.
 *
 */

package com.teva.cloud.enumerations

import com.teva.common.utilities.SystemMonitorActivity
import org.threeten.bp.Duration

/**
 * This class represents the Cloud activities included in system monitoring.
 * This class implements the SystemMonitorActivity and is passed as a parameter to the SystemMonitorMessage.
 */
sealed class CloudActivity : SystemMonitorActivity {

    class CloudSync(succeeded: Boolean , timingValue: Duration?) : CloudActivity() {
        override val timing: Duration? = timingValue
        override val activityName: String = "Cloud Sync ${if(succeeded) "succeeded" else "failed"}"
    }
    class DhpLogin(succeeded: Boolean) : CloudActivity() {
        override val activityName: String = "DHP Login ${if(succeeded) "succeeded" else "failed"}"
    }
    class IdentityHubLogin(succeeded: Boolean) : CloudActivity() {
        override val activityName: String = "Identity Hub Login ${if(succeeded) "succeeded" else "failed"}"
    }
    class ApiRequest(uri: String, timingValue: Duration?, label: String?) : CloudActivity() {
        override val timing: Duration? = timingValue
        override val activityLabel: String? = label
        override val activityName: String = "API Request: $uri"
        override val timingOnly: Boolean = (label == null)
    }
    class MoreThan100InhalesSynced : CloudActivity() {
        override val activityName: String = "More than 100 inhales synced"
    }

    override val source: String
        get() = getActivitySource()

    override val activityName: String = ""

    override val activityLabel: String? = null

    override val timing: Duration? = null

    override val timingOnly: Boolean = false

    private fun getActivitySource() : String {
        return when(this.javaClass) {
            DhpLogin::class.java, IdentityHubLogin::class.java -> "Onboarding"
            else -> "DHP"
        }
    }
}