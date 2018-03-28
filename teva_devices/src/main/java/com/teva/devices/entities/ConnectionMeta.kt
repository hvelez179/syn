//
// ConnectionMeta.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.entities

import com.teva.common.entities.TrackedModelObject
import org.threeten.bp.LocalDate

/**
 * This class provides device connection information.
 *
 * @property serialize The device serial number associated with the connection.
 * @property connectionDate The calendar date when the connection occurred.
 */
data class ConnectionMeta(var connectionDate: LocalDate? = null,
                          var serialNumber: String? = null) : TrackedModelObject()
