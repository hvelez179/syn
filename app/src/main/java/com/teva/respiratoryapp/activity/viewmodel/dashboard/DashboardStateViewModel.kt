//
// DashboardStateViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.dashboard

import android.databinding.Bindable
import android.os.Bundle
import com.teva.analysis.messages.HistoryUpdatedMessage
import com.teva.analysis.model.HistoryCollator
import com.teva.analysis.model.HistoryDay
import com.teva.common.services.TimeService
import com.teva.common.utilities.DataTask
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.devices.model.DeviceQuery
import com.teva.respiratoryapp.BR
import com.teva.respiratoryapp.common.messages.ModelUpdatedMessage
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel
import org.greenrobot.eventbus.Subscribe
import org.threeten.bp.LocalDate

/**
 * Viewmodel containing the common state properties used by the dashboard and the popup background.
 *
 * @param dependencyProvider The dependency injection mechanism.
 */
class DashboardStateViewModel(dependencyProvider: DependencyProvider) : FragmentViewModel(dependencyProvider) {

    private val historyCollator: HistoryCollator = dependencyProvider.resolve()
    private val messenger: Messenger = dependencyProvider.resolve()
    private var inhalesToday: Int = 0
    private var eventsCritical: Boolean = false
    private var environmentCritical: Boolean = false
    private var devicesCritical: Boolean = false
    private var updateTask: UpdateTask? = null
    private var updatePending: Boolean = false

    /**
     * Gets the current day's inhalation count.
     */
    @Bindable
    fun getInhalesToday(): Int {
        return inhalesToday
    }

    /**
     * Sets the current day's inhalation count.
     */
    fun setInhalesToday(inhalesToday: Int) {
        this.inhalesToday = inhalesToday
        notifyPropertyChanged(BR.inhalesToday)
    }

    /**
     * Gets a value indicating whether the Inhale Events widget should be displayed with the critical appearance.
     */
    /**
     * Sets a value indicating whether the Inhale Events widget should be displayed with the critical appearance.
     */
    var isEventsCritical: Boolean
        @Bindable
        get() = eventsCritical
        set(eventsCritical) {
            this.eventsCritical = eventsCritical
            notifyPropertyChanged(BR.eventsCritical)
        }

    /**
     * Gets a value indicating whether the Environment widget should be displayed with the critical appearance.
     */
    /**
     * Sets a value indicating whether the Environment widget should be displayed with the critical appearance.
     */
    var isEnvironmentCritical: Boolean
        @Bindable
        get() = environmentCritical
        set(environmentCritical) {
            this.environmentCritical = environmentCritical
            notifyPropertyChanged(BR.environmentCritical)
        }

    /**
     * Gets a value indicating whether the My Inhalers widget should be displayed with the critical appearance.
     */
    /**
     * Sets a value indicating whether the My Inhalers widget should be displayed with the critical appearance.
     */
    var isDevicesCritical: Boolean
        @Bindable
        get() = devicesCritical
        set(devicesCritical) {
            this.devicesCritical = devicesCritical
            notifyPropertyChanged(BR.devicesCritical)
        }

    /**
     * Method called by the BaseFragment when the fragment's onStart() lifecycle method is called.
     */
    override fun onStart() {
        super.onStart()

        messenger.subscribe(this)
        update()

    }

    /**
     * Method called by the BaseFragment when the fragment's onStop() lifecycle method is called.
     */
    override fun onStop() {
        super.onStop()

        messenger.unsubscribeToAll(this)
    }

    /**
     * Saves the current state of the fragment into a saved instance state.

     * @param savedInstanceState The Bundle to save the state into.
     */
    override fun saveInstanceState(savedInstanceState: Bundle) {
        super.saveInstanceState(savedInstanceState)

        savedInstanceState.putInt(INHALES_TODAY_KEY, inhalesToday)
        savedInstanceState.putBoolean(EVENTS_CRITICAL_KEY, eventsCritical)
        savedInstanceState.putBoolean(ENVIRONMENT_CRITICAL_KEY, environmentCritical)
        savedInstanceState.putBoolean(DEVICES_CRITICAL_KEY, devicesCritical)
    }

