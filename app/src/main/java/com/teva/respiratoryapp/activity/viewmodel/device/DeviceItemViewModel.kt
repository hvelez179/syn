//
// DeviceItemViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.device

import android.databinding.BaseObservable
import android.databinding.Bindable

import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.mvvmframework.ui.IItemViewModel
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.devices.entities.Device

import org.threeten.bp.temporal.ChronoUnit

import java.util.HashMap

/**
 * ViewModel class for items in the Device List view that wraps a Device model object.
 */
class DeviceItemViewModel(private val dependencyProvider: DependencyProvider) : BaseObservable(), IItemViewModel<Device> {

    private var device: Device? = null

    /**
     * Retrieves the nickname of the device.
     */
    val nickname: String?
        @Bindable
        get() {
            if (device != null) {
                return device!!.nickname
            }

            return null
        }

    /**
     * Returns a value indicating whether the device is connected.
     */
    val isConnected: Boolean
        @Bindable
        get() {
            if (device != null) {
                return device!!.isConnected
            }

            return false
        }

    /**
     * Returns a value indicating whether the device is near empty.
     */
    val isNearEmpty: Boolean
        @Bindable
        get() {
            if (device != null) {
                return device!!.isNearEmpty
            }

            return false
        }

    /**
     * Gets an enumeration describing the current state of the DeviceItemViewModel.
     */
    val state: DeviceItemState
        @Bindable
        get() {
            if (isNearEmpty) {
                return DeviceItemState.NEAR_EMPTY
            }

            if (isConnected) {
                return DeviceItemState.CONNECTED
            }

            return DeviceItemState.DISCONNECTED
        }

    /**
     * Retrieves the serial number of the device.
     */
    val serialNumber: String?
        @Bindable
        get() {
            if (device != null) {
                return device!!.serialNumber
            }

            return null
        }

    /**
     * Retrieves the status message for the device.
     */
    // never connected
    val statusMessage: String?
        @Bindable
        get() {
            var message: String? = null

            if (device != null) {
                val localizationService = dependencyProvider.resolve<LocalizationService>()

                if (device!!.isConnected) {
                    message = localizationService.getString(R.string.deviceStateConnected_text)
                } else {
                    if (device!!.dosesTaken == 0) {
                        message = localizationService.getString(R.string.deviceListStartTracking_text)
                    } else {
                        val lastConnectionTime = device!!.lastConnection
                        val now = dependencyProvider.resolve<TimeService>().now()

                        if (lastConnectionTime == null || lastConnectionTime.until(now, ChronoUnit.HOURS) < SEARCHING_MESSAGE_THRESHOLD) {
                            message = localizationService.getString(R.string.deviceStateSearching_text)
                        } else {
                            val params = HashMap<String, Any>()
                            val days = lastConnectionTime.until(now, ChronoUnit.DAYS)
                            params.put(DAYS_REPLACEMENT_KEY, days)

                            if (days == 1L) {
                                message = localizationService.getString(R.string.deviceStateDisconnected_one_day_text, params)
                            } else {
                                message = localizationService.getString(R.string.deviceStateDisconnected_text, params)
                            }
                        }
                    }
                }
            }

            return message
        }

    /**
     * Sets the Device model object for this ViewModel.
     */
    override fun setItem(item: Device) {
        this.device = item
        notifyChange()
    }

    /**
     * Click handler for the chevron in the device list.
     */
    fun onShowInfo() {
        dependencyProvider.resolve<DeviceListViewModel.DeviceListEvents>().showDeviceInfo(device!!)
    }

    companion object {
        private val SEARCHING_MESSAGE_THRESHOLD: Long = 24
        private val DAYS_REPLACEMENT_KEY = "Days"
    }
}
