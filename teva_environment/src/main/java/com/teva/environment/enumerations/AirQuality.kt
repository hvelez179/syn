///
// AirQuality.kt
// teva_environment
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.environment.enumerations

/**
 * This enum contains air quality terms that correspond to ranges of air quality indexes.
 * Unknown : Not specified
 * Good : Air quality is considered satisfactory, and air pollution poses little or no risk.
 * Moderate : Air quality is acceptable; however, for some pollutants there may be a moderate health concern for a very small number of people who are unusually sensitive to air pollution.
 * UnhealthyForSensitiveGroups : Members of sensitive groups may experience health effects. The general public is not likely to be affected.
 * Unhealthy : Everyone may begin to experience health effects; members of sensitive groups may experience more serious health effects.
 * VeryUnhealthy : Health warnings of emergency conditions. The entire population is more likely to be affected.
 * Hazardous : Health alert: everyone may experience more serious health effects.
 */
enum class AirQuality {
    UNKNOWN,
    GOOD,
    MODERATE,
    UNHEALTHY_FOR_SENSITIVE_GROUPS,
    UNHEALTHY,
    VERY_UNHEALTHY,
    HAZARDOUS;


    companion object {

        private val values = values()

        fun fromOrdinal(rawValue: Int): AirQuality {

            if (rawValue < 0 || rawValue >= values.size) {
                throw IndexOutOfBoundsException("Invalid air quality")
            }

            return values[rawValue]
        }

        /**
         * This method maps an air quality index to an air quality category.
         * The values are based on the EPA AirNow values:
         * 0-50 = Good; 51-100 = Moderate; 101-150 = Unhealthy for Sensitive Groups
         * 151-200 = Unhealthy; 201-300 = Very Unhealthy; 301+ = Hazardous

         * @param airQualityIndex - This parameter is the value to convert to AirQuality enum value.
         * *
         * @return - Returns an AirQuality enum value based on the range in which the Air Quality Index resides.
         */
        fun fromIndex(airQualityIndex: Int): AirQuality {
            var airQuality = AirQuality.UNKNOWN

            when (airQualityIndex) {
                in 0..50 -> airQuality = AirQuality.GOOD
                in 51..100 -> airQuality = AirQuality.MODERATE
                in 101..150 -> airQuality = AirQuality.UNHEALTHY_FOR_SENSITIVE_GROUPS
                in 151..200 -> airQuality = AirQuality.UNHEALTHY
                in 201..300 -> airQuality = AirQuality.VERY_UNHEALTHY
                in 301..500 -> airQuality = AirQuality.HAZARDOUS
            }

            return airQuality
        }
    }
}