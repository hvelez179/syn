//
// ConsentViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.setup

import android.databinding.Bindable
import android.os.Bundle
import com.teva.cloud.ConsentStatus
import com.teva.cloud.dataentities.ConsentData
import com.teva.cloud.dataquery.ConsentDataQuery
import com.teva.common.messages.SystemMonitorMessage
import com.teva.common.services.TimeService
import com.teva.common.utilities.DataTask
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.respiratoryapp.BR
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.controls.InputValidator
import com.teva.respiratoryapp.activity.controls.InputValidator.ValidationState
import com.teva.respiratoryapp.activity.viewmodel.SupportEvents
import com.teva.respiratoryapp.activity.viewmodel.TextEntryViewModel
import com.teva.respiratoryapp.activity.viewmodel.dashboard.ContactSupportViewModel
import com.teva.respiratoryapp.common.AppSystemMonitorActivity
import com.teva.respiratoryapp.models.ApplicationSettings
import com.teva.respiratoryapp.services.alert.AlertButton
import com.teva.respiratoryapp.services.alert.SystemAlertManager
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit


/**
 * This class is the view model for the Consent screen.
 *
 * @param dependencyProvider The dependency injection mechanism.
 */
class ConsentViewModel(dependencyProvider: DependencyProvider) : TextEntryViewModel(dependencyProvider) {

    private val consentDataQuery = dependencyProvider.resolve<ConsentDataQuery>()
    private var consentData: ConsentData? = null
    private var consentDisplayTime: Instant? = null

    /**
     * Property defining whether there is an incomplete consent in the database
     */
    private var pendingCloudConsentExists = false

    init {
        retrieveConsentDataFromDatabase()
    }

    private fun retrieveConsentDataFromDatabase() {
        DataTask<Unit, ConsentData?>("ConsentViewModel_retrieveConsentDataFromDatabase")
                .inBackground {
                    consentDataQuery.getConsentData()
                }
                .onResult { result ->
                    if(result != null) {
                        consentData = result
                        pendingCloudConsentExists = true
                    } else {
                        initializeConsentData()
                    }
                }
                .execute()
    }

    private fun initializeConsentData() {
        consentData = ConsentData()
        consentData!!.created = dependencyProvider.resolve<TimeService>().now()
        consentData!!.hasConsented = false
    }

