//
// DeviceExtensions.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.extensions

import com.teva.cloud.models.CloudSessionState
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.toInstant
import com.teva.devices.entities.Device
import com.teva.devices.enumerations.InhalerNameType
import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects.DHPMedicalDeviceInfo
import com.teva.dhp.DataEntities.DHPDataTypes.Resources.CommonTypes.DHPExtensionKeys
import com.teva.dhp.DataEntities.DHPDataTypes.Resources.CommonTypes.ExtensionVersion.r1
import com.teva.dhp.DataEntities.DHPDataTypes.Resources.DHPMedicalDeviceResource
import com.teva.medication.dataquery.MedicationDataQuery
import org.threeten.bp.LocalDate

/**
 * These extension methods provide conversion of a Device to/from JSON
 */

fun Device.toDHPType(): DHPMedicalDeviceInfo {

    val obj = DHPMedicalDeviceInfo ()

    obj.serialNumber = serialNumber.toStringOrUnknown()
    obj.authenticationKey = authenticationKey.toStringOrUnknown()
    obj.drugID = medication?.drugUID.toStringOrUnknown()
    obj.manufacturerName = manufacturerName.toStringOrUnknown()
    obj.hardwareRevision = hardwareRevision.toStringOrUnknown()
    obj.softwareRevision = softwareRevision.toStringOrUnknown()

    obj.deviceClassification = DHPCodes.DeviceClassification.smartInhaler.value
    obj.deviceType = DHPCodes.DeviceType.mdi.value
    obj.deviceTechnology = DHPCodes.DeviceTechnology.proAirDigihaler.value
    obj.deviceName = DHPCodes.DeviceName.proAirDigihaler.value

    obj.lotCode = lotCode.toStringOrUnknown()
    obj.dateCode = dateCode.toStringOrUnknown()
    if (expirationDate == null) {
        // TODO - Cloud requires an expiration date
        obj.expirationDate = LocalDate.now().plusYears(10).toInstant().toGMTString(false)
    } else {
        obj.expirationDate = expirationDate!!.toInstant().toGMTString(false)
    }

    obj.doseCount = doseCount.toString()
    obj.remainingDoseCount = remainingDoseCount.toString()
    obj.lastRecord = lastRecordId.toString()

    obj.lastConnectionDate = lastConnection?.toGMTString(false) ?: Device.minConnectionDateString
    obj.nickName = "${inhalerNameType.toString().toUpperCase()} (${nickname.toStringOrUnknown()})"

    obj.deviceStatus = if(isActive) "1" else "0"
    obj.objectName = obj.dhpObjectName
    obj.externalEntityID = CloudSessionState.shared.activeProfileID
    obj.sourceTime_GMT = changeTime?.toGMTString(false)
    obj.sourceTime_TZ = changeTime?.toGMTOffset()

    obj.serverTimeOffset = serverTimeOffset?.toServerTimeOffsetString()

    return obj
}

fun DHPMedicalDeviceInfo.fromDHPType(): Device? {

    val device = Device()

    val medicationDataQuery: MedicationDataQuery = DependencyProvider.default.resolve<MedicationDataQuery>()
    device.medication = medicationDataQuery.get(drugUID = this.drugID.fromStringOrUnknown()) ?: return null
    device.serialNumber = this.serialNumber.fromStringOrUnknown()
    device.authenticationKey = this.authenticationKey.fromStringOrUnknown()
    device.manufacturerName = this.manufacturerName.fromStringOrUnknown()
    device.hardwareRevision = this.hardwareRevision.fromStringOrUnknown()
    device.softwareRevision = this.softwareRevision.fromStringOrUnknown()
    device.lotCode = this.lotCode.fromStringOrUnknown()
    device.dateCode = this.dateCode.fromStringOrUnknown()
    device.expirationDate = localDateFromGMTString(this.expirationDate.fromStringOrUnknown())
    device.doseCount = (this.doseCount ?: "0").toInt()
    device.remainingDoseCount = (this.remainingDoseCount ?: "0").toInt()
    device.lastRecordId = (this.lastRecord ?: "0").toInt()
    device.lastConnection = instantFromGMTString(this.lastConnectionDate.fromStringOrUnknown())
    device.nickname = this.nickName.fromStringOrUnknown()
    this.nickName?.let { nickname ->
        val paranthesisIndex = nickname.indexOf("(", 0, true)

            if (paranthesisIndex != -1) {
            device.nickname = nickname.substring(paranthesisIndex + 1, nickname.length - 1)
            device.inhalerNameType = InhalerNameType.fromString(nickname.substring(0, paranthesisIndex))
        } else {

            device.nickname = nickname
        }
    }
    device.isActive = this.deviceStatus.fromStringOrUnknown() == "1"
    device.changeTime = instantFromGMTString(this.sourceTime_GMT.fromStringOrUnknown())
    device.serverTimeOffset = this.serverTimeOffset.fromServerTimeOffsetString()

    return device
}

fun DHPMedicalDeviceResource.fromDHPResource(): Device? {

    val device = DHPMedicalDeviceInfo()

    device.serialNumber = this.identifier?.first()?.value
    device.drugID = this.tevaInhalerObservation?.getFirstExtension(device.serialNumber, DHPExtensionKeys.drugIdentification, r1)?.value
    device.authenticationKey = this.`extension`?.getExtension(DHPExtensionKeys.authenticationKey, r1)?.value
    device.manufacturerName = this.manufacturer
    device.hardwareRevision = this.model
    device.softwareRevision = this.version
    device.lotCode = this.lotNumber
    device.dateCode = this.`extension`?.getExtension(DHPExtensionKeys.dateCode, r1)?.value
    device.expirationDate = this.expiry
    device.doseCount = this.tevaInhalerObservation?.getFirstExtension(device.serialNumber, DHPExtensionKeys.doseCount, r1)?.value
    device.remainingDoseCount = this.tevaInhalerObservation?.first { it.valueQuantity != null }?.valueQuantity?.value
    device.lastRecord = this.tevaInhalerObservation?.getFirstExtension(device.serialNumber, DHPExtensionKeys.lastRecord, r1)?.value
    device.lastConnectionDate = this.`extension`?.getExtension(DHPExtensionKeys.lastConnectionDate, r1)?.value
    device.nickName = this.tevaInhalerObservation?.getFirstExtension(device.serialNumber, DHPExtensionKeys.nickName, r1)?.value
    device.deviceStatus = this.`extension`?.getExtension(DHPExtensionKeys.inhalerDeviceStatus, r1)?.value

    device.serverTimeOffset = this.extension?.getExtension(DHPExtensionKeys.serverTimeOffset, r1)?.value
    device.sourceTime_GMT = this.extension?.getExtension(DHPExtensionKeys.sourceTimeGMT, r1)?.value
    device.sourceTime_TZ = this.extension?.getExtension(DHPExtensionKeys.sourceTimeTZ, r1)?.value

    return device.fromDHPType()
}

