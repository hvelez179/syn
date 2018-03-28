//
// DHPInvitation.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects
import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.DHPDataType
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObject
import com.teva.dhp.DataEntities.DHPDataTypes.SyncedObject

data class ConsentedAppDetail (
        @Transient override val dhpObjectName: String ="consentedAppDetail",
        var consentedAppName: String? = null,
        var consentedAppVersionNumber: String? = null
): DHPDataType

data class DHPInvitation(
    @Transient override val dhpObjectName: String = "invitation",
    var emailID: String? = null,
    var acceptorExternalEntityID: String? = null,
    var programID: String? = null,
    var invitationType: String? = null,
    var status: String? = null,
    var roleOfAcceptor: DHPCodes.RoleOfAcceptor? = null,
    var acceptedDate_GMT: String? = null,
    var acceptedDate_TZ: String? = null,
    var serverTimeOffset: String? = null,
    override var messageID: String? = null,
    override var UUID: String? = null,
    override var appName: String? = null,
    override var appVersionNumber: String? = null,
    override var sourceTime_GMT: String? = null,
    override var sourceTime_TZ: String? = null,
    override var dataEntryClassification: DHPCodes.DataEntryClassification? = null,
    var consentedAppDetails: List<ConsentedAppDetail>? = null
): FHIRObject, SyncedObject
