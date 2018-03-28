//
// CloudLoginStateChangedMessage.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.messages

import com.teva.cloud.enumerations.CloudManagerState
import com.teva.common.utilities.CombinableMessage

/**
 * This message is published to indicate that the cloud login state has changed.
 */
class CloudLoginStateChangedMessage(var loginState: CloudManagerState = CloudManagerState.UNINITIALIZED) : CombinableMessage {
    override fun combineWith(message: CombinableMessage): Boolean {
        if(message is CloudLoginStateChangedMessage) {
            return true
        }
        return false
    }
}