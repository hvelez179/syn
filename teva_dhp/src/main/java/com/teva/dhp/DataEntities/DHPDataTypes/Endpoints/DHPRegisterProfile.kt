//
// DHPRegisterProfile.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.Endpoints

import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.DHPDataType
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects.DHPConsentInfo
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects.DHPMobileDeviceInfo
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects.DHPProfileInfo

data class DHPRegisterProfile(
    @Transient override val dhpObjectName: String = "registerProfile",
    var invokingExternalEntityID: String? = null,
    var invokingRole: DHPCodes.Role? = null,
    var apiExecutionMode: DHPCodes.ApiExecutionMode? = null,
    var patientPseudoName: String? = null,
    var patientStudyHashKey: String? = null,
    var guardian_info: DHPProfileInfo? = null,
    var profile_info: DHPProfileInfo? = null,
    var consent_info: DHPConsentInfo? = null,
    var mobile_device_info: DHPMobileDeviceInfo? = null
): DHPDataType