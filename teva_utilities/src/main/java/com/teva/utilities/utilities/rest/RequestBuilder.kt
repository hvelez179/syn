//
// RequestBuilder.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.utilities.utilities.rest

/**
 * This interface supports the creation of a RestRequest.
 */

interface RequestBuilder {
    /**
     * Sets the HTTP method (GET, POST, PUT, DELETE) for the request.
     *
     * @param value - The HTTP method name (e.g., "GET").
     * @return - Returns the RequestBuilder with the method name set.
     */
    fun method(value: String): RequestBuilder

    /**
     * Adds a URL query param for the request (query string, and key).
     *
     * @param value - The query string portion of the URL.
     * @param key   - The key associated with the query string value.
     * @return - Returns the RequestBuilder with the query key/value pair set.
     */
    fun queryParam(value: String, key: String): RequestBuilder

    /**
     * Sets the cookies for the REST request.
     *
     * @param cookie - The cookie to add to the REST request header.
     * @return - Returns the RequestBuilder with the cookie set.
     */
    fun cookies(cookie: String): RequestBuilder

    /**
     * Sets the content type for the REST request.
     *
     * @param value - The content type string (e.g., "application/x-www-form-urlencoded").
     * @return - Returns the RequestBuilder with the contentType set.
     */
    fun contentType(value: String): RequestBuilder

    /**
     * Adds a header to the REST request.
     *
     * @param value - he value to add to the header.
     * @param key   - The key associated with header value.
     * @return - Returns the RequestBuilder with the header key/value pair set.
     */
    fun header(value: String, key: String): RequestBuilder

    /**
     * Sets a dictionary used to create the JSON for the request body.
     *
     * @param dict - The JSON dictionary for the request.
     * @return - Returns the RequestBuilder with the JSON set.
     */
    fun jsonBody(dict: Map<String, Any>?): RequestBuilder

    /**
     * Sets the body from the specified string.
     *
     * @param body - The text body for the request.
     * @return - Returns the RequestBuilder with the text body set.
     */
    fun textBody(body: String?): RequestBuilder

    /**
     * Uses the supplied information to create a new RestRequest.
     *
     * @return - Returns the RestRequest from this RequestBuilder object.
     */
    fun build(): RestRequest
}
