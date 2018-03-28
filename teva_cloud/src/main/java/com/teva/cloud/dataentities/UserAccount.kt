//
// UserAccount.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.dataentities

import com.teva.common.entities.TrackedModelObject
import org.threeten.bp.Instant


/**
 * This class represents a user account.
 */
class UserAccount (var studyHashKey: String? = null,
                   var pseudoName: String? = null,
                   var federationId: String? = null,
                   var username: String? = null,

                   var identityHubIdToken: String? = null,
                   var identityHubAccessToken: String? = null,
                   var identityHubRefreshToken: String? = null,
                   var identityHubProfileUrl: String? = null,

                   var DHPAccessToken: String? = null,
                   var DHPRefreshToken: String? = null,
                   var lastInhalerSyncTime: Instant? = null,
                   var lastNonInhalerSyncTime: Instant?  = null,
                   var created: Instant? = null) : TrackedModelObject() {
    /**
     * This function returns true if the UserAccount has non-nil Identity Hub and DHP tokens.
     * The access tokens can be nil as long as refresh tokens still exist.
     * @return: True if the UserAccount is expected to still have a valid session.
     */
    fun hasTokens(): Boolean {
        return (identityHubIdToken ?: "") != "" && (identityHubProfileUrl ?: "") != "" && (identityHubRefreshToken ?: "") != "" && (DHPRefreshToken ?: "") != ""
    }
}