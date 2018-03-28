//
//  LoginState.kt
//  Teva_DHP
//
//  Copyright © 2017 Teva. All rights reserved.
//

package com.teva.dhp.enumerations

/**
 This enumeration represents the login state of the DHPManager.
 */
enum class LoginState {

    /**
     The state has not yet been determined.
     */
    uninitialized,

    /**
     The user has not opted-in/consented or has revoked consent.
     */
    notConsented,

    /**
     The user does not currently have a valid DHP access token.
     */
    notLoggedIn,

    /**
     The login process in currently being executed.
     */
    inProgress,

    /**
     The token refresh process in currently being executed.
     */
    refreshing,

    /**
     The user is logged in.
     */
    loggedIn
}
