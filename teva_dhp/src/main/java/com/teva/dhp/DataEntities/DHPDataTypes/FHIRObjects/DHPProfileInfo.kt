//
// DHPProfileInfo.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects
import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObject
import com.teva.dhp.DataEntities.DHPDataTypes.SyncedObject

data class DHPProfileInfo (
    @Transient override val dhpObjectName: String = "profile_info",
    var externalEntityID: String? = null,
    var emailID: String? = null,
    var username: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var gender: String? = null,
    var isAdult: String? = null,
    var role: DHPCodes.Role? = null,
    var relationshipStatus: String? = null,
    var addressState: String? = null,
    var addressZip: String? = null,
    var addressCountry: String? = null,
    var dateofBirth: String? = null,
    var serverTimeOffset: String? = null,
    override var messageID: String? = null,
    override var UUID: String? = null,
    override var appName: String? = null,
    override var appVersionNumber: String? = null,
    override var sourceTime_GMT: String? = null,
    override var sourceTime_TZ: String? = null,
    override var dataEntryClassification: DHPCodes.DataEntryClassification? = null,
    var objectName: String? = null
): FHIRObject, SyncedObject