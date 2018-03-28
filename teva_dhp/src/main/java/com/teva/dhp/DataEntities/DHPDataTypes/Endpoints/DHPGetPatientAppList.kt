//
// DHPGetPatientAppList.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.Endpoints

import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.DHPDataType
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects.DHPGetAppList

data class DHPGetPatientAppList(
    @Transient override val dhpObjectName: String ="getPatientMobileAppList",
    var invokingExternalEntityID: String? = null,
    var patientExternalEntityID: String? = null,
    var invokingRole: DHPCodes.Role? = null,
    var apiExecutionMode: DHPCodes.ApiExecutionMode? = null,
    var getAppList: DHPGetAppList? = null
): DHPDataType