//
// InhaleStatus.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.enumerations

import java.util.ArrayList
import java.util.EnumSet
import java.util.HashMap

/**
 * This class provides an enumeration of inhalation status values.
 */

object InhaleStatus {

    enum class InhaleStatusFlag (val inhaleStatusFlagValue: Int) {
        /**
         * The event timed out before the cap was closed.
         */
        INHALE_STATUS_TIMEOUT(1 shl 0),

        /**
         * The event record was corrupted.
         */
        INHALE_STATUS_BAD_DATA(1 shl 1),

        /**
         * Multiple inhalations detected.
         */
        INHALE_STATUS_MULTIPLE_INHALATIONS(1 shl 2),

        /**
         * Exhalation detected instead of inhalation.
         */
        INHALE_STATUS_UNEXPECTED_EXHALATION(1 shl 3),

        /**
         * The device has detected a power reset and the timestamp is not valid.
         */
        INHALE_STATUS_TIMESTAMP_ERROR(1 shl 4),

        /**
         * No inhalation was detected.
         */
        INHALE_STATUS_NO_INHALATION(1 shl 5),

        /**
         * Something went wrong with the detection of the inhalation event and the
         * the inhale profile parameters (event time, duration, peak, time to peak,
         * volume) may not be valid.  (still counts as an inhale for adherence purposes)
         */
        INHALE_STATUS_INHALE_PARAMETER_ERROR(1 shl 6)

    }

    private var systemErrorMapper = hashMapOf(
            InhaleStatusFlag.INHALE_STATUS_BAD_DATA to SystemErrorCode.BAD_DATA,
            InhaleStatusFlag.INHALE_STATUS_TIMESTAMP_ERROR to SystemErrorCode.TIMESTAMP_ERROR,
            InhaleStatusFlag.INHALE_STATUS_INHALE_PARAMETER_ERROR to SystemErrorCode.INHALER_PARAMETER_ERROR)

    val SystemErrorStatuses: IntArray = systemErrorMapper.keys.map { it.inhaleStatusFlagValue }.toIntArray()

    /**
     * Translates a numeric status code into a Set of InhaleStatusFlag enums
     *
     * @param statusValue - the numeric status code to be translated
     * @return EnumSet representing an inhale status
     */
    fun getInhaleStatusFlags(statusValue: Int): EnumSet<InhaleStatusFlag> {
        val statusFlags = EnumSet.noneOf(InhaleStatusFlag::class.java)
        for (flag in InhaleStatusFlag.values()) {
            val flagValue = flag.inhaleStatusFlagValue
            if (flagValue and statusValue == flagValue) {
                statusFlags.add(flag)
            }
        }

        return statusFlags
    }

    /**
     * Translates a set of InhaleStatusFlag enums into a numeric status code
     *
     * @param flags - Set of inhale status flags
     * @return - numeric status code representing the inhale status
     */
    fun getInhaleStatusValue(flags: Set<InhaleStatusFlag>): Int {
        var value = 0
        for (flag in InhaleStatusFlag.values()) {
            value = value or flag.inhaleStatusFlagValue
        }
        return value
    }

    fun getSystemErrorCodes(flags: Set<InhaleStatusFlag>): List<SystemErrorCode> {
        val errorCodes = ArrayList<SystemErrorCode>()

        for ((key, value) in systemErrorMapper) {
            if (flags.contains(key)) {
                errorCodes.add(value)
            }
        }

        return errorCodes
    }
}
