//
// DHPPrescriptionMedicationOrderResource.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.Resources

import com.teva.dhp.DataEntities.DHPDataTypes.Resources.CommonTypes.*

data class DHPPrescriptionMedicationOrderResource(
        @Transient override val dhpObjectName: String = "MedicationOrder",
        override var id: String? = null,
        override var resourceType: String? = null,
        override var meta: Meta? = null,
        override var extension: List<Extension>?,
        var note: String?,
        var status: String?,
        var dateWritten: String?,
        var dateEnded: String?,
        var medicationReference: Reference?,
        var dosageInstruction: List<DosageInstruction>?

): DHPResource {
    fun isValidObject() : Boolean {
        var ext = "${Extension.extensionKeyPath}${DHPExtensionKeys.documentStatus}/${ExtensionVersion.r1}"
        val documentStatus = extension?.first { it.url == ext }?.value
        return resourceType == dhpObjectName && (documentStatus == null || documentStatus == "Success")
    }

    data class DosageInstruction(var doseQuantity: ValueWithUnit?, var rateRatio: ValueWithUnitRatio?, var maxDosePerPeriod: ValueWithUnitRatio?)
}