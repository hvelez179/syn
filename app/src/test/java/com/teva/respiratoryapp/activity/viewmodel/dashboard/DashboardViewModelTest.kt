//
// DashboardViewModelTest.java
// app
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.activity.viewmodel.dashboard

import com.nhaarman.mockito_kotlin.*
import com.teva.analysis.entities.SummaryInfo
import com.teva.analysis.enumerations.SummaryTextId
import com.teva.analysis.messages.SummaryUpdatedMessage
import com.teva.analysis.model.AnalyzedDataProvider
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.common.utilities.Messenger
import com.teva.environment.models.EnvironmentMonitor
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.SupportEvents
import com.teva.respiratoryapp.activity.viewmodel.environment.EnvironmentViewModel
import com.teva.respiratoryapp.common.DateTimeLocalization
import com.teva.respiratoryapp.mocks.MockedLocalizationService
import com.teva.respiratoryapp.services.alert.SystemAlertManager
import com.teva.respiratoryapp.testutils.BaseTest
import com.teva.userfeedback.entities.DailyUserFeeling
import com.teva.userfeedback.enumerations.UserFeeling
import com.teva.userfeedback.model.UserFeelingManager
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset

/**
 * This class defines unit tests for the DashboardViewModel class.
 */

class DashboardViewModelTest : BaseTest() {
    private lateinit var dependencyProvider: DependencyProvider
    private lateinit var timeService: TimeService
    private lateinit var dashboardEvents: DashboardViewModel.DashboardEvents
    private lateinit var supportEvents: SupportEvents
    private lateinit var localizationService: MockedLocalizationService
    private lateinit var messenger: Messenger
    private lateinit var environmentViewModel: EnvironmentViewModel
    private lateinit var environmentMonitor: EnvironmentMonitor
    private lateinit var analyzedDataProvider: AnalyzedDataProvider
    private lateinit var systemAlertManager: SystemAlertManager
    private val today = LocalDate.of(2017, 5, 8)

    @Before
    fun setup() {
        dependencyProvider =  DependencyProvider.default
        dependencyProvider.unregisterAll()

        timeService = mock()
        whenever(timeService.today()).thenReturn(today)
        dependencyProvider.register(TimeService::class, timeService)
        dashboardEvents = mock()
        dependencyProvider.register(DashboardViewModel.DashboardEvents::class, dashboardEvents)
        supportEvents = mock()
        dependencyProvider.register(SupportEvents::class, supportEvents)
        localizationService = MockedLocalizationService()
        dependencyProvider.register(LocalizationService::class, localizationService)
        messenger = mock()
        dependencyProvider.register(Messenger::class, messenger)
        environmentViewModel = mock()
        dependencyProvider.register(EnvironmentViewModel::class, environmentViewModel)
        environmentMonitor = mock()
        dependencyProvider.register(EnvironmentMonitor::class, environmentMonitor)
        analyzedDataProvider = mock()
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
        systemAlertManager = mock()
        dependencyProvider.register(SystemAlertManager::class, systemAlertManager)

        dependencyProvider.register(DateTimeLocalization::class, mock<DateTimeLocalization>())
    }

    @Test
    fun testUserReportEventIsNotRaisedAndAnAlertIsDisplayedIfNoDataIsAvailable() {
        val USER_REPORT_NO_DATA_MESSAGE = "USER_REPORT_NO_DATA_MESSAGE"
        val USER_REPORT_NO_DATA_TITLE = "USER_REPORT_NO_DATA_TITLE"

        localizationService.add(R.string.userReportNoData_text, USER_REPORT_NO_DATA_MESSAGE)
        localizationService.add(R.string.userReportNoDataTitle_text, USER_REPORT_NO_DATA_TITLE)

        analyzedDataProvider = mock()
        whenever(analyzedDataProvider.trackingStartDate).thenReturn(null)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)

        val dashboardViewModel = DashboardViewModel(dependencyProvider)
        dashboardViewModel.showReport()

