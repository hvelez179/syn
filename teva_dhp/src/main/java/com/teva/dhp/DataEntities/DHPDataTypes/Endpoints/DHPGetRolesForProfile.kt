//
// DHPGetRolesForProfile.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.Endpoints

import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.DHPDataType
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects.DHPGetRolesRequest

data class DHPGetRolesForProfile(
    @Transient override val dhpObjectName: String ="getRolesForProfile",
    var invokingExternalEntityID: String? = null,
    var apiExecutionMode: DHPCodes.ApiExecutionMode? = null,
    var patientPseudoName: String? = null,
    var patientStudyHashKey: String? = null,
    var getRoles: DHPGetRolesRequest? = null
): DHPDataType
