///
// WeatherCondition.kt
// teva_environment
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.environment.enumerations

/**
 * This enum contains the weather conditions that are provided by weather.com API.
 * https://docs.google.com/document/d/1MZwWYqki8Ee-V7c7InBuA5CDVkjb3XJgpc39hI9FsI0/
 */
enum class WeatherCondition(val numVal: Int) {
    UNKNOWN(0),
    THUNDERSTORM(4),
    RAIN_SNOW(5),
    RAIN_SLEET(6),
    SNOW_SLEET(7),
    FREEZING_DRIZZLE(8),
    DRIZZLE(9),
    FREEZING_RAIN(10),
    RAIN_SHOWER(11),
    RAIN(12),
    SNOW_GRAINS(13),
    HEAVY_SNOW_GRAINS(14),
    BLOWING_SNOW(15),
    HEAVY_SNOW_SHOWER(16),
    SMALL_HAIL(17),
    SNOW_PELLETS(18),
    WIDESPREAD_DUST(19),
    FOG(20),
    HAZE(21),
    SMOKE(22),
    BLOWING_SAND_NEARBY(24),
    ICE_CRYSTALS(25),
    CLOUDY(26),
    SHOWERS_IN_THE_VICINITY(27),
    MOSTLY_CLOUDY_DAY(28),
    PARTLY_CLOUDY_NIGHT(29),
    PARTLY_CLOUDY_DAY(30),
    CLEAR_NIGHT(31),
    SUNNY_DAY(32),
    FAIR_NIGHT(33),
    FAIR_DAY(34),
    THUNDER(38),
    HEAVY_RAIN_SHOWER(40),
    HEAVY_SNOW(42),
    THUNDER2(47),
    LAST(48);


    companion object {

        /**
         * This method converts an integer into WeatherCondition and returns null
         * if the integer is not a valid WeatherCondition value.
         *
         * @param numVal - the numeric value to be converted to WeatherCondition.
         * @return - The WeatherCondition equivalent of the integer value.
         */
        fun fromInteger(numVal: Int): WeatherCondition? {
            return values().firstOrNull { it.numVal == numVal }
        }
    }
}