    /**
     * Restores the current state of the fragment from a saved instance state.

     * @param savedInstanceState The Bundle to restore the state from.
     */
    override fun restoreInstanceState(savedInstanceState: Bundle?) {
        super.restoreInstanceState(savedInstanceState)

        inhalesToday = savedInstanceState!!.getInt(INHALES_TODAY_KEY, 0)
        eventsCritical = savedInstanceState.getBoolean(INHALES_TODAY_KEY, false)
        environmentCritical = savedInstanceState.getBoolean(INHALES_TODAY_KEY, false)
        devicesCritical = savedInstanceState.getBoolean(INHALES_TODAY_KEY, false)
    }

    /**
     * Message handler for the HistoryUpdatedMessage.
     * @param message The message received
     */
    @Subscribe
    fun onHistoryUpdated(message: HistoryUpdatedMessage) {
        update()
    }

    /**
     * Message handler for the ModelUpdatedMessage.
     * @param message The message received
     */
    @Subscribe
    fun onModelUpdated(message: ModelUpdatedMessage) {
        checkForEmptyInhalers()
    }

    /**
     * This method checks for empty inhalers and sets the devices critical flag.
     */
    private fun checkForEmptyInhalers() {
        DataTask<Unit, Boolean>("DashboardStateViewModel_CheckForEmptyInhalers")
                .inBackground {
                    val query = dependencyProvider.resolve<DeviceQuery>()
                    val devices = query.getAllActive()

                    val nearEmptyDevice = devices.asSequence().filter {device -> device.isEmpty or device.isNearEmpty}.firstOrNull()

                    return@inBackground nearEmptyDevice != null
                }
                .onResult { result ->
                    isDevicesCritical = result!!
                }
                .execute()
    }

    /**
     * Begins an asynchronous update of the viewmodel's data.
     */
    private fun update() {
        if (updateTask == null) {
            val today = dependencyProvider.resolve<TimeService>().today()

            updateTask = UpdateTask()
            updateTask!!.execute(today)
        } else {
            updatePending = true
        }
    }


    /**
     * Completes the update of the viewmodel's data with the information retrieved asynchronously.
     * @param day The HistoryDay object for today.
     */
    private fun completeUpdate(day: HistoryDay) {
        val inhales = day.relieverDoses.size + day.invalidDoses.size
        setInhalesToday(inhales)

        val prescriptionList = day.prescriptions
        if (prescriptionList.isNotEmpty()) {
            val medication = prescriptionList[0].medication

            isEventsCritical = inhales >= medication!!.overdoseInhalationCount
        } else {
            isEventsCritical = false
        }
    }

    /**
     * Task to retrieve the update data asynchronously.
     */
    /**
     * Constructor
     */
    private inner class UpdateTask : DataTask<LocalDate, HistoryDay>("DashboardStateViewModel_UpdateTask") {

        /**
         * Called to perform the task on a worker thread.

         * @param params The task parameters
         * *
         * @return The result of the task.
         */
        override fun doInBackground(vararg params: LocalDate): HistoryDay? {
            val today = params[0]

            val result = historyCollator.getHistory(today, today)

            var historyDay: HistoryDay? = null
            if (result.isNotEmpty()) {
                historyDay = result[0]
            }

            return historyDay
        }

        /**
         * Called on the main thread after the task is executed.

         * @param result The result of the task.
         */
        override fun onPostExecute(result: HistoryDay?) {
            val updateAgain = updatePending

            updatePending = false
            updateTask = null

            if (updateAgain) {
                update()
            }

            completeUpdate(result!!)
        }
    }

    companion object {
        private val INHALES_TODAY_KEY = "inhalesToday"
        private val EVENTS_CRITICAL_KEY = "eventsCritical"
        private val ENVIRONMENT_CRITICAL_KEY = "environmentCritical"
        private val DEVICES_CRITICAL_KEY = "devicesCritical"
    }
}
