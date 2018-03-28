//
// HistoryUpdatedMessage.kt
// teva_analysis
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.analysis.messages

import com.teva.common.utilities.CombinableMessage
import com.teva.devices.entities.InhaleEvent

/**
 * This message is sent to indicate that history has been updated.
 *
 * @property objectsChanged - the list of objects that caused the history update.
 */
class HistoryUpdatedMessage(var objectsChanged: List<Any>) : CombinableMessage {

    /**
     * This method verifies if any of the changed objects are of the specified type.
     *
     * @param T -  the type which the change objects should be verified against.
     * @return - true if any of the changed objects belong to the specified type, else false.
     */
    inline fun <reified T> containsObjectsOfType(): Boolean {
        return objectsChanged.any { it.javaClass == T::class.java }
    }

    /**
     * This method checks whether an inhalation event is part of the changed objects in this HistoryUpdatedMessage.
     * @param inhaleEvent The inhale event to check against the list of changed objects.
     */
    operator fun contains(inhaleEvent: InhaleEvent): Boolean {

        return objectsChanged.any { (it is InhaleEvent) && (it.uniqueId == inhaleEvent.uniqueId) }
    }

    /**
     * Attempts to combine this message with another message.
     *
     * @param message The message to attempt combining to.
     * @return True if the messages were combined, False otherwise.
     */
    override fun combineWith(message: CombinableMessage): Boolean {

        if (message is HistoryUpdatedMessage) {
            objectsChanged += message.objectsChanged
            return true
        }

        return false
    }
}
