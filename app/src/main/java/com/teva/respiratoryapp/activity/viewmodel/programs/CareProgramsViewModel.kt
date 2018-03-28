package com.teva.respiratoryapp.activity.viewmodel.programs

import android.databinding.Bindable
import com.teva.cloud.dataentities.ProgramData
import com.teva.cloud.dataentities.UserProfile
import com.teva.cloud.messages.LeaveProgramMessage
import com.teva.cloud.models.programmanagement.CareProgramManager
import com.teva.cloud.models.userprofile.UserProfileManager
import com.teva.cloud.services.programmanagement.CareProgramErrorCode
import com.teva.common.utilities.DataTask
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.common.utilities.Messenger
import com.teva.respiratoryapp.BR
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.LoadingEvents
import com.teva.respiratoryapp.common.messages.ModelUpdatedMessage
import com.teva.respiratoryapp.mvvmframework.ui.FragmentListViewModel
import com.teva.respiratoryapp.services.alert.AlertButton
import com.teva.respiratoryapp.services.alert.SystemAlertManager
import org.greenrobot.eventbus.Subscribe

/**
 * ViewModel for the Care Programs screen.
 */
class CareProgramsViewModel(dependencyProvider: DependencyProvider)
    : FragmentListViewModel<ProgramData>(dependencyProvider) {

    /**
     * Indicates whether the list of care carePrograms is empty.
     */
    @Bindable
    @get:Bindable
    @get:JvmName("getListEmpty")
    var isListEmpty: Boolean = false
        get() { return field }
        set(value) {
            field = value

            notifyPropertyChanged(BR.listEmpty)
        }

    /**
     * The list of care carePrograms to display.
     */
    override var items: List<ProgramData> = ArrayList()
        private set(value) {
            field = value
            listChangedListener?.onListChanged()
        }

    /**
     * CareProgramManager
     */
    var careProgramManager: CareProgramManager = dependencyProvider.resolve()
    var userProfileManager: UserProfileManager = dependencyProvider.resolve()

    lateinit var activeUserProfile: UserProfile

    /**
     * Handler for the add program button.
     */
    fun onAddProgram() {
        dependencyProvider.resolve<Events>().addProgram()
    }

    /**
     * Handler for the leave program button
     */
    fun onLeaveProgram(programData: ProgramData) {
        val message = localizationService!!.getString(
                R.string.leave_care_program_message,
                mapOf("ProgramName" to (programData.programName as Any)))

        dependencyProvider.resolve<SystemAlertManager>()
                .showAlert(titleId = R.string.leave_care_program_title,
                        message = message,
                        primaryButtonTextId = R.string.leave_program,
                        secondaryButtonTextId = R.string.cancel_text, onClickClose = { button ->
                    if (button == AlertButton.PRIMARY) {
                        leaveCareProgram(programData.programId!!)
                    }

                    true
                })
    }

    /**
     * Sends out request to leave program
     */
    fun leaveCareProgram(programId: String) {
        dependencyProvider.resolve<LoadingEvents>().showLoadingIndicator()
        careProgramManager.leaveCareProgramAsync(programId)
    }

    /**
     * Called when the call to Leave Program completes.
     * @param message: This parameter contains an LeaveProgramMessage
     */
    @Subscribe()
    fun leaveProgramMessageCompleted(message: LeaveProgramMessage) {
        val logLevel = if (message.errorCode == CareProgramErrorCode.NO_ERROR) Logger.Level.INFO else Logger.Level.ERROR
        logger.log(level = logLevel, message = "leaveProgramMessageCompleted: errorCode = ${message.errorCode}")

        dependencyProvider.resolve<LoadingEvents>().hideLoadingIndicator()

        if (message.errorCode != CareProgramErrorCode.NO_ERROR){
            dependencyProvider.resolve<SystemAlertManager>().showAlert(id = LEAVE_FAIL, messageId = R.string.care_program_fail_message, titleId = R.string.care_program_removal_fail_title)
        } else {
            updateList()
        }
    }

    /**
     * Method called by the BaseFragment when the fragment's onStart() lifecycle method is called.
     */
    override fun onStart() {
        super.onStart()

        dependencyProvider.resolve<Messenger>().subscribe(this)

        activeUserProfile = userProfileManager.getActive()!!

        dependencyProvider.resolve<LoadingEvents>().showLoadingIndicator()
        updateList()
    }

    /**
     * Method called by the BaseFragment when the fragment's onStop() lifecycle method is called.
     */
    override fun onStop() {
        super.onStop()

        dependencyProvider.resolve<Messenger>().unsubscribeToAll(this)
    }

    /**
     * Message handler for the ModelUpdatedMessage.
     *
     * @param message The message received.
     */
    @Subscribe
    fun onModelUpdated(message: ModelUpdatedMessage) {
        val shouldUpdate = message.objectsUpdated.any { it is ProgramData }

        if (shouldUpdate) {
            updateList()
        }
    }

    /**
     * Updates the list of care carePrograms
     */
    private fun updateList() {
        DataTask<Unit, List<ProgramData>>("CareProgramsViewModel")
                .inBackground {
                    careProgramManager.getCarePrograms(activeUserProfile)
                }
                .onResult { list ->
                    items = list ?: ArrayList()
                    isListEmpty = items.isEmpty()
                    listChangedListener?.onListChanged()

                    // Now that the list of carePrograms is loaded,
                    // clear the loading indicator if it's visible
                    dependencyProvider.resolve<LoadingEvents>().hideLoadingIndicator()
                }
                .execute()
    }

    /**
     * Events used to inform the main activity of screen events.
     */
    interface Events {
        /**
         * Indicates that the add program button was pressed.
         */
        fun addProgram()
    }

    companion object {
        var LEAVE_FAIL = "CareProgramLeaveFail"
    }
}