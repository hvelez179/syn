//
// DHPGetRolesForPatientResponseBody.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies

class DHPGetRolesForPatientResponseBody(json: String) : DHPResponseBody(json) {
    var role: String? = null

    private data class DHPRoleReturnObject(
        var role: String?
    ): DHPReturnObject {
        override fun isValidObject() : Boolean {
            return role != null
        }
    }

    init {
        val objects : List<DHPRoleReturnObject> = decodeReturnObjects<DHPRoleReturnObject>(Array<DHPRoleReturnObject>::class.java).toList()
        if (!objects.isEmpty()) {
            role = objects.first().role
        }
    }
}