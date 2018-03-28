//
// DHPGetServerTimeResponseBody.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies

class DHPGetServerTimeResponseBody(json: String) : DHPResponseBody(json) {
    var serverTimeGMT: String? = null

    private data class ServerTimeReturnObject(
        var serverTime_GMT: String?,
        var serverTime_TZ: String?
    ): DHPReturnObject {
        override fun isValidObject() : Boolean {
            return serverTime_GMT != null && serverTime_TZ != null
        }
    }

    init {
        val objects : List<ServerTimeReturnObject> = decodeReturnObjects<ServerTimeReturnObject>(Array<ServerTimeReturnObject>::class.java).toList()
        if (!objects.isEmpty()) {
            serverTimeGMT = objects.first().serverTime_GMT
        }
    }
}