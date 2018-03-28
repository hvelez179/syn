//
// DHPGetPatientPrescriptions.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.Endpoints

import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.DHPDataType
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects.DHPProgramMedicalListRequest

data class DHPGetPatientPrescriptions(
    @Transient override val dhpObjectName: String ="getPatientPrescriptionList",
    var invokingExternalEntityID: String? = null,
    var invokingRole: DHPCodes.Role? = null,
    var patientExternalEntityID: String? = null,
    var apiExecutionMode: DHPCodes.ApiExecutionMode? = null,
    var patientPseudoName: String? = null,
    var patientStudyHashKey: String? = null,
    var program_medical_list: DHPProgramMedicalListRequest? = null
): DHPDataType
