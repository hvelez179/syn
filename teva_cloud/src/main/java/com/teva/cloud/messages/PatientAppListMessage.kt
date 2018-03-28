//
// PatientAppListMessage.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.messages

import com.teva.cloud.services.programmanagement.CareProgramErrorCode
import com.teva.cloud.services.programmanagement.CareProgramErrorDetail
import com.teva.cloud.services.programmanagement.PatientAppInfo

//import com.teva.cloud.services.programmanagement.PatientAppInfo

class PatientAppListMessage {
    companion object {
        fun messageName() : String {
            return "PatientAppListMessage"
        }
    }

    val name: String
        get() {
            return PatientAppListMessage.messageName()
        }
    val info: Map<Any, Any>?
        get() {
            return null
        }
    var errorCode = CareProgramErrorCode.NO_ERROR
    var errorDetails: List<CareProgramErrorDetail>?
    var patientAppList: List<PatientAppInfo>

    constructor(errorCode: CareProgramErrorCode, errorDetails: List<CareProgramErrorDetail>?, patientAppList: List<PatientAppInfo>) {
        this.errorCode = errorCode
        this.errorDetails = errorDetails
        this.patientAppList = patientAppList
    }
    fun combineWith(message: Any) : Boolean {
        return false
    }
}
