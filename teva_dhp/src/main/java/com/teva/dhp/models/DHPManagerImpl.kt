//
//  DHPManagerImpl.kt
//  Teva_DHP
//
//  Copyright © 2017 Teva. All rights reserved.
//

package com.teva.dhp.models

import android.net.Uri
import android.util.Base64
import android.util.Base64.DEFAULT
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.*
import com.teva.utilities.utilities.rest.RestClient
import com.teva.utilities.utilities.rest.RestRequest
import com.teva.dhp.DataEntities.DHPDataTypes.DHPTypes
import com.teva.dhp.DataEntities.DHPDataTypes.GenericDHPRequest
import com.teva.dhp.enumerations.DHPRequestStatus
import com.teva.dhp.enumerations.LoginResult
import com.teva.dhp.enumerations.LoginState
import com.teva.dhp.enumerations.LoginStep
import com.teva.dhp.extensions.convertToMap
import com.teva.dhp.extensions.dataFromBase64URLEncoding
import org.json.JSONObject
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import java.math.BigInteger
import java.net.URL
import java.security.SecureRandom
import java.security.Signature
import java.util.*
import kotlin.collections.HashMap


/**
 The concrete implementation of DHPManager.
 */
internal class DHPManagerImpl(override var loginInfo: DHPLoginInfo) : DHPManager {

    // MARK: - Internal properties

    override var loginState: LoginState = LoginState.uninitialized
        set(value)
        {
            // If the login state changes in a way that affects the consumer, notify them.
            if (field != value) {

                if (value == LoginState.loggedIn) {
                    delegate?.didLoginToDHP()
                } else if (value == LoginState.notLoggedIn) {
                    delegate?.failLogin()
                }
            }
            field = value
        }

    internal var loginStep: LoginStep = LoginStep.uninitialized

    override var delegate: DHPDelegate? = null

    override var maxUploadObjects: Int = DHPConstants.maxUploadObjects

    override var apiRequestMetricsCallback: ((String, Boolean, String, Duration) -> Unit)? = null

    /**
     This property is the Logger used to log to the console.
     */
    private val logger = Logger("DHPManagerImpl")

    /**
     This property is a function to call when the login process succeeds or fails.
     */
    private var loginCallback: (LoginResult) -> Unit = {  }

    // Token validation properties

    /**
     * This property indicates whether we have already attempted and failed to retrieve the public key modulus and exponent.
     */
    private var attemptedToGetRetrieveSalesForceKeyData = false


    /**
     * This property is the public key ID found in the Identity Hub ID Token header.
     */
    private var salesForcePublicKeyID = ""

    /**
     * This property is the public key modulus used to sign the Identity Hub ID Token.
     */
    private var salesForcePublicKeyModulusString = ""

    /**
     * This property is the public key exponent used to sign the Identity Hub ID Token.
     */
    private var salesForcePublicKeyExpononentString = ""

    /**
     This typealias is used to persist a DHP request from the consumer so it can be replayed later.
     */
    private data class DHPRequest(var uri: String, var payload: Map<String, Any>, var callback: (Boolean, String, Any?) -> Unit)

    /**
     This property contains DHPRequests to be executed after a DHP token refresh.
     If a DHP request receives an unauthorized error, it will be added to this array and the refresh token process will begin.
     If the refresh succeeds, each request and corresponding callback will be retried.
     If the refresh fails, each request's callback will be called with a failure message.
     */
    //private var pendingRequestsWithOldCallback = ArrayList<DHPRequest>()
    private var pendingRequests = HashMap<GenericDHPRequest<*>, ((Boolean, String, String?) -> Unit)?>()

    /**
     This property is a dictionary containing possible expected response codes and messages from the DHP.
     */
    private data class ResponseStatusMessage(var status: DHPRequestStatus, var message: String)
    private val responseCodeMappings: Map<String, ResponseStatusMessage> = mapOf(
            "110" to ResponseStatusMessage(DHPRequestStatus.success, DHPConstants.dhpMessageSuccess),
            "113" to ResponseStatusMessage(DHPRequestStatus.payloadFailedValidation, DHPConstants.dhpMessagePayloadFailedValidation),
            "117" to ResponseStatusMessage(DHPRequestStatus.consentAlreadyOptedIn, DHPConstants.dhpMessageConsentAlreadyOptedIn),
            "129" to ResponseStatusMessage(DHPRequestStatus.patientAlreadyRegistered, DHPConstants.dhpMessagePatientAlreadyRegistered),
            "132" to ResponseStatusMessage(DHPRequestStatus.weatherPartiallyProcessed, DHPConstants.dhpMessageWeatherPartiallyProcessed))

    // MARK: - Internal methods

