/*
 *
 *  DeviceActivity.kt
 *  teva_devices
 *
 *  Copyright © 2018 Teva. All rights reserved.
 *
 */

package com.teva.devices.enumerations

import com.teva.common.utilities.SystemMonitorActivity
import org.threeten.bp.Duration

/**
 * This class represents the Device activities included in system monitoring.
 * This class implements the SystemMonitorActivity and is passed as a parameter to the SystemMonitorMessage.
 */
sealed class DeviceActivity : SystemMonitorActivity {
    class Authentication(timingValue: Duration) : DeviceActivity() {
        override val activityName: String = "Authentication"
        override val timing: Duration? = timingValue
        override val timingOnly: Boolean = true
    }
    class AuthenticationTimeout : DeviceActivity() {
        override val activityName: String = "Authentication Timeout"
        override val activityLabel: String? = "Timeout"
    }
    class Pairing(succeeded: Boolean, connectedInhalers: Int) : DeviceActivity() {
        override val activityName: String = "Pairing ${if(succeeded) "Succeeded" else "Failed"}"
        override val activityLabel: String? = "Connected Inhalers: $connectedInhalers"
    }

    override val source: String = "Inhalers"
    override val activityName: String = ""
    override val activityLabel: String? = null
    override val timing: Duration? = null
    override val timingOnly: Boolean = false

}