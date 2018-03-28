//
// InvitationDetailsils.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.models.programmanagement

import com.teva.cloud.dataentities.CloudAppData
import com.teva.dhp.DataEntities.DHPDataTypes.Resources.CommonTypes.*
import com.teva.dhp.DataEntities.DHPDataTypes.Resources.DHPInvitationResource
import java.io.Serializable


/**
 * This class contains Program details.
 */
data class InvitationDetails(var programId: String = "",
                             var programName: String = "",
                             var invitationCode: String = "",
                             var programConsentText: String = "",
                             var programSupportedApps: List<CloudAppData> = listOf(),
                             var programSupportedUserApps: List<CloudAppData> = listOf(),
                             var invitationType: String = "",
                             var invitationStatus: String = "",
                             var expirationDateGMT: String = "",
                             var expirationDateTZ: String = ""
): Serializable {
    fun parseInvitationDetails(resource: DHPInvitationResource) {
        invitationType = resource.extension?.getExtension(DHPExtensionKeys.invitationType, ExtensionVersion.r1)?.value ?: ""
        invitationStatus = resource.extension?.getExtension(DHPExtensionKeys.invitationStatus, ExtensionVersion.r1)?.value ?: ""
        expirationDateGMT = resource.extension?.getExtension(DHPExtensionKeys.expirationDateGMT, ExtensionVersion.r1)?.value ?: ""
        expirationDateTZ = resource.extension?.getExtension(DHPExtensionKeys.expirationDateTZ, ExtensionVersion.r1)?.value ?: ""
        invitationCode = resource.identifier?.getSystemValuePair(DHPExtensionKeys.invitationCode, ExtensionVersion.r1)?.value ?: ""
    }

    fun parseInvitationProgramDetails(resource: DHPProgramResource) {
        programId = resource.identifier?.getSystemValuePair(DHPExtensionKeys.programId, ExtensionVersion.r1)?.value ?: ""
        programName = resource.name ?: ""
        var programSupportedAppsMutable = programSupportedApps.toMutableList()
        for (ext in resource.extension?.getExtension(DHPExtensionKeys.programApp, ExtensionVersion.r1)?.extension ?: listOf()) {
            val appName = ext.extension?.getExtension(DHPExtensionKeys.programAppName, ExtensionVersion.r1)?.value
            val appVersion = ext.extension?.getExtension(DHPExtensionKeys.programAppVersion, ExtensionVersion.r1)?.value
            if (appName != null && appVersion != null) {
                val programAppInfo = CloudAppData(appName, appVersion)
                programSupportedAppsMutable.add(programAppInfo)
            }
        }
        programSupportedApps = programSupportedAppsMutable.toList()
    }


}