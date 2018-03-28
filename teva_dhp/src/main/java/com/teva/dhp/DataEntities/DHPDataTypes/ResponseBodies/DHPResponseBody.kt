//
// DHPResponseBody.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies
import com.google.gson.Gson
import com.teva.dhp.extensions.convertToList
import org.json.JSONArray
import org.json.JSONObject

open class DHPResponseBody (json:String){
    var responseCode: String? = null
    var responseMessage: String? = null
    var responseMessageCode: String? = null
    var messageID: String? = null
    var processingTime: String? = null
    var objectCount: String? = null
    var returnObjects: JSONArray? = null
    var errorDetails: List<String>? = null

    init {
        val jsonObject = JSONObject(json)
        responseCode = jsonObject.getString("responseCode")
        responseMessage = jsonObject.getString("responseMessage")
        responseMessageCode = jsonObject.getString("responseMessageCode")
        messageID = jsonObject.getString("messageID")
        processingTime = jsonObject.getString("processingTime")
        errorDetails = jsonObject.getJSONArray("errorDetails").convertToList()
        objectCount = jsonObject.getString("objectCount")
        returnObjects = jsonObject.getJSONArray("returnObjects")
    }


    internal fun <T> decodeReturnObjects(clazz: Class<Array<T>>) : Array<T> where T: DHPReturnObject {
        val gson = Gson()
        return gson.fromJson(returnObjects.toString(), clazz) as Array<T>
    }
}

data class GetPatientAppListReturnObject(
        var appName: String?,
        var appVersionNumber: String?
): DHPReturnObject {
    override fun isValidObject() : Boolean {
        return appName != null || appVersionNumber != null
    }
}

data class GetUserProgramAppListReturnObject(
        var programGroups: List<Group>?
): DHPReturnObject {
    override fun isValidObject() : Boolean {
        return programGroups != null
    }
}

data class AppInfo (
    var appName: String?,
    var appVersionNumber: String?,
    var consentAcceptDate: String?
)

data class Group (
    var program_id: String?,
    var programName: String?,
    var groupName: String?,
    var active: String?,
    var groupReference: String?,
    var invitationCode: String?,
    var AppList: List<AppInfo>?
)
