//
// DHPProgramResource.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.Resources.CommonTypes

import com.teva.dhp.DataEntities.DHPDataTypes.Resources.DHPResource

data class DHPProgramResource (
        @Transient override val dhpObjectName: String = "Group",
        var name: String? = null,
        override var resourceType: String? = null,
        override var meta: Meta? = null,
        override var id: String? = null,
        override var extension: List<Extension>?,
        var identifier: List<SystemValuePair>?
): DHPResource