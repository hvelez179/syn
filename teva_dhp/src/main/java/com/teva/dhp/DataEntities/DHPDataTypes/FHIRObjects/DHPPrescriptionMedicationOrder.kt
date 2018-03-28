//
// DHPPrescriptionMedicationOrder.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects
import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObject
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPReturnObject
import com.teva.dhp.DataEntities.DHPDataTypes.SyncedObject

data class DHPPrescriptionMedicationOrder (
    @Transient override val dhpObjectName: String = "prescription_medication_order",
    var drugID: String? = null,
    var doses: String? = null,
    var dosesUOM: String? = "unit",
    var doseQuantity: String? = null,
    var doseQuantityUOM: String? = "unit",
    var dateWritten: String? = null,
    var objectName: String? = null,
    var serverTimeOffset: String? = null,
    var documentStatus: String? = null,
    override var messageID: String? = null,
    override var UUID: String? = null,
    override var appName: String? = null,
    override var appVersionNumber: String? = null,
    override var sourceTime_GMT: String? = null,
    override var sourceTime_TZ: String? = null,
    override var dataEntryClassification: DHPCodes.DataEntryClassification? = null,
    var externalEntityID: String? = null
): FHIRObject, DHPReturnObject, SyncedObject{
    override fun isValidObject() : Boolean {
        return objectName == dhpObjectName && (documentStatus == null || documentStatus == "Success")
    }
}
