//
// DHPMedicalDeviceInfo.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects

import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObject
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPReturnObject
import com.teva.dhp.DataEntities.DHPDataTypes.SyncedObject

data class DHPMedicalDeviceInfo(
    @Transient override val dhpObjectName: String =  "medical_device_info",
    var serialNumber: String? = null,
    var authenticationKey: String? = null,
    var drugID: String? = null,
    var manufacturerName: String? = null,
    var hardwareRevision: String? = null,
    var softwareRevision: String? = null,
    var lotCode: String? = null,
    var dateCode: String? = null,
    var expirationDate: String? = null,
    var doseCount: String? = null,
    var remainingDoseCount: String? = null,
    var lastRecord: String? = null,
    var lastConnectionDate: String? = null,
    var nickName: String? = null,
    var deviceStatus: String? = null,
    var objectName: String? = null,
    var deviceClassification: String? = null,
    var deviceType: String? = null,
    var deviceTechnology: String? = null,
    var deviceName: String? = null,
    var serverTimeOffset: String? = null,
    override var messageID: String? = null,
    override var UUID: String? = null,
    override var appName: String? = null,
    override var appVersionNumber: String? = null,
    override var sourceTime_GMT: String? = null,
    override var sourceTime_TZ: String? = null,
    override var dataEntryClassification: DHPCodes.DataEntryClassification? = null,
    var externalEntityID: String? = null,
    var documentStatus: String? = null
): FHIRObject, DHPReturnObject, SyncedObject {
    override fun isValidObject(): Boolean {
        return objectName == dhpObjectName && (documentStatus == null || documentStatus == "Success")
    }
}
