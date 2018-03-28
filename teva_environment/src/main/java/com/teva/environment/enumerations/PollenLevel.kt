///
// PollenLevel.kt
// teva_environment
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.environment.enumerations

/**
 * This enum contains pollen level terms that correspond to ranges of pollen count.
 */
enum class PollenLevel(val numVal: Int) {
    NONE(0),
    LOW(1),
    MODERATE(2),
    HIGH(3),
    VERY_HIGH(4),
    NO_DATA(9);


    companion object {

        /**
         * This method converts an integer into PollenLevel and returns null
         * if the integer is not a valid PollenLevel value.
         *
         * @param numVal - the numeric value to be converted to PollenLevel.
         * @return - The PollenLevel equivalent of the integer value.
         */
        fun fromInteger(numVal: Int): PollenLevel {
            return values().firstOrNull { it.numVal == numVal } ?: NO_DATA
        }
    }
}
