//
// MessageShadeViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel


import android.databinding.Bindable
import com.teva.cloud.messages.ServerTimeOffsetUpdatedMessage
import com.teva.common.messages.PermissionUpdateMessage
import com.teva.common.services.ServerTimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.devices.messages.BluetoothStateChangedMessage
import com.teva.devices.model.DeviceManager
import com.teva.location.models.LocationManager
import com.teva.location.services.LocationProvidersChangedMessage
import com.teva.respiratoryapp.BR
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.CheckBluetoothAndLocationEvents
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel
import org.greenrobot.eventbus.Subscribe

/**
 * Viewmodel for the message shade that displays enable bluetooth and location messages.
 *
 * @param dependencyProvider The dependency injection mechanism.
 */
class MessageShadeViewModel(dependencyProvider: DependencyProvider)
    : FragmentViewModel(dependencyProvider) {

    /**
     * A value indicating whether the shade should be open.
     */
    @get:Bindable
    var isOpen: Boolean = false
        set(open) {
            field = open
            notifyPropertyChanged(BR.open)
        }

    /**
     * The message to be displayed.
     */
    @get:Bindable
    var message: String? = null
        set(message) {
            field = message
            notifyPropertyChanged(BR.message)
        }

    private val deviceManager: DeviceManager = dependencyProvider.resolve()
    private val locationManager: LocationManager = dependencyProvider.resolve()
    private val messenger: Messenger = dependencyProvider.resolve()
    private val serverTimeService: ServerTimeService = dependencyProvider.resolve()

    /**
     * Called when the MessageShade is tapped.
     */
    fun onTapped() {
        dependencyProvider.resolve<CheckBluetoothAndLocationEvents>().checkBluetoothAndLocationStatus()
    }

    /**
     * Method called by the BaseFragment when the fragment's onStart() lifecycle method is called.
     */
    override fun onStart() {
        super.onStart()

        messenger.subscribe(this)

        updateMessage()
    }

    /**
     * Method called by the BaseFragment when the fragment's onStop() lifecycle method is called.
     */
    override fun onStop() {
        super.onStop()

        messenger.unsubscribeToAll(this)
    }

    /**
     * Message handler for the BluetoothStateChangedMessage.
     */
    @Subscribe
    fun onBluetoothStateChangedMessage(message: BluetoothStateChangedMessage) {
        updateMessage()
    }

    /**
     * Message handler for the LocationProvidersChangedMessage.
     */
    @Subscribe
    fun onLocationProvidersChangedMessage(message: LocationProvidersChangedMessage) {
        updateMessage()
    }

    /**
     * Message handler for the PermissionUpdateMessage.
     */
    @Subscribe
    fun onPermissionUpdatedMessage(message: PermissionUpdateMessage) {
        updateMessage()
    }

    /**
     * Message handler for the ServerTimeOffsetUpdatedMessage.
     */
    @Subscribe
    fun onServerTimeOffsetUpdatedMessage(message: ServerTimeOffsetUpdatedMessage) {
        updateMessage()
    }

    /**
     * Updates the message based on the state of the bluetooth and location services.
     */
    private fun updateMessage() {
        var open = false

        if (!deviceManager.isBluetoothEnabled) {
            message = getString(R.string.enableBluetooth_text)
            open = true
        } else if (!locationManager.isLocationServicesEnabled) {
            message = getString(R.string.enableLocationServices_text)
            open = true
        } else if(!serverTimeService.isServerTimeOffsetWithinAcceptableRange()) {
            message = getString(R.string.fixDeviceTimeSetting_text)
            open = true
        }

        isOpen = open
    }
}
