//
// RestClientImpl.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.utilities.utilities.rest

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Handler
import com.teva.utilities.utilities.Logger
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*
import javax.net.ssl.HttpsURLConnection
//import org.gradle.internal.impldep.org.bouncycastle.crypto.tls.ConnectionEnd.client
import javax.net.ssl.HostnameVerifier
import com.google.gson.Gson
import com.teva.utilities.utilities.Logger.Level.*


/**
 * This class implements the RestClient interface for making REST calls.
 */

class RestClientImpl : RestClient {

    private val logger = Logger("RestClientImpl")

    /**
     * This class provides implementation of the RequestBuilder.
     * This class is used to create a new RestRequest.
     */
    private inner class RequestBuilderImpl(private val url: String) : RequestBuilder {
        private var method = "GET"
        private val queryParams = HashMap<String, String>()
        private val headers = HashMap<String, String>()
        private var contentType: String? = null
        private val bodyDictionary = HashMap<String, Any>()
        private var bodyText = ""

        init {
            this.contentType = "application/json"
        }

        /**
         * Sets the http method name.
         *
         * @param value - The HTTP method name (e.g., "GET").
         * @return - the request builder.
         */
        override fun method(value: String): RequestBuilder {
            this.method = value
            return this
        }

        /**
         * Sets query parameters for the request.
         *
         * @param value - The query string portion of the URL.
         * @param key   - The key associated with the query string value.
         * @return - the request builder.
         */
        override fun queryParam(value: String, key: String): RequestBuilder {
            this.queryParams.put(key, value)
            return this
        }

        /**
         * Adds cookies to the request header.
         *
         * @param cookie - The cookie to add to the REST request header.
         * @return - the request builder.
         */
        override fun cookies(cookie: String): RequestBuilder {
            this.headers.put("cookie", cookie)
            return this
        }

        /**
         * Sets the content type for the request.
         *
         * @param value - The content type string (e.g., "application/x-www-form-urlencoded").
         * @return - the request builder.
         */
        override fun contentType(value: String): RequestBuilder {
            this.contentType = value
            return this
        }

        /**
         * Adds new values in the header.
         *
         * @param value - the value to add to the header.
         * @param key   - The key associated with header value.
         * @return - the request builder.
         */
        override fun header(value: String, key: String): RequestBuilder {
            this.headers.put(key, value)
            return this
        }

        /**
         * Adds the JSON dictionary for the request.
         *
         * @param dict - The JSON dictionary for the request.
         * @return - the request builder.
         */
        override fun jsonBody(dict: Map<String, Any>?): RequestBuilder {
            if (dict != null) {
                this.bodyDictionary.putAll(dict)
            }
            return this
        }

        /**
         * Sets the text body for the request.
         *
         * @param body - The text body for the request.
         * @return - the request builder.
         */
        override fun textBody(body: String?): RequestBuilder {
            if (body != null) {
                this.bodyText = body
            }
            return this
        }

        /**
         * Builds the Rest Request with the specified request attributes.
         *
         * @return - the RestRequest that was built.
         */
        override fun build(): RestRequest {
            val completeURL = StringBuilder(url)
            val queryParamsString = StringBuilder()

            if (queryParams.size > 0) {
                var first = true
                for (key in queryParams.keys) {
                    if (!first) {
                        queryParamsString.append("&")
                    }

                    queryParamsString.append(key)
                    queryParamsString.append("=")
                    queryParamsString.append(queryParams[key])

                    first = false
                }
            }

            if (contentType != "application/x-www-form-urlencoded" && queryParamsString.isNotEmpty()) {
                completeURL.append("?")
                completeURL.append(queryParamsString)
            }
            logger.log(VERBOSE, completeURL.toString())

            val request = RestRequestImpl(completeURL.toString())
            request.method = method

            if (contentType == "application/x-www-form-urlencoded" && queryParamsString.isNotEmpty()) {
                headers.put("Content-Type", contentType!!)
                request.bodyText = queryParamsString.toString()
            } else if (bodyText.isNotEmpty()) {
                request.bodyText = bodyText
            } else if (bodyDictionary.size > 0) {
                headers.put("Content-Type", "application/json")
                var gson = Gson()
                val json = gson.toJson(bodyDictionary)
                request.bodyText = json.toString()
            }

            request.headers = headers
            return request
        }
    }

