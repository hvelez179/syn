//
// DHPGetInvitationDetailResponseBody.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies

import com.google.gson.Gson
import com.teva.dhp.DataEntities.DHPDataTypes.Resources.CommonTypes.DHPProgramResource
import com.teva.dhp.DataEntities.DHPDataTypes.Resources.DHPInvitationResource

class DHPGetInvitationDetailResponseBody(json: String) : DHPResponseBody(json) {

    var invitationResource: DHPInvitationResource? = null
    var programResource: DHPProgramResource? = null

    init {
        val gson = Gson()
        for(i in 0 until returnObjects!!.length()){
            val returnObject = returnObjects!!.getJSONObject(i)
            if (returnObject.has("invitation")){
                val jsonString = returnObject.getString("invitation")
                invitationResource = gson.fromJson(jsonString, DHPInvitationResource::class.java)
            } else if (returnObject.has("program")){
                val jsonString = returnObject.getString("program")
                programResource = gson.fromJson(jsonString, DHPProgramResource::class.java)
            }
        }
    }

}