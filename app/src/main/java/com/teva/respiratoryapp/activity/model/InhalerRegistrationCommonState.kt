//
// InhalerRegistrationCommonState.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.model


import android.os.Bundle
import android.support.annotation.UiThread
import android.support.annotation.WorkerThread
import com.teva.common.services.TimeService
import com.teva.common.utilities.DataTask
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.common.utilities.Messenger
import com.teva.devices.dataquery.DeviceDataQuery
import com.teva.devices.entities.Device
import com.teva.devices.enumerations.InhalerNameType
import com.teva.devices.model.DeviceQuery
import com.teva.medication.dataquery.MedicationDataQuery
import com.teva.medication.dataquery.PrescriptionDataQuery
import com.teva.medication.entities.Prescription
import com.teva.medication.enumerations.MedicationClassification
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.common.AppSystemMonitorActivity
import java.util.*

/**
 * The common state data shared between the views of the Device activity.
 *
 * @param dependencyProvider The dependency injection object.
 */
@UiThread
class InhalerRegistrationCommonState(private val dependencyProvider: DependencyProvider) {

    enum class Mode {
        Add,
        Edit,
        Reactivate;
    }

    /**
     * The mode of the device state (add or edit).
     */
    var mode = Mode.Add


    /**
     * A value indicating whether this is an add operation with an empty device list.
     */
    var isFirstAdd: Boolean = false
        private set

    /**
     * A value indicating whether the device has been loaded from the database.
     */
    var isDeviceLoaded: Boolean = false
        private set

    private val listeners = ArrayList<Listener>()

    /**
     * The serial number
     */
    var serialNumber: String? = null

    /**
     * The authentication key
     */
    var authenticationKey: String? = null

    /**
     * The base64 encoded authentication key entered on the manual authentication code screen.
     */
    var base64AuthenticationKey: String? = null

    /**
     * The customNickname for the device.
     */
    var customNickname: String? = null

    /**
     * The inhaler name type
     */
    var inhalerNameType: InhalerNameType? = null

    var doseCount: Int? = null

    var remainingDoseCount: Int? = null

    /**
     * Resets the state into the Edit mode.
     *
     * @param deviceSerialNumber The serial number of a device to edit, or null to add new device.
     */
    fun initForEdit(deviceSerialNumber: String) {
        mode = Mode.Edit
        isFirstAdd = false
        serialNumber = deviceSerialNumber
        customNickname = null
        isDeviceLoaded = false
        loadDevice()

        raiseDeviceChanged()
    }

    /**
     * Resets the state into the Add mode.
     *
     * @param isFirstAdd A value indicating whether this is an add to an empty device list.
     */
    fun initForAdd(isFirstAdd: Boolean) {
        mode = Mode.Add
        this.isFirstAdd = isFirstAdd
        customNickname = null
        serialNumber = null
        authenticationKey = null
        base64AuthenticationKey = null
        inhalerNameType = null
        isDeviceLoaded = true

        raiseDeviceChanged()
    }


    /**
     * Reads the object state from a bundle.
     *
     * @param bundle The bundle to ready the object state from.
     */
    fun loadInstanceState(bundle: Bundle) {
        mode = Mode.values()[bundle.getInt(KEY_MODE)]
        isFirstAdd = bundle.getBoolean(KEY_FIRST_ADD)
        serialNumber = bundle.getString(KEY_SERIAL_NUMBER)
        authenticationKey = bundle.getString(KEY_AUTHENTICATION_KEY)
        inhalerNameType = bundle.getSerializable(KEY_INHALER_NAME_TYPE) as InhalerNameType?
        customNickname = bundle.getString(KEY_NICKNAME)
        isDeviceLoaded = bundle.getBoolean(KEY_DEVICE_LOADED)
    }

    /**
     * Saves the object state from a bundle.
     *
     * @param bundle The bundle to save the object state to.
     */
    fun saveInstanceState(bundle: Bundle) {
        bundle.putInt(KEY_MODE, mode.ordinal)
        bundle.putBoolean(KEY_FIRST_ADD, isFirstAdd)
        bundle.putString(KEY_SERIAL_NUMBER, serialNumber)
        bundle.putString(KEY_AUTHENTICATION_KEY, authenticationKey)
        bundle.putSerializable(KEY_INHALER_NAME_TYPE, inhalerNameType)
        bundle.putString(KEY_NICKNAME, customNickname)
        bundle.putBoolean(KEY_DEVICE_LOADED, isDeviceLoaded)
    }


