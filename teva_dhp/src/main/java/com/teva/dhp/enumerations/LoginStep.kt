//
//  LoginStep.kt
//  Teva_DHP
//
//  Copyright © 2017 Teva. All rights reserved.
//

package com.teva.dhp.enumerations

/**
 This enumeration lists the steps the DHPManager goes through to log in.
 */
enum class LoginStep {
    uninitialized,
    login,
    oauthAuthorization,
    parseAuthorizationSuccessURL,
    validateIDToken,
    validateIDTokenPayload,
    logIntoDHP,
    getIdentityHubProfileInfo,
    checkIdentityHubProfile,
    refreshIdentityHubToken,
    refreshDHPToken
}
