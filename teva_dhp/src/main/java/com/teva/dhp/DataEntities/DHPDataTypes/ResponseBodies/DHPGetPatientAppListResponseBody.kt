//
// DHPGetPatientAppListResponseBody.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies

class DHPGetPatientAppListResponseBody(json: String) : DHPResponseBody(json) {
    var patientApps: List<GetPatientAppListReturnObject>? = null

    init {
        patientApps = decodeReturnObjects<GetPatientAppListReturnObject>(Array<GetPatientAppListReturnObject>::class.java).toList()
    }

}