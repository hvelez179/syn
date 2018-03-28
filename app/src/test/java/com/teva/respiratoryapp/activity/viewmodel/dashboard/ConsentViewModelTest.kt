//
// ConsentViewModelTest.java
// app
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.activity.viewmodel.dashboard

import com.nhaarman.mockito_kotlin.*
import com.teva.cloud.dataquery.ConsentDataQuery
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.common.utilities.Messenger
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.controls.InputValidator
import com.teva.respiratoryapp.activity.viewmodel.setup.ConsentViewModel
import com.teva.respiratoryapp.models.ApplicationSettings
import com.teva.respiratoryapp.services.alert.SystemAlertManager
import com.teva.respiratoryapp.testutils.BaseTest
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue

import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

/**
 * This class defines unit tests for the ConsentViewModel class.
 */
class ConsentViewModelTest : BaseTest() {
    lateinit var dependencyProvider: DependencyProvider
    lateinit var consentEvents: ConsentViewModel.Events
    lateinit var localizationService: LocalizationService
    lateinit var systemAlertManager: SystemAlertManager
    lateinit var timeService: TimeService
    lateinit var consentDataQuery: ConsentDataQuery
    lateinit var messenger: Messenger

    val today = LocalDate.of(2017, 11, 12)

    @Before
    fun setup() {
        dependencyProvider = DependencyProvider.default

        consentEvents = mock()
        dependencyProvider.register(ConsentViewModel.Events::class, consentEvents)

        localizationService = mock()
        dependencyProvider.register(LocalizationService::class, localizationService)

        systemAlertManager = mock()
        dependencyProvider.register(SystemAlertManager::class, systemAlertManager)

        timeService = mock()
        whenever(timeService.today()).thenReturn(today)
        dependencyProvider.register(TimeService::class, timeService)

        consentDataQuery = mock()
        dependencyProvider.register(ConsentDataQuery::class, consentDataQuery)

        messenger = mock()
        dependencyProvider.register(Messenger::class, messenger)
    }

    @Test
    fun testInvalidDateValidationStateDisplaysWarning() {
        val viewmodel = ConsentViewModel(dependencyProvider)

        viewmodel.onValidationStateChanged(InputValidator.ValidationState.IN_ERROR)
        assertTrue(viewmodel.isWarningVisible)
    }

    @Test
    fun testValidDateValidationStateClearsWarning() {
        val viewmodel = ConsentViewModel(dependencyProvider)

        viewmodel.onValidationStateChanged(InputValidator.ValidationState.VALID)
        assertFalse(viewmodel.isWarningVisible)
    }

    @Test
    fun testIncompleteDateValidationStateClearsWarning() {
        val viewmodel = ConsentViewModel(dependencyProvider)

        viewmodel.onValidationStateChanged(InputValidator.ValidationState.INCOMPLETE)
        assertFalse(viewmodel.isWarningVisible)
    }

    @Test
    fun testValidDateValidationStateAndConsentCheckboxEnablesNextButton() {
        val viewmodel = ConsentViewModel(dependencyProvider)

        viewmodel.onValidationStateChanged(InputValidator.ValidationState.VALID)
        viewmodel.isTermsAndPrivacyConditionsAccepted = true
        assertTrue(viewmodel.isNextEnabled)
    }

    @Test
    fun testInvalidValidDateValidationStateOrConsentCheckboxDisablesNextButton() {
        val viewmodel = ConsentViewModel(dependencyProvider)

        viewmodel.onValidationStateChanged(InputValidator.ValidationState.INCOMPLETE)
        viewmodel.isTermsAndPrivacyConditionsAccepted = false
        assertFalse(viewmodel.isNextEnabled)

        viewmodel.onValidationStateChanged(InputValidator.ValidationState.INCOMPLETE)
        viewmodel.isTermsAndPrivacyConditionsAccepted = true
        assertFalse(viewmodel.isNextEnabled)

        viewmodel.onValidationStateChanged(InputValidator.ValidationState.VALID)
        viewmodel.isTermsAndPrivacyConditionsAccepted = false
        assertFalse(viewmodel.isNextEnabled)
    }

    @Test
    fun testAgeUnder13DisplaysTooYoungToUseDialogWhenNextClicked() {
        val viewmodel = ConsentViewModel(dependencyProvider)

        val dob = today.minusYears(13).plusDays(1)
        viewmodel.date = dob
        viewmodel.onValidationStateChanged(InputValidator.ValidationState.VALID)
        viewmodel.isTermsAndPrivacyConditionsAccepted = true

        viewmodel.onConfirm()

        verify(systemAlertManager).showAlert(
                id = eq(ConsentViewModel.AGE_ALERT_ID),
                message = isNull(),
                messageId = eq(R.string.consent_age_too_young_to_use),
                title = isNull(),
                titleId = eq(R.string.consent_age_alert_title),
                primaryButtonTextId = eq(R.string.ok_text),
                secondaryButtonTextId = eq(R.string.contact_teva_support),
                onClick = isNull(),
                onClickClose = any(),
                imageTextId = isNull(),
                imageId = isNull(),
                onImageClick = isNull())
    }

    @Test
    fun testAgeUnder18DisplaysTooYoungToConsentDialogWhenNextClicked() {
        val viewmodel = ConsentViewModel(dependencyProvider)

        val dob = today.minusYears(17).plusDays(1)
        viewmodel.date = dob
        viewmodel.onValidationStateChanged(InputValidator.ValidationState.VALID)
        viewmodel.isTermsAndPrivacyConditionsAccepted = true

        viewmodel.onConfirm()

        verify(systemAlertManager).showAlert(
                id = eq(ConsentViewModel.AGE_ALERT_ID),
                message = isNull(),
                messageId = eq(R.string.consent_age_too_young_to_consent),
                title = isNull(),
                titleId = eq(R.string.consent_age_alert_title),
                primaryButtonTextId = eq(R.string.ok_text),
                secondaryButtonTextId = eq(R.string.contact_teva_support),
                onClick = isNull(),
                onClickClose = any(),
                imageTextId = isNull(),
                imageId = isNull(),
                onImageClick = isNull())
    }

    @Test
    fun testAgeOver18NavigatesToLoginWhenNextClicked() {
        val applicationSettings: ApplicationSettings = mock()
        dependencyProvider.register(ApplicationSettings::class, applicationSettings)
        whenever(timeService.now()).thenReturn(Instant.ofEpochMilli(1517437608654))
        val viewmodel = ConsentViewModel(dependencyProvider)
        viewmodel.onStart()

        val dob = today.minusYears(18)
        viewmodel.date = dob
        viewmodel.onValidationStateChanged(InputValidator.ValidationState.VALID)
        viewmodel.isTermsAndPrivacyConditionsAccepted = true

        viewmodel.onConfirm()

        verify(consentEvents).onConfirm()
    }
}
