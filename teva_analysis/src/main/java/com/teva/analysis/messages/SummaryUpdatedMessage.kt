//
// SummaryUpdatedMessage.kt
// teva_analysis
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.analysis.messages

import com.teva.common.utilities.CombinableMessage

/**
 * Message published when summary is updated.
 */
class SummaryUpdatedMessage : CombinableMessage {
    /**
     * Attempts to combine this message with another message.
     *
     * @param message The message to attempt combining to.
     * @return True if the messages were combined, False otherwise.
     */
    override fun combineWith(message: CombinableMessage): Boolean {
        return message is SummaryUpdatedMessage
    }
}
