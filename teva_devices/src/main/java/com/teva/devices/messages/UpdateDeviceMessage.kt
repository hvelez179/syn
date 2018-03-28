//
// UpdateDeviceMessage.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.messages

import com.teva.common.utilities.CombinableMessage

/**
 * Message sent from the SystemManager to instruct the DeviceManager to update the
 * ConnectionMeta records for the connected devices.
 */
class UpdateDeviceMessage : CombinableMessage {
    /**
     * Attempts to combine this message with another message.
     *
     * @param message The message to attempt combining to.
     * @return True if the messages were combined, False otherwise.
     */
    override fun combineWith(message: CombinableMessage): Boolean {
        return message is UpdateDeviceMessage
    }
}
