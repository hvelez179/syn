//
// DHPGetUserProgramAppListResponseBody.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies

class DHPGetUserProgramAppListResponseBody(json: String) : DHPResponseBody(json) {
    var getUserProgramAppListReturnObject: GetUserProgramAppListReturnObject? = null

    init {
        val returnObjects = decodeReturnObjects<GetUserProgramAppListReturnObject>(Array<GetUserProgramAppListReturnObject>::class.java)
        getUserProgramAppListReturnObject = returnObjects.firstOrNull()
    }

}