    override fun initialize() {

        if (delegate == null) {

            logger.log(Logger.Level.ERROR, "The DHP Manager requires its delegate to be set to be initialized properly.")
            return failPendingRequests()
        }

        if (DHPSession.shared.dhpAccessToken.isEmpty()) {
            checkIdentityHubProfile()
        } else {

            loginState = LoginState.loggedIn
            executePendingRequests()
        }
    }

    override fun executeAsync(dhpRequest: GenericDHPRequest<*>, callback : ((Boolean, String, String?) -> Unit)?) {
        logger.log(Logger.Level.VERBOSE, dhpRequest.uri.uri)
        if (loginState != LoginState.loggedIn) {
            pendingRequests.put(dhpRequest, callback)
            when (loginState) {
                LoginState.uninitialized -> initialize()
                LoginState.refreshing, LoginState.inProgress -> return
                else -> failPendingRequests()
            }
            return
        }
        val uri = dhpRequest.uri.uri
        val payload = DHPTypes.getJSON(dhpRequest.payload)
        logger.log(INFO, payload.toString())
        var json = payload
        if (dhpRequest.prependObjectName) {
            json = mapOf(dhpRequest.payload.dhpObjectName to DHPTypes.unwrap(any = payload))
        }
        logger.log(INFO, json.toString())

        val processStartTime = Instant.now()

        fun internalCallback(request: RestRequest, statusCode: Int?, result: String?, error: Exception?) {

            val requestDuration = Duration.between(processStartTime, Instant.now())
            val parsedResult = parseDHPResponse(statusCode, result, error)
            logger.log(Logger.Level.VERBOSE, "DHP Request Completed - URI: $uri status: ${parsedResult.status} message: ${parsedResult.message}")
            val success: Boolean
            val message: String
            when (parsedResult.status) {
                DHPRequestStatus.success -> {
                    success = true
                    message = "Success"
                }
                DHPRequestStatus.unauthorized -> {
                    logger.log(INFO, "Access Token expired. Refresh DHP Token & Retry")
                    pendingRequests.put(dhpRequest, callback)
                    checkIdentityHubProfile()
                    return
                }
                DHPRequestStatus.weatherPartiallyProcessed -> {
                    success = true
                    message = "Some weather details could not be retrieved"
                }
                else -> {
                    logger.log(ERROR, "Error: uri: ${uri}\nstatus: ${parsedResult.status}\nmessage: ${parsedResult.message}")
                    success = false
                    message = parsedResult.message
                }
            }

            // if the callback is null, the request was not sent through the request
            // manager. Invoke the callback method on the request directly.
            apiRequestMetricsCallback?.invoke(uri, success, message, requestDuration)
            if(callback != null) {
                callback(success, message, result)
            } else {
                dhpRequest.callback(success, message, result)
            }
        }
        executeDHPRestRequestAsync(uri, json, ::internalCallback)
    }

    override fun getRegistrationURL(): URL {

        logger.log(Logger.Level.VERBOSE, "")

        val registrationUriParsed = Uri.parse(loginInfo.identityHubRegistrationURL)
        val requestUrlComponents = registrationUriParsed.buildUpon()

        if (DHPSession.shared.clinicalPseudoName.isNotEmpty()) {

            requestUrlComponents.appendQueryParameter("firstname", DHPSession.shared.clinicalPseudoName)
        }

        return URL(requestUrlComponents.build().toString())
    }

    override fun getLoginURL(): URL {

        logger.log(Logger.Level.VERBOSE, "beginLogin")

        loginState = LoginState.inProgress
        loginStep = LoginStep.login

        val loginUriParsed = Uri.parse(loginInfo.identityHubLoginURL)
        val requestUrlComponents = loginUriParsed.buildUpon()

        // Provide username to the login screen, if previously logged in
        if (!DHPSession.shared.username.isEmpty()) {
            requestUrlComponents.appendQueryParameter("prompt", DHPConstants.prompt)
            requestUrlComponents.appendQueryParameter("login_hint", DHPSession.shared.username)
        }
        val url = URL(requestUrlComponents.build().toString())
        logger.log(VERBOSE, "Login URL: $url")
        return url
    }

    override fun isLoginSuccessRedirectURL(url: URL) : Boolean {
        logger.log(Logger.Level.VERBOSE, "${""}: ${url}")
        return url.toString() == loginInfo.identityHubLoginSuccessRedirectURL
    }

