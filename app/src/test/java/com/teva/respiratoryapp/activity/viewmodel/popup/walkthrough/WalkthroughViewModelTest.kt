//
// WalkthroughViewModelTest.java
// app
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.activity.viewmodel.popup.walkthrough

import com.nhaarman.mockito_kotlin.*
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.respiratoryapp.activity.view.popup.walkthrough.Walkthrough
import com.teva.respiratoryapp.activity.viewmodel.popup.PopupDashboardButton
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.*

/**
 * This class defines unit tests for the WalkthroughViewModel class.
 */

class WalkthroughViewModelTest {

    private lateinit var dependencyProvider: DependencyProvider
    private lateinit var localizationService: LocalizationService
    private lateinit var walkthroughViewModelEvents: WalkthroughViewModel.Events
    private lateinit var navigationEvents: FragmentViewModel.NavigationEvents

    private inner class WalkthroughProperties {
        internal var backButtonVisible: Boolean = false
        internal var nextButtonVisible: Boolean = false
        internal var closeButtonVisible: Boolean = false
        internal var hyperlinkVisible: Boolean = false
        internal var contentButtonVisible: Boolean = false
        internal var nextScreen: Walkthrough? = null
        internal var dashboardButton: PopupDashboardButton? = null
    }

    /**
     * Sets up the test pre-requisites
     */
    @Before
    fun setup() {
        dependencyProvider = DependencyProvider.default
        localizationService = mock()
        whenever(localizationService.getString(any<Int>())).thenReturn("SomeText")
        dependencyProvider.register(LocalizationService::class, localizationService)
        walkthroughViewModelEvents = mock()
        dependencyProvider.register(WalkthroughViewModel.Events::class, walkthroughViewModelEvents)
        navigationEvents = mock()
        dependencyProvider.register(FragmentViewModel.NavigationEvents::class, navigationEvents)
    }

    @Test
    fun testInitializingWalkthroughViewModelInWalkthroughModeInitializesScreenCorrectly() {

        // create expected properties for each screen in the walkthrough mode
        val expectedPropertiesMap = initWalkthroughDataForWalkThroughMode()

        for (walkthrough in Walkthrough.values()) {

            // verify for each screen that the properties match.
            val walkthroughViewModel = WalkthroughViewModel(dependencyProvider, walkthrough, false)
            val expectedScreenProperties = expectedPropertiesMap[walkthrough]!!
            assertEquals(expectedScreenProperties.backButtonVisible, walkthroughViewModel.isBackButtonVisible)
            assertEquals(expectedScreenProperties.closeButtonVisible, walkthroughViewModel.isCloseButtonVisible)
            assertEquals(expectedScreenProperties.contentButtonVisible, walkthroughViewModel.isButtonVisible)
            assertEquals(expectedScreenProperties.hyperlinkVisible, walkthroughViewModel.isHyperlinkVisible)
            assertEquals(expectedScreenProperties.nextButtonVisible, walkthroughViewModel.isNextButtonVisible)
            assertEquals(expectedScreenProperties.nextScreen, walkthroughViewModel.nextScreen)
        }
    }

    @Test
    fun testInitializingWalkthroughViewModelInHowToUseTheAppModeInitializesScreenCorrectly() {

        // create expected properties for each screen in the how to use the app mode
        val expectedPropertiesMap = initWalkthroughDataForHowToUseTheAppMode()

        for (walkthrough in Walkthrough.values()) {

            // verify for each screen that the properties match.
            if (walkthrough == Walkthrough.WELCOME || walkthrough == Walkthrough.GET_STARTED) {
                continue
            }

            val walkthroughViewModel = WalkthroughViewModel(dependencyProvider, walkthrough, true)
            val expectedScreenProperties = expectedPropertiesMap[walkthrough]!!
            assertEquals(expectedScreenProperties.backButtonVisible, walkthroughViewModel.isBackButtonVisible)
            assertEquals(expectedScreenProperties.closeButtonVisible, walkthroughViewModel.isCloseButtonVisible)
            assertEquals(expectedScreenProperties.contentButtonVisible, walkthroughViewModel.isButtonVisible)
            assertEquals(expectedScreenProperties.hyperlinkVisible, walkthroughViewModel.isHyperlinkVisible)
            assertEquals(expectedScreenProperties.nextButtonVisible, walkthroughViewModel.isNextButtonVisible)
            assertEquals(expectedScreenProperties.nextScreen, walkthroughViewModel.nextScreen)
        }
    }

