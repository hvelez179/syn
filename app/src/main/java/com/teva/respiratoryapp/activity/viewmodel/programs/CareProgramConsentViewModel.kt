//
// CareProgramConsentViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.programs

import android.databinding.Bindable
import com.teva.cloud.messages.InvitationAcceptedMessage
import com.teva.cloud.messages.LeaveProgramMessage
import com.teva.cloud.models.programmanagement.CareProgramManager
import com.teva.cloud.models.programmanagement.InvitationDetails
import com.teva.cloud.services.programmanagement.CareProgramErrorCode
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.common.utilities.Messenger
import com.teva.respiratoryapp.BR
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.LoadingEvents
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel
import com.teva.respiratoryapp.services.alert.AlertButton
import com.teva.respiratoryapp.services.alert.SystemAlertManager
import org.greenrobot.eventbus.Subscribe

/**
 * ViewModel for the Care Program Consent screen.
 */
class CareProgramConsentViewModel(dependencyProvider: DependencyProvider, val invitationDetails: InvitationDetails)
    : FragmentViewModel(dependencyProvider) {

    /**
     * The list of carePrograms that are used to share data
     */
    var apps: List<String>? = null

    /**
     * The list of other apps that can be used to share data
     */
    var otherApps: List<String>? = null

    /**
     * The name of the care program
     */
    @Bindable
    var programName: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.programName)
        }

    /**
     * The consent message and terms.
     */
    @Bindable
    var message: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.message)
        }

    /**
     * The CareProgramManager
     */
    var careProgramManager: CareProgramManager = dependencyProvider.resolve()

    /**
     * Method called by the BaseFragment when the fragment's onStart() lifecycle method is called.
     */
    override fun onStart() {
        super.onStart()
        dependencyProvider.resolve<Messenger>().subscribe(this)
        dependencyProvider.resolve<LoadingEvents>().hideLoadingIndicator()
    }

    /**
     * Method called by the BaseFragment when the fragment's onStop() lifecycle method is called.
     */
    override fun onStop() {
        super.onStop()
        dependencyProvider.resolve<Messenger>().unsubscribeToAll(this)
    }

    init {
        programName = invitationDetails.programName
        apps = listOf("ProAir Digihaler")
        otherApps = invitationDetails.programSupportedApps.map { it.appName }
        message = String.format(localizationService.getString(R.string.care_program_consent_message), programName, invitationDetails.programId)
    }

    /**
     * Button handler for the consent button.
     */
    fun onConsent() {
        dependencyProvider.resolve<LoadingEvents>().showLoadingIndicator()
        careProgramManager.acceptInvitationAsync(invitationDetails)
    }

    /**
     * Handler for InvitationDetailsMessage
     * When triggered, get the program details and pass them on
     * @param message: This param contains an InvitationAcceptedMessage
     */
    @Subscribe()
    fun programDetailsMessageCompleted(message: InvitationAcceptedMessage){
        dependencyProvider.resolve<LoadingEvents>().hideLoadingIndicator()

        if (message.errorCode == CareProgramErrorCode.NO_ERROR) {
            dependencyProvider.resolve<Events>().onConsentOrDecline()
        } else {
            logger.log(Logger.Level.ERROR, message.errorDetails.toString())
            dependencyProvider.resolve<SystemAlertManager>().showAlert(id = ACCEPT_FAIL, messageId = R.string.care_program_fail_message, titleId = R.string.care_program_acceptance_fail_title)
        }
    }

    /**
     * Handler for the decline hyperlink.
     */
    fun onDecline() {
        val message = localizationService.getString(
                R.string.confirm_decline_care_program_message,
                mapOf("ProgramName" to (invitationDetails.programName as Any)))

        dependencyProvider.resolve<SystemAlertManager>()
                .showAlert(titleId = R.string.confirm_decline_care_program_title,
                        message = message,
                        primaryButtonTextId = R.string.decline_program_invitation,
                        secondaryButtonTextId = R.string.cancel_text, onClickClose = { button ->
                    if (button == AlertButton.PRIMARY) {
                        declineProgram()
                    }

                    true
                })

    }

    /**
     * Declines the care program.
     */
    private fun declineProgram() {
        dependencyProvider.resolve<LoadingEvents>().showLoadingIndicator()
        careProgramManager.leaveCareProgramAsync(invitationDetails.programId)
    }



    /**
     * Called when the call to Leave Program completes.
     * - Parameters:
     * - errorCode: This parameter contains an indication of success or failure.
     */
    @Subscribe()
    fun leaveProgramMessageCompleted(message: LeaveProgramMessage) {
        val logLevel = if (message.errorCode == CareProgramErrorCode.NO_ERROR) Logger.Level.INFO else Logger.Level.ERROR
        logger.log(level = logLevel, message = "leaveProgramMessageCompleted: errorCode = ${message.errorCode}")

        dependencyProvider.resolve<LoadingEvents>().hideLoadingIndicator()

        if (message.errorCode != CareProgramErrorCode.NO_ERROR){
            dependencyProvider.resolve<SystemAlertManager>().showAlert(id = DECLINE_FAIL, messageId = R.string.care_program_fail_message, titleId = R.string.care_program_removal_fail_title)
        } else {
            dependencyProvider.resolve<Events>().onConsentOrDecline()
        }
    }

    /**
     * Handler for the close button.
     */
    fun onClose() {
        onBackPressed()
    }

    /**
     * Events used to inform the main activity of screen events.
     */
    interface Events {
        /**
         * Indicates that the care program has been consented or declined.
         */
        fun onConsentOrDecline()
    }

    companion object {
        var ACCEPT_FAIL = "CareProgramAcceptanceFail"
        var DECLINE_FAIL = "CareProgramDeclineFail"
    }
}