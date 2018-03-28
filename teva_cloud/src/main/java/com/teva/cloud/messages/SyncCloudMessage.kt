//
// SyncCloudMessage.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.messages

import com.teva.common.utilities.CombinableMessage

/**
 * This message indicates that data should be synced with the cloud.
 */
class SyncCloudMessage : CombinableMessage {
    override fun combineWith(message: CombinableMessage): Boolean {
        if(message is SyncCloudMessage) {
            return true
        }
        return false
    }
}
