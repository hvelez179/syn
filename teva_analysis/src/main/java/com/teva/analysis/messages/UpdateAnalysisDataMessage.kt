//
// UpdateAnalysisDataMessage.kt
// teva_analysis
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.analysis.messages

import com.teva.common.utilities.CombinableMessage

import java.lang.reflect.Type

/**
 * Message sent when analysis data needs to be updated due to day change, inhalation, device connection etc.
 *
 * @param objectsChanged -  the list of objects whose change triggered the message.
 */

class UpdateAnalysisDataMessage(var objectsChanged: List<Any>) : CombinableMessage {

    /**
     * This method verifies if any of the changed objects are of the specified type.
     *
     * @param clazz -  the type which the change objects should be verified against.
     * @return - true if any of the changed objects belong to the specified type, else false.
     */
    fun containsObjectsOfType(clazz: Class<*>): Boolean {
        return objectsChanged.any { it.javaClass == clazz }
    }

    /**
     * This method verifies if any of the changed objects belong to any of the specified types.
     *
     * @param classes - the types which the changed objects should be verified against.
     * @return - true if any of the changed objects belongs to any of the specified types, else false.
     */
    fun containsObjectsOfType(classes: List<Class<*>>): Boolean {
        return objectsChanged.any { classes.contains(it.javaClass) }
    }

    /**
     * Attempts to combine this message with another message.
     *
     * @param message The message to attempt combining to.
     * @return True if the messages were combined, False otherwise.
     */
    override fun combineWith(message: CombinableMessage): Boolean {

        if (message is UpdateAnalysisDataMessage) {
            objectsChanged += message.objectsChanged
            return true
        }

        return false
    }
}
