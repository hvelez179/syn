//
// DHPMedicalDeviceResource.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.Resources

import com.teva.dhp.DataEntities.DHPDataTypes.Resources.CommonTypes.*

data class DHPMedicalDeviceResource (
        @Transient override val dhpObjectName: String = "Device",
        override var id: String? = null,
        override var resourceType: String? = null,
        override var meta: Meta? = null,
        override var extension: List<Extension>?,
        var identifier: List<SystemValuePair>?,
        var manufacturer: String?,
        var model: String?,
        var version: String?,
        var lotNumber: String?,
        var expiry: String?,
        var tevaInhalerObservation: List<Observation>?
): DHPResource {
    fun isValidObject() : Boolean {
        var ext = "${Extension.extensionKeyPath}${DHPExtensionKeys.documentStatus}/${ExtensionVersion.r1}"
        val documentStatus = extension?.first { it.url == ext }?.value
        return resourceType == dhpObjectName && (documentStatus == null || documentStatus == "Success")
    }
}
