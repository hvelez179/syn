//
// ConsentStatus.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud

/**
 * This enumeration represents the user's current opt-in/opt-out consent status.
 */
enum class ConsentStatus(var status: String) {
    /**
     * User has consented but not yet completed the login process.
     */
    IN_PROGRESS("In Progress"),

    /**
     * User has consented and logged in at least once.
     */
    ACTIVE("Active"),

    /**
     * User denied consent. App can only be used if commercial.
     */
    DENIED("Denied")
}