//
// DailyAirQuality.kt
// teva_environment
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.environment.entities

import com.teva.common.entities.TrackedModelObject
import com.teva.environment.enumerations.AirQuality

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

/**
 * This class provides daily air quality information.
 *
 * @property airQualityIndex A value indicating current air quality.
 *                           Range is [0..500] Low value is good, high value is poor.
 * @property airQuality The AirQuality enum, mapped from the airQualityIndex
 * @property date The date associated with the Daily Air Quality.
 *                It is used as a key to save, and lookup the DailyAirQuality in the database and cloud.
 */
class DailyAirQuality(
        var airQualityIndex: Int = 0,
        var airQuality: AirQuality = AirQuality.UNKNOWN,
        var date: LocalDate? = null) : TrackedModelObject()