    override fun getAuthorizationURL(): URL {
        //logger
        loginStep = LoginStep.oauthAuthorization

        // The openid workflow requires a nonce value
        // TODO: The spec describes generating a cryptographically secure random number, then using a hash of that for the nonce
        // https://openid.net/specs/openid-connect-implicit-1_0.html
        val nonceSize = 32
        val nonceBytes = ByteArray(nonceSize / 8)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(nonceBytes)
        DHPSession.shared.identityHubLoginNonce = nonceBytes.joinToString("", transform = { "%02x".format(it) })

        val loginUriParsed = Uri.parse(loginInfo.identityHubAuthorizationURL)
        val requestUrlComponents = loginUriParsed.buildUpon()

        // Salesforce should give us an access_token and an id_token
        requestUrlComponents.appendQueryParameter("response_type", DHPConstants.responseType)

        // This is the id of the Identity Hub App's registration with Salesforce
        requestUrlComponents.appendQueryParameter("client_id", loginInfo.clientId)

        // This is the url that Salesforce will redirect to after the client allows Identity Hub to access it's information
        // It *must* match the redirect uri registered with Salesforce
        requestUrlComponents.appendQueryParameter("redirect_uri", loginInfo.identityHubAuthorizationSuccessRedirectURL)

        // OAuth was traditionally used only for authorization,
        // the "openid" CloudConstants.scope (OpenID Connect) is a layer on top of OAuth that supports authentication
        requestUrlComponents.appendQueryParameter("scope", DHPConstants.scope)

        // A nonce is used to prevent a man-in-the-middle from replaying our authorization request,
        // https://stackoverflow.com/questions/24831597/random-unique-string-generation-to-use-as-nonce-oauth
        requestUrlComponents.appendQueryParameter("nonce", DHPSession.shared.identityHubLoginNonce)

        // "page", "popup", "touch" (recommended for Android and iPhone), or mobile
        requestUrlComponents.appendQueryParameter("display", DHPConstants.displayType)

        val url = URL(requestUrlComponents.build().toString())
        logger.log(VERBOSE, "Authorization URL: $url")
        return url
    }

    override fun isAuthorizationSuccessRedirectURL(url: URL): Boolean {

        logger.log(Logger.Level.VERBOSE, "isAuthorizationRedirectURL:$url")

        val uri = Uri.parse(url.toURI().toString())
        val uriBuilder = Uri.Builder()
        uriBuilder.scheme(uri.scheme)
        uriBuilder.authority(uri.authority)
        uriBuilder.path(uri.path)

        val baseUrlString = uriBuilder.build().toString()
        if (baseUrlString.startsWith(loginInfo.identityHubAuthorizationSuccessRedirectURL)) {
            return true
        }

        return false
    }

    override fun completeLoginAsync(authorizationSuccessRedirectURL: URL, callback:  (LoginResult) -> Unit) {
        logger.log(Logger.Level.VERBOSE, "completeLogin: ${authorizationSuccessRedirectURL}")

        loginState = LoginState.inProgress
        loginCallback = callback
        parseAuthorizationSuccessURL(authorizationSuccessRedirectURL)
    }

    override fun logOut() {
        invalidateTokens(true)
    }

    // MARK: - Private methods for API calls

    /**
     This method executes REST requests against the DHP.
     */
    private fun executeDHPRestRequestAsync(uri: String, payload: Map<String, Any>, callback: (request: RestRequest, statusCode: Int, result: String?, exception: Exception?)->Unit) {

        val restClient: RestClient = DependencyProvider.default.resolve()

        val request = restClient.request("${loginInfo.DHPAPIURL}$uri")
            .method("POST")
            .header("Bearer ${DHPSession.shared.dhpAccessToken}", "Authorization")
            .header("123456", "ChecksumKey")
            .jsonBody(payload)
            .build()

        restClient.execute(request, callback)
    }

    /**
     This method parses REST response error codes.
     */
    private fun parseDHPResponse(statusCode: Int?, result: String?, exception: Exception?): ResponseStatusMessage {

        var status: DHPRequestStatus = DHPRequestStatus.failure
        val message: String

        if (exception != null) {

            // networking error
            status = DHPRequestStatus.failure
            message = exception.localizedMessage
        } else if (statusCode != null) {

            if (statusCode == 200) {
                val json = if( result != null) JSONObject(result) else null
                // check for DHP response codes
                val responseCode = json?.getString("responseCode")
                if (responseCode != null) {

                    if (responseCode == "Success") {

                        status = DHPRequestStatus.success
                        message = "Success"
                    } else {

                        val responseMessageCode = json.getString("responseMessageCode")
                        if (responseMessageCode != null) {

                            val mapping = responseCodeMappings[responseMessageCode]
                            if (mapping != null) {

                                status = mapping.status
                                message = mapping.message
                            } else {

                                message = "Error executing request, DHP responseMessageCode = $responseMessageCode"
                            }
                        } else {

                            message = "Error executing request, no DHP responseMessageCode"
                        }
                    }
                } else {

                    message = "Error executing request, no DHP responseCode"
                }
            } else if (statusCode == 401) {

                status = DHPRequestStatus.unauthorized
                message = "Error executing request, HTTP statusCode = $statusCode"
            } else {

                // HTTP server error
                message = "Error executing request, HTTP statusCode = $statusCode"
            }
        } else {

            message = "Error executing request, no HTTP status code"
        }

        return ResponseStatusMessage(status, message)
    }

