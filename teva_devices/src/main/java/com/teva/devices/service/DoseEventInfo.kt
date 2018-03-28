//
// DoseEventInfo.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.service

import org.threeten.bp.Instant

/**
 * Represents the information common to all types of medication dose events.
 *
 * @property eventUID The unique id of the dose event
 * @property eventTime The time of the event.
 * @property timezoneOffsetMinutes The offset in minutes of the timezone where the event was uploaded.
 * @property drugUID The drug id.
 */
open class DoseEventInfo(var eventUID: Int = 0,
                         var eventTime: Instant? = null,
                         var timezoneOffsetMinutes: Int = 0,
                         var drugUID: String? = null)