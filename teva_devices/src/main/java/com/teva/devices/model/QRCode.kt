//
// QRCode.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.model


/**
 * Data entity representing a QRCode.
 *
 * @property serialNumber      The serial number
 * @property authenticationKey The authentication key.
 */
data class QRCode(var productID: String? = null,
                  var serialNumber: String = "",
                  var authenticationKey: String = "")
