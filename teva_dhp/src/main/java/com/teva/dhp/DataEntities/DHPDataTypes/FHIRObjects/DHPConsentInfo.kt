//
// DHPConsentInfo.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects
import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObject
import com.teva.dhp.DataEntities.DHPDataTypes.SyncedObject

data class DHPConsentInfo (
    @Transient override val dhpObjectName: String ="consent_info",
    var externalEntityID: String?  = null,
    var role: DHPCodes.Role? = null,
    var username: String?  = null,
    var programID: String?  = null,
    var termsAndConditions: String?  = null,
    var privacyNotice: String?  = null,
    var privacyNoticeReadIndicator: String?  = null,
    var consentStartDate: String?  = null,
    var consentEndDate: String?  = null,
    var electronicSignature: String?  = null,
    var status: String?  = null,
    var acceptIndicator: String?  = null,
    var historicalDataIndicator: String?  = null,
    var consentAuditableEventDate: String?  = null,
    var consentEnabledStatus: String?  = null,
    var serverTimeOffset: String?  = null,
    var consentType: DHPCodes.ConsentType? = null,
    override var messageID: String? = null,
    override var UUID: String? = null,
    override var appName: String? = null,
    override var appVersionNumber: String? = null,
    override var sourceTime_GMT: String? = null,
    override var sourceTime_TZ: String? = null,
    override var dataEntryClassification: DHPCodes.DataEntryClassification? = null
    ): FHIRObject, SyncedObject
