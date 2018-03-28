//
// LocationInfo.kt
// teva_location
//
// Copyright (c) 2017 Teva. All rights reserved.
//
package com.teva.location.services

/**
 * This class holds information related to a location.
 *
 * @property latitude - latitude of the location.
 * @property longitude - longitude of the location.
 * @property streetAddress - street address of the location.
 * @property locality - locality to which the location belongs.
 * @property state - state to which the location belongs.
 * @property country - country to which the location belongs.
 */
class LocationInfo(
        val latitude: Double,
        val longitude: Double,
        val streetAddress: String,
        val locality: String,
        val state: String,
        val country: String)
