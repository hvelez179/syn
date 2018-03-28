//
// DHPGetServerTime.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.Endpoints
import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObject

data class DHPGetServerTime(
    @Transient override val dhpObjectName: String ="getServerTime",
    var invokingExternalEntityID: String? = null,
    var invokingRole: DHPCodes.Role? = null,
    var patientExternalEntityID: String? = null,
    override var messageID: String? = null,
    override var appName: String? = null,
    override var appVersionNumber: String? = null,
    override var UUID: String? = null,
    override var sourceTime_GMT: String? = null,
    override var sourceTime_TZ: String? = null,
    override var dataEntryClassification: DHPCodes.DataEntryClassification? = null,
    var apiExecutionMode: DHPCodes.ApiExecutionMode? = null
): FHIRObject