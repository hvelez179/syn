//
// CloudServiceDelegate.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.services

import org.threeten.bp.Instant


/**
 * This interface is implemented to act as the delegate of the CloudService
 * in order to accept callbacks and updates on the service's status.
 */
interface CloudServiceDelegate {

    /**
     * This property indicates whether the service is currently logged in.
     */
    var isLoggedIn: Boolean

    /**
     * This method is called when CloudService.getServerTime completes to indicate success or failure.
     * @param success - was the disable successful
     * @param serverTime - the server time; nil if unsuccessful
     */
    fun getServerTimeCompleted(success: Boolean, serverTime: Instant?)
}