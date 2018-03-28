//
// DHPCloudService.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.services

import com.teva.cloud.dataentities.UserAccount
import com.teva.cloud.dataquery.UserAccountQuery
import com.teva.cloud.enumerations.CloudActivity
import com.teva.cloud.extensions.instantFromGMTString
import com.teva.cloud.models.CloudSessionState
import com.teva.common.messages.SystemMonitorMessage
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.common.utilities.Messenger
import com.teva.utilities.utilities.Logger.Level.VERBOSE
import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.Endpoints.DHPGetServerTime
import com.teva.dhp.DataEntities.DHPDataTypes.GenericDHPRequest
import com.teva.dhp.DataEntities.DHPDataTypes.GenericDHPResponse
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPGetServerTimeResponseBody
import com.teva.dhp.models.DHPAPIs
import com.teva.dhp.models.DHPDelegate
import com.teva.dhp.models.DHPManager
import com.teva.dhp.models.DHPSession
import org.json.JSONObject
import org.threeten.bp.Duration
import org.threeten.bp.Instant

/**
 * This class implements the CloudService interface for the DHP.
 */
class DHPCloudService(private val dhpManager: DHPManager): CloudService, DHPDelegate {

    init {
        dhpManager.delegate = this
    }

    // Internal properties

    override var delegate: CloudServiceDelegate? = null
        set(newValue) {
            field = newValue
            delegate?.isLoggedIn = getUserAccount().hasTokens()
        }

    /**
     * This property is the Logger used to log to the console.
     */
    internal val logger = Logger("DHPCloudService")

    /**
     * This property is used to get and save the UserAccount stored in the database.
     */
    private var userAccountQuery = DependencyProvider.default.resolve<UserAccountQuery>()

    /**
     * This property is used to get the current time for the app.
     */
    internal val timeService = DependencyProvider.default.resolve<TimeService>()

    /**
     * This property stores the last calculated server time.
     */
    private var serverTime: Instant? = null

    // Internal methods

    init {
        dhpManager.delegate = this
        dhpManager.apiRequestMetricsCallback = this::reportApiRequestMetrics
    }

    override fun initialize() {
        dhpManager.initialize()
    }

    override fun logOut() {

        logger.log(VERBOSE, "logout()")

        dhpManager.logOut()

        //URLSession.shared.reset(completionHandler: { })

        failLogin()

        userAccountQuery.getUserAccount()?.let { account ->

            account.DHPAccessToken = ""
            account.DHPRefreshToken = ""
            account.identityHubProfileUrl = ""
            account.identityHubAccessToken = ""
            account.identityHubRefreshToken = ""

            userAccountQuery.update(account, true)
        }
    }

    override fun getServerTimeAsync() {

        val api = DHPAPIs.getServerTime

        val payload = DHPGetServerTime()
        payload.invokingExternalEntityID = DHPSession.shared.federationId
        payload.invokingRole = CloudSessionState.shared.activeInvokingRole

        if (payload.invokingRole == DHPCodes.Role.guardian) {
            payload.patientExternalEntityID = CloudSessionState.shared.activeProfileID
        }

        payload.messageID = api.messageId
        payload.appVersionNumber = CloudSessionState.shared.appVersionNumber
        payload.appName = CloudSessionState.shared.appName
        payload.UUID = CloudSessionState.shared.mobileUUID
        payload.apiExecutionMode = DHPCodes.ApiExecutionMode.synchronous

        val getServerTime = GenericDHPRequest(api, payload, DHPGetServerTimeResponseBody::class, this::getServerTimeCompleted)
        dhpManager.executeAsync(getServerTime, null)
    }

        /**
         * This method provides the callback for DHPManager.getServerTime.
         * @param response: the DHP response.
         */
        private fun getServerTimeCompleted(response: GenericDHPResponse<DHPGetServerTimeResponseBody>) {

            logger.log(VERBOSE, "getServerTimeCompleted - succeeded: ${response.success}")

            serverTime = null

            if (response.success) {

                response.body?.serverTimeGMT?.let { serverTimeGMT ->
                    serverTime = instantFromGMTString(serverTimeGMT)
                }
            }

            delegate?.getServerTimeCompleted(response.success, serverTime)
        }

