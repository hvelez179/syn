//
// DeviceInfoViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.device

import android.databinding.Bindable
import android.os.Handler
import android.view.MenuItem
import com.teva.common.utilities.DataTask
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.devices.entities.Device
import com.teva.devices.messages.UpdateDeviceMessage
import com.teva.devices.model.DeviceQuery
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.common.AppSystemMonitorActivity
import com.teva.respiratoryapp.common.messages.ModelUpdatedMessage
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel
import com.teva.respiratoryapp.services.alert.AlertButton
import com.teva.respiratoryapp.services.alert.SystemAlertManager
import org.greenrobot.eventbus.Subscribe
import java.util.*

/**
 * ViewModel for the Scan Device screen
 */
class DeviceInfoViewModel
/**
 * Constructor

 * @param dependencyProvider Dependency Injection object.
 */
(dependencyProvider: DependencyProvider, private val serialNumber: String, private val mode: DeviceInfoViewModel.Mode) : FragmentViewModel(dependencyProvider) {
    private val USED_DOSE_COUNT_FOR_UNCONNECTED_DEVICE = 0

    /**
     * Retrieves the nickname of the device being created or edited.
     */
    @get:Bindable
    var nickname: String? = null
        private set

    /**
     * Retrieves the medication name of the device being created or edited.
     */
    @get:Bindable
    var medicationName: String? = null
        private set

    /**
     * Retrieves the status message for the device.

     * @return
     */
    @get:Bindable
    var status: String? = null
        private set

    private var deleting: Boolean = false

    /**
     * Enumeration describing what action was occurring when this screen was displayed.
     */
    enum class Mode {
        FIRST_ADD,
        ADD,
        EDIT,
        DELETED,
        DELETED_LAST
    }

    /**
     * Method called by the BaseFragment when the fragment's onStart() lifecycle method is called.
     */
    override fun onStart() {
        super.onStart()

        updateDevice()
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
     * Message handler for the ModelUpdatedMessage.

     * @param message The message received.
     */
    @Subscribe
    fun onModelUpdated(message: ModelUpdatedMessage) {
        val shouldUpdate = message.objectsUpdated.any { it is Device && it.serialNumber == serialNumber }

        if (shouldUpdate) {
            updateDevice()
        }
    }

    /**
     * Message handler for the DayChangeMessage.
     * @param message The message received.
     */
    @Subscribe
    fun onUpdateDeviceMessage(message: UpdateDeviceMessage) {
        updateDevice()
    }

    /**
     * Asynchronously retrieves the device from the database and udpates the fields.
     */
    private fun updateDevice() {
        DataTask<String, Device>("DeviceInfo_updateTask")
                .inBackground { params ->
                    val serialNumber = params[0]
                    val query = dependencyProvider.resolve<DeviceQuery>()

                    return@inBackground query.get(serialNumber)
                }
                .onResult { device ->
                    nickname = device?.nickname ?: ""
                    medicationName = device?.medication?.brandName ?: ""
                    status = createStatusMessage(device)

                    notifyChange()
                }
                .execute(serialNumber)
    }

    /**
     * Method called by the BaseFragment when a toolbar menu item is clicked.
     */
    override fun onMenuItem(item: MenuItem): Boolean {
        if (!deleting && item.itemId == R.id.remove) {
            onRemoveInhalerClicked()
            return true
        }

        return false
    }

    /**
     * Called when the user clicks the Remove This Inhaler hyperlink.
     */
    fun onRemoveInhalerClicked() {
        val systemAlertManager = dependencyProvider.resolve<SystemAlertManager>()

        val messageFormat = getString(R.string.removeDeviceConfirmation_text)
        val message = String.format(messageFormat, nickname)
        systemAlertManager.showQuery(message = message) { alertButton ->
            if (alertButton == AlertButton.PRIMARY) {
                removeInhaler()
            }
        }
    }

    /**
     * Asynchronously removes the device and closes the screen.
     */
    private fun removeInhaler() {
        deleting = true
        dependencyProvider.resolve<Messenger>().post(AppSystemMonitorActivity.InhalerRemoved())
        DataTask<String, Boolean>("DeviceInfoViewModel_removeInhaler")
                .inBackground { params ->
                    val deviceQuery = dependencyProvider.resolve<DeviceQuery>()
                    val device = deviceQuery.get(params[0])
                    deviceQuery.markAsDeleted(device!!)

                    return@inBackground deviceQuery.hasActiveDevices()
                }
                .onResult { hasActiveInhalers ->
                    Handler().post({
                        val deleteMode = if (hasActiveInhalers ?: false) Mode.DELETED else Mode.DELETED_LAST
                        dependencyProvider.resolve<InfoEvents>().onDone(deleteMode)
                    })
                }
                .execute(serialNumber)
    }

    /**
     * Method called by the BaseFragment when the hardware back button is pressed.
     */
    override fun onBackPressed() {
        // Marking an inhaler as deleted is done in a worker thread and when the
        // thread is complete, it will navigate back to the My Inhalers screen.
        // This check protects against the user initiating any navigation during
        // the time that the inhaler is being removed in the worker thread.
        if (!deleting) {
            done()
        }
    }

    /**
     * Called when the user clicks the edit nickname icon.
     */
    fun onEditName() {
        // Marking an inhaler as deleted is done in a worker thread and when the
        // thread is complete, it will navigate back to the My Inhalers screen.
        // This check protects against the user initiating any navigation during
        // the time that the inhaler is being removed in the worker thread.
        if (!deleting) {
            dependencyProvider.resolve<InfoEvents>().onEditDeviceName(serialNumber)
        }
    }

    /**
     * Saves the device object into the database and notifies the parent activity.
     */
    private fun done() {
        dependencyProvider.resolve<InfoEvents>().onDone(mode)
    }

    /**
     * Retrieves the status message for the device.
     */
    private fun createStatusMessage(device: Device?): String? {
        var message: String? = null

        if (device != null) {
            if (device.isConnected) {
                message = getString(R.string.deviceStateConnected_text)
            } else if (device.dosesTaken == USED_DOSE_COUNT_FOR_UNCONNECTED_DEVICE) {
                // A used dose count of 0 indicates that the device was never connected
                // or never used. Display the message that tracking starts after first inhalation.
                message = getString(R.string.deviceListStartTracking_text)
            } else {
                val disconnectedDuration = device.disconnectedTimeSpan

                if (disconnectedDuration == null || disconnectedDuration.toHours() < SEARCHING_MESSAGE_THRESHOLD) {
                    message = getString(R.string.deviceStateSearching_text)
                } else {
                    val params = HashMap<String, Any>()
                    params.put(DAYS_REPLACEMENT_KEY, disconnectedDuration.toDays())
                    if (disconnectedDuration.toDays() == 1L) {
                        message = getString(R.string.deviceStateDisconnected_one_day_text, params)
                    } else {
                        message = getString(R.string.deviceStateDisconnected_text, params)
                    }
                }
            }
        }

        return message
    }

    /**
     * Events produced by the ViewModel to request actions by the Activity.
     */
    interface InfoEvents {
        /**
         * Requests the display of the Device Name screen.
         */
        fun onEditDeviceName(serialNumber: String)

        /**
         * Indicates the user is done viewing the device info.
         */
        fun onDone(mode: Mode)
    }

    companion object {
        private val SEARCHING_MESSAGE_THRESHOLD: Long = 24
        private val DAYS_REPLACEMENT_KEY = "Days"
    }
}
