//
// TrackedModelObject.java
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.common.entities

import com.teva.common.services.ServerTimeService
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider

import org.threeten.bp.Instant
import java.security.InvalidParameterException

/**
 * The base class for tracked model classes.
 *
 * @property hasChanged Indicates whether the model's record in the database has been changed
 * @property changeTime The time the model was last changed
 */
open class TrackedModelObject(var hasChanged: Boolean = false,
                              var changeTime: Instant? = null) {

    /**
     * This property is the difference in seconds between the real server time and the local device time setting,
     * without time zone, at the time the object was created.
     * If the property is null, the offset has not yet been determined.
     */
    var serverTimeOffset: Int? = null

    /**
     * Marks the object as changed and updates the change time.
     */
    fun markAsChanged(changed: Boolean) {
        if (changed) {
            hasChanged = true
            changeTime = DependencyProvider.default.resolve<TimeService>().now()
            try {
                serverTimeOffset = DependencyProvider.default.resolve<ServerTimeService>().serverTimeOffset
            } catch( exception: InvalidParameterException) {

            }
        }
    }
}