        verify(dashboardEvents, never()).onReport()
        verify(dashboardEvents).onReportEmpty()
    }

    @Test
    fun testUserReportEventIsRaisedIfDataIsAvailable() {
        analyzedDataProvider = mock()
        whenever(analyzedDataProvider.trackingStartDate).thenReturn(today)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)

        val dashboardViewModel = DashboardViewModel(dependencyProvider)
        dashboardViewModel.showReport()

        verify(dashboardEvents).onReport()
    }

    @Test
    fun testShowDsaRaiseEventToDisplayDsaIfDsaHasAlreadyBeenEntered() {
        val DSA_ALREADY_ENTERED_MESSAGE = "DSA_ALREADY_ENTERED_MESSAGE"
        val DSA_ALREADY_ENTERED_TITLE = "DSA_ALREADY_ENTERED_TITLE"

        val dailyUserFeeling = DailyUserFeeling(today.atStartOfDay(ZoneOffset.UTC).toInstant(), UserFeeling.GOOD)
        val userFeelingManager = mock<UserFeelingManager>()
        whenever(userFeelingManager.getUserFeelingAtDate(eq(today))).thenReturn(dailyUserFeeling)
        dependencyProvider.register(UserFeelingManager::class, userFeelingManager)

        localizationService.add(R.string.vasInputAlreadySpecified_text, DSA_ALREADY_ENTERED_MESSAGE)
        localizationService.add(R.string.vasInputAlreadySpecifiedTitle_text, DSA_ALREADY_ENTERED_TITLE)

        val dashboardViewModel = DashboardViewModel(dependencyProvider)
        dashboardViewModel.showDsa()

        verify(dashboardEvents).onDsa()
    }

    @Test
    fun testShowDsaRaisesEventToDisplayDsaAndDoesNotDisplayAlertIfDsaHasNotBeenEntered() {
        val DSA_ALREADY_ENTERED_MESSAGE = "DSA_ALREADY_ENTERED_MESSAGE"
        val DSA_ALREADY_ENTERED_TITLE = "DSA_ALREADY_ENTERED_TITLE"

        val dailyUserFeeling = DailyUserFeeling(today.atStartOfDay(ZoneOffset.UTC).toInstant(), UserFeeling.UNKNOWN)
        val userFeelingManager = mock<UserFeelingManager>()
        whenever(userFeelingManager.getUserFeelingAtDate(eq(today))).thenReturn(dailyUserFeeling)
        dependencyProvider.register(UserFeelingManager::class, userFeelingManager)

        localizationService.add(R.string.vasInputAlreadySpecified_text, DSA_ALREADY_ENTERED_MESSAGE)
        localizationService.add(R.string.vasInputAlreadySpecifiedTitle_text, DSA_ALREADY_ENTERED_TITLE)

        val dashboardViewModel = DashboardViewModel(dependencyProvider)
        dashboardViewModel.showDsa()

        verify(dashboardEvents).onDsa()
    }

    @Test
    fun testDashBoardViewOnStartSubscribesToMessenger() {
        val dashboardViewModel = DashboardViewModel(dependencyProvider)
        dashboardViewModel.onStart()

        verify(messenger).subscribe(eq(dashboardViewModel))
    }

    @Test
    fun testDashBoardViewOnStopUnSubscribesFromMessenger() {
        val dashboardViewModel = DashboardViewModel(dependencyProvider)
        dashboardViewModel.onStop()

        verify(messenger).unsubscribeToAll(eq(dashboardViewModel))
    }

    @Test
    fun testDashBoardViewModelDisplaysNeutralMessageIfNoSummaryMessageExists() {
        val NEUTRAL_MESSAGE = "NEUTRAL_MESSAGE"
        mockSummaryMessage(null, R.string.dashboardNeutralMessage_text, NEUTRAL_MESSAGE)

        val dashboardViewModel = DashboardViewModel(dependencyProvider)
        dashboardViewModel.onStart()
        assertEquals(NEUTRAL_MESSAGE, dashboardViewModel.summaryMessage)
    }

    @Test
    fun testDashBoardViewModelDisplaysWeatherSummaryWhenEnvironmentMessageIsTheHighestPriorityMessage() {
        mockSummaryMessage(SummaryTextId.ENVIRONMENT_MESSAGE, 0, null)

        val dashboardViewModel = DashboardViewModel(dependencyProvider)
        dashboardViewModel.onSummaryUpdateMessage(SummaryUpdatedMessage())

        assertEquals(SummaryTextId.ENVIRONMENT_MESSAGE, dashboardViewModel.summaryCard)
    }

    @Test
    fun testDashBoardViewModelDisplaysNonCriticalNoInhalersMessageWhenItIsTheHighestPriorityMessage() {
        val NO_INHALERS_MESSAGE = "NO_INHALERS_MESSAGE"
        mockSummaryMessage(SummaryTextId.NO_INHALERS, R.string.dashboardNoInhalers_text, NO_INHALERS_MESSAGE)

        val dashboardViewModel = DashboardViewModel(dependencyProvider)
        dashboardViewModel.onSummaryUpdateMessage(SummaryUpdatedMessage())

        assertEquals(SummaryTextId.NO_INHALERS, dashboardViewModel.summaryCard)
        assertEquals(NO_INHALERS_MESSAGE, dashboardViewModel.summaryMessage)
    }

    @Test
    fun testDashBoardViewModelDisplaysCriticalOveruseMessageWhenItIsTheHighestPriorityMessage() {
        val OVERUSE_MESSAGE = "OVERUSE_MESSAGE"

        mockSummaryMessage(SummaryTextId.OVERUSE, R.string.dashboardOveruse_text, OVERUSE_MESSAGE)

        val dashboardViewModel = DashboardViewModel(dependencyProvider)
        dashboardViewModel.onSummaryUpdateMessage(SummaryUpdatedMessage())

        assertEquals(SummaryTextId.OVERUSE, dashboardViewModel.summaryCard)
        assertEquals(OVERUSE_MESSAGE, dashboardViewModel.summaryMessage)
    }

    @Test
    fun testDashBoardViewModelDisplaysNonCriticalEmptyInhalerMessageWhenItIsTheHighestPriorityMessage() {
        val EMPTY_INHALER_MESSAGE = "EMPTY_INHALER_MESSAGE"

        mockSummaryMessage(SummaryTextId.EMPTY_INHALER, R.string.dashboardEmptyInhaler_text, EMPTY_INHALER_MESSAGE)


        val dashboardViewModel = DashboardViewModel(dependencyProvider)
        dashboardViewModel.onSummaryUpdateMessage(SummaryUpdatedMessage())

        assertEquals(SummaryTextId.EMPTY_INHALER, dashboardViewModel.summaryCard)
        assertEquals(EMPTY_INHALER_MESSAGE, dashboardViewModel.summaryMessage)
    }

    @Test
    fun testDashboardViewModelRaisesEventWithCorrectIDWhenMenuItemIsClicked() {
        val dashboardViewModel = DashboardViewModel(dependencyProvider)
        val menuItem = mock<MenuItem>()
        val menu_item_id = mock<DashboardViewModel.MENU_ITEM_ID>()
        whenever(menuItem.id).thenReturn(menu_item_id)
        dashboardViewModel.onItemClicked(menuItem)

        verify(dashboardEvents).onMenuItemClicked(menu_item_id)
    }

    @Test
    fun testDashboardViewModelRaisesEventToDisplayTrackerWhenShowHistoryIsInvoked() {
        val dashboardViewModel = DashboardViewModel(dependencyProvider)
        dashboardViewModel.showHistory()

        verify(dashboardEvents).onTracker()
    }

    @Test
    fun testDashboardViewModelRaisesEventToDisplayDeviceListWhenShowDevicesIsInvoked() {
        val dashboardViewModel = DashboardViewModel(dependencyProvider)
        dashboardViewModel.showDevices()

        verify(dashboardEvents).onDeviceList()
    }

    @Test
    fun testDashboardViewModelRaisesEventToDisplayEnvironmentWhenShowEnvironmentIsInvoked() {
        val dashboardViewModel = DashboardViewModel(dependencyProvider)
        dashboardViewModel.showEnvironment()

        verify(dashboardEvents).onEnvironment()
    }

    @Test
    fun testDashboardViewModelRaisesEventToDisplayMenuWhenShowMenuIsInvoked() {
        val dashboardViewModel = DashboardViewModel(dependencyProvider)
        dashboardViewModel.showMenu()

        verify(dashboardEvents).onMenu()
    }

    @Test
    fun testDashboardViewModelRaisesEventToDisplayReportWhenShowReportIsInvoked() {
        whenever(analyzedDataProvider.trackingStartDate).thenReturn(LocalDate.of(2017, 1, 1))
        val dashboardViewModel = DashboardViewModel(dependencyProvider)
        dashboardViewModel.showReport()

        verify(dashboardEvents).onReport()
    }

    @Test
    fun testDashboardViewModelRaisesEventToDisplaySupportInformationWhenShowSupportIsInvoked() {
        val dashboardViewModel = DashboardViewModel(dependencyProvider)
        dashboardViewModel.showSupport()

        verify(supportEvents).onSupport()
    }

    /**
     * This method mocks the analyzed data provider to return the specified message type as
     * the highest priority message from the summary message queue. This method also mocks the
     * localization service to return the specified display string for the specified resource id.
     * @param messageType - the message type to be returned from the analyzed data provider.
     * *
     * @param resourceId - the resource id for the resource string to be mocked in the localization service.
     * *
     * @param displayString - the string to be returned from localization service for the specified resource id.
     */
    private fun mockSummaryMessage(messageType: SummaryTextId?, resourceId: Int, displayString: String?) {
        var summaryInfo: SummaryInfo? = null

        if (messageType != null) {
            summaryInfo = SummaryInfo(messageType, null)
        }

        whenever(analyzedDataProvider.summaryInfo).thenReturn(summaryInfo)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)

        if (displayString != null) {
            localizationService.add(resourceId, displayString)
        }
    }
}
