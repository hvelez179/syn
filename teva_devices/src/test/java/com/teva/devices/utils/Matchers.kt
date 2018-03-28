//
// Matchers.java
// teva_devices
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.devices.utils

import com.teva.devices.entities.Device
import com.teva.devices.entities.InhaleEvent
import com.teva.devices.service.AdvertisementFilter
import com.teva.devices.service.ConnectionInfo
import com.teva.devices.service.MedicationDispenserImpl
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import java.util.*

/**
 * This class is used in unit tests to compare various device service components.
 */

object Matchers {

    /**
     * Matcher for ConnectionInfo
     *
     * @param expectedConnectionInfo The connectionInfo to be matched against
     * @return An implementation of the Hamcrest Matcher interface for matching connectionInfo
     */
    fun matchesConnectionInfo(expectedConnectionInfo: ConnectionInfo): Matcher<ConnectionInfo> {
        return object : BaseMatcher<ConnectionInfo>() {
            override fun matches(o: Any): Boolean {
                val actualConnectionInfo = o as ConnectionInfo

                return expectedConnectionInfo.authenticationKey == actualConnectionInfo.authenticationKey &&
                        expectedConnectionInfo.serialNumber == actualConnectionInfo.serialNumber &&
                        expectedConnectionInfo.protocolType == actualConnectionInfo.protocolType &&
                        expectedConnectionInfo.lastRecordId == actualConnectionInfo.lastRecordId
            }

            override fun describeTo(description: Description) {
                description.appendText("ConnectionInfo fields should match")
            }
        }
    }

    /**
     * Matcher for ConnectionInfo
     * @param expectedConnectionInfoList The connectionInfo list to be matched against
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching connectionInfo
     */
    fun matchesConnectionInfoList(expectedConnectionInfoList: List<ConnectionInfo>?): Matcher<List<ConnectionInfo>> {
        return object : BaseMatcher<List<ConnectionInfo>>() {
            override fun matches(o: Any): Boolean {
                val actualConnectionInfoList = o as List<ConnectionInfo>?

                if (actualConnectionInfoList == null && expectedConnectionInfoList == null) {
                    return true
                } else if (actualConnectionInfoList == null || expectedConnectionInfoList == null) {
                    return false
                }

                var result = actualConnectionInfoList.size == expectedConnectionInfoList.size

                if (result) {
                    for (index in actualConnectionInfoList.indices) {
                        if (!matchesConnectionInfo(expectedConnectionInfoList[index]).matches(actualConnectionInfoList[index])) {
                            result = false
                            break
                        }
                    }
                }

                return result
            }

            override fun describeTo(description: Description) {
                description.appendText("ConnectionInfo fields should match")
            }
        }
    }

    /**
     * Matcher for AdvertisementFilter
     *
     * @param expectedAdvertisementFilter The AdvertisementFilter to be matched against
     * @return An implementation of the Hamcrest Matcher interface for matching AdvertisementFilter
     */
    fun matchesAdvertisementFilter(expectedAdvertisementFilter: AdvertisementFilter): Matcher<AdvertisementFilter> {
        return object : BaseMatcher<AdvertisementFilter>() {
            override fun matches(o: Any): Boolean {
                val actualAdvertisementFilter = o as AdvertisementFilter

                return expectedAdvertisementFilter.manufacturerId == actualAdvertisementFilter.manufacturerId &&
                        expectedAdvertisementFilter.name == actualAdvertisementFilter.name &&
                        expectedAdvertisementFilter.serviceUUID == actualAdvertisementFilter.serviceUUID &&
                        Arrays.equals(expectedAdvertisementFilter.manufacturerData, actualAdvertisementFilter.manufacturerData) &&
                        matchesConnectionInfo(expectedAdvertisementFilter.connectionInfo!!).matches(actualAdvertisementFilter.connectionInfo)
            }

            override fun describeTo(description: Description) {
                description.appendText("ConnectionInfo fields should match")
            }
        }
    }

    /**
     * Matcher for MedicationDispenser
     * @param expectedMedicationDispenser The MedicationDispenser to be matched against
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching MedicationDispenser
     */
    fun matchesMedicationDispenser(expectedMedicationDispenser: MedicationDispenserImpl): Matcher<MedicationDispenserImpl> {
        return object : BaseMatcher<MedicationDispenserImpl>() {
            override fun matches(o: Any): Boolean {
                val actualMedicationDispenser = o as MedicationDispenserImpl

                return matchesConnectionInfo(expectedMedicationDispenser.connectionInfo).matches(actualMedicationDispenser.connectionInfo) && expectedMedicationDispenser.bluetoothDevice == actualMedicationDispenser.bluetoothDevice
            }

            override fun describeTo(description: Description) {
                description.appendText("ConnectionInfo fields should match")
            }
        }
    }

