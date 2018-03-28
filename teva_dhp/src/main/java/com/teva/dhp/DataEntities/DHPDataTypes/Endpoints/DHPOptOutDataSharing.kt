//
// DHPOptOutDataSharing.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.Endpoints
import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.DHPDataType
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects.DHPConsentInfo

data class DHPOptOutDataSharing(
    @Transient override val dhpObjectName: String ="opt-In-Out",
    var programId: String? = null,
    var invokingExternalEntityID: String? = null,
    var invokingRole: DHPCodes.Role? = null,
    var patientExternalEntityID: String? = null,
    var apiExecutionMode: DHPCodes.ApiExecutionMode? = null,
    var consent_info: DHPConsentInfo? = null
): DHPDataType