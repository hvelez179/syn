//
// DHPSetting.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects
import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.DHPDataType
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObject
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPReturnObject
import com.teva.dhp.DataEntities.DHPDataTypes.SyncedObject

data class DHPSetting(
    @Transient override val dhpObjectName: String ="setting",
    var settingName: String? = null,
    var settingDataType: String? = null,
    var settingValue: String? = null,
    var settingStatus: String? = null,
    var settingDate: String? = null
): DHPDataType

data class DHPUserPreferenceSettings(
    var setting: List<DHPSetting>? = null,
    var objectName: String? = null,
    var serverTimeOffset: String? = null,
    @Transient override val dhpObjectName: String ="user_preference_settings",
    override var messageID: String? = null,
    override var UUID: String? = null,
    override var appName: String? = null,
    override var appVersionNumber: String? = null,
    override var sourceTime_GMT: String? = null,
    override var sourceTime_TZ: String? = null,
    override var dataEntryClassification: DHPCodes.DataEntryClassification? = null,
    var externalEntityID: String? = null,
    var documentStatus: String? = null
): FHIRObject, DHPReturnObject, SyncedObject {
    override fun isValidObject() : Boolean {
        return objectName == dhpObjectName && (documentStatus == null || documentStatus == "Success")
    }
}