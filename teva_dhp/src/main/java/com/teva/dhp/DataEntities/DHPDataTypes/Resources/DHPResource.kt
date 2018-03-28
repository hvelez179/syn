//
// DHPResource.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.Resources

import com.teva.dhp.DataEntities.DHPDataTypes.DHPDataType
import com.teva.dhp.DataEntities.DHPDataTypes.Resources.CommonTypes.Extension
import com.teva.dhp.DataEntities.DHPDataTypes.Resources.CommonTypes.Meta

interface DHPResource: DHPDataType {
    var resourceType: String?
    var id: String?
    var meta: Meta?
    var extension: List<Extension>?
}
