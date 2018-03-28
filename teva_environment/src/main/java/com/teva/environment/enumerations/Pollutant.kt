///
// Pollutant.kt
// teva_environment
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.environment.enumerations

/**
 * The pollutants that affect air quality
 * Unknown : Not specified
 * Ozone : Ground level or "bad" ozone
 * ParticulateMatterLessThan2pt5Microns : Particulate matter less than 2.5
 * ParticulateMattersLessThan10Microns : Particulate matter less than 10 microns
 * CarbonMonoxide : Carbon Monoxide
 * NitrogenDioxide : Nitrogen Dioxide
 * SulfurDioxide : Sulfur Dioxide
 */
enum class Pollutant {
    UNKNOWN,
    OZONE,
    PARTICULATE_MATTER_LESS_THAN_2PT5_MICRONS,
    PARTICULATE_MATTER_LESS_THAN_10_MICRONS,
    CARBON_MONOXIDE,
    NITROGEN_DIOXIDE,
    SULFUR_DIOXIDE,

    // NEW ITEMS MUST BE ADDED BEFORE LAST.
    LAST;


    companion object {

        /**
         * This method converts the pollutant string to a Pollutant enum value
         *
         * @param pollutantString - This parameter is the value to convert to Pollutant enum value.
         *
         * @return - Returns an Pollutant enum value.
         */
        fun fromString(pollutantString: String?): Pollutant {
            var pollutant = Pollutant.UNKNOWN

            if (pollutantString != null) {
                when (pollutantString) {
                    "OZONE" -> pollutant = Pollutant.OZONE
                    "CO" -> pollutant = Pollutant.CARBON_MONOXIDE
                    "NO2" -> pollutant = Pollutant.NITROGEN_DIOXIDE
                    "SO2" -> pollutant = Pollutant.SULFUR_DIOXIDE
                    "PM2.5" -> pollutant = Pollutant.PARTICULATE_MATTER_LESS_THAN_2PT5_MICRONS
                    "PM10" -> pollutant = Pollutant.PARTICULATE_MATTER_LESS_THAN_10_MICRONS
                }
            }

            return pollutant
        }
    }
}

