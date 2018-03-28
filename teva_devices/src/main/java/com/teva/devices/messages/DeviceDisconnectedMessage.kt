//
// DeviceDisconnectedMessage.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.messages

import com.teva.devices.entities.Device

/**
 * Message broadcast when a device has disconnected.
 *
 * @property device The device referred to by the message.
 */
class DeviceDisconnectedMessage(val device: Device)
