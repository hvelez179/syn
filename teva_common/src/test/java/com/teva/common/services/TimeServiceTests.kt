///
// TimeServiceTests.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.common.services

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

/**
 * This class defines unit tests for the time service class.
 */
class TimeServiceTests {

    private lateinit var dependencyProvider: DependencyProvider
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var messenger: Messenger
    private var zoneId = ZoneOffset.ofHoursMinutes(3, 30)

    private val MINUTES_PER_DAY = (60 * 24).toLong()
    private val MEDIUM_HYPER_FACTOR = 10
    private val FAST_HYPER_FACTOR = 4
    private val HYPER_HYPER_FACTOR = 0.5

    @SuppressLint("CommitPrefEdits")
    @Before
    fun setup() {
        DependencyProvider.default.unregisterAll()

        dependencyProvider = DependencyProvider.default

        dependencyProvider.register(ZoneId::class, zoneId)

        sharedPreferences = mock()
        editor = mock()

        whenever(sharedPreferences.edit()).thenReturn(editor)
        dependencyProvider.register(SharedPreferences::class, sharedPreferences, "TimeService")
        messenger = mock()
        dependencyProvider.register(Messenger::class, messenger)
    }

    @Test
    fun testGetTimeFromRealTimeIntervalReturnsCorrectTime() {
        val realTimeInterval = 1
        val mediumTimeInterval = 3600
        val fastTimeInterval = 3600
        val hyperTimeInterval = 3600
        val longTime = -3000000

        val currentTime = 1492433221740L
        val now = Instant.ofEpochMilli(currentTime)
        dependencyProvider.register(now)

        val timeService = TimeServiceImpl(dependencyProvider)
        timeService.initializeTimeService(RunModes.REALTIME)

        val instant1 = timeService.getTimeFromRealTimeInterval(realTimeInterval)
        assertTrue(timeService.timeMode === RunModes.REALTIME)
        timeService.initializeTimeService(RunModes.MEDIUM)
        val instant2 = timeService.getTimeFromRealTimeInterval(mediumTimeInterval)
        assertTrue(timeService.timeMode === RunModes.MEDIUM)
        timeService.initializeTimeService(RunModes.FAST)
        val instant3 = timeService.getTimeFromRealTimeInterval(fastTimeInterval)
        assertTrue(timeService.timeMode === RunModes.FAST)
        timeService.initializeTimeService(RunModes.HYPER)
        val instant4 = timeService.getTimeFromRealTimeInterval(hyperTimeInterval)
        assertTrue(timeService.timeMode === RunModes.HYPER)
        val instant5 = timeService.getTimeFromRealTimeInterval(longTime)

        assertTrue(instant2.isAfter(instant1))
        assertTrue(instant3.isAfter(instant2))
        assertTrue(instant4.isAfter(instant3))

        assertTrue(instant5.isBefore(instant1))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testNowInRealTimeModeReturnsCorrectTime() {
        val currentTime = 1492433221740L
        val timeAfterOneSecond = 1492433222740L

        val now = Instant.ofEpochMilli(currentTime)
        dependencyProvider.register(now)

        val afterOneSecond = Instant.ofEpochMilli(timeAfterOneSecond)

        val timeService = TimeServiceImpl(dependencyProvider)
        timeService.initializeTimeService(RunModes.REALTIME)
        val instant1 = timeService.now()

        // simulate a one second time interval
        dependencyProvider.register(afterOneSecond)
        val instant2 = timeService.now()
        val expectedDifferenceInTimes: Long = 1
        val timeDifferenceInSeconds = instant2.epochSecond - instant1.epochSecond
        assertEquals(expectedDifferenceInTimes, timeDifferenceInSeconds)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testNowInMediumTimeModeReturnsCorrectTime() {
        val currentTime = 1492433221740L
        val timeAfterOneSecond = 1492433222740L
        val mediumTimeAfterOneSecond = 1492433365740L

        val now = Instant.ofEpochMilli(currentTime)
        dependencyProvider.register(now)

        val afterOneSecond = Instant.ofEpochMilli(timeAfterOneSecond)
        val mediumAfterOneSecond = Instant.ofEpochMilli(mediumTimeAfterOneSecond)

        val timeService = TimeServiceImpl(dependencyProvider)
        timeService.initializeTimeService(RunModes.MEDIUM)
        val instant1 = timeService.now()

        //simulate a one second time interval
        dependencyProvider.register(afterOneSecond)

        val instant2 = timeService.now()
        val expectedDifferenceInTimes = MINUTES_PER_DAY / MEDIUM_HYPER_FACTOR
        val timeDifferenceInSeconds = instant2.epochSecond - instant1.epochSecond
        assertEquals(expectedDifferenceInTimes, timeDifferenceInSeconds)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testNowInFastTimeModeReturnsCorrectTime() {
        val currentTime = 1492433221740L
        val timeAfterOneSecond = 1492433222740L
        val fastTimeAfterOneSecond = 1492433581740L

        val now = Instant.ofEpochMilli(currentTime)
        dependencyProvider.register(now)

        val afterOneSecond = Instant.ofEpochMilli(timeAfterOneSecond)
        val fastAfterOneSecond = Instant.ofEpochMilli(fastTimeAfterOneSecond)

        val timeService = TimeServiceImpl(dependencyProvider)
        timeService.initializeTimeService(RunModes.FAST)
        val instant1 = timeService.now()
        // simulate a one second time interval
        dependencyProvider.register(afterOneSecond)

        val instant2 = timeService.now()
        val expectedDifferenceInTimes = MINUTES_PER_DAY / FAST_HYPER_FACTOR
        val timeDifferenceInSeconds = instant2.epochSecond - instant1.epochSecond
        assertEquals(expectedDifferenceInTimes, timeDifferenceInSeconds)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testNowInHyperTimeModeReturnsCorrectTime() {
        val currentTime = 1492433221740L
        val timeAfterOneSecond = 1492433222740L
        val hyperTimeAfterOneSecond = 1492436101740L

        val now = Instant.ofEpochMilli(currentTime)
        dependencyProvider.register(now)

        val afterOneSecond = Instant.ofEpochMilli(timeAfterOneSecond)

        val hyperAfterOneSecond = Instant.ofEpochMilli(hyperTimeAfterOneSecond)

        val timeService = TimeServiceImpl(dependencyProvider)
        timeService.initializeTimeService(RunModes.HYPER)
        val instant1 = timeService.now()

        // simulate a one second time interval
        dependencyProvider.register(afterOneSecond)

        val instant2 = timeService.now()
        val expectedDifferenceInTimes = (MINUTES_PER_DAY / HYPER_HYPER_FACTOR).toInt().toLong()
        val timeDifferenceInSeconds = instant2.epochSecond - instant1.epochSecond
        assertEquals(expectedDifferenceInTimes, timeDifferenceInSeconds)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testTodayInHyperTimeModeReturnsCorrectDate() {
        val currentTime = 1492433221740L
        val timeAfterThirtySeconds = 1492433251740L
        val hyperTimeAfterThirtySeconds = 1492519621740L

        val now = Instant.ofEpochMilli(currentTime)
        dependencyProvider.register(now)

        val afterThirtySeconds = Instant.ofEpochMilli(timeAfterThirtySeconds)
        val hyperAfterThirtySeconds = Instant.ofEpochMilli(hyperTimeAfterThirtySeconds)


        val timeService = TimeServiceImpl(dependencyProvider)
        timeService.initializeTimeService(RunModes.HYPER)

        val date1 = timeService.today()

        //simulate a 30 second time interval(one day in hyper time)
        dependencyProvider.register(afterThirtySeconds)

        val date2 = timeService.today()
        val period = date1.until(date2)
        val minimumExpectedDays: Long = 1
        assertTrue(period.days >= minimumExpectedDays)
    }

    @Test
    fun testReferenceTimeAndReferenceHyperTimeAreRetrievedCorrectlyFromSettings() {

        val currentTime = 1492433221740L
        val now = Instant.ofEpochMilli(currentTime)
        dependencyProvider.register(now)

        val referenceTime = Instant.from(ZonedDateTime.of(2017, 12, 1, 11, 10, 9, 0, ZoneId.of("GMT")))
        val referenceTimeEpochMillSeconds = referenceTime.toEpochMilli()

        whenever(sharedPreferences.getLong(any(), any())).thenReturn(referenceTimeEpochMillSeconds)
        dependencyProvider.register(SharedPreferences::class, sharedPreferences, "TimeService")

        val timeService = TimeServiceImpl(dependencyProvider)
        timeService.initializeTimeService(RunModes.REALTIME)

        assertTrue(timeService.referenceTime == referenceTime)
        assertTrue(timeService.referenceHypertime == referenceTime)
    }

    @Test
    fun testTimeConversionInRealTimeModeReturnsCorrectTime() {
        val currentTime = 1492433221740L
        val timeAfterTenSeconds = 1492433231740L

        val now = Instant.ofEpochMilli(currentTime)
        dependencyProvider.register(now)

        val afterTenSeconds = Instant.ofEpochMilli(timeAfterTenSeconds)

        val timeService = TimeServiceImpl(dependencyProvider)
        timeService.initializeTimeService(RunModes.REALTIME)

        dependencyProvider.register(now)

        val instant1 = now.plusSeconds(1000)
        val instant2 = timeService.getRealTimeFromDate(instant1)
        val instant3 = timeService.getApplicationTime(instant2)
        assertEquals(instant1.epochSecond, instant3.epochSecond)
    }

    @Test
    fun testTimeConversionInMediumTimeModeReturnsCorrectTime() {
        val currentTime = 1492433221740L
        val timeAfterTenSeconds = 1492433231740L

        val now = Instant.ofEpochMilli(currentTime)
        dependencyProvider.register(now)

        val afterTenSeconds = Instant.ofEpochMilli(timeAfterTenSeconds)

        val timeService = TimeServiceImpl(dependencyProvider)
        timeService.initializeTimeService(RunModes.MEDIUM)
        dependencyProvider.register(afterTenSeconds)

        val instant1 = now.plusSeconds(1000)
        val instant2 = timeService.getRealTimeFromDate(instant1)
        val instant3 = timeService.getApplicationTime(instant2)
        assertEquals(instant1.epochSecond, instant3.epochSecond)
    }

    @Test
    fun testTimeConversionInFastTimeModeReturnsCorrectTime() {
        val currentTime = 1492433221740L
        val timeAfterTenSeconds = 1492433231740L

        val now = Instant.ofEpochMilli(currentTime)
        dependencyProvider.register(now)

        val afterTenSeconds = Instant.ofEpochMilli(timeAfterTenSeconds)

        val timeService = TimeServiceImpl(dependencyProvider)
        timeService.initializeTimeService(RunModes.FAST)
        dependencyProvider.register(afterTenSeconds)

        val instant1 = now.plusSeconds(1000)
        val instant2 = timeService.getRealTimeFromDate(instant1)
        val instant3 = timeService.getApplicationTime(instant2)
        assertEquals(instant1.epochSecond, instant3.epochSecond)
    }

    @Test
    fun testTimeConversionInHyperTimeModeReturnsCorrectTime() {
        val currentTime = 1492433221740L
        val timeAfterTenSeconds = 1492433231740L

        val now = Instant.ofEpochMilli(currentTime)
        dependencyProvider.register(now)

        val afterTenSeconds = Instant.ofEpochMilli(timeAfterTenSeconds)

        val timeService = TimeServiceImpl(dependencyProvider)
        timeService.initializeTimeService(RunModes.HYPER)
        dependencyProvider.register(afterTenSeconds)

        val instant1 = now.plusSeconds(1000)
        val instant2 = timeService.getRealTimeFromDate(instant1)
        val instant3 = timeService.getApplicationTime(instant2)
        assertEquals(instant1.epochSecond, instant3.epochSecond)
    }

    @Test
    fun testTimeZoneOffsetInMinutesReturnsCorrectTimeZoneOffsetValue() {
        val currentTime = 1492433221740L
        val now = Instant.ofEpochMilli(currentTime)
        dependencyProvider.register(now)

        val expectedOffsetHours = 3
        val expectedOffsetMinutes = 30
        val timeService = TimeServiceImpl(dependencyProvider)
        timeService.initializeTimeService(RunModes.REALTIME)

        val currentTimeZoneOffsetInMinutes = timeService.timezoneOffsetMinutes
        assertEquals((expectedOffsetHours * 60 + expectedOffsetMinutes).toLong(), currentTimeZoneOffsetInMinutes.toLong())
    }
}