    /**
     This method replays all requests to replay.
     */
    private fun executePendingRequests() {

        logger.log(Logger.Level.VERBOSE, "")

        for (pendingRequest in pendingRequests.keys) {

            executeAsync(pendingRequest, pendingRequests[pendingRequest]!!)
        }

        pendingRequests.clear()
    }

    /**
     This method returns failure for all pending requests to replay.
     */
    private fun failPendingRequests() {

        logger.log(Logger.Level.VERBOSE, "")

        for (request in pendingRequests.keys) {

            pendingRequests[request]?.invoke(false, "Error executing request", null)
        }

        pendingRequests.clear()
    }

    // MARK: - Private methods for Login process

    /**
     This method handles login failures from the various operations used to log in the user.
     */
    private fun failLogin(loginResult: LoginResult = LoginResult.FAILURE) {

        logger.log(Logger.Level.ERROR, "")

        if (loginState == LoginState.inProgress || loginState == LoginState.refreshing) {

            logger.log(Logger.Level.ERROR, "Login failed at step: ${loginStep}")
        }

        val loginWasInProgress = (loginState == LoginState.inProgress)

        loginState = LoginState.notLoggedIn

        if (loginWasInProgress) {

            loginCallback(loginResult)
        }

        failPendingRequests()
    }

    /**
    This function can be used to invalidate the Identity Hub and DHP tokens, effectively logging the user out.
    - Parameter forceLogout: indicates whether the consumer should be notified of the logout immediately
     */
    private fun invalidateTokens(forceLogout: Boolean) {
        logger.log(Logger.Level.VERBOSE, "")
        DHPSession.shared.identityHubIDToken = ""
        DHPSession.shared.identityHubAccessToken = ""
        DHPSession.shared.identityHubRefreshToken = ""
        DHPSession.shared.identityHubProfileURL = ""
        DHPSession.shared.dhpAccessToken = ""
        DHPSession.shared.dhpRefreshToken = ""

        // If the app uses an iOS web view to log in, this ensures the user's login is removed from the cache.
//        URLSession.shared.reset(completionHandler = { () })

        if (forceLogout) {
            loginState = LoginState.notLoggedIn
        }
    }


    /**
     This method reads the Identity Hub success URL and extracts the relevant information from the query string.
     If anything is missing, login fails, otherwise, it continues to next step.
     - Parameter url: the URL to parse
     */
    private fun parseAuthorizationSuccessURL(url: URL) {

        logger.log(Logger.Level.VERBOSE, "parseAuthorizationSuccessURL -  $url")
        loginStep = LoginStep.parseAuthorizationSuccessURL

        var result = true

        var uri = Uri.parse(url.toURI().toString())
        logger.log(VERBOSE, "Authorization Success Url: $url")

        // Get the query items,
        // For the Implicit OAuth worflow, these are actually in the url hash fragment
        var queryItems = uri.queryParameterNames
        if (queryItems == null || queryItems.size == 0) {

            val fragment = uri.fragment ?: return failLogin()

            val uriBuilder = Uri.Builder()
            uriBuilder.scheme(uri.scheme)
            uriBuilder.authority(uri.authority)
            uriBuilder.path(uri.path)

            val baseUrlString = uriBuilder.build().toString()
            val urlWithQueryItems = baseUrlString + "?" + fragment
            uri = Uri.parse(urlWithQueryItems)
            queryItems = uri.queryParameterNames
        }

        val errorIndex = queryItems.indexOf("error")
        if (errorIndex != -1) {

            logger.log(Logger.Level.INFO, queryItems.elementAt(errorIndex).toString())
            result = false
        }

        // The id token is sent to DHP
        val idTokenIndex = queryItems.contains("id_token" )

        if (result && idTokenIndex) {

            DHPSession.shared.identityHubIDToken = uri.getQueryParameter("id_token")
            logger.log(Logger.Level.ERROR, "ID Hud Token: ${DHPSession.shared.identityHubIDToken}")
        } else {

            result = false
        }

        // The access token is needed to get the federation id, and is also sent to DHP
        val accessTokenIndex = queryItems.contains("access_token")
        if (result && accessTokenIndex) {

            DHPSession.shared.identityHubAccessToken = uri.getQueryParameter("access_token")
        } else {

            result = false
        }

        val refreshTokenIndex = queryItems.contains("refresh_token")
        if (result && refreshTokenIndex) {

            DHPSession.shared.identityHubRefreshToken = uri.getQueryParameter("refresh_token")
        } else {

            result = false
        }

        // The id is a url we can use to access the user's profile
        val idIndex = queryItems.contains("id" )
        if (result && idIndex) {

            DHPSession.shared.identityHubProfileURL = uri.getQueryParameter("id")
        } else {

            result = false
        }

        if (result) {
            delegate?.didGetIdentityHubTokens(DHPSession.shared.identityHubAccessToken,
                    DHPSession.shared.identityHubProfileURL,
                    DHPSession.shared.identityHubIDToken,
                    DHPSession.shared.identityHubRefreshToken)
            // Validate the ID Token's signature and payload, then log into DHP if successful.
            // Requires an API call to get public key data, so cannot be part of this method's return.
            validateIDToken()
        } else {

            failLogin()
        }
    }

