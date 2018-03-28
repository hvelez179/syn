//
// ModelUpdatedMessage.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.common.messages

import com.teva.common.utilities.CombinableMessage

class ModelUpdatedMessage(val updateType: ModelUpdateType,
                          val objectsUpdated: MutableList<Any>) : CombinableMessage {


    fun containsObjectsOfType(vararg types: Class<*>): Boolean {
        var result = false

        for (obj in objectsUpdated) {
            val cls = obj.javaClass

            // Check if the changed object type is in the type list
            for (i in types.indices) {
                if (types[i] == cls) {
                    result = true
                    break
                }
            }

            if (result) {
                break
            }
        }

        return result
    }

    /**
     * Attempts to combine this message with another message.
     *
     * @param message The message to attempt combining to.
     * @return True if the messages were combined, False otherwise.
     */
    override fun combineWith(message: CombinableMessage): Boolean {
        if (message is ModelUpdatedMessage) {
            if (message.updateType == this.updateType) {
                this.objectsUpdated.addAll(message.objectsUpdated)
                return true
            }
        }
        return false
    }
}
