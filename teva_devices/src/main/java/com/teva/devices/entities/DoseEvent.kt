//
// DoseEvent.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.entities

import com.teva.common.entities.TrackedModelObject

import org.threeten.bp.Instant

/**
 * The base class for medication dose events.
 *
 * @property deviceSerialNumber Serial number of the device.
 * @property eventUID Unique dose event identifier.
 * @property drugUID Medication identifier.
 * @property eventTime The time at which the event took place.
 * @property timezoneOffsetMinutes The offset of the timezone where the dose was taken.
 */
open class DoseEvent(var deviceSerialNumber: String = "",
                     var eventUID: Int = 0,
                     var drugUID: String = "",
                     var eventTime: Instant? = null,
                     var timezoneOffsetMinutes: Int = 0) : TrackedModelObject()