///
// ScanIntroViewModelTest.java
// app
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.respiratoryapp.activity.viewmodel.device

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.common.utilities.Messenger
import com.teva.respiratoryapp.activity.model.InhalerRegistrationCommonState
import com.teva.respiratoryapp.mocks.MockedLocalizationService
import com.teva.respiratoryapp.testutils.BaseTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ScanIntroViewModelTest : BaseTest() {
    private lateinit var commonState: InhalerRegistrationCommonState
    private lateinit var messenger: Messenger
    private lateinit var localizationService: MockedLocalizationService

    private lateinit var dependencyProvider: DependencyProvider

    @Before
    @Throws(Exception::class)
    fun setUp() {
        dependencyProvider = DependencyProvider.default

        messenger = mock()
        dependencyProvider.register(Messenger::class, messenger)

        // initialize common state
        commonState = mock()
        dependencyProvider.register(InhalerRegistrationCommonState::class, commonState)

        localizationService = MockedLocalizationService()
        dependencyProvider.register(LocalizationService::class, localizationService)
    }

    @Test
    fun testViewModelListensForDeviceLoadedEventAndUpdatesHasDeviceProperty() {
        val viewmodel = ScanIntroViewModel(dependencyProvider)

        whenever(commonState.isDeviceLoaded).thenReturn(false)
        viewmodel.onStart()

        verify(commonState).addListener(viewmodel)
        assertFalse(viewmodel.hasDevice)

        whenever(commonState.isDeviceLoaded).thenReturn(true)
        viewmodel.onDeviceLoaded()
        assertTrue(viewmodel.hasDevice)
    }

    @Test
    fun testOnStopRemovesInhalerRegistrationListener() {
        val viewmodel = ScanIntroViewModel(dependencyProvider)
        viewmodel.onStart()

        viewmodel.onStop()
        verify(commonState).removeListener(viewmodel)
    }

    @Test
    fun testStartScanningHandlerFiresOnStartScanEvent() {
        val events: ScanIntroViewModel.IntroEvents = mock()
        dependencyProvider.register(ScanIntroViewModel.IntroEvents::class, events)

        val viewmodel = ScanIntroViewModel(dependencyProvider)
        viewmodel.startScanning()

        verify(events).onStartScan()
    }

}