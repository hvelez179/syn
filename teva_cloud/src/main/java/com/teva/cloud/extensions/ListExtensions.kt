//
// ArrayExtensions.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.extensions

import com.teva.dhp.DataEntities.DHPDataTypes.Resources.CommonTypes.Extension
import com.teva.dhp.DataEntities.DHPDataTypes.Resources.CommonTypes.ExtensionVersion
import com.teva.dhp.DataEntities.DHPDataTypes.Resources.CommonTypes.Observation

fun List<Extension>.getExtension(key: String, version: ExtensionVersion): Extension? {
    val fullKeyPath = "${Extension.extensionKeyPath}${version.version}/$key"
    return this.firstOrNull {it.url == fullKeyPath}
}

fun List<Observation>.getFirstExtension(identifierValue: String? = null, key: String, version: ExtensionVersion): Extension? {
    val filteredObservations = this.filter({ it.identifier?.first()?.value == (identifierValue ?: it.identifier?.first()?.value) })
    for (observation in filteredObservations) {
        val ext = observation.extension?.getExtension(key, version)
        if (ext != null) {
            return ext
        }
    }

    return null
}
