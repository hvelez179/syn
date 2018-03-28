//
// ServiceControl.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.common.services

/**
 * Interface containing methods used by the App's Android service to start
 * and stop services.
 */
interface ServiceControl {
    /**
     * Starts a service's background processes.
     */
    fun startService()

    /**
     * Stops a service's background processes.
     */
    fun stopService()
}
