//
// SyncedObjectExtensions.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.extensions

import com.teva.cloud.models.CloudConstants
import com.teva.cloud.models.CloudSessionState
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObject
import com.teva.dhp.DataEntities.DHPDataTypes.SyncedObject


fun SyncedObject.addCommonAttributes(messageId: String, dataEntryClassification: DHPCodes.DataEntryClassification? = DHPCodes.DataEntryClassification.manual, withUUID: Boolean = true,  withSourceTime: Boolean = true) {

    (this as? FHIRObject)?.let { obj ->

        obj.messageID = messageId
        obj.appName = CloudSessionState.shared.appName
        obj.appVersionNumber = CloudSessionState.shared.appVersionNumber

        if(withUUID) {
            obj.UUID = CloudSessionState.shared.mobileUUID
        }

        obj.dataEntryClassification = dataEntryClassification

        if (withSourceTime) {

            val timeService = DependencyProvider.default.resolve<TimeService>()

            val now = timeService.now()
            obj.sourceTime_GMT = CloudSessionState.shared.dateFormatter.format(now)
            obj.sourceTime_TZ = now.plusSeconds(CloudConstants.sourceTimeOffset).toGMTOffset()
        }
    }
}