    private inner class RestRequestImpl internal constructor(internal var httpUrl: String) : RestRequest {
        internal var method: String? = null
        internal var headers: Map<String, String>? = null
        internal var bodyText: String? = null
        private val handler = Handler()

        /**
         * This method executes the rest request using an async task and sends
         * the response to the callback.
         */
        internal fun execute(callback: (request: RestRequest, statusCode: Int, result: String?, exception: Exception?) -> Unit) {
            val httpRequestTask = @SuppressLint("StaticFieldLeak")
            object : AsyncTask<RestRequest, Void, Void>() {
                override fun doInBackground(vararg params: RestRequest): Void? {
                    var urlConnection: HttpURLConnection? = null

                    try {
                        val url = URL(httpUrl)
                        logger.log(DEBUG, "Request----------------")
                        urlConnection = if(httpUrl.startsWith("https", 0, true)) { url.openConnection() as HttpsURLConnection } else { url.openConnection() as  HttpURLConnection }
                        urlConnection.defaultUseCaches = false
                        urlConnection.useCaches = false
                        urlConnection.requestMethod = method
                        urlConnection.connectTimeout = REQUEST_TIMEOUT_INTERVAL
                        urlConnection.readTimeout = REQUEST_TIMEOUT_INTERVAL
                        urlConnection.doInput = true
                        //Todo: Remove after DHP certificate issue is resolved.
                        if (httpUrl.contains("https")) {
                            (urlConnection as HttpsURLConnection).hostnameVerifier = HostnameVerifier { _, _ -> true }
                        }

                        logger.log(DEBUG, "Url: $httpUrl")
                        logger.log(DEBUG, "Http Method: $method")
                        logger.log(DEBUG, "Headers")
                        for (key in headers!!.keys) {
                            logger.log(VERBOSE, "$key: ${headers!![key]}")
                            urlConnection.setRequestProperty(key, headers!![key])
                        }
                        logger.log(VERBOSE, "Body")
                        logger.log(VERBOSE, "$bodyText")
                        logger.log(DEBUG, "------------------------")
                        if (bodyText != null) {
                            urlConnection.doOutput = true
                            val outputStream = urlConnection.outputStream

                            val bufferedWriter = BufferedWriter(OutputStreamWriter(outputStream))
                            bufferedWriter.write(bodyText)
                            bufferedWriter.flush()
                            bufferedWriter.close()
                            outputStream.close()
                        }

                        val httpResult = urlConnection.responseCode
                        logger.log(DEBUG, "Response-----------------")
                        logger.log(DEBUG, "Response code: $httpResult")
                        if (httpResult == HttpURLConnection.HTTP_OK) {

                            val responseString = StringBuilder()
                            val bufferedReader = BufferedReader(InputStreamReader(
                                    urlConnection.inputStream, "utf-8"))

                            var line = bufferedReader.readLine()
                            while (line != null) {
                                responseString.append(line)
                                responseString.append("\n")
                                line = bufferedReader.readLine()
                            }

                            bufferedReader.close()
                            logger.log(DEBUG, "Response:")
                            logger.log(DEBUG, "$responseString")
                            handler.post({
                                callback(params[0], httpResult, responseString.toString(), null)
                            })

                        } else {
                            val errorStream = urlConnection.errorStream
                            val builder = StringBuilder()
                            val iReader = InputStreamReader(errorStream)
                            val bReader = BufferedReader(iReader)
                            var line: String? = bReader.readLine()
                            while (line  != null) {
                                builder.append(line) // + "\r\n"(no need, json has no line breaks!)
                                line = bReader.readLine()
                            }
                            bReader.close()
                            iReader.close()

                            val error = builder.toString()
                            logger.log(DEBUG, "Error: $error")
                            logger.log(DEBUG, "Response Message: ${urlConnection.responseMessage}")
                            handler.post({
                                callback(params[0], httpResult, null, null)
                            })
                        }
                        logger.log(DEBUG, "-----------------------------")
                    } catch (exception: Exception) {
                        logger.log(ERROR, "Error executing request $httpUrl", exception)
                        handler.post({
                            callback(params[0], HttpStatusCode.BAD_REQUEST.value, null, exception)
                        })
                    } finally {
                        if (urlConnection != null) {
                            urlConnection.disconnect()
                        }
                    }

                    return null
                }
            }

            httpRequestTask.execute(this)
        }
    }

    /**
     * Returns the request builder to use for creating the request.
     *
     * @param url - The URL to use for the request.
     * @return - the request builder to use for creating the request.
     */
    override fun request(url: String): RequestBuilder {
        return RequestBuilderImpl(url)
    }

    /**
     * Executes the rest request.
     *
     * @param request  - The REST request to execute.
     * @param callback - Provides the callback method to invoke when the request completes.
     */
    override fun execute(request: RestRequest, callback: (request: RestRequest, statusCode: Int, result: String?, exception: Exception?)->Unit) {
        val restRequestImpl = request as RestRequestImpl
        restRequestImpl.execute(callback)
    }

    companion object {
        private val REQUEST_TIMEOUT_INTERVAL = 60000 // 60 seconds
    }
}