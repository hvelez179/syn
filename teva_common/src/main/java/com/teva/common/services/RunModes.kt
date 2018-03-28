//
// RunModes.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.common.services

/**
 * Run modes for the TimeService
 *
 * @property rawValue The raw value of the enum
 */
enum class RunModes(val rawValue: Int) {
    REALTIME(1),
    MEDIUM(2),
    FAST(3),
    HYPER(4);

    companion object {

        /**
         * Converts a raw value into a RunMode enum
         *
         * @param rawValue The raw enum value
         */
        fun fromRawValue(rawValue: Int): RunModes {
            val result: RunModes

            when (rawValue) {
                4 -> result = HYPER
                3 -> result = FAST
                2 -> result = MEDIUM
                1 -> result = REALTIME
                else -> result = REALTIME
            }

            return result
        }
    }
}
