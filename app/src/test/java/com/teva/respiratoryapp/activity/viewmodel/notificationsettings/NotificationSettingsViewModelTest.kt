//
// NotificationSettingsViewModelTest.java
// app
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.activity.viewmodel.notificationsettings

import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.environment.models.DailyEnvironmentalReminderManager
import com.teva.notifications.entities.ReminderSetting
import com.teva.respiratoryapp.mocks.MockedLocalizationService
import com.teva.respiratoryapp.testutils.BaseTest
import com.teva.userfeedback.model.DailyAssessmentReminderManager
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * This class defines unit tests for the NotificationSettingsViewModel class.
 */
class NotificationSettingsViewModelTest : BaseTest() {

    private lateinit var dependencyProvider: DependencyProvider
    private lateinit var dailyAssessmentReminderManager: DailyAssessmentReminderManager
    private lateinit var dailyEnvironmentalReminderManager: DailyEnvironmentalReminderManager
    private lateinit var localizationService: MockedLocalizationService

    private lateinit var selfAssessmentReminderSetting: ReminderSetting
    private lateinit var environmentReminderSetting: ReminderSetting

    private val selfAssessmentEnabled = true
    private val environmentEnabled = false

    @Before
    fun setup() {
        dependencyProvider = DependencyProvider.default

        dailyAssessmentReminderManager = mock()
        dailyEnvironmentalReminderManager = mock()

        selfAssessmentReminderSetting = ReminderSetting()
        environmentReminderSetting = ReminderSetting()
        selfAssessmentReminderSetting.isEnabled = selfAssessmentEnabled
        environmentReminderSetting.isEnabled = environmentEnabled

        whenever(dailyAssessmentReminderManager.reminderSetting).thenReturn(selfAssessmentReminderSetting)
        whenever(dailyEnvironmentalReminderManager.reminderSetting).thenReturn(environmentReminderSetting)

        dependencyProvider.register(DailyAssessmentReminderManager::class, dailyAssessmentReminderManager)
        dependencyProvider.register(DailyEnvironmentalReminderManager::class, dailyEnvironmentalReminderManager)

        localizationService = MockedLocalizationService()
        dependencyProvider.register(LocalizationService::class, localizationService)
    }

    @Test
    fun testNotificationSettingsViewModelSetsTheDatabaseValuesForSettingsWhenStarted() {
        val notificationSettingsViewModel = NotificationSettingsViewModel(dependencyProvider)
        notificationSettingsViewModel.onStart()
        assertEquals(environmentEnabled, notificationSettingsViewModel.environmentNotificationEnabled.get())
        assertEquals(selfAssessmentEnabled, notificationSettingsViewModel.selfAssessmentNotificationEnabled.get())
    }

    @Test
    fun testNotificationSettingsViewModelEnablesOrDisablesSettingsOnManagersWhenMemberValuesAreChanged() {
        val notificationSettingsViewModel = NotificationSettingsViewModel(dependencyProvider)
        notificationSettingsViewModel.onStart()
        notificationSettingsViewModel.environmentNotificationEnabled.set(true)
        verify(dailyEnvironmentalReminderManager).enableReminder(eq(true))
        notificationSettingsViewModel.selfAssessmentNotificationEnabled.set(false)
        verify(dailyAssessmentReminderManager).enableReminder(eq(false))
    }
}
