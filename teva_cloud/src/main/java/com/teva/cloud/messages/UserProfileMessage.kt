//
// UserProfileMessage.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.messages

import com.teva.cloud.dataentities.UserProfile
import com.teva.cloud.enumerations.UserProfileStatusCode
import com.teva.common.utilities.CombinableMessage


/**
 * This message indicates that user profile was updated.
 */
class UserProfileMessage(var messageCode: UserProfileStatusCode, var resposeCode: String = "", var responseMessage: String = ""): CombinableMessage {

    var profileData: MutableCollection<UserProfile> = ArrayList()

    constructor(messageCode: UserProfileStatusCode, profileData: List<UserProfile>?) : this(messageCode, "", "") {
        if(profileData != null) {
            this.profileData.addAll(profileData)
        }
    }

    override fun combineWith(message: CombinableMessage): Boolean {
        if(message is UserProfileMessage) {
            return true
        }
        return false
    }
}