//
// FHIRObject.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes

interface FHIRObject: DHPDataType {
    var messageID: String?
    var UUID: String?
    var appName: String?
    var appVersionNumber: String?
    var sourceTime_GMT: String?
    var sourceTime_TZ: String?
    var dataEntryClassification: DHPCodes.DataEntryClassification?
}
