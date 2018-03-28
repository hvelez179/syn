//
// DHPInvitationResource.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.Resources
import com.teva.dhp.DataEntities.DHPDataTypes.Resources.CommonTypes.Extension
import com.teva.dhp.DataEntities.DHPDataTypes.Resources.CommonTypes.Meta
import com.teva.dhp.DataEntities.DHPDataTypes.Resources.CommonTypes.SystemValuePair

data class DHPInvitationResource (
        @Transient override val dhpObjectName: String = "Communication",
        var status: String? = null,
        override var resourceType: String? = null,
        override var meta: Meta? = null,
        override var id: String? = null,
        override var extension: List<Extension>?,
        var identifier: List<SystemValuePair>?
): DHPResource
