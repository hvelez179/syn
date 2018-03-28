//
// AddProfileViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.setup

import android.databinding.Bindable
import android.support.annotation.MainThread
import com.teva.cloud.dataentities.UserProfile
import com.teva.cloud.enumerations.UserProfileStatusCode
import com.teva.cloud.messages.SyncCloudMessage
import com.teva.cloud.messages.UserProfileMessage
import com.teva.cloud.models.userprofile.UserProfileManager
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.common.utilities.Messenger
import com.teva.respiratoryapp.BR
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.controls.InputValidator
import com.teva.respiratoryapp.activity.viewmodel.LoadingEvents
import com.teva.respiratoryapp.activity.viewmodel.SupportEvents
import com.teva.respiratoryapp.activity.viewmodel.TextEntryViewModel
import com.teva.respiratoryapp.activity.viewmodel.dashboard.ContactSupportViewModel
import com.teva.respiratoryapp.services.alert.AlertButton
import com.teva.respiratoryapp.services.alert.SystemAlertManager
import org.greenrobot.eventbus.Subscribe
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit
import java.util.*

/**
 * Viewmodel class for the Add Profile screen.
 */
class AddProfileViewModel(dependencyProvider: DependencyProvider, val existingProfiles: ArrayList<UserProfile>)
    : TextEntryViewModel(dependencyProvider) {

    private val userProfileManager = dependencyProvider.resolve<UserProfileManager>()

    /**
     * The first name of the dependent
     */
    @Bindable
    var firstName: String? = null
        set(value) {
            field = value
            updateNextState()
            notifyPropertyChanged(BR.firstName)
        }

    @Bindable
    var lastName: String? = null
        set(value) {
            field = value
            updateNextState()
            notifyPropertyChanged(BR.lastName)
        }

    /**
     * The current validation state for the date field.
     */
    private var dateValidationState = InputValidator.ValidationState.INCOMPLETE
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.warningVisible)
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
                updateNextState()
            }
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
            return dateValidationState == InputValidator.ValidationState.IN_ERROR
        }

    /**
     * Updates the nextEnabled property based on the current conditions.
     */
    private fun updateNextState() {
        isNextEnabled = !firstName.isNullOrBlank() &&
                !lastName.isNullOrBlank() &&
                dateValidationState == InputValidator.ValidationState.VALID
    }

    /**
     * Called when the validation state of the date changes.
     */
    fun onValidationStateChanged(state: InputValidator.ValidationState) {
        dateValidationState = state
    }

    /**
     * Called by the fragment when the keyboard's action button is pressed.
     */
    override fun onEditorActionButton() {
        onNext()
    }

    /**
     * Method called by the BaseFragment when the fragment's onStart() lifecycle method is called.
     */
    override fun onStart() {
        super.onStart()
        dependencyProvider.resolve<Messenger>().subscribe(this)
    }

    /**
     * Method called by the BaseFragment when the fragment's onStop() lifecycle method is called.
     */
    override fun onStop() {
        super.onStop()
        dependencyProvider.resolve<Messenger>().unsubscribeToAll(this)
    }

   /**
     * Click handler for the Next CTA button
     */
    fun onNext() {
        if (isNextEnabled) {

            val today = dependencyProvider.resolve<TimeService>().today()
            val age = ChronoUnit.YEARS.between(date, today)

            when {
                age < MIN_AGE -> showAgeAlert()
                age >= MAX_AGE -> showAgeAlert(false)
                profileAlreadyExists() -> showAlreadyExistsAlert()
                else -> saveDependent()
            }
        }
    }

    private fun profileAlreadyExists(): Boolean {
        return existingProfiles.any { it.firstName?.toLowerCase() == firstName?.toLowerCase() && it.lastName?.toLowerCase() == lastName?.toLowerCase() && it.dateOfBirth == date }
    }

    /**
     * Validates and saves the dependent information and navigates
     * to the next screen.
     */
    private fun saveDependent() {

        // temporary stand-in code for model
        // need to validate whether the dependent already exists
        val profile = UserProfile(null,
                firstName!!, lastName!!, false, false, false, date!!)

        userProfileManager.setupActiveProfileAsync(profile)
        dependencyProvider.resolve<LoadingEvents>().showLoadingIndicator()
    }

    /**
     * Called to display an age alert.
     */
    private fun showAgeAlert(tooYoung: Boolean = true) {

        val clickHandler: (AlertButton) -> Boolean = { alertButton ->
            if (alertButton == AlertButton.SECONDARY) {
                dependencyProvider.resolve<SupportEvents>().onSupport()
                false
            } else {
                true
            }
        }

        dependencyProvider.resolve<SystemAlertManager>().showAlert(
                id = ConsentViewModel.AGE_ALERT_ID,
                messageId = if(tooYoung) R.string.dependent_age_too_young_to_use else R.string.dependentTooOldErrorMessage_text,
                titleId = if(tooYoung) R.string.consent_age_alert_title else R.string.dependentTooOldErrorTitle_text,
                primaryButtonTextId = R.string.ok_text,
                secondaryButtonTextId = R.string.contact_teva_support,
                onClickClose = clickHandler)
    }

    /**
     * Called to display profile already exists alert.
     */
    private fun showAlreadyExistsAlert() {
        dependencyProvider.resolve<SystemAlertManager>().showAlert(
                messageId = R.string.dependentAlreadyExistsMessage_text,
                titleId = R.string.dependentAlreadyExistsTitle_text,
                primaryButtonTextId = R.string.ok_text
                )
    }

    /**
     * Click handler for the Contact Teva Support hyperlink
     */
    fun onContactSupport() {
        dependencyProvider.resolve<SupportEvents>().onSupport()
    }

    /**
     * This method is the handler for the user profile message.
     */
    @Subscribe
    @MainThread
    fun onUserProfileMessage(userProfileMessage: UserProfileMessage) {
        when(userProfileMessage.messageCode) {
            UserProfileStatusCode.DID_SETUP_ACTIVE_PROFILE -> {
                dependencyProvider.resolve<LoadingEvents>().hideLoadingIndicator()
                // if valid and saved, then navigate to next screen
                dependencyProvider.resolve<ProfileSetupViewModel.Events>().onNext()
                dependencyProvider.resolve<Messenger>().post(SyncCloudMessage())
            }
            UserProfileStatusCode.ERROR_DURING_SETUP_ACTIVE_PROFILE -> {
                dependencyProvider.resolve<LoadingEvents>().hideLoadingIndicator()
                //Todo - Show error message.
                dependencyProvider.resolve<SystemAlertManager>().showAlert(
                        id = AddProfileViewModel.ADD_PROFILE_FAILED_ALERT_ID,
                        messageId = R.string.add_profile_failed_alert_message,
                        titleId = R.string.add_profile_failed_alert_title,
                        primaryButtonTextId = R.string.ok_text,
                        secondaryButtonTextId = R.string.contact_teva_support)
            }
            else -> {
                logger.log(Logger.Level.ERROR, "Received one of the get profiles messages")
            }
        }
    }

    companion object {
        private val MIN_AGE = 13
        private val MAX_AGE = 18
        val ADD_PROFILE_FAILED_ALERT_ID = "AddProfileFailedAlert"

    }

}