    @Test
    fun testNextButtonClickRaisesOnNextEventOnTheEventsInterfaceWithCorrectNextScreenType() {

        // iterate through the screens
        for (walkthrough in Walkthrough.values()) {

            if (walkthrough == Walkthrough.GET_STARTED) {
                continue
            }

            // for each screen verify that clicking the next button launches the correct screen
            val walkthroughViewModel = WalkthroughViewModel(dependencyProvider, walkthrough, false)
            walkthroughViewModel.onNextButtonClicked()
            val walkthroughArgumentCaptor = argumentCaptor<Walkthrough>()
            verify(walkthroughViewModelEvents).onNext(walkthroughArgumentCaptor.capture())
            assertEquals(walkthroughViewModel.nextScreen, walkthroughArgumentCaptor.lastValue)
            Mockito.reset(walkthroughViewModelEvents)
        }
    }

    @Test
    fun testScanInhalerClickRaisesOnScanInhalerEventOnTheEventsInterface() {
        val walkthroughViewModel = WalkthroughViewModel(dependencyProvider, Walkthrough.GET_STARTED, true)
        walkthroughViewModel.onButton()
        verify(walkthroughViewModelEvents).onScanInhaler()
    }

    @Test
    fun testScanInhalerLaterClickRaisesOnDoneEventOnTheEventsInterface() {
        val walkthroughViewModel = WalkthroughViewModel(dependencyProvider, Walkthrough.GET_STARTED, true)
        walkthroughViewModel.onHyperlink()
        verify(walkthroughViewModelEvents).onDone()
    }

    @Test
    fun testCloseButtonClickRaisesOnDoneEventOnTheEventsInterface() {
        val walkthroughViewModel = WalkthroughViewModel(dependencyProvider, Walkthrough.GET_STARTED, true)
        walkthroughViewModel.onClose()
        verify(walkthroughViewModelEvents).onDone()
    }

    @Test
    fun testBackButtonClickRaisesOnBackButtonPressedEventOnTheNavigationEventsInterface() {
        val walkthroughViewModel = WalkthroughViewModel(dependencyProvider, Walkthrough.INHALER_READY, true)
        walkthroughViewModel.onBackButtonClicked()
        verify(navigationEvents).onBackPressed()
    }

    @Test
    fun testBackPressDoesNotRaiseOnBackButtonPressedEventForTheGetStartedScreen() {
        val walkthroughViewModel = WalkthroughViewModel(dependencyProvider, Walkthrough.GET_STARTED, true)
        walkthroughViewModel.onBackPressed()
        verify(navigationEvents, never()).onBackPressed()
    }

