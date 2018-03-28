//
// RestClient.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.utilities.utilities.rest

/**
 * REST client object that handles the network communication for a REST call.
 */
interface RestClient {
    /**
     * Creates a builder object used to create a RestRequest.
     *
     * @param url - The URL to use for the request.
     * @return - Returns the RequestBuilder for the URL.
     */
    fun request(url: String): RequestBuilder

   /**
     * Executes a REST request.
     *
     * @param request  - The REST request to execute.
     * @param callback - Provides the callback method to invoke when the request completes.
     */
    fun execute(request: RestRequest, callback: (request: RestRequest, statusCode: Int, result: String?, exception: Exception?)->Unit)
}
