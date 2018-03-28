//
// PrescriptionExtensions.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.extensions

import com.teva.cloud.models.CloudSessionState
import com.teva.utilities.services.DependencyProvider
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects.DHPPrescriptionMedicationOrder
import com.teva.dhp.DataEntities.DHPDataTypes.Resources.CommonTypes.DHPExtensionKeys
import com.teva.dhp.DataEntities.DHPDataTypes.Resources.CommonTypes.ExtensionVersion
import com.teva.dhp.DataEntities.DHPDataTypes.Resources.DHPPrescriptionMedicationOrderResource
import com.teva.medication.dataquery.MedicationDataQuery
import com.teva.medication.entities.Medication
import com.teva.medication.entities.Prescription

/**
 * These extension methods provide conversion of a Prescription to/from DHPType
 */

fun Prescription.toDHPType(): DHPPrescriptionMedicationOrder {

    val obj = DHPPrescriptionMedicationOrder()

    obj.drugID = medication?.drugUID
    obj.doses = dosesPerDay.toString()
    obj.dosesUOM = "unit"
    obj.doseQuantity = inhalesPerDose.toString()
    obj.doseQuantityUOM = "unit"
    obj.dateWritten = prescriptionDate?.toGMTString(false)
    obj.objectName = obj.dhpObjectName
    obj.externalEntityID = CloudSessionState.shared.activeProfileID
    obj.sourceTime_GMT = changeTime?.toGMTString(false)
    obj.sourceTime_TZ = changeTime?.toGMTOffset()

    return obj
}

fun DHPPrescriptionMedicationOrder.fromDHPType(): Prescription {

    val prescription = Prescription()

    val medicationDataQuery = DependencyProvider.default.resolve<MedicationDataQuery>()
    prescription.medication = medicationDataQuery.get(this.drugID.fromStringOrUnknown()) ?: Medication()
    prescription.dosesPerDay = (this.doses ?: "0").toInt()
    prescription.inhalesPerDose = (this.doseQuantity ?: "0").toInt()
    prescription.prescriptionDate = instantFromGMTString(this.dateWritten.fromStringOrUnknown())
    prescription.changeTime = instantFromGMTString(this.sourceTime_GMT.fromStringOrUnknown())
    prescription.serverTimeOffset = this.serverTimeOffset.fromServerTimeOffsetString()

    return prescription
}


fun DHPPrescriptionMedicationOrderResource.fromDHPResource(): Prescription? {

    val prescription = DHPPrescriptionMedicationOrder()

    prescription.drugID = this.medicationReference?.display

    prescription.doses = this.dosageInstruction?.first()?.rateRatio?.numerator?.value
    prescription.dosesUOM = this.dosageInstruction?.first()?.rateRatio?.numerator?.unit
    prescription.doseQuantity = this.dosageInstruction?.first()?.doseQuantity?.value
    prescription.doseQuantityUOM = this.dosageInstruction?.first()?.doseQuantity?.unit

    prescription.dateWritten = this.dateWritten

    prescription.serverTimeOffset = this.extension?.getExtension(DHPExtensionKeys.serverTimeOffset, ExtensionVersion.r1)?.value
    prescription.sourceTime_GMT = this.extension?.getExtension(DHPExtensionKeys.sourceTimeGMT, ExtensionVersion.r1)?.value
    prescription.sourceTime_TZ = this.extension?.getExtension(DHPExtensionKeys.sourceTimeTZ, ExtensionVersion.r1)?.value

    return prescription.fromDHPType()
}

