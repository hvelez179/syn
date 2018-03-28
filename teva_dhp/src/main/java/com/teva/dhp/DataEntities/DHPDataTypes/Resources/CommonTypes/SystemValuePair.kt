//
// SystemValuePair.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.Resources.CommonTypes

data class SystemValuePair(var system: String?, var value: String?)


fun List<SystemValuePair>.getSystemValuePair(key: String, version: ExtensionVersion) : SystemValuePair? {
    val fullKeyPath = "${Extension.extensionKeyPath}${version.version}/${key}"
    return this.first { it.system == fullKeyPath }
}