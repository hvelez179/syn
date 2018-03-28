//
// InhalationEffort.kt
// teva_analysis
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.analysis.extensions.enumerations

/**
 * This enum contains quality of the inhalation, or indication of a system error.
 * GoodInhalation: Indicates valid inhale and PIF above low inhalation threshold and less than or equal to high inhalation threshold.
 * LowInhalation: Indicates valid inhale and PIF below low inhalation but equal to or above no inhalation threshold.
 * NoInhalation: Indicates either valid inhale and PIF below no inhalation threshold, or invalid inhale that is not a System Error, nor exhalation.
 * Exhalation: Indicates an invalid inhale with an Unexpected Exhalation status.
 * Error: Indicates valid inhale and PIF above normal (i.e., High Flow), OR .
 * SystemError: Indicates a System Error where status is bad data, timestamp error, or inhaler parameter error
 */
enum class InhalationEffort {
    GOOD_INHALATION,
    LOW_INHALATION,
    NO_INHALATION,
    EXHALATION,
    ERROR,
    SYSTEM_ERROR;

    /**
     * This method determines whether the inhalation effort is acceptable.
     *
     * @return - Returns true if the inhalation effort is good or low, otherwise false.
     */
    val isAcceptable: Boolean
        get() = (this == GOOD_INHALATION || this == LOW_INHALATION)

    /**
     * This method determines if the inhalation effort is unsuccessful.
     *
     * @return - Returns true if there was no inhalation, exhalation or error, else false.
     */
    val isUnsuccessful: Boolean
        get() = (this == NO_INHALATION || this == EXHALATION || this == ERROR)
}