    private fun getSalesForcePublicKeyData() {

        if (attemptedToGetRetrieveSalesForceKeyData) {
            logger.log(ERROR, "Attempt to get key data failed.")
            return failLogin()
        }

        attemptedToGetRetrieveSalesForceKeyData = true

        val restClient = DependencyProvider.default.resolve<RestClient>()

        val profileRequest = restClient.request(DHPConstants.salesForceKeysUrl)
                .method("GET")
                .contentType("application/x-www-form-urlencoded")
                .build()

        restClient.execute(profileRequest, this::getSalesForcePublicKeyDataCompleted)
    }

    private fun getSalesForcePublicKeyDataCompleted(request: RestRequest, statusCode: Int, result: String?, exception: Exception?) {

            logger.log(VERBOSE, "getSalesForcePublicKeyDataCompleted")

            if (exception == null && statusCode == 200 && result != null) {

            val container = JSONObject(result)

            val keys = container.getJSONArray("keys")
            if (keys != null) {

                for (i in 0 until keys.length()) {
                    val key = keys[i] as? JSONObject
                    if (key != null) {

                        if (key.getString("kid") == salesForcePublicKeyID) {
                            salesForcePublicKeyModulusString = key.getString("n") ?: ""
                            salesForcePublicKeyExpononentString = key.getString("e") ?: ""
                            break
                        }
                    }
                }
            }
            }

            if (salesForcePublicKeyModulusString.isEmpty() || salesForcePublicKeyExpononentString.isEmpty()) {

                logger.log(VERBOSE, "Unable to retrieve public key data.")
                getSalesForcePublicKeyData()
                return
            } else {

                validateIDToken()
            }
    }


    /**
     This method validates the JWT signature based on OpenID Connect specifications:
     http://releasenotes.docs.salesforce.com/en-us/spring14/release-notes/rn_forcecom_security_public_key.htm
     If successful, logs into DHP.
     */
    internal fun validateIDToken() {

        logger.log(VERBOSE, "validateIDToken()")
        loginStep = LoginStep.validateIDToken
        val components = DHPSession.shared.identityHubIDToken.split(".")
        if (components.count() != 3) {

            logger.log(ERROR, "Invalid JWT token.")
            return failLogin()
        }

        val headerComponent = components[0]
        val payloadComponent = components[1]
        val signatureComponent = components[2]

        if (salesForcePublicKeyModulusString.isEmpty() || salesForcePublicKeyExpononentString.isEmpty() || signatureComponent.isEmpty()) {
            val decodedBytes = Base64.decode(headerComponent, DEFAULT)
            val decodedString = String(decodedBytes)
            val header = JSONObject(decodedString)
            salesForcePublicKeyID = header.getString("kid")

            if (salesForcePublicKeyID.isEmpty()) {

                logger.log(ERROR, "Could not determine public key ID to verify signature.")
                return failLogin()
            }

            getSalesForcePublicKeyData()
            return
        }

        var verified = false

        val signedData = "$headerComponent.$payloadComponent".toByteArray()
        val signatureData = signatureComponent.dataFromBase64URLEncoding()
        var modulusString = byteArrayToHexString(salesForcePublicKeyModulusString.dataFromBase64URLEncoding())
        var exponentString = byteArrayToHexString(salesForcePublicKeyExpononentString.dataFromBase64URLEncoding())
        val key = SecKeyHelpers.createKey(modulusString, exponentString)
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initVerify(key)
        signature.update(signedData)

        verified = signature.verify(signatureData)

        if (verified && validateIDTokenPayload(payloadComponent)) {
            logIntoDHP()
        } else {
            return failLogin()
        }
    }

    /**
     * This function converts a ByteArray to a hexadecimal string
     */
    private fun byteArrayToHexString(data: ByteArray): String {
        return String.format("%0" + data.size * 2 + 'x', BigInteger(1, data))
    }