    // DHPDelegate members

    override fun didLoginToDHP() {
        delegate?.isLoggedIn = true
        postSystemMonitorActivity(CloudActivity.DhpLogin(true))
    }

    override fun failLogin() {
        delegate?.isLoggedIn = false
        postSystemMonitorActivity(CloudActivity.DhpLogin(false))
    }

    override fun getCurrentDate(): Instant {
        return Instant.now()
    }

    /**
     * Called upon receiving the user profile from identity hub.
     * @param username: the user's username (their email address)
     * @param federationId: Identity hub identifier
     * @param identityHubProfile: Dictionary containing id hub user profile info.
     * @param clinicalPseudoName: the user's first name.
     */
    override fun didGetIdentityHubProfile(username: String, federationId: String, identityHubProfile: JSONObject, clinicalPseudoName: String) {

        val userAccount = getUserAccount()
        userAccount.username = username
        userAccount.federationId = federationId
        userAccount.pseudoName = clinicalPseudoName

        val firstName = identityHubProfile.getString("first_name")
        val lastName = identityHubProfile.getString("last_name")
        if( firstName != null && lastName != null) {
            CloudSessionState.shared.idHubFirstName = firstName
            CloudSessionState.shared.idHubLastName = lastName
        }

        saveUserAccount(userAccount)
    }

    /**
     * Called upon receiving or refreshing the DHP access tokens.
     * @param dhpAccessToken: the DHP access token
     * @param dhpRefreshToken: DHP refresh token.
     */
    override fun didGetDHPTokens(dhpAccessToken: String, dhpRefreshToken: String) {
        val userAccount = getUserAccount()
        userAccount.DHPRefreshToken = dhpRefreshToken
        userAccount.DHPAccessToken = dhpAccessToken

        saveUserAccount(userAccount)
    }

    /**
     * Called upon receiving identity hub access tokens.
     * @param accessToken: the id hub access token
     * @param profileUrl: the user's profile url
     * @param idToken: user's identifier token
     * @param refreshToken: id hub refresh token for the session.
     */
    override fun didGetIdentityHubTokens(accessToken: String, profileUrl: String, idToken: String, refreshToken: String) {
        val userAccount = getUserAccount()
        userAccount.identityHubAccessToken = accessToken
        userAccount.identityHubProfileUrl = profileUrl
        userAccount.identityHubIdToken = idToken
        userAccount.identityHubRefreshToken = refreshToken

        saveUserAccount(userAccount)
    }

    // Private methods to support DHPDelegate

    /**
     * This method gets the UserAccount from the database.
     */
    private fun getUserAccount(): UserAccount {

        var obj = userAccountQuery.getUserAccount()

        if (obj == null) {
            obj = UserAccount()
            obj.created = timeService.now()
            userAccountQuery.insert(obj, true)
        }

        return obj
    }

    /**
     * This method saves the UserAccount object into the database.
     */
    private fun saveUserAccount(userAccount: UserAccount) {
        userAccountQuery.update(userAccount, true)
        logger.log(VERBOSE, "saveUserAccount: ${userAccount.federationId ?: ""}")
    }

    /**
     * This method posts a SystemMonitorMessage for the specified activity.
     */
    private  fun postSystemMonitorActivity(activity: CloudActivity) {
        DependencyProvider.default.resolve<Messenger>().post(SystemMonitorMessage(activity))
    }

    /**
     * This is the callback method invoked for logging metrics when a dhp request is completed.
     */
    private fun reportApiRequestMetrics(uri: String, success: Boolean, message: String, duration: Duration) {

        var label: String? = ""
        val statusCodeIndex = message.indexOf("HTTP statusCode = ", 0, true)
        if (statusCodeIndex != -1){
            label = message.substring(statusCodeIndex..message.length)
        } else if (message.contains("timed out")) {
            label = "Timeout"
        }

        postSystemMonitorActivity(CloudActivity.ApiRequest(uri, duration, label))
    }
}