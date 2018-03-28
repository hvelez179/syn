//
// DHPRequestQueueManager.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.services

import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.dhp.DataEntities.DHPDataTypes.GenericDHPRequest
import com.teva.dhp.models.DHPManager
import java.util.*


/**
 * This class manages a DHPRequestQueue to execute one asynchronous DHP request at a time.
 * This class automatically executes the next request upon receiving response from a previous request.
 */

open class DHPRequestQueueManager {

    private val logger = Logger("DHPRequestQueueManager")
    /**
     * This property is used to execute DHP requests.
     */
    private var dhpManager  = DependencyProvider.default.resolve<DHPManager>()

    /**
     * This property is an array of DHPRequest objects.
     */
    private var requestQueue: MutableList<GenericDHPRequest<*>> = ArrayList()

    private var inProgress = false

    // Public Methods
    /**
     * This method executes the passed-in request if there is not an outstanding request.
     * Otherwise, the request is enqueued.
     *
     * @param dhpRequest: This parameter contains the DHP API request data.
     */
    internal fun add(request: GenericDHPRequest<*>) {

        // Execute request if queue is empty
        if (!inProgress) {
            //var requestCopy = request
            // Swap out the callback with local callback
            //requestCopy.callback = this::requestCallback
            dhpManager.executeAsync(request, this::requestCallback)
            inProgress = true
        }

        // Add request to queue
        requestQueue.add(request)
    }

    /**
     * This method is the private DHP Request callback. It removes the pending request from the Request Queue, then invokes the original request callback. It then executes the next request in the queue.
     */
    private fun requestCallback(success: Boolean, message: String, json: String?) {

        if(requestQueue.isEmpty()) {
            logger.log(Logger.Level.ERROR, "Received a callback without a request!!")
            return
        }

        // Remove the just-completed request from the queue.
        val originalDhpRequest = requestQueue.removeAt(0)

        // Invoke the original DHP Request callback.
        originalDhpRequest.callback(success, message, json)

        // Check if there are any more requests pending.
        requestQueue.firstOrNull()?.let { nextRequest ->
            dhpManager.executeAsync(nextRequest, this::requestCallback)
            inProgress = true
        }

        if (requestQueue.isEmpty()) {
            inProgress = false
        }
    }
}
