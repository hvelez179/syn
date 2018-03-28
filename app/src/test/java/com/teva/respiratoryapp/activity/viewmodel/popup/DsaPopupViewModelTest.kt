package com.teva.respiratoryapp.activity.viewmodel.popup

import android.app.NotificationManager
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.common.utilities.Messenger
import com.teva.respiratoryapp.mocks.MockedLocalizationService
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel
import com.teva.respiratoryapp.testutils.BaseTest
import com.teva.userfeedback.enumerations.UserFeeling
import com.teva.userfeedback.model.UserFeelingManager

import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

class DsaPopupViewModelTest : BaseTest() {

    private lateinit var localizationService: MockedLocalizationService
    private lateinit var dependencyProvider: DependencyProvider
    private lateinit var messenger: Messenger
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setup() {
        dependencyProvider = DependencyProvider.default

        notificationManager = mock()
        dependencyProvider.register(NotificationManager::class, notificationManager)

        localizationService = MockedLocalizationService()
        dependencyProvider.register(LocalizationService::class, localizationService)

        val timeService: TimeService = mock()
        whenever(timeService.now()).thenReturn(NOW)

        messenger = mock()
        dependencyProvider.register(Messenger::class, messenger)

        dependencyProvider.register(TimeService::class, timeService)
    }

    @Test
    fun testConstructor_ConfiguresPopup() {
        val viewModel = DsaPopupViewModel(dependencyProvider, NOW)

        assertEquals(PopupColor.WHITE, viewModel.popupColor)
        assertTrue(viewModel.arrowState == PopupDashboardButton.DSA)
        assertTrue(viewModel.buttonState == PopupDashboardButton.ALL)
        assertFalse(viewModel.buttonsDimmed)
    }

    @Test
    fun testOnButton_SavesDsaAndCallsOnCloseEvent() {
        val events: FragmentViewModel.NavigationEvents = mock()
        dependencyProvider.register(FragmentViewModel.NavigationEvents::class, events)

        val userFeelingManager: UserFeelingManager = mock()
        dependencyProvider.register(UserFeelingManager::class, userFeelingManager)


        val viewModel = DsaPopupViewModel(dependencyProvider, NOW)
        viewModel.userFeeling = UserFeeling.BAD
        viewModel.onButton()

        verify(events).onBackPressed()
        verify(userFeelingManager).saveUserFeeling(UserFeeling.BAD, DsaPopupViewModelTest.NOW)
    }

    companion object {

        private val GMT_ZONE_ID = ZoneId.ofOffset("GMT", ZoneOffset.UTC)
        private val NOW = Instant.from(ZonedDateTime.of(2020, 1, 31, 10, 10, 10, 0, GMT_ZONE_ID))
    }
}