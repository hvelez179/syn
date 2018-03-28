///
///
// DailyReportItemViewModelTest.java
// app
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.respiratoryapp.activity.viewmodel.tracker

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.teva.analysis.extensions.enumerations.InhalationEffort
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.common.DateTimeLocalization
import com.teva.respiratoryapp.mocks.MockedLocalizationService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.devices.entities.Device
import com.teva.devices.entities.InhaleEvent
import com.teva.devices.enumerations.InhaleStatus
import com.teva.respiratoryapp.testutils.BaseTest
import com.teva.userfeedback.enumerations.UserFeeling

import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class DailyReportItemViewModelTest : BaseTest() {

    private lateinit var dependencyProvider: DependencyProvider
    private lateinit var device: Device
    private lateinit var inhaleEvent: InhaleEvent

    private val systemDefaultZoneId = ZoneOffset.ofTotalSeconds(0)

    @Before
    fun setup() {
        dependencyProvider = DependencyProvider.default
        dependencyProvider.unregisterAll()

        dependencyProvider.register(ZoneId::class, systemDefaultZoneId)

        val localizationService = MockedLocalizationService()
        localizationService.add(R.string.trackerDoseStatusOk_text, GOOD_INHALATION_STATUS)
        localizationService.add(R.string.trackerDoseStatusNoInhalation_text, NO_INHALATION_STATUS)
        localizationService.add(R.string.trackerDoseStatusExhalation_text, EXHALATION_STATUS)
        localizationService.add(R.string.trackerDoseStatusVentBlocked_text, ERROR_STATUS)
        localizationService.add(R.string.trackerDoseStatusError_text, SYSTEM_ERROR_STATUS)
        localizationService.add(R.string.commaSpace_text, separator)

        dependencyProvider.register(LocalizationService::class, localizationService)

        device = Device()
        device.nickname = DEVICE_NICKNAME
        device.serialNumber = DEVICE_SERIAL_NUMBER

        inhaleEvent = InhaleEvent()
        inhaleEvent.isValidInhale = true
        inhaleEvent.inhalePeak = GOOD_PEAK_FLOW

        val zonedDateTime = ZonedDateTime.of(LOCAL_EVENT_TIME, systemDefaultZoneId)
        val zoneOffset = ZoneOffset.from(zonedDateTime)
        val zoneOffsetMinutes = zoneOffset.totalSeconds / SECONDS_PER_MINUTES

        inhaleEvent.eventTime = zonedDateTime.toInstant()
        inhaleEvent.timezoneOffsetMinutes = zoneOffsetMinutes

        val dateTimeLocalization: DateTimeLocalization = mock()
        whenever(dateTimeLocalization.toShortTime(LOCAL_EVENT_TIME.toLocalTime())).thenReturn(FORMATTED_LOCAL_TIME)

        dependencyProvider.register(DateTimeLocalization::class, dateTimeLocalization)
    }

    private fun verifyInhaleEventItemProperties(viewModel: DailyReportItemViewModel,
                                                inhalationEffort: InhalationEffort,
                                                statusText: String) {

        // verify viewmodel properties match expected values
        assertTrue(viewModel.isEvent)
        assertEquals(LOCAL_EVENT_TIME.toLocalTime(), viewModel.time)
        assertEquals(FORMATTED_LOCAL_TIME, viewModel.formattedTime)
        assertEquals(DEVICE_NICKNAME, viewModel.inhalerName)
        assertEquals(DEVICE_SERIAL_NUMBER, viewModel.serialNumber)
        assertEquals(inhalationEffort, viewModel.inhalationEffort)
        assertEquals(statusText, viewModel.status)
    }

    @Test
    fun testThatInhaleEventConstructorWithGoodInhalationSetsPropertiesCorrectly() {
        val viewModel = DailyReportItemViewModel(dependencyProvider, inhaleEvent, device)

        verifyInhaleEventItemProperties(viewModel, InhalationEffort.GOOD_INHALATION, GOOD_INHALATION_STATUS)
    }

    @Test
    fun testThatInhaleEventConstructorWithLowInhalationSetsPropertiesCorrectly() {
        inhaleEvent.inhalePeak = LOW_PEAK_FLOW

        val viewModel = DailyReportItemViewModel(dependencyProvider, inhaleEvent, device)

        verifyInhaleEventItemProperties(viewModel, InhalationEffort.LOW_INHALATION, LOW_INHALATION_STATUS)
    }

    @Test
    fun testThatInhaleEventConstructorWithNoInhalationSetsPropertiesCorrectly() {
        inhaleEvent.inhalePeak = NO_PEAK_FLOW

        val viewModel = DailyReportItemViewModel(dependencyProvider, inhaleEvent, device)

        verifyInhaleEventItemProperties(viewModel, InhalationEffort.NO_INHALATION, NO_INHALATION_STATUS)
    }

    @Test
    fun testThatInhaleEventConstructorWithExhalationSetsPropertiesCorrectly() {
        inhaleEvent.inhalePeak = NO_PEAK_FLOW
        inhaleEvent.isValidInhale = false
        inhaleEvent.status = InhaleStatus.InhaleStatusFlag.INHALE_STATUS_UNEXPECTED_EXHALATION.inhaleStatusFlagValue

        val viewModel = DailyReportItemViewModel(dependencyProvider, inhaleEvent, device)

        verifyInhaleEventItemProperties(viewModel, InhalationEffort.EXHALATION, EXHALATION_STATUS)
    }

    @Test
    fun testThatInhaleEventConstructorWithErrorSetsPropertiesCorrectly() {
        inhaleEvent.inhalePeak = HIGH_PEAK_FLOW

        val viewModel = DailyReportItemViewModel(dependencyProvider, inhaleEvent, device)

        verifyInhaleEventItemProperties(viewModel, InhalationEffort.ERROR, ERROR_STATUS)
    }

    @Test
    fun testThatInhaleEventConstructorWithSystemErrorSetsPropertiesCorrectly() {
        inhaleEvent.isValidInhale = false
        inhaleEvent.status = SYSTEM_ERRORS

        val viewModel = DailyReportItemViewModel(dependencyProvider, inhaleEvent, device)

        verifyInhaleEventItemProperties(viewModel, InhalationEffort.SYSTEM_ERROR, SYSTEM_ERROR_STATUS_WITHOUT_CODES + SYSTEM_ERROR_CODES)
    }

    companion object {
        private val SECONDS_PER_MINUTES = 60

        private val DEVICE_NICKNAME = "Nickname"
        private val DEVICE_SERIAL_NUMBER = "12345678901"
        private val USER_FEELING = UserFeeling.POOR
        private val NO_PEAK_FLOW = 100
        private val LOW_PEAK_FLOW = 400
        private val GOOD_PEAK_FLOW = 460
        private val HIGH_PEAK_FLOW = 2100
        private val SYSTEM_ERRORS = 82 // BAD_DATA | TIMESTAMP_ERROR | PARAMETER_ERROR

        private val GOOD_INHALATION_STATUS = "Good Inhalation"
        private val LOW_INHALATION_STATUS = "Good Inhalation"
        private val NO_INHALATION_STATUS = "No Inhalation"
        private val EXHALATION_STATUS = "Exhalation"
        private val ERROR_STATUS = "Error"
        private val SYSTEM_ERROR_STATUS = "System Error \$ErrorCodes$"
        private val SYSTEM_ERROR_STATUS_WITHOUT_CODES = "System Error"
        private val SYSTEM_ERROR_CODES = " 1, 2, 3"
        private val separator = ", "

        private val LOCAL_EVENT_TIME = LocalDateTime.of(2017, 2, 17, 10, 25)
        private val FORMATTED_LOCAL_TIME = "10:25 AM"
    }
}