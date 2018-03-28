package com.teva.respiratoryapp.activity.viewmodel.popup

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.common.utilities.Messenger
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.mocks.MockedLocalizationService
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel
import com.teva.userfeedback.enumerations.UserFeeling
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

class DsaConfirmPopupViewModelTest {

    private lateinit var dependencyProvider: DependencyProvider

    @Before
    fun setup() {
        dependencyProvider = DependencyProvider.default

        val messenger: Messenger = mock()
        dependencyProvider.register(Messenger::class, messenger)

        val localizationService = MockedLocalizationService()

        localizationService.add(R.string.ok_text, OK)

        dependencyProvider.register(LocalizationService::class, localizationService)
    }

    @Test
    fun testConstructor_WithGoodDSA_InitializesPopup() {
        val viewModel = DsaConfirmPopupViewModel(dependencyProvider, UserFeeling.GOOD)

        assertTrue(viewModel.arrowState == PopupDashboardButton.DSA)
        assertTrue(viewModel.buttonState == PopupDashboardButton.ALL)
        assertFalse(viewModel.buttonsDimmed)
    }

    @Test
    fun testConstructor_WithPoorDSA_InitializesPopup() {
        val viewModel = DsaConfirmPopupViewModel(dependencyProvider, UserFeeling.POOR)

        assertTrue(viewModel.arrowState == PopupDashboardButton.DSA)
        assertTrue(viewModel.buttonState == PopupDashboardButton.ALL)
        assertFalse(viewModel.buttonsDimmed)
    }

    @Test
    fun testConstructor_WithBadDSA_InitializesPopup() {
        val viewModel = DsaConfirmPopupViewModel(dependencyProvider, UserFeeling.BAD)

        assertTrue(viewModel.arrowState == PopupDashboardButton.DSA)
        assertTrue(viewModel.buttonState == PopupDashboardButton.ALL)
        assertFalse(viewModel.buttonsDimmed)
    }


    @Test
    fun testOnClose_CallsOnCloseEvent() {
        val events: FragmentViewModel.NavigationEvents = mock()
        dependencyProvider.register(FragmentViewModel.NavigationEvents::class, events)

        val viewModel = DsaConfirmPopupViewModel(dependencyProvider, UserFeeling.BAD)
        viewModel.onClose()

        verify(events).onBackPressed()
    }

    companion object {
        private val OK = "ok"

        private val GMT_ZONE_ID = ZoneId.ofOffset("GMT", ZoneOffset.UTC)
        private val NOW = Instant.from(ZonedDateTime.of(2020, 1, 31, 10, 10, 10, 0, GMT_ZONE_ID))
    }
}