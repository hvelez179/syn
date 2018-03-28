//
// ServerTimeService.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.common.services


/**
 * The class implementing this interface provides access to server time offset information.
 */
interface ServerTimeService {
    /**
     * This property is the current difference in seconds between the real server time and the local device time setting, without time zone.
     * If the device time is ahead of or equal to the server time, it should be positive (e.g. "+60", "0").
     * If the device time is behind the server time, it should be negative (e.g. "-60").
     * If serverTimeOffSet cannot currently be determined, it should be null.
     */
    var serverTimeOffset: Int?
        get

    /**
     * This function determines whether the serverTimeOffset is within an acceptable range determined by the ServerTimeService.
     * This function is used to determine whether action needs to be taken to correct the time offset.
     */
    fun isServerTimeOffsetWithinAcceptableRange(): Boolean

    /**
     * This property removes the current serverTimeOffset, requiring it to be re-initialized.
     */
    fun clearServerTimeOffset()
}