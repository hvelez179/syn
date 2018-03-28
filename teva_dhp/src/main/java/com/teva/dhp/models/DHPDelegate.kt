//
//  DHPDelegate.kt
//  Teva_DHP
//
//  Copyright © 2017 Teva. All rights reserved.
//

package com.teva.dhp.models

import org.json.JSONObject
import org.threeten.bp.Instant

/**
 This protocol must be implemented by the consumer of the Teva_DHP project.
 It is used to provide everything the project needs in order to function.
 This includes Constant values such as Client ID and URLs, as well as data that
 must be persisted by the consumer, such as access tokens and user information.
 */
interface DHPDelegate {

    /**
     This method is used to tell the consumer that the login state has changed to logged in.
     */
    fun didLoginToDHP()

    /**
     This method is used to tell the consumer that the user is not logged in.
     It is called when Login fails or when the refresh of the DHP access token fails.
     */
    fun failLogin()

    /**
     This method allows the delegate to specify the current date and time used by the DHP Manager.
     */
    fun getCurrentDate(): Instant

    /**
    Call upon getting the user profile from Identity Hub.

    - Parameters:
    - username: the account holder's username
    - federationId: the account holders external entity id.
    - identityHubProfile: Dictionary containing the id hub profile.
    - clinicalPsuedoName: the account holder's first name.
     */
    fun didGetIdentityHubProfile(username: String, federationId: String, identityHubProfile: JSONObject, clinicalPseudoName: String)

    /**
    Called after login to identity hub. Also called after refreshing identity hub tokens.
    - Parameters:
    - accessToken: the access token for identity hub.
    - profileURL: the identity hub profile url.
    - idToken: the identity hub identifier token.
    - refreshToken: the identity hub refresh token.
     */
    fun didGetIdentityHubTokens(accessToken: String, profileUrl: String, idToken: String, refreshToken: String)

    /**
    Called after obtaining DHP access and refresh tokens.
    - Parameters:
    - dhpAccessToken: the access token for the DHP
    - dhpRefreshToken: the refresh token for the DHP.
     */
    fun didGetDHPTokens(dhpAccessToken: String, dhpRefreshToken: String)
}
