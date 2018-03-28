//
// AboutAppViewModelTest.java
// app
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.activity.viewmodel.dashboard

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.respiratoryapp.common.DateTimeLocalization
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate

/**
 * This class defines unit tests for the AboutAppViewModel.
 */
class AboutAppViewModelTest {

    private lateinit var dependencyProvider: DependencyProvider
    private lateinit var aboutAppViewModelEvents: AboutAppViewModel.Events
    private lateinit var localizationService: LocalizationService
    private lateinit var dateTimeLocalization: DateTimeLocalization
    private val mockReleaseDate = LocalDate.of(2017, 1, 1)
    private val mockReleaseDisplayDate = "Jan 1, 2017"

    @Before
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun setup() {
        dependencyProvider = DependencyProvider.default
        aboutAppViewModelEvents = mock()
        dependencyProvider.register(AboutAppViewModel.Events::class, aboutAppViewModelEvents)
        localizationService = mock()
        dependencyProvider.register(LocalizationService::class, localizationService)
        dateTimeLocalization = mock()
        whenever(dateTimeLocalization.toShortMonthDayYear(mockReleaseDate)).thenReturn(mockReleaseDisplayDate)
        dependencyProvider.register(DateTimeLocalization::class, dateTimeLocalization)
    }

    @Test
    fun testAboutAppViewModelInvokesMethodOnEventsInterfaceWhenInstructionsForUseIsClicked() {
        // create the view model and simulate a click on
        // "Instructions for use".
        val aboutAppViewModel = AboutAppViewModel(dependencyProvider)
        aboutAppViewModel.onClickInstructionsForUse()

        // verify that the method on the events interface is invoked.
        verify(aboutAppViewModelEvents).onInstructionsForUseClick()
    }
}
