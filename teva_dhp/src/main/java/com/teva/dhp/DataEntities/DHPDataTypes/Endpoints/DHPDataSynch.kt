//
// DHPDataSynch.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.Endpoints

import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.DHPDataType

data class DHPDataSynch(
    @Transient override val dhpObjectName: String ="DataSynch",
    var invokingExternalEntityID: String? = null,
    var invokingRole: DHPCodes.Role? = null,
    var patientExternalEntityID: String? = null,
    var patientStudyHashKey: String? = null,
    var apiExecutionMode: DHPCodes.ApiExecutionMode? = null,
    var objects: List<Any> = listOf()
): DHPDataType