    /**
     * This method creates a map of expected properties for each screen in the walk through mode.

     * @return - a map of expected properties for each screen
     */
    private fun initWalkthroughDataForWalkThroughMode(): Map<Walkthrough, WalkthroughProperties> {
        val walkthroughPropertiesMap = HashMap<Walkthrough, WalkthroughProperties>()

        val welcomeScreenProperties = WalkthroughProperties()
        welcomeScreenProperties.backButtonVisible = false
        welcomeScreenProperties.nextButtonVisible = true
        welcomeScreenProperties.closeButtonVisible = false
        welcomeScreenProperties.contentButtonVisible = false
        welcomeScreenProperties.hyperlinkVisible = false
        welcomeScreenProperties.nextScreen = Walkthrough.INHALER_READY
        welcomeScreenProperties.dashboardButton = PopupDashboardButton.NONE

        walkthroughPropertiesMap.put(Walkthrough.WELCOME, welcomeScreenProperties)

        val inhalerReadyScreenProperties = WalkthroughProperties()
        inhalerReadyScreenProperties.backButtonVisible = true
        inhalerReadyScreenProperties.nextButtonVisible = true
        inhalerReadyScreenProperties.closeButtonVisible = false
        inhalerReadyScreenProperties.contentButtonVisible = false
        inhalerReadyScreenProperties.hyperlinkVisible = false
        inhalerReadyScreenProperties.nextScreen = Walkthrough.INHALE_EVENTS
        inhalerReadyScreenProperties.dashboardButton = PopupDashboardButton.NONE

        walkthroughPropertiesMap.put(Walkthrough.INHALER_READY, inhalerReadyScreenProperties)

        val inhaleEvents1ScreenProperties = WalkthroughProperties()
        inhaleEvents1ScreenProperties.backButtonVisible = true
        inhaleEvents1ScreenProperties.nextButtonVisible = true
        inhaleEvents1ScreenProperties.closeButtonVisible = false
        inhaleEvents1ScreenProperties.contentButtonVisible = false
        inhaleEvents1ScreenProperties.hyperlinkVisible = false
        inhaleEvents1ScreenProperties.nextScreen = Walkthrough.ENVIRONMENT
        inhaleEvents1ScreenProperties.dashboardButton = PopupDashboardButton.EVENTS

        walkthroughPropertiesMap.put(Walkthrough.INHALE_EVENTS, inhaleEvents1ScreenProperties)

        val environmentScreenProperties = WalkthroughProperties()
        environmentScreenProperties.backButtonVisible = true
        environmentScreenProperties.nextButtonVisible = true
        environmentScreenProperties.closeButtonVisible = false
        environmentScreenProperties.contentButtonVisible = false
        environmentScreenProperties.hyperlinkVisible = false
        environmentScreenProperties.nextScreen = Walkthrough.DEVICES_1
        environmentScreenProperties.dashboardButton = PopupDashboardButton.ENVIRONMENT

        walkthroughPropertiesMap.put(Walkthrough.ENVIRONMENT, environmentScreenProperties)

        val devices1ScreenProperties = WalkthroughProperties()
        devices1ScreenProperties.backButtonVisible = true
        devices1ScreenProperties.nextButtonVisible = true
        devices1ScreenProperties.closeButtonVisible = false
        devices1ScreenProperties.contentButtonVisible = false
        devices1ScreenProperties.hyperlinkVisible = false
        devices1ScreenProperties.nextScreen = Walkthrough.DEVICES_2
        devices1ScreenProperties.dashboardButton = PopupDashboardButton.DEVICES

        walkthroughPropertiesMap.put(Walkthrough.DEVICES_1, devices1ScreenProperties)

        val devices2ScreenProperties = WalkthroughProperties()
        devices2ScreenProperties.backButtonVisible = true
        devices2ScreenProperties.nextButtonVisible = true
        devices2ScreenProperties.closeButtonVisible = false
        devices2ScreenProperties.contentButtonVisible = false
        devices2ScreenProperties.hyperlinkVisible = false
        devices2ScreenProperties.nextScreen = Walkthrough.SUPPORT
        devices2ScreenProperties.dashboardButton = PopupDashboardButton.DEVICES

        walkthroughPropertiesMap.put(Walkthrough.DEVICES_2, devices2ScreenProperties)

        val supportScreenProperties = WalkthroughProperties()
        supportScreenProperties.backButtonVisible = true
        supportScreenProperties.nextButtonVisible = true
        supportScreenProperties.closeButtonVisible = false
        supportScreenProperties.contentButtonVisible = false
        supportScreenProperties.hyperlinkVisible = false
        supportScreenProperties.nextScreen = Walkthrough.DSA
        supportScreenProperties.dashboardButton = PopupDashboardButton.SUPPORT

        walkthroughPropertiesMap.put(Walkthrough.SUPPORT, supportScreenProperties)

        val dsaScreenProperties = WalkthroughProperties()
        dsaScreenProperties.backButtonVisible = true
        dsaScreenProperties.nextButtonVisible = true
        dsaScreenProperties.closeButtonVisible = false
        dsaScreenProperties.contentButtonVisible = false
        dsaScreenProperties.hyperlinkVisible = false
        dsaScreenProperties.nextScreen = Walkthrough.REPORT
        dsaScreenProperties.dashboardButton = PopupDashboardButton.DSA

        walkthroughPropertiesMap.put(Walkthrough.DSA, dsaScreenProperties)

        val reportScreenProperties = WalkthroughProperties()
        reportScreenProperties.backButtonVisible = true
        reportScreenProperties.nextButtonVisible = true
        reportScreenProperties.closeButtonVisible = false
        reportScreenProperties.contentButtonVisible = false
        reportScreenProperties.hyperlinkVisible = false
        reportScreenProperties.nextScreen = Walkthrough.SECURITY
        reportScreenProperties.dashboardButton = PopupDashboardButton.REPORT

        walkthroughPropertiesMap.put(Walkthrough.REPORT, reportScreenProperties)

        val securityScreenProperties = WalkthroughProperties()
        securityScreenProperties.backButtonVisible = true
        securityScreenProperties.nextButtonVisible = true
        securityScreenProperties.closeButtonVisible = false
        securityScreenProperties.contentButtonVisible = false
        securityScreenProperties.hyperlinkVisible = false
        securityScreenProperties.nextScreen = Walkthrough.GET_STARTED
        securityScreenProperties.dashboardButton = PopupDashboardButton.NONE

        walkthroughPropertiesMap.put(Walkthrough.SECURITY, securityScreenProperties)

        val getStartedScreenProperties = WalkthroughProperties()
        getStartedScreenProperties.backButtonVisible = false
        getStartedScreenProperties.nextButtonVisible = false
        getStartedScreenProperties.closeButtonVisible = false
        getStartedScreenProperties.contentButtonVisible = true
        getStartedScreenProperties.hyperlinkVisible = true
        getStartedScreenProperties.nextScreen = null
        getStartedScreenProperties.dashboardButton = PopupDashboardButton.NONE

        walkthroughPropertiesMap.put(Walkthrough.GET_STARTED, getStartedScreenProperties)

        return walkthroughPropertiesMap
    }

