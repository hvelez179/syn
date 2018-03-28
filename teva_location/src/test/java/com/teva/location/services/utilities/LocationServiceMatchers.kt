//
// LocationServiceMatchers.kt
// teva_location
//
// Copyright (c) 2017 Teva. All rights reserved.
//
package com.teva.location.services.utilities

import android.location.Location

import com.teva.location.services.LocationInfo

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

/**
 * This class defines matchers for the objects used by the location service to be used in the unit tests.
 */

object LocationServiceMatchers {
    /**
     * Matcher for Location

     * @param expectedLocation The location to be matched against.
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching locations
     */
    fun matchesLocation(expectedLocation: Location?): Matcher<Location> {
        return object : BaseMatcher<Location>() {
            override fun matches(o: Any): Boolean {
                val actualLocation = o as? Location

                return expectedLocation?.latitude == actualLocation?.latitude && expectedLocation?.longitude == actualLocation?.longitude
            }

            override fun describeTo(description: Description) {
                description.appendText("Location fields should match")
            }
        }
    }

    /**
     * Matcher for LocationInfo

     * @param expectedLocationInfo The location info to be matched against.
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching locationinfo
     */
    fun matchesLocationInfo(expectedLocationInfo: LocationInfo?): Matcher<LocationInfo> {
        return object : BaseMatcher<LocationInfo>() {
            override fun matches(o: Any?): Boolean {
                val actualLocationInfo = o as? LocationInfo

                return expectedLocationInfo?.latitude == actualLocationInfo?.latitude &&
                        expectedLocationInfo?.longitude == actualLocationInfo?.longitude &&
                        expectedLocationInfo?.country == actualLocationInfo?.country &&
                        expectedLocationInfo?.state == actualLocationInfo?.state &&
                        expectedLocationInfo?.locality == actualLocationInfo?.locality &&
                        expectedLocationInfo?.streetAddress == actualLocationInfo?.streetAddress
            }

            override fun describeTo(description: Description) {
                description.appendText("LocationInfo fields should match")
            }
        }
    }
}