    /**
     * Matcher for Device
     * @param expectedDevice The Device to be matched against
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching Device
     */
    fun matchesDevice(expectedDevice: Device): Matcher<Device> {
        return object : BaseMatcher<Device>() {
            override fun matches(obj: Any): Boolean {
                val actualDevice = obj as Device

                return expectedDevice.softwareRevision == actualDevice.softwareRevision &&
                        expectedDevice.serialNumber == actualDevice.serialNumber &&
                        expectedDevice.hardwareRevision == actualDevice.hardwareRevision &&
                        expectedDevice.doseCount == actualDevice.doseCount &&
                        expectedDevice.isActive == actualDevice.isActive &&
                        expectedDevice.remainingDoseCount == actualDevice.remainingDoseCount &&
                        expectedDevice.lastRecordId == actualDevice.lastRecordId &&
                        expectedDevice.lastConnection!!.toEpochMilli() == actualDevice.lastConnection!!.toEpochMilli() &&
                        expectedDevice.authenticationKey == actualDevice.authenticationKey &&
                        expectedDevice.manufacturerName == actualDevice.manufacturerName
            }

            override fun describeTo(description: Description) {
                description.appendText("Device fields should match")
            }
        }
    }

    /**
     * Matcher for Device list
     * @param expectedDeviceList The Device list to be matched against
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching Device list
     */
    fun matchesDeviceList(expectedDeviceList: List<Device>?): Matcher<List<Device>> {
        return object : BaseMatcher<List<Device>>() {
            override fun matches(obj: Any): Boolean {
                val actualDeviceList = obj as List<Device>?

                if (actualDeviceList == null && expectedDeviceList == null) {
                    return true
                } else if (actualDeviceList == null || expectedDeviceList == null) {
                    return false
                }

                var result = actualDeviceList.size == expectedDeviceList.size

                if (result) {
                    for (index in actualDeviceList.indices) {
                        if (!matchesDevice(expectedDeviceList[index]).matches(actualDeviceList[index])) {
                            result = false
                            break
                        }
                    }
                }

                return result
            }

            override fun describeTo(description: Description) {
                description.appendText("Device fields should match")
            }
        }
    }

    /**
     * Matcher for InhaleEvent
     * @param expectedInhaleEvent The InhaleEvent to be matched against
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching InhaleEvent
     */
    fun matchesInhaleEvent(expectedInhaleEvent: InhaleEvent): Matcher<InhaleEvent> {
        return object : BaseMatcher<InhaleEvent>() {
            override fun matches(obj: Any): Boolean {
                val actualInhaleEvent = obj as InhaleEvent

                return expectedInhaleEvent.drugUID == actualInhaleEvent.drugUID &&
                        expectedInhaleEvent.eventTime!!.toEpochMilli() == actualInhaleEvent.eventTime!!.toEpochMilli() &&
                        expectedInhaleEvent.eventUID == actualInhaleEvent.eventUID &&
                        expectedInhaleEvent.inhaleEventTime == actualInhaleEvent.inhaleEventTime &&
                        expectedInhaleEvent.inhaleDuration == actualInhaleEvent.inhaleDuration &&
                        expectedInhaleEvent.inhalePeak == actualInhaleEvent.inhalePeak &&
                        expectedInhaleEvent.inhaleTimeToPeak == actualInhaleEvent.inhaleTimeToPeak &&
                        expectedInhaleEvent.inhaleVolume == actualInhaleEvent.inhaleVolume &&
                        expectedInhaleEvent.closeTime == actualInhaleEvent.closeTime &&
                        expectedInhaleEvent.upperThresholdTime == actualInhaleEvent.upperThresholdTime &&
                        expectedInhaleEvent.upperThresholdDuration == actualInhaleEvent.upperThresholdDuration &&
                        expectedInhaleEvent.doseId == actualInhaleEvent.doseId
            }

            override fun describeTo(description: Description) {
                description.appendText("InhaleEvent fields should match")
            }
        }
    }

    /**
     * Matcher for InhaleEvent list
     * @param expectedInhaleEventList The InhaleEvent list to be matched against
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching InhaleEvent list
     */
    fun matchesInhaleEventList(expectedInhaleEventList: List<InhaleEvent>?): Matcher<List<InhaleEvent>> {
        return object : BaseMatcher<List<InhaleEvent>>() {
            override fun matches(obj: Any): Boolean {
                val actualInhaleEventList = obj as List<InhaleEvent>?

                if (actualInhaleEventList == null && expectedInhaleEventList == null) {
                    return true
                } else if (actualInhaleEventList == null || expectedInhaleEventList == null) {
                    return false
                }

                var result = actualInhaleEventList.size == expectedInhaleEventList.size

                if (result) {
                    for (index in actualInhaleEventList.indices) {
                        if (!matchesInhaleEvent(expectedInhaleEventList[index]).matches(actualInhaleEventList[index])) {
                            result = false
                            break
                        }
                    }
                }

                return result
            }

            override fun describeTo(description: Description) {
                description.appendText("InhaleEvent fields should match")
            }
        }
    }
}
