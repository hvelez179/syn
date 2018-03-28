///
// WindDirectionCardinal.kt
// teva_environment
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.environment.enumerations

/**
 * This enum specifies wind direction values.
 */
enum class WindDirectionCardinal(val text: String) {
    North("N"),
    NorthNorthEast("NNE"),
    NorthEast("NE"),
    EastNorthEast("ENE"),
    East("E"),
    EastSouthEast("ESE"),
    SouthEast("SE"),
    SouthSouthEast("SSE"),
    South("S"),
    SouthSouthWest("SSW"),
    SouthWest("SW"),
    WestSouthWest("WSW"),
    West("W"),
    WestNorthWest("WNW"),
    NorthWest("NW"),
    NorthNorthWest("NNW"),
    Calm("CALM");

    override fun toString(): String {
        return text
    }

    companion object {

        /**
         * This method converts a string to a WindDirectionCardinal and returns null
         * if the String is not a valid WindDirectionCardinal value.
         *
         * @param value - the String to be converted to WindDirectionCardinal.
         * @return - the WindDirectionCardinal equivalent of the passed String.
         */
        fun fromString(value: String): WindDirectionCardinal? {
            return values().firstOrNull { it.text == value }
        }
    }
}
