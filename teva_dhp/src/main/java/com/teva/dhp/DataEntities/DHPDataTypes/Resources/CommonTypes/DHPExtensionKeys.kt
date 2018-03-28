//
// DHPExtensionKeys.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.Resources.CommonTypes

class DHPExtensionKeys {
    companion object {

        val documentStatus = "documentStatus"
        val serverTimeOffset = "serverTimeOffset"
        val sourceTimeGMT = "sourceTime_GMT"
        val sourceTimeTZ = "sourceTime_TZ"
        val appName = "appName"
        val appVersionNumber = "appVersionNumber"

        // Medical Devices
        val drugIdentification = "DrugIdentification"
        val inhalerSerialNumber = "inhalerSerialNumber"
        val authenticationKey = "authenticationKey"
        val dateCode = "dateCode"
        val doseCount = "doseCount"
        val lastRecord = "lastRecord"
        val lastConnectionDate = "lastConnectionDate"
        val nickName = "nickName"
        val inhalerDeviceStatus = "inhalerDeviceStatus"

        // Invitation Details
        val invitationCode = "invitationCode"
        val roleOfAcceptor = "roleOfAcceptor"
        val invitationType = "invitationType"
        val invitationStatus = "invitationStatus"
        val expirationDateGMT = "expirationDate_GMT"
        val expirationDateTZ = "expirationDate_TZ"

        // Program Details
        val programId = "programid"
        val programApp = "programApp"
        val programAppName = "programApp/appName"
        val programAppVersion = "programApp/appVersion"
    }
}
