//
// SelectInhalerNameViewModel.java
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.device

import android.view.MenuItem

import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.model.InhalerRegistrationCommonState
import com.teva.respiratoryapp.models.ApplicationSettings
import com.teva.respiratoryapp.mvvmframework.ui.FragmentListViewModel
import com.teva.respiratoryapp.mvvmframework.ui.ListSelectionMode
import com.teva.respiratoryapp.mvvmframework.utils.ObservableHashSet
import com.teva.respiratoryapp.services.alert.SystemAlertManager
import com.teva.common.utilities.DataTask
import com.teva.utilities.services.DependencyProvider
import com.teva.devices.entities.Device
import com.teva.devices.enumerations.InhalerNameType
import java.util.*

/**
 * ViewModel for the Select Inhaler Name screen.
 *
 * @param dependencyProvider The dependency injection mechanism.
 */
class SelectInhalerNameViewModel(dependencyProvider: DependencyProvider)
    : FragmentListViewModel<InhalerNameItem>(dependencyProvider), InhalerRegistrationCommonState.Listener {

    override val selectedItemSet = ObservableHashSet<InhalerNameItem>()

    private val customNameItem: InhalerNameItem = InhalerNameItem(InhalerNameType.CUSTOM, "")
    private val commonState: InhalerRegistrationCommonState = dependencyProvider.resolve()
    override val items: List<InhalerNameItem>

    init {

        items = listOf(
                InhalerNameItem(InhalerNameType.HOME, getString(R.string.homeInhaler_text)),
                InhalerNameItem(InhalerNameType.SPORTS, getString(R.string.sportsInhaler_text)),
                InhalerNameItem(InhalerNameType.CARRY_WITH_ME, getString(R.string.travelInhaler_text)),
                InhalerNameItem(InhalerNameType.WORK, getString(R.string.workInhaler_text)),
                customNameItem)
    }

    /**
     * Method called by the BaseFragment when the fragment's onStart() lifecycle method is called.
     */
    override fun onStart() {
        super.onStart()

        commonState.addListener(this)
    }

    /**
     * Method called by the BaseFragment when the fragment's onStop() lifecycle method is called.
     */
    override fun onStop() {
        super.onStop()

        commonState.removeListener(this)
    }

    /**
     * Indicates that the device property has changed.
     */
    override fun onDeviceLoaded() {

        updateViewModel()
    }

    /**
     * Method called by the BaseFragment when the fragment's onResume() lifecycle method is called.
     */
    override fun onResume() {
        super.onResume()

        if (commonState.isDeviceLoaded) {
            updateViewModel()
        } else {
            selectedItemSet.clear()
        }
    }

    /**
     * Updates the selection and custom name properties of the viewmodel.
     */
    private fun updateViewModel() {
        val inhalerNameType = commonState.inhalerNameType

        if (inhalerNameType === InhalerNameType.CUSTOM) {
            customNameItem.name = commonState.customNickname ?: ""
        } else {
            customNameItem.name = commonState.customNickname ?: getString(R.string.otherInhaler_text)
        }

        if (inhalerNameType != null) {
            val item = items.first { obj -> obj.type === inhalerNameType }
            selectedItemSet.add(item)
        }
    }

    /**
     * Gets the selection mode to use for the list.
     */
    override val listSelectionModel: ListSelectionMode
        get() = ListSelectionMode.MANUAL

    override fun onItemClicked(item: InhalerNameItem) {
        if (item == customNameItem) {
            dependencyProvider.resolve<SelectInhalerNameEvents>().chooseCustomName()
        } else {
            commonState.inhalerNameType = item.type
            selectedItemSet.clear()
            selectedItemSet.add(item)

            // When the user selects a name, move to the next screen after a short  half-second delay.
            val timer = Timer()
            timer.schedule(object:TimerTask() {
                override fun run() {
                    next()
                    timer.cancel()
                    timer.purge()
                }
            }, 500)
        }
    }

    /**
     * Method called by the BaseFragment when a toolbar menu item is clicked.
     */
    override fun onMenuItem(item: MenuItem): Boolean {
        if (item.itemId == R.id.next) {
            next()
            return true
        }

        return false
    }

    /**
     * Handler for the next menu option.
     */
    operator fun next() {
        val task = object : SaveTask() {
            override fun onPostExecute(result: Device?) {
                super.onPostExecute(result)
                dependencyProvider.resolve<SelectInhalerNameEvents>().next()
            }
        }

        task.execute()
    }

    /**
     * Asynchronous task to save the device changes.
     */
    private open inner class SaveTask : DataTask<Unit, Device>("SelectInhalerNameViewModel_SaveTask") {

        /**
         * Called to perform the task on a worker thread.

         * @param params The task parameters
         * *
         * @return The result of the task.
         */
        override fun doInBackground(vararg params: Unit): Device? {
            return commonState.save()
        }

        /**
         * Called on the main thread after the task is executed.

         * @param result The result of the task.
         */
        override fun onPostExecute(result: Device?) {
            dependencyProvider.resolve<ApplicationSettings>().firstInhalerScanned = true

            // display the "Your inhaler is now registered" message if this is an add.
            if (commonState.mode === InhalerRegistrationCommonState.Mode.Add) {
                val message = String.format(
                        getString(R.string.inhalerRegistrationConfirmation_text),
                        result!!.nickname)

                val systemAlertManager = dependencyProvider.resolve<SystemAlertManager>()
                systemAlertManager.showAlert(message = message, title = null)

                //dependencyProvider.resolve<AnalyticsService>().event(AnalyticsEvent.)
            }
        }
    }

    /**
     * SelectInhalerNameEvents produced by the viewmodel to request actions by the activity.
     */
    interface SelectInhalerNameEvents {
        /**
         * Requests that the Custom Name screen be displayed.
         */
        fun chooseCustomName()

        /**
         * Requests that the next screen be displayed
         */
        operator fun next()
    }
}
