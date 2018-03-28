//
// DHPGetInvitationDetails.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.Endpoints

import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.DHPDataType
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects.DHPInvitationDetails

data class DHPGetInvitationDetails(
    @Transient override val dhpObjectName: String ="getInvitationDetails",
    var invokingExternalEntityID: String? = null,
    var invokingRole: DHPCodes.Role? = null,
    var invitationCode: String? = null,
    var apiExecutionMode: DHPCodes.ApiExecutionMode? = null,
    var patientExternalEntityID: String? = null,
    var invitationDetails: DHPInvitationDetails? = null
): DHPDataType
