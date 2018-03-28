//
// DHPRetrieval.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.Endpoints

import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.DHPDataType

data class DHPRetrieval(
    @Transient override val dhpObjectName: String = "getUserProgramAppList",
    var invokingExternalEntityID: String? = null,
    var invokingRole: DHPCodes.Role? = null,
    var patientStudyHashKey: String? = null,
    var patientExternalEntityID: String? = null,
    var apiExecutionMode: DHPCodes.ApiExecutionMode? = null,
    var retrievalType: String? = null,
    var inhalerSynchTime_GMT: String? = null,
    var nonInhalerSynchTime_GMT: String? = null,
    var username: String? = null,
    var messageID: String? = null,
    var UUID: String? = null,
    var appName: String? = null,
    var appVersionNumber: String? = null
): DHPDataType