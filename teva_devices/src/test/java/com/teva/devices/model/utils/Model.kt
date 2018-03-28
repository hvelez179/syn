//
// Model.java
// teva_devices
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.devices.model.utils

import com.teva.devices.entities.Device
import com.teva.devices.enumerations.InhalerNameType
import com.teva.medication.entities.Medication

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

/**
 * This class is a helper class for creating model objects to be used in testing.
 */
object Model {
    /**
     * This method creates a Device object from the passed parameters.
     *
     * @param isActive           - indicates if the device is active.
     * @param dateCode           - the date code.
     * @param doseCount          - the dose count.
     * @param expirationDate     - the expiration date.
     * @param hardwareRevision   - the hardware revision of the device.
     * @param softwareRevision   - the software revision of the device.
     * @param inhalerNameType    - the type of the inhaler name.
     * @param lastConnection     - the last connection information.
     * @param lotCode            - the lot code.
     * @param lastRecordId       - the last record Id.
     * @param manufacturerName   - the manufacturer name of the device.
     * @param nickname           - the device nickname.
     * @param remainingDoseCount - the remaining doses.
     * @param serialNumber       - the device serial number.
     * @param authenticationKey  - the authentication key.
     * @param hasChanged         - flag to indicate if the device has changed.
     * @param changedTime        - the changed time.
     * @param drugUID            - the drug UID.
     * @return - the device object created with the passed parameters.
     */
    fun Device(isActive: Boolean,
               dateCode: String,
               doseCount: Int,
               expirationDate: LocalDate,
               hardwareRevision: String,
               softwareRevision: String,
               inhalerNameType: InhalerNameType,
               lastConnection: Instant,
               lotCode: String,
               lastRecordId: Int,
               manufacturerName: String,
               nickname: String,
               remainingDoseCount: Int,
               serialNumber: String,
               authenticationKey: String,
               hasChanged: Boolean,
               changedTime: Instant,
               drugUID: String): Device {
        val model = Device()
        model.isActive = isActive
        model.dateCode = dateCode
        model.doseCount = doseCount
        model.expirationDate = expirationDate
        model.hardwareRevision = hardwareRevision
        model.softwareRevision = softwareRevision
        model.inhalerNameType = inhalerNameType
        model.lastConnection = lastConnection
        model.lotCode = lotCode
        model.lastRecordId = lastRecordId
        model.manufacturerName = manufacturerName
        model.nickname = nickname
        model.remainingDoseCount = remainingDoseCount
        model.serialNumber = serialNumber
        model.authenticationKey = authenticationKey
        model.changeTime = changedTime
        model.hasChanged = hasChanged

        val medication = Medication()
        medication.drugUID = drugUID
        medication.brandName = drugUID
        model.medication = medication

        return model
    }
}
