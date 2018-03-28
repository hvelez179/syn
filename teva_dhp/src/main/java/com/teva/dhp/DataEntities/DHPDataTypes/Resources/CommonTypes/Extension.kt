//
// Extension.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.Resources.CommonTypes

import java.util.*

class Extension {
    companion object {
        val extensionKeyPath: String = "http://www.ibm.com/watsonhealth/fhir/extensions/whc-lsf/"
    }
    var url: String? = null
    var valueString: String? = null
    var valueDateTime: String? = null
    var valueCode: String? = null
    var valueReference: Reference? = null
    var extension: List<Extension>? = null
    val value: String?
        get() {
            if (!valueString.isNullOrEmpty()) return valueString
            if (!valueDateTime.isNullOrEmpty()) return valueDateTime
            if (!valueCode.isNullOrEmpty()) return valueCode
            if (valueReference != null) return valueReference!!.display

            return null
        }
    val dateValue: Date?
        get() {
            return null
        }
}

fun List<Extension>.getExtension(key: String, version: ExtensionVersion) : Extension? {
    val fullKeyPath = "${Extension.extensionKeyPath}${version.version}/${key}"
    return this.first { it.url == fullKeyPath }
}


