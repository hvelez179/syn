//
// DeviceListViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.device

import android.databinding.Bindable
import com.teva.common.messages.SystemMonitorMessage
import com.teva.common.utilities.DataTask
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.devices.dataquery.DeviceDataQuery
import com.teva.devices.entities.Device
import com.teva.devices.messages.UpdateDeviceMessage
import com.teva.respiratoryapp.BR
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.common.AppSystemMonitorActivity
import com.teva.respiratoryapp.common.messages.ModelUpdatedMessage
import com.teva.respiratoryapp.mvvmframework.ui.FragmentListViewModel
import com.teva.respiratoryapp.mvvmframework.ui.ItemList
import com.teva.respiratoryapp.services.alert.SystemAlertManager
import org.greenrobot.eventbus.Subscribe
import java.util.*

/**
 * ViewModel for the My Inhalers screen.
 *
 * @param dependencyProvider Dependency Injection object.
 */
class DeviceListViewModel(dependencyProvider: DependencyProvider) : FragmentListViewModel<Device>(dependencyProvider) {

    private val deviceList: ItemList<Device, String>
    private var updating: Boolean = false
    private var updatePending: Boolean = false
    private val maximumAllowedDevices = 5

    var noActiveDevices: Boolean = false
        private set(newValue) {
            field = newValue
            notifyChange()
        }

    var deviceLimitReached: Boolean = false
        private set(newValue) {
            field = newValue
            notifyChange()
        }

    val devicesSupportedLabel: String = getString(R.string.deviceListMaxInhalersLabel_text)

    init {

        deviceList = object : ItemList<Device, String>() {
            override fun getItemId(item: Device): String {
                return item.serialNumber
            }

            override fun compareItems(item1: Device, item2: Device): Boolean {
                return item1.isConnected != item2.isConnected
            }
        }
    }

    /**
     * Method called by the BaseFragment when the fragment's onStart() lifecycle method is called.
     */
    override fun onStart() {
        super.onStart()

        dependencyProvider.resolve<Messenger>().subscribe(this)

        updateList()
    }

    @get:Bindable
    var deviceListStarted: Boolean = false
        set(value) {
            if (this.deviceListStarted != value) {
                field = value
                notifyPropertyChanged(BR.deviceListStarted)
            }
        }

    /**
     * Method called by the BaseFragment when the fragment's onStop() lifecycle method is called.
     */
    override fun onStop() {
        super.onStop()

        dependencyProvider.resolve<Messenger>().unsubscribeToAll(this)
    }

    /**
     * Handler for the ModelUpdatedMessage. Requests an update of the device list if devices have
     * changed.
     */
    @Subscribe
    fun onModelUpdatedMessage(message: ModelUpdatedMessage) {
        if (message.containsObjectsOfType(Device::class.java)) {
            updateList()
        }
    }

    @Subscribe
    fun onUpdateDeviceMessage(message: UpdateDeviceMessage) {
        notifyListChanged()
    }

    /**
     * Called when an item is clicked.

     * @param item The item that was clicked.
     */
    override fun onItemClicked(item: Device) {
        dependencyProvider.resolve<DeviceListEvents>().showDeviceInfo(item)
    }

    /**
     * Retrieves the list of inhaler devices.
     */
    override val items: List<Device>
        get() = deviceList

    /**
     * Handler for the "Add Inhaler" button.
     */
    fun addInhaler() {

        DataTask<Unit, Int>("DeviceListViewModel_addInhaler")
                .inBackground {
                    val deviceQuery = dependencyProvider.resolve<DeviceDataQuery>()
                    return@inBackground deviceQuery.getAllActive().size
                }
                .onResult { deviceCount ->
                    if (deviceCount?.compareTo(MAX_INHALERS) ?: 0 < 0) {
                        dependencyProvider.resolve<DeviceListEvents>().addInhaler(false)
                    } else {
                        dependencyProvider.resolve<Messenger>().post(SystemMonitorMessage(AppSystemMonitorActivity.Adding6thInhaler()))
                        val systemAlertManager = dependencyProvider.resolve<SystemAlertManager>()
                        systemAlertManager.showAlert(
                                getString(R.string.systemErrorMaxInhalersReachedContent_text),
                                getString(R.string.systemErrorMaxInhalersReachedTitle_text))
                    }
                }
                .execute()
    }

    /**
     * Updates the device list using a worker thread.
     */
    private fun updateList() {
        if (!updating) {
            updating = true
            updatePending = false

            DataTask<Unit, List<Device>>("DeviceListViewModel_UpdateList")
                    .inBackground {
                        dependencyProvider.resolve<DeviceDataQuery>().getAllActive()
                    }
                    .onResult { devices ->
                        Collections.sort(devices!!) { device1, device2 ->
                            val time1 = device1.lastConnection
                            val time2 = device2.lastConnection

                            when {
                                time1 == null && time2 == null -> 0
                                time1 == null -> -1
                                time2 == null -> 1
                                else -> time2.compareTo(time1)
                            }
                        }

                        deviceList.merge(devices)
                        deviceLimitReached = devices.size >= maximumAllowedDevices
                        noActiveDevices = devices.isEmpty()
                        updating = false

                        if (updatePending) {
                            // We received an update request while we were processing the last one,
                            // so update the list again.
                            updateList()
                        }
                    }
                    .execute()
        } else {
            updatePending = true
        }
    }

    /**
     * SelectInhalerNameEvents produced by the viewmodel to request actions by the activity.
     */
    interface DeviceListEvents {
        /**
         * Requests that the inhaler add UI be displayed.

         * @param isInhalerListEmpty True if the current inhaler list is empty.
         */
        fun addInhaler(isInhalerListEmpty: Boolean)

        /**
         * Requests that the Device Info screen be displayed.
         */
        fun showDeviceInfo(device: Device)
    }

    companion object {
        val MAX_INHALERS = 5
    }
}