    /**
     This method validates the JWT payload based on OpenID Connect specifications:
     https://openid.net/specs/openid-connect-basic-1_0-28.html#id.token.validation
     - Parameter payloadComponent: the payload component of the JWT
     */
    private fun validateIDTokenPayload(payloadComponent: String): Boolean {

        logger.log(VERBOSE, "validateIDTokenPayload")
        loginStep = LoginStep.validateIDTokenPayload

        val decodedData = Base64.decode(payloadComponent,Base64.DEFAULT) ?: return false

        val jsonString = String(decodedData)
        val payload = JSONObject(jsonString)

        val issuer = payload.getString("iss") ?: ""
        if (!(loginInfo.identityHubLoginURL).contains(issuer)) {
            logger.log(ERROR, "Invalid issuer")
            return false
        }

        val audience = payload.getString("aud") ?: ""
        if (loginInfo.clientId != audience) {
            logger.log(ERROR, "Invalid audience")
            return false
        }

        val expiration = Instant.ofEpochMilli((payload.getDouble("exp") * 1000).toLong())
        logger.log(VERBOSE, "Expiration: $expiration")
        if (expiration.isBefore(delegate?.getCurrentDate() ?: Instant.now())) {
            logger.log(ERROR, "Token is expired")
            return false
        }

        // The iat Claim can be used to reject tokens that were issued too far away from the current time, limiting the amount of time that nonces need to be stored to prevent attacks. The acceptable range is Client specific.
        //_ = Date((payload.getDouble("iat") * 1000).toLong())

        // If the acr Claim was requested, the Client SHOULD check that the asserted Claim Value is appropriate. The meaning and processing of acr Claim Values is out of scope for this specification.
//        _ = payload!["acr"]

        // When a max_age request is made, the Client SHOULD check the auth_time Claim value and request reFreturnObjects
        // -authentication if it determines too much time has elapsed since the last End-User authentication.
//        _ = payload!["auth_time"]

        val nonceInToken = payload.getString("nonce")//payload!["nonce"] as? String ?: ""
        if (nonceInToken != DHPSession.shared.identityHubLoginNonce) {
            logger.log(ERROR, "Nonce does not match")
            return false
        }

        logger.log(INFO, "ID token payload validation successful")
        return true
    }

    /**
     This method initiates the DHP login. It has been made internal for testing purposes.
     **/
    fun logIntoDHP() {

        logger.log(Logger.Level.VERBOSE, "")
        loginStep = LoginStep.logIntoDHP

        val restClient: RestClient = DependencyProvider.default.resolve<RestClient>()

        val loginRequest = restClient.request(loginInfo.dhpLoginURL)
            .method("POST")
            .contentType("application/x-www-form-urlencoded")
            .queryParam(DHPSession.shared.identityHubIDToken, "assertion")
            .queryParam(loginInfo.clientId, "client_id")
            .queryParam("/m", "scope")
            .queryParam("urn:ietf:params:oauth:grant-type:jwt-bearer", "grant_type")
            .build()

        restClient.execute(loginRequest, this::dhpLoginCompleted)
    }

    /**
     This method is the response handler for the DHP login.
     - Parameters:
        - request: the rest request that was executed
        - statusCode: the HTTP status code
        - json: the JSON data from the response body
        - error: the error returned from the server, if any
     */
    fun dhpLoginCompleted(request: RestRequest, statusCode: Int?, result: String?, exception: Exception?) {

        logger.log(Logger.Level.VERBOSE, "dhpLoginCompleted")

        if (exception == null && statusCode != null && statusCode == 200 && result != null) {
            val json = JSONObject(result)
            DHPSession.shared.dhpAccessToken = json.getString("access_token")
            DHPSession.shared.dhpRefreshToken = json.getString("refresh_token")

            val succeeded = DHPSession.shared.dhpAccessToken.isNotEmpty() && DHPSession.shared.dhpRefreshToken.isNotEmpty()

            if (succeeded) {
                delegate?.didGetDHPTokens(DHPSession.shared.dhpAccessToken, DHPSession.shared.dhpRefreshToken)
                // Next step - must get profile info even if logging in a 2nd time to confirm the same username was used
                getIdentityHubProfileInfo()
            } else {
                 logger.log(Logger.Level.ERROR, "Error while logging into DHP - missing access token and/or refresh token")
                return failLogin()
            }
        } else {
             logger.log(Logger.Level.ERROR, "Error while logging into DHP")
            return failLogin()
        }
    }

    /**
     This method initiates the REST call to retrieve the Identity Hub account profile.
     */
    private fun getIdentityHubProfileInfo() {

        logger.log(Logger.Level.VERBOSE, "getIdentityHubProfileInfo")
        loginStep = LoginStep.getIdentityHubProfileInfo

        val restClient: RestClient = DependencyProvider.default.resolve<RestClient>()

        val profileRequest = restClient.request(DHPSession.shared.identityHubProfileURL)
            .method("POST")
            .contentType("application/x-www-form-urlencoded")
            .queryParam("latest", "version")
            .queryParam(DHPSession.shared.identityHubAccessToken , "access_token")
            .build()

        restClient.execute(profileRequest, this::profileRequestCompleted)
    }

