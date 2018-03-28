//
// DHPGetPatientPrescriptionsResponseBody.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies

import com.teva.dhp.DataEntities.DHPDataTypes.Resources.DHPPrescriptionMedicationOrderResource

class DHPGetPatientPrescriptionsResponseBody(json: String) : DHPResponseBody(json) {
    var prescriptionMedicationOrderResources: List<DHPPrescriptionMedicationOrderResource> = ArrayList()


    private data class DHPPrescriptionMedicationOrderResourceArray(var tevaPrescription: List<DHPPrescriptionMedicationOrderResource>?) : DHPReturnObject {

        override fun isValidObject(): Boolean {
            return tevaPrescription != null
        }
    }

    init {
        val resourceArray = decodeReturnObjects<DHPPrescriptionMedicationOrderResourceArray>(Array<DHPPrescriptionMedicationOrderResourceArray>::class.java).filter({it.isValidObject()})
        prescriptionMedicationOrderResources = resourceArray.first().tevaPrescription ?: ArrayList()
    }


}