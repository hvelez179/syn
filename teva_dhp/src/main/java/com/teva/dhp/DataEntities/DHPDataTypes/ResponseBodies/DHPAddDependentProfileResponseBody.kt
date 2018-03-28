//
// DHPAddDependentProfileResponseBody.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies

class DHPAddDependentProfileResponseBody(json: String) : DHPResponseBody(json) {
    var externalEntityID: String?

    private data class DHPExternalEntityIDReturnObject(
        var externalEntityID: String?
    ): DHPReturnObject {
        override fun isValidObject() : Boolean {
            return externalEntityID != null
        }
    }

    init {
        val externalEntityIDs = decodeReturnObjects<DHPExternalEntityIDReturnObject>(Array<DHPExternalEntityIDReturnObject>::class.java)
        externalEntityID = externalEntityIDs.first().externalEntityID
    }
}