    /**
     This method is the response handler for the Identity Hub account profile REST call.
     - Parameters:
        - request: the rest request that was executed
        - statusCode: the HTTP status code
        - json: the JSON data from the response body
        - error: the error returned from the server, if any
     */
    private fun profileRequestCompleted(request: RestRequest, statusCode: Int?, result: String?, error: Exception?) {
        logger.log(Logger.Level.VERBOSE, "profileRequestCompleted")
        var succeeded = false
        val parsedResult = JSONObject(result)
        val customAttributes = parsedResult.getJSONObject("custom_attributes")
        if (error != null || customAttributes == null) {
            logger.log(Logger.Level.ERROR, "Error converting/casting json: ${error.toString()}")
            return failLogin()
        }
        val username = parsedResult.getString("username")
        val federationId = customAttributes.getString("federationID")
        val clinicalPseudoName = parsedResult.getString("first_name")
        if (username != null && federationId != null && clinicalPseudoName != null) {
            if (!DHPSession.shared.username.isEmpty() && DHPSession.shared.username != username) {
                logger.log(Logger.Level.ERROR, "Username does not match the previously logged in user")
                return failLogin(LoginResult.INCORRECT_ACCOUNT)
            }
            logger.log(Logger.Level.VERBOSE, "Identity Hub Profile...")
//            printDictionary(dictionary)
            if (username.isNotEmpty() && federationId.isNotEmpty() && clinicalPseudoName.isNotEmpty()) {
                succeeded = true
                DHPSession.shared.username = username
                DHPSession.shared.federationId = federationId
                DHPSession.shared.clinicalPseudoName = clinicalPseudoName
                DHPSession.shared.identityHubProfile = parsedResult.convertToMap(arrayOf())
            }
            if (succeeded) {
                delegate?.didGetIdentityHubProfile(username, federationId, parsedResult, clinicalPseudoName)
                loginState = LoginState.loggedIn
                        loginCallback(LoginResult.SUCCESS)
            } else {
                failLogin()
            }
        } else {
            logger.log(Logger.Level.ERROR, "profile request must include username, federation id and first name")
            failLogin()
        }
    }




    // MARK: - Private methods for token refresh process

    /**
     This method initiates the REST call to retrieve the Identity Hub account profile to check that the
     app is still authrorized by Identity Hub.
     */
    private fun checkIdentityHubProfile() {

        if (loginState == LoginState.refreshing) {

            return
        } else if (loginState == LoginState.notLoggedIn) {

            return failPendingRequests()
        }

        logger.log(Logger.Level.VERBOSE, "")
        loginState = LoginState.refreshing
        loginStep = LoginStep.checkIdentityHubProfile

        if (DHPSession.shared.identityHubProfileURL.isEmpty() || DHPSession.shared.identityHubAccessToken.isEmpty()) {

            logger.log(Logger.Level.ERROR, "Identity Hub profile URL and/or Access Token not found")
            return failLogin()
        }

        val restClient: RestClient = DependencyProvider.default.resolve<RestClient>()

        val profileRequest = restClient.request(DHPSession.shared.identityHubProfileURL)
            .method("POST")
            .contentType("application/x-www-form-urlencoded")
            .queryParam("latest", "version")
            .queryParam(DHPSession.shared.identityHubAccessToken, "access_token")
            .build()

        restClient.execute(profileRequest, this::checkIdentityHubProfileRequestCompleted)
    }

    /**
     This method is the response handler for the Identity Hub account profile REST call.
     - Parameters:
        - request: the rest request that was executed
        - statusCode: the HTTP status code
        - json: the JSON data from the response body
        - error: the error returned from the server, if any
     */
    private fun checkIdentityHubProfileRequestCompleted(request: RestRequest, statusCode: Int, result: String?, exception: Exception?) {

        logger.log(Logger.Level.VERBOSE, "")

        if (DHPSession.shared.identityHubRefreshToken.isNotEmpty()) {

            logger.log(Logger.Level.WARN, "No DHP refresh token")
            failLogin()
            return
        }

        if (exception != null) {

            // Network error or something.  Just go to idle.
            failPendingRequests()
            return
        }

        if (statusCode == 200) {
            refreshDHPToken()
        } else if (statusCode == 401 || statusCode == 403) {
            refreshIdentityHubToken()
        } else {

            failPendingRequests()
        }
    }

