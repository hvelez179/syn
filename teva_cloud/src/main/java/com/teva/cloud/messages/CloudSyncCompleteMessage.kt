package com.teva.cloud.messages

import com.teva.common.utilities.CombinableMessage

/**
 * This message indicates that cloud sync was completed.
 */
class CloudSyncCompleteMessage : CombinableMessage {
    override fun combineWith(message: CombinableMessage): Boolean {
        if(message is CloudSyncCompleteMessage) {
            return true
        }
        return false
    }
}