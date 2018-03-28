//
// DHPGetUserProgramAppList.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.Endpoints

import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.DHPDataType
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects.DHPGetProgramAppList

data class DHPGetUserProgramAppList(
    @Transient override val dhpObjectName: String = "getUserProgramAppList",
    var invokingExternalEntityID: String? = null,
    var invokingRole: DHPCodes.Role? = null,
    var apiExecutionMode: DHPCodes.ApiExecutionMode? = null,
    var patientExternalEntityID: String? = null,
    var getProgramAppList: DHPGetProgramAppList? = null
): DHPDataType
