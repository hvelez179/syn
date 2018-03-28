//
// DHPGetMedicalDevicesResponseBody.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies

import com.teva.dhp.DataEntities.DHPDataTypes.Resources.DHPMedicalDeviceResource

class DHPGetMedicalDevicesResponseBody(json: String) : DHPResponseBody(json) {

    var medicalDeviceResources: List<DHPMedicalDeviceResource> = ArrayList()


    private data class DHPMedicalDeviceResourceArray(var tevaInhaler: List<DHPMedicalDeviceResource>?) : DHPReturnObject {
        override fun isValidObject(): Boolean {
            return tevaInhaler != null
        }
    }

    init {
        val resourceArray = decodeReturnObjects<DHPMedicalDeviceResourceArray>(Array<DHPMedicalDeviceResourceArray>::class.java).filter({it.isValidObject()})
        medicalDeviceResources = resourceArray.first().tevaInhaler ?: ArrayList()
    }
}