    /**
     This method initiates a token refresh operation for Identity Hub
     */
    private fun refreshIdentityHubToken() {

        logger.log(Logger.Level.VERBOSE, "refreshIdentityHubToken")
        loginStep = LoginStep.refreshIdentityHubToken

        if (DHPSession.shared.identityHubRefreshToken.isEmpty()) {

            logger.log(Logger.Level.WARN, "No Identity Hub refresh token")
            failLogin()
            return
        }

        val restClient: RestClient = DependencyProvider.default.resolve<RestClient>()

        val loginRequest = restClient.request(loginInfo.identityHubRefreshURL)
            .method("POST")
            .contentType("application/x-www-form-urlencoded")
            .queryParam(DHPSession.shared.identityHubRefreshToken, "refresh_token")
            .queryParam(loginInfo.clientId, "client_id")
            .queryParam("refresh_token", "grant_type")
            .build()

        restClient.execute(loginRequest, this::refreshIdentityHubTokenCompleted)
    }

    /**
     This method is the response handler for the Identity Hub token refresh operation.
     - Parameters:
        - request: the rest request that was executed
        - statusCode: the HTTP status code
        - json: the JSON data from the response body
        - error: the error returned from the server, if any
     */
    private fun refreshIdentityHubTokenCompleted(request: RestRequest, statusCode: Int, result: String?, exception: Exception?) {

        logger.log(Logger.Level.VERBOSE, "refreshIdentityHubTokenCompleted")

        if (exception == null && statusCode == 200) {
            val dictionary = JSONObject(result)
                DHPSession.shared.identityHubAccessToken = dictionary.getString("access_token")
                DHPSession.shared.identityHubProfileURL = dictionary.getString("id")

                delegate?.didGetIdentityHubTokens(DHPSession.shared.identityHubAccessToken,
                        DHPSession.shared.identityHubProfileURL,
                        DHPSession.shared.identityHubIDToken,
                        DHPSession.shared.identityHubRefreshToken)

                loginState = LoginState.loggedIn
                checkIdentityHubProfile()
        } else {

            logger.log(Logger.Level.ERROR, "Error while refreshing IdentityHub token")

            if (exception != null) {

                // network error or something.  Just go to idle.
                failPendingRequests()
            } else {

                // we're no longer logged in.
                failLogin()
            }
        }
    }

    /**
     This method initiates a refresh token operation for the DHP.
     */
    private fun refreshDHPToken() {

        logger.log(Logger.Level.VERBOSE, "")
        loginStep = LoginStep.refreshDHPToken

        if (DHPSession.shared.dhpRefreshToken.isEmpty()) {

            logger.log(Logger.Level.WARN, "No DHP refresh token")
            failLogin()
            return
        }

        val restClient: RestClient = DependencyProvider.default.resolve<RestClient>()

        val loginRequest = restClient.request(loginInfo.dhpLoginURL)
            .method("POST")
            .contentType("application/x-www-form-urlencoded")
            .queryParam(DHPSession.shared.dhpRefreshToken, "refresh_token")
            .queryParam(loginInfo.clientId, "client_id")
            .queryParam("refresh_token", "grant_type")
            .build()

        restClient.execute(loginRequest, this::refreshDHPTokenCompleted)
    }

    /**
     This method is the response handler for the DHP token refresh operation.
     - Parameters:
        - request: the rest request that was executed
        - statusCode: the HTTP status code
        - json: the JSON data from the response body
        - error: the error returned from the server, if any
     */
    private fun refreshDHPTokenCompleted(request: RestRequest, statusCode: Int, result: String?, exception: Exception?) {

        logger.log(Logger.Level.VERBOSE, "refreshDHPTokenCompleted")

        var succeeded = false

        if (exception == null && statusCode == 200 && result != null) {

            logger.log(Logger.Level.VERBOSE, "DHP refresh received result")
            val dictionary = JSONObject(result)

            DHPSession.shared.dhpAccessToken = dictionary.getString("access_token")
            DHPSession.shared.dhpRefreshToken = dictionary.getString("refresh_token")

            // On final refresh, refresh_token will not be included in the payload.
            // Subsequent refreshDHPToken() call will failLogin() on guard.
            if (DHPSession.shared.dhpAccessToken.isNotEmpty()) {
//                    logger.log(Logger.Level.VERBOSE, "DHP refresh succeeded")
                succeeded = true
                delegate?.didGetDHPTokens(DHPSession.shared.dhpAccessToken, DHPSession.shared.dhpRefreshToken)
                loginState = LoginState.loggedIn
                executePendingRequests()
            }
        }

        if (!succeeded) {

            logger.log(Logger.Level.ERROR, "Error while logging into DHP")

            if (parseDHPResponse(statusCode, result, exception).status == DHPRequestStatus.unauthorized) {
                failLogin()
            } else {
                loginState = LoginState.uninitialized
            }

            failPendingRequests()
        }
    }
}
