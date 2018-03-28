package com.teva.respiratoryapp.activity.viewmodel.popup

import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.mocks.MockedLocalizationService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.respiratoryapp.testutils.BaseTest

import org.junit.Before
import org.junit.Test

import org.junit.Assert.*

class InhalerRegistrationPopupViewModelTest : BaseTest() {

    private var dependencyProvider: DependencyProvider? = null

    @Before
    fun setup() {
        dependencyProvider = DependencyProvider.default

        val localizationService = MockedLocalizationService()
        localizationService.add(R.string.ok_text, OK)

        dependencyProvider!!.register(LocalizationService::class, localizationService)
    }

    @Test
    fun testConstructor_ConfiguresPopup() {
        val viewModel = InhalerRegistrationPopupViewModel(dependencyProvider!!)

        assertEquals(PopupColor.GRAY, viewModel.popupColor)
        assertTrue(viewModel.arrowState == PopupDashboardButton.DEVICES)
        assertTrue(viewModel.buttonState == PopupDashboardButton.ALL)
        assertFalse(viewModel.buttonsDimmed)
        assertTrue(viewModel.isCloseButtonVisible)
        assertEquals(OK, viewModel.buttonText)
    }

    companion object {
        private val OK = "ok"
    }

}