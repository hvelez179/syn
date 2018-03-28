//
// BluetoothStateChangedMessage.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.messages

/**
 * Message sent when the Bluetooth adapter is enabled or disabled.
 *
 * @property isEnabled A value indicating whether the Bluetooth Adapter is enabled.
 */
class BluetoothStateChangedMessage(val isEnabled: Boolean)
