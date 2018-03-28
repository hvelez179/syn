//
// DHPAcceptInvitation.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.Endpoints

import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.DHPDataType
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects.DHPConsentInfo
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects.DHPInvitation

data class DHPAcceptInvitation(
    @Transient override val dhpObjectName: String ="acceptInvitation",
    var invokingExternalEntityID: String? = null,
    var invokingRole: DHPCodes.Role? = null,
    var invitationCode: String? = null,
    var apiExecutionMode: DHPCodes.ApiExecutionMode? = null,
    var patientExternalEntityID: String? = null,
    var invitation: DHPInvitation? = null,
    var consent_info: DHPConsentInfo? = null
): DHPDataType
