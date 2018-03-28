//
// DeviceUpdatedMessage.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.messages

import com.teva.devices.entities.Device
import com.teva.devices.entities.InhaleEvent

/**
 * Message broadcast when a device has been updated.
 *
 * @property device The device referred to by the message.
 */
class DeviceUpdatedMessage(val device: Device,
                           val inhaleEvents: List<InhaleEvent>)
