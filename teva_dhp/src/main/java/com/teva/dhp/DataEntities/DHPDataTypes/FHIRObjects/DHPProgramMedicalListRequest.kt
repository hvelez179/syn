//
// DHPProgramMedicalListRequest.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects
import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObject
import com.teva.dhp.DataEntities.DHPDataTypes.SyncedObject

data class DHPProgramMedicalListRequest (
    @Transient override val dhpObjectName: String = "program_medical_list",
    var externalEntityID: String? = null,
    var username: String? = null,
    var role: String? = null,
    var retrievalType: String? = null,
    override var messageID: String? = null,
    override var UUID: String? = null,
    override var appName: String? = null,
    override var appVersionNumber: String? = null,
    override var sourceTime_GMT: String? = null,
    override var sourceTime_TZ: String? = null,
    override var dataEntryClassification: DHPCodes.DataEntryClassification? = null
): FHIRObject, SyncedObject