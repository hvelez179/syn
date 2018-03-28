///
// RepeatType.kt
// teva_notifications
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.notifications.enumerations

/**
 * Indicates the type of repetition .
 */
enum class RepeatType {
    NONE,
    ONCE_PER_DAY,
    ONCE_PER_WEEK,
    MONTHLY;

    companion object {

        private val values = values()

        /**
         * This method initializes the RepeatType from a numeric ordinal value.
         *
         * @param rawValue The enumeration ordinal value.
         * @return The RepeatType corresponding to the ordinal value.
         */
        fun fromOrdinal(rawValue: Int): RepeatType {

            // if the ordinal value is not valid, throw an exception.
            if (rawValue >= values.size || rawValue < 0) {
                throw IndexOutOfBoundsException("Invalid RepeatType.")
            }

            return values[rawValue]
        }
    }
}
