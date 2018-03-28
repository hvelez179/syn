//
// DHPMedicationAdministration.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects
import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObject
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPReturnObject
import com.teva.dhp.DataEntities.DHPDataTypes.SyncedObject

data class DHPMedicationAdministration (
        @Transient override val dhpObjectName: String =  "medication_administration",
    var eventID: String? = null,
    var deviceSerialNumber: String? = null,
    var medicationEventUID: String? = null,
    var medicationEventTime: String? = null,
    var medicationEventTime_TZ: String? = null,
    var medicationStartOffset: String? = null,
    var medicationStartOffsetUOM: String?  = "milliseconds",
    var medicationDuration: String? = null,
    var medicationDurationUOM: String? = "milliseconds",
    var medicationPeakFlow: String? = null,
    var medicationPeakFlowUOM: String? = "ml/minute",
    var medicationPeakOffset: String? = null,
    var medicationPeakOffsetUOM: String? = "milliseconds",
    var medicationVolume: String? = null,
    var medicationVolumeUOM: String? = "ml",
    var medicationEventDuration: String? = null,
    var medicationEventDurationUOM: String? = "seconds",
    var doseID: String? = null,
    var cartridgeID: String? = null,
    var drugID: String? = null,
    var upperThresholdOffset: String? = null,
    var upperThresholdOffsetUOM: String? = "milliseconds",
    var upperThresholdDuration: String? = null,
    var upperThresholdDurationUOM: String? = "milliseconds",
    var isInvalidMedication: String? = null,
    var status: String? = null,
    var objectName: String? = null,
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
): FHIRObject, DHPReturnObject, SyncedObject{
    override fun isValidObject() : Boolean {
        return objectName == dhpObjectName && (documentStatus == null || documentStatus == "Success")
    }
}
