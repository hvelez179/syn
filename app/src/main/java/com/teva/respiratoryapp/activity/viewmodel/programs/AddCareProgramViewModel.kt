//
// AddCareProgramViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.programs

import android.databinding.Bindable
import android.os.Bundle
import com.teva.cloud.messages.InvitationDetailsMessage
import com.teva.cloud.models.programmanagement.CareProgramManager
import com.teva.cloud.models.programmanagement.InvitationDetails
import com.teva.cloud.services.programmanagement.CareProgramErrorCode
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.common.utilities.Messenger
import com.teva.common.utilities.isAlphaNumeric
import com.teva.respiratoryapp.BR
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.LoadingEvents
import com.teva.respiratoryapp.activity.viewmodel.SupportEvents
import com.teva.respiratoryapp.activity.viewmodel.TextEntryViewModel
import org.greenrobot.eventbus.Subscribe

/**
 * Viewmodel class for the Add Care Program screen
 */
class AddCareProgramViewModel(dependencyProvider: DependencyProvider)
    : TextEntryViewModel(dependencyProvider) {

    /**
     * The invitation code string.
     */
    @Bindable
    var invitationCode: String? = null
        set(value) {
            field = value
            validate()
            notifyPropertyChanged(BR.invitationCode)
            validateClipboardText()
        }

    /**
     * Indicates whether the invitation code is valid.
     */
    @get:Bindable
    @get:JvmName("getValid")
    var isValid: Boolean = false
        get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.valid)
        }

    /**
     * Error message for when an error has been returned.
     */
    @get:Bindable
    var errorMessage: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.errorMessage)
        }

    /**
     * The text from the clipboard.
     */
    @get:Bindable
    var clipboardText: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.clipboardText)
            validateClipboardText()
        }

    /**
     * Indicates if the clipboard text is a valid invitation code.
     */
    @get:Bindable
    @get:JvmName("getClipboardValid")
    var isClipboardValid: Boolean = false
        get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.clipboardValid)
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
    }


    /**
     * Method called by the BaseFragment when the fragment's onStop() lifecycle method is called.
     */
    override fun onStop() {
        super.onStop()
        dependencyProvider.resolve<Messenger>().unsubscribeToAll(this)
    }

    /**
     * Validates the clipboard text.
     */
    private fun validateClipboardText() {
        isClipboardValid =
                clipboardText?.let {it.isAlphaNumeric() && it.length == InvitationCodeLength && it != invitationCode} ?: false
    }

    /**
     * Validates the invitation code.
     */
    private fun validate() {
        errorMessage = null

        if (invitationCode.isNullOrEmpty()){
            isValid = false
        } else if(!invitationCode!!.isAlphaNumeric()){
            isValid = false
            errorMessage = localizationService.getString(R.string.addCareProgramInvitationCodeError_text)
        } else if (invitationCode!!.length == InvitationCodeLength){
            isValid = true
        }
    }

    /**
     * Called by the fragment when the keyboard's action button is pressed.
     */
    override fun onEditorActionButton() {
        onSignUp()
    }

    /**
     * Handler for the clipboard Add hyperlink.
     */
    fun onAddClipboard() {
        if (isClipboardValid) {
            invitationCode = clipboardText
        }
    }

    /**
     * Handler for the next menu option.
     */
    fun onSignUp() {
        if (isValid) {

            // Show loading indicator while retrieving program data
            dependencyProvider.resolve<LoadingEvents>().showLoadingIndicator()
            careProgramManager.getInvitationDetailsAsync(invitationCode!!)
        }
    }

    /**
     * Handler for InvitationDetailsMessage
     * When triggered, get the program details and pass them on
     * @param message: This parameter contains an InvitationDetailsMessage
     */
    @Subscribe()
    fun invitationDetailsMessageCompleted(message: InvitationDetailsMessage){
        val logLevel = if(message.errorCode == CareProgramErrorCode.NO_ERROR) Logger.Level.INFO else Logger.Level.ERROR

        val errorString = message.errorCode.toString(message.errorDetails)

        if (errorString != null) {
            logger.log(Logger.Level.ERROR, "An error occured while getting program details: ${errorString}")

            dependencyProvider.resolve<LoadingEvents>().hideLoadingIndicator()
            errorMessage = errorString
            return
        }

        val details = message.invitationDetails

        if (details == null) {
            logger.log(Logger.Level.ERROR, "An error occured while getting program details: Expected CareProgramManager programDetails to be set.")

            dependencyProvider.resolve<LoadingEvents>().hideLoadingIndicator()
            errorMessage = errorString
            return
        }
        logger.log(logLevel, "invitationDetailsMessageCompleted: errorCode = ${message.errorCode}, programDetails=[${details.programId}, ${details.programName}]")

        errorMessage = null

        dependencyProvider.resolve<Events>().onSignUp(details)
    }

    /**
     * Saves the current state of the fragment into a saved instance state.
     *
     * @param savedInstanceState The Bundle to save the state into.
     */
    override fun saveInstanceState(savedInstanceState: Bundle) {
        super.saveInstanceState(savedInstanceState)
        savedInstanceState.putString(InvitationCodeKey, invitationCode)
    }

    /**
     * Restores the current state of the fragment from a saved instance state.
     *
     * @param savedInstanceState The Bundle to restore the state from.
     */
    override fun restoreInstanceState(savedInstanceState: Bundle?) {
        super.restoreInstanceState(savedInstanceState)
        invitationCode = savedInstanceState?.getString(InvitationCodeKey)
    }

    /**
     * Event handler for the Contact Teva Support hyperlink.
     */
    fun onContactSupport() {
        dependencyProvider.resolve<SupportEvents>().onSupport()
    }

    /**
     * Events interface used to notify the main activity of screen events.
     */
    interface Events {
        /**
         * Indicates that the Sign Up button was pressed.
         */
        fun onSignUp(invitationDetails: InvitationDetails)
    }

    companion object {
        val InvitationCodeLength = 10

        val InvitationCodeKey = "InvitationCode"
        val ConsentDialogId = "ProgramConsentDialog"
    }

}