    /**
     * The current validation state for the date field.
     */
    private var dateValidationState = InputValidator.ValidationState.INCOMPLETE
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.warningVisible)
                updateNextState()
            }
        }

    /**
     * The date entered by the user.
     */
    @Bindable
    var date: LocalDate? = null
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.date)
            }
        }

    /**
     * The user has checked the terms checkbox.
     */
    @get:Bindable
    @get:JvmName("getTermsAndPrivacyConditionsAccepted")
    var isTermsAndPrivacyConditionsAccepted: Boolean = false
        set(value) {
            field = value

            notifyPropertyChanged(BR.termsAndPrivacyConditionsAccepted)
            updateNextState()
        }

    /**
     * The enable state of the Next button.
     */
    @get:Bindable
    @get:JvmName("getNextEnabled")
    var isNextEnabled: Boolean = false
        get() { return field }
        set(value) {
            field = value

            notifyPropertyChanged(BR.nextEnabled)
        }

    /**
     * A flag which indicates if the warning message should be displayed.
     */
    @get:Bindable
    @get:JvmName("getWarningVisible")
    val isWarningVisible: Boolean
        get() {
            return dateValidationState == ValidationState.IN_ERROR
        }

    /**
     * The welcome message text.
     */
    val messageText: String = getString(R.string.consentMessage_text)

    /**
     * Updates the nextEnabled property based on the current conditions.
     */
    private fun updateNextState() {
        isNextEnabled = isTermsAndPrivacyConditionsAccepted &&
                dateValidationState == ValidationState.VALID
    }

    fun onValidationStateChanged(state: ValidationState) {
        dateValidationState = state
    }

    /**
     * The handler for link clicks.
     *
     * @param id The id of the clicked link.
     */
    fun onLinkClicked(id: Any) {
        val events = dependencyProvider.resolve<Events>()
        when (id) {
            TERMS_OF_USE_LINK_ID -> events.onTermsOfUse()
            PRIVACY_NOTICE_LINK_ID -> events.onPrivacyNotice()
        }
    }

    /**
     * Called by the fragment when the keyboard's action button is pressed.
     */
    override fun onEditorActionButton() {
        onConfirm()
    }

    /**
     * Method called by the BaseFragment when the fragment's onStart() lifecycle method is called.
     */
    override fun onStart() {
        super.onStart()
        consentDisplayTime = dependencyProvider.resolve<TimeService>().now()
    }

    /**
     * The handler for the confirm button click.
     */
    fun onConfirm() {
        if (isNextEnabled) {
            val timeService = dependencyProvider.resolve<TimeService>()
            val today = timeService.today()
            val age = ChronoUnit.YEARS.between(date, today)

            when {
                age < MIN_AGE -> showTooYoungToUseAlert()
                age < AGE_OF_CONSENT -> showTooYoungToConsentAlert()
                else -> {
                    val now = timeService.now()
                    val consentDuration = Duration.between(consentDisplayTime, now)
                    dependencyProvider.resolve<Messenger>().publish(SystemMonitorMessage(AppSystemMonitorActivity.Consent(consentDuration)))
                    storeConsent(date)
                }
            }
        }
    }

    /**
     * Stores the consent record in the database and in application settings
     */
    private fun storeConsent(dob: LocalDate?) {

        if(consentData ==  null) {
            initializeConsentData()
        }

        consentData!!.patientDOB = dob

        dependencyProvider.resolve<ApplicationSettings>().hasUserAcceptedTermsOfUse = true
        consentData!!.hasConsented = true

        consentData!!.status = ConsentStatus.IN_PROGRESS.status
        consentData!!.termsAndConditions = "termsAndConditions"
        consentData!!.privacyNotice = "privacyNotice"
        consentData!!.consentStartDate = dependencyProvider.resolve<TimeService>().today()
        consentData!!.addressCountry = "addressCountry"

        DataTask<Unit, Unit>("ConsentViewModel_storeConsentData")
                .inBackground {
                    if (pendingCloudConsentExists) {
                        consentDataQuery.update(consentData!!, true)
                    } else {
                        consentDataQuery.insert(consentData!!, true)
                        pendingCloudConsentExists = true
                    }
                }
                .onResult {
                    dependencyProvider.resolve<Events>().onConfirm()
                }
                .execute()


    }

    /**
     * Called to display the alert indicating that the user is too young to use the app.
     */
    private fun showTooYoungToUseAlert() {

        showAgeAlert(R.string.consent_age_too_young_to_use)
    }

    /**
     * Called to display the alert indicating that the user is too young to use the app.
     */
    private fun showTooYoungToConsentAlert() {

        showAgeAlert(R.string.consent_age_too_young_to_consent)
    }

    /**
     * Called to display an age alert.
     */
    private fun showAgeAlert(message: Int) {

        val clickHandler: (AlertButton) -> Boolean = { alertButton ->
            if (alertButton == AlertButton.SECONDARY) {
                dependencyProvider.resolve<ContactSupportViewModel.Events>().onContactSupport()
                false
            } else {
                true
            }
        }

        dependencyProvider.resolve<SystemAlertManager>().showAlert(
                id = AGE_ALERT_ID,
                messageId = message,
                titleId = R.string.consent_age_alert_title,
                primaryButtonTextId = R.string.ok_text,
                secondaryButtonTextId = R.string.contact_teva_support,
                onClickClose = clickHandler)
    }

    /**
     * Saves the current state of the fragment into a saved instance state.
     *
     * @param savedInstanceState The Bundle to save the state into.
     */
    override fun saveInstanceState(savedInstanceState: Bundle) {
        super.saveInstanceState(savedInstanceState)

        savedInstanceState.putSerializable(DATE_VALIDATION_STATE_KEY, dateValidationState)
        savedInstanceState.putBoolean(TERMS_OF_USE_ACCEPTED_KEY, isTermsAndPrivacyConditionsAccepted)
        date?.let {
            savedInstanceState.putSerializable(DATE_KEY, it)
        }
    }

    /**
     * Restores the current state of the fragment from a saved instance state.
     *
     * @param savedInstanceState The Bundle to restore the state from.
     */
    override fun restoreInstanceState(savedInstanceState: Bundle?) {
        super.restoreInstanceState(savedInstanceState)

        if (savedInstanceState != null) {
            isTermsAndPrivacyConditionsAccepted =
                    savedInstanceState.getBoolean(TERMS_OF_USE_ACCEPTED_KEY, false)

            dateValidationState = savedInstanceState.getSerializable(DATE_VALIDATION_STATE_KEY) as ValidationState

            if ( savedInstanceState.containsKey(DATE_KEY)) {
                date = savedInstanceState.getSerializable(DATE_KEY) as LocalDate
            }
        }
    }

    /**
     * This interface defines the events produced by the viewmodel to request actions by the activity.
     */
    interface Events {
        /**
         * Indicates that the user accepted the terms of use.
         */
        fun onConfirm()

        /**
         * Requests for the display of the privacy notice.
         */
        fun onPrivacyNotice()

        /**
         * Requests for the display of the terms of use.
         */
        fun onTermsOfUse()

    }

    companion object {
        private val TERMS_OF_USE_ACCEPTED_KEY = "TermsOfUseAccepted"
        private val DATE_VALIDATION_STATE_KEY = "DateValidationState"
        private val DATE_KEY = "Date"

        private val TERMS_OF_USE_LINK_ID = 1
        private val PRIVACY_NOTICE_LINK_ID = 2

        private val MIN_AGE = 13
        private val AGE_OF_CONSENT = 18

        val AGE_ALERT_ID = "ConsentAgeAlert"
    }
}
