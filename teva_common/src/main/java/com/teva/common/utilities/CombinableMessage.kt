//
// CombinableMessage.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.common.utilities

/**
 * Interface for messages that be combined with other pending messages when they are
 * posted in the messenger.
 */
interface CombinableMessage {
    /**
     * Attempts to combine this message with another message.
     *
     * @param message The message to attempt combining to.
     * @return True if the messages were combined, False otherwise.
     */
    fun combineWith(message: CombinableMessage): Boolean
}
