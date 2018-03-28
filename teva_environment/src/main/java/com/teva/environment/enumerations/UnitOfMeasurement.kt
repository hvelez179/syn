///
// UnitOfMeasurement.kt
// teva_environment
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.environment.enumerations

/**
 * This enum defines the units of measurement used in the weather information returned by the provider
 */

enum class UnitOfMeasurement {
    ENGLISH,
    METRIC;


    companion object {

        /**
         * This method converts the weather.com unit string to an enum value.
         *
         * @param unit This parameter is the unit string returned by the weather.com request.
         * @return Returns the corresponding enum value.
         */
        fun fromString(unit: String): UnitOfMeasurement? {
            when (unit) {
                "e" -> return UnitOfMeasurement.ENGLISH
                "m" -> return UnitOfMeasurement.METRIC
            }
            return null
        }
    }
}