    /**
     * This method creates a map of expected properties for each screen in the how to use the app mode.

     * @return - a map of expected properties for each screen
     */
    private fun initWalkthroughDataForHowToUseTheAppMode(): Map<Walkthrough, WalkthroughProperties> {
        val walkthroughPropertiesMap = HashMap<Walkthrough, WalkthroughProperties>()

        val inhalerReadyScreenProperties = WalkthroughProperties()
        inhalerReadyScreenProperties.backButtonVisible = false
        inhalerReadyScreenProperties.nextButtonVisible = true
        inhalerReadyScreenProperties.closeButtonVisible = true
        inhalerReadyScreenProperties.contentButtonVisible = false
        inhalerReadyScreenProperties.hyperlinkVisible = false
        inhalerReadyScreenProperties.nextScreen = Walkthrough.INHALE_EVENTS
        inhalerReadyScreenProperties.dashboardButton = PopupDashboardButton.NONE

        walkthroughPropertiesMap.put(Walkthrough.INHALER_READY, inhalerReadyScreenProperties)

        val inhaleEvents1ScreenProperties = WalkthroughProperties()
        inhaleEvents1ScreenProperties.backButtonVisible = true
        inhaleEvents1ScreenProperties.nextButtonVisible = true
        inhaleEvents1ScreenProperties.closeButtonVisible = true
        inhaleEvents1ScreenProperties.contentButtonVisible = false
        inhaleEvents1ScreenProperties.hyperlinkVisible = false
        inhaleEvents1ScreenProperties.nextScreen = Walkthrough.ENVIRONMENT
        inhaleEvents1ScreenProperties.dashboardButton = PopupDashboardButton.EVENTS

        walkthroughPropertiesMap.put(Walkthrough.INHALE_EVENTS, inhaleEvents1ScreenProperties)

        val environmentScreenProperties = WalkthroughProperties()
        environmentScreenProperties.backButtonVisible = true
        environmentScreenProperties.nextButtonVisible = true
        environmentScreenProperties.closeButtonVisible = true
        environmentScreenProperties.contentButtonVisible = false
        environmentScreenProperties.hyperlinkVisible = false
        environmentScreenProperties.nextScreen = Walkthrough.DEVICES_1
        environmentScreenProperties.dashboardButton = PopupDashboardButton.ENVIRONMENT

        walkthroughPropertiesMap.put(Walkthrough.ENVIRONMENT, environmentScreenProperties)

        val devices1ScreenProperties = WalkthroughProperties()
        devices1ScreenProperties.backButtonVisible = true
        devices1ScreenProperties.nextButtonVisible = true
        devices1ScreenProperties.closeButtonVisible = true
        devices1ScreenProperties.contentButtonVisible = false
        devices1ScreenProperties.hyperlinkVisible = false
        devices1ScreenProperties.nextScreen = Walkthrough.DEVICES_2
        devices1ScreenProperties.dashboardButton = PopupDashboardButton.DEVICES

        walkthroughPropertiesMap.put(Walkthrough.DEVICES_1, devices1ScreenProperties)

        val devices2ScreenProperties = WalkthroughProperties()
        devices2ScreenProperties.backButtonVisible = true
        devices2ScreenProperties.nextButtonVisible = true
        devices2ScreenProperties.closeButtonVisible = true
        devices2ScreenProperties.contentButtonVisible = false
        devices2ScreenProperties.hyperlinkVisible = false
        devices2ScreenProperties.nextScreen = Walkthrough.SUPPORT
        devices2ScreenProperties.dashboardButton = PopupDashboardButton.DEVICES

        walkthroughPropertiesMap.put(Walkthrough.DEVICES_2, devices2ScreenProperties)

        val supportScreenProperties = WalkthroughProperties()
        supportScreenProperties.backButtonVisible = true
        supportScreenProperties.nextButtonVisible = true
        supportScreenProperties.closeButtonVisible = true
        supportScreenProperties.contentButtonVisible = false
        supportScreenProperties.hyperlinkVisible = false
        supportScreenProperties.nextScreen = Walkthrough.DSA
        supportScreenProperties.dashboardButton = PopupDashboardButton.SUPPORT

        walkthroughPropertiesMap.put(Walkthrough.SUPPORT, supportScreenProperties)

        val dsaScreenProperties = WalkthroughProperties()
        dsaScreenProperties.backButtonVisible = true
        dsaScreenProperties.nextButtonVisible = true
        dsaScreenProperties.closeButtonVisible = true
        dsaScreenProperties.contentButtonVisible = false
        dsaScreenProperties.hyperlinkVisible = false
        dsaScreenProperties.nextScreen = Walkthrough.REPORT
        dsaScreenProperties.dashboardButton = PopupDashboardButton.DSA

        walkthroughPropertiesMap.put(Walkthrough.DSA, dsaScreenProperties)

        val reportScreenProperties = WalkthroughProperties()
        reportScreenProperties.backButtonVisible = true
        reportScreenProperties.nextButtonVisible = true
        reportScreenProperties.closeButtonVisible = true
        reportScreenProperties.contentButtonVisible = false
        reportScreenProperties.hyperlinkVisible = false
        reportScreenProperties.nextScreen = Walkthrough.SECURITY
        reportScreenProperties.dashboardButton = PopupDashboardButton.REPORT

        walkthroughPropertiesMap.put(Walkthrough.REPORT, reportScreenProperties)

        val securityScreenProperties = WalkthroughProperties()
        securityScreenProperties.backButtonVisible = true
        securityScreenProperties.nextButtonVisible = false
        securityScreenProperties.closeButtonVisible = true
        securityScreenProperties.contentButtonVisible = false
        securityScreenProperties.hyperlinkVisible = false
        securityScreenProperties.nextScreen = null
        securityScreenProperties.dashboardButton = PopupDashboardButton.NONE

        walkthroughPropertiesMap.put(Walkthrough.SECURITY, securityScreenProperties)

        return walkthroughPropertiesMap
    }
}
