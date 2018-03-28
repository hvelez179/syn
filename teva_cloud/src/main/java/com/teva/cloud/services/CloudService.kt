//
// CloudService.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.services


/**
 * This is the service used by the CloudManager to upload and download data in the cloud.
 */
interface CloudService {

    /**
     * This property is the CloudService's delegate, used to provide callback's and report on the service status.
     */
    var delegate: CloudServiceDelegate?

    /**
     * This method initializes the cloud service for use and sets its delegate's isLoggedIn and isSyncingEnabled properties.
     */
    fun initialize()

    /**
     * This method gets the current server time of the cloud provider.
     */
    fun getServerTimeAsync()

    /**
     * This method logs out of the cloud service with the underlying provider and sets its delegate's isLoggedIn property.
     */
    fun logOut()

}