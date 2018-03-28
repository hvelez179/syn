package com.teva.respiratoryapp.activity.viewmodel.popup

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.mocks.MockedLocalizationService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.devices.model.DeviceManagerNotificationId
import com.teva.notifications.services.notification.NotificationInfo
import com.teva.respiratoryapp.testutils.BaseTest

import org.junit.Before
import org.junit.Test

import java.util.HashMap

import org.junit.Assert.*

class NotificationPopupViewModelTest : BaseTest() {

    private lateinit var dependencyProvider: DependencyProvider
    private lateinit var notificationInfo: NotificationInfo
    private lateinit var events: NotificationPopupViewModel.Events

    @Before
    fun setup() {
        dependencyProvider = DependencyProvider.default

        val localizationService = MockedLocalizationService()
        localizationService.add(R.string.ok_text, OK)
        localizationService.add(R.string.inhalationsFeedbackGoodInhalation_text, HEADER_TEXT)
        localizationService.add(R.string.inhalationsFeedbackLowInhalation_text, BODY_TEXT)
        localizationService.add(R.string.menuInstructionsForUseTitle_text, HYPERLINK_TEXT)

        dependencyProvider.register(LocalizationService::class, localizationService)

        val notificationData = HashMap<String, Any>()
        notificationInfo = NotificationInfo(CATEGORY_ID, notificationData)

        events = mock()
        dependencyProvider.register(NotificationPopupViewModel.Events::class, events)
    }

    @Test
    fun testConstructor_ConfiguresPopup() {
        val viewModel = NotificationPopupViewModel(dependencyProvider, notificationInfo)

        assertEquals(POPUP_COLOR, viewModel.popupColor)
        assertTrue(viewModel.arrowState == PopupDashboardButton.EVENTS)
        assertTrue(viewModel.buttonState == PopupDashboardButton.ALL)
        assertFalse(viewModel.buttonsDimmed)
        assertFalse(viewModel.isCloseButtonVisible)
        assertEquals(HEADER_TEXT, viewModel.headerText)
        assertEquals(BODY_TEXT, viewModel.bodyText)
        assertEquals(OK, viewModel.buttonText)
        assertEquals(HYPERLINK_TEXT, viewModel.hyperlinkText)
    }

    @Test
    fun testOnClose_CallsOnCloseEvent() {
        val viewModel = NotificationPopupViewModel(dependencyProvider, notificationInfo)

        viewModel.onClose()

        verify(events).onClose(notificationInfo)
    }

    @Test
    fun testOnButton_CallsOnButtonEvent() {
        val viewModel = NotificationPopupViewModel(dependencyProvider, notificationInfo)

        viewModel.onButton()

        verify(events).onButton(notificationInfo)
    }

    @Test
    fun testOnHyperlink_CallsOnHyperlinkEvent() {
        val viewModel = NotificationPopupViewModel(dependencyProvider, notificationInfo)

        viewModel.onHyperlink()

        verify(events).onHyperlink(notificationInfo)
    }

    companion object {
        private val CATEGORY_ID = DeviceManagerNotificationId.INHALATIONS_FEEDBACK_LOW_INHALATION
        private val POPUP_COLOR = PopupColor.GREEN
        private val HEADER_TEXT = "HeaderText"
        private val BODY_TEXT = "BodyText"
        private val HYPERLINK_TEXT = "HyperlinkText"

        private val OK = "ok"
    }
}