    /**
     * Asynchronously retrieves a device for Edit mode.
     */
    private fun loadDevice() {
        val task = object : DataTask<Unit, Device>("DeviceCommonState_loadDevice") {
            override fun doInBackground(vararg params: Unit): Device? {
                val query = dependencyProvider.resolve<DeviceQuery>()

                return query.get(serialNumber!!)
            }

            override fun onPostExecute(result: Device?) {
                if (result != null) {
                    authenticationKey = result.authenticationKey
                    inhalerNameType = result.inhalerNameType

                    if (inhalerNameType === InhalerNameType.CUSTOM) {
                        customNickname = result.nickname
                    } else {
                        customNickname = null
                    }

                    isDeviceLoaded = true

                    raiseDeviceChanged()
                }
            }
        }

        task.execute()
    }

    /**
     * Adds a listener to the state object.
     *
     * @param listener
     */
    fun addListener(listener: Listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    /**
     * Removes a listener from the state object.
     *
     * @param listener
     */
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    /**
     * Raises the device changed event.
     */
    private fun raiseDeviceChanged() {
        for (listener in listeners) {
            listener.onDeviceLoaded()
        }
    }

    /**
     * Finds an available name for the device.
     *
     * @param device The device whose customNickname will be set.
     */
    @WorkerThread
    private fun updateNickname(device: Device) {
        if (inhalerNameType === InhalerNameType.CUSTOM) {
            device.nickname = customNickname ?: ""
        } else {
            val deviceQuery = dependencyProvider.resolve<DeviceDataQuery>()

            val nameList = deviceQuery.getAll()
                    .filter { serialNumber != it.serialNumber }
                    .map { it.nickname }
                    .toSet()

            val localizationService = dependencyProvider.resolve<LocalizationService>()

            val nameRoot: String
            when (inhalerNameType) {
                InhalerNameType.HOME -> nameRoot = localizationService.getString(R.string.homeInhaler_text)

                InhalerNameType.SPORTS -> nameRoot = localizationService.getString(R.string.sportsInhaler_text)

                InhalerNameType.CARRY_WITH_ME -> nameRoot = localizationService.getString(R.string.travelInhaler_text)

                else -> nameRoot = localizationService.getString(R.string.workInhaler_text)
            }

            var generatedNickname: String
            var index = 0
            do {
                index++

                if (index == 1) {
                    generatedNickname = nameRoot
                } else {
                    generatedNickname = nameRoot + Integer.toString(index)
                }
            } while (nameList.contains(generatedNickname))

            device.nickname = generatedNickname
        }
    }

    /**
     * Saves or creates the device object in the database.
     */
    @WorkerThread
    fun save(): Device {
        val device: Device

        val deviceQuery = dependencyProvider.resolve<DeviceDataQuery>()

        val prescriptionDataQuery = dependencyProvider.resolve<PrescriptionDataQuery>()

        when(mode) {
            Mode.Add -> {
                val timeService = dependencyProvider.resolve<TimeService>()

                device = Device()
                device.serialNumber = serialNumber!!
                device.authenticationKey = authenticationKey!!
                device.inhalerNameType = inhalerNameType!!
                device.doseCount = doseCount!!
                device.remainingDoseCount = remainingDoseCount!!
                updateNickname(device)

                val medicationDataQuery = dependencyProvider.resolve<MedicationDataQuery>()
                val medication = medicationDataQuery.getFirst(MedicationClassification.RELIEVER)
                device.medication = medication
                //device.doseCount = medication!!.initialDoseCount
                //device.remainingDoseCount = medication.initialDoseCount

                val prescriptions = medication?.prescriptions
                if (prescriptions!!.isEmpty()) {
                    val prescription = Prescription()
                    prescription.medication = medication
                    prescription.dosesPerDay = 0
                    prescription.inhalesPerDose = 0
                    prescription.prescriptionDate = timeService.now()
                    prescriptionDataQuery.insert(prescription, true)
                }
                dependencyProvider.resolve<Messenger>().post(AppSystemMonitorActivity.InhalerRegisteredViaQRCode(deviceQuery.getAllActive().size))
                deviceQuery.insert(device, true)
            }
            Mode.Edit -> {
                device = deviceQuery.get(serialNumber!!)!!

                device.inhalerNameType = inhalerNameType!!
                updateNickname(device)

                deviceQuery.update(device, true)
            }
            Mode.Reactivate -> {
                device = deviceQuery.get(serialNumber!!)!!

                device.serialNumber = serialNumber!!
                device.authenticationKey = authenticationKey!!

                deviceQuery.update(device, true)
            }
        }

        return device
    }

    /**
     * An interface to listen for changes to the state object.
     */
    interface Listener {
        /**
         * Indicates that the device property has changed.
         */
        fun onDeviceLoaded()
    }

    companion object {

        private val KEY_MODE = "mode"
        private val KEY_FIRST_ADD = "firstAdd"
        private val KEY_NICKNAME = "customName"
        private val KEY_SERIAL_NUMBER = "serialNumber"
        private val KEY_AUTHENTICATION_KEY = "authenticationKey"
        private val KEY_INHALER_NAME_TYPE = "inhalerNameType"
        private val KEY_DEVICE_LOADED = "isDeviceLoaded"
    }
}
