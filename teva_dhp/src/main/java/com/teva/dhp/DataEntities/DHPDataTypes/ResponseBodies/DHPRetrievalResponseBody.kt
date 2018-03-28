//
// DHPRetrievalResponseBody.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies

import com.google.gson.Gson
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects.*
import org.json.JSONArray
import org.json.JSONObject

class DHPRetrievalResponseBody(json: String) : DHPResponseBody(json) {
    var prescriptionMedicationOrders: List<DHPPrescriptionMedicationOrder>? = null
    var medicalDevices: List<DHPMedicalDeviceInfo>? = null
    var medicationAdministrations: List<DHPMedicationAdministration>? = null
    var questionnaireResponses: List<DHPQuestionnaireResponse>? = null
    var userPreferenceSettings: List<DHPUserPreferenceSettings>? = null
    var profiles: List<DHPProfileInfo>? = null
    var inhalerSynchTime_GMT: String?
    var nonInhalerSynchTime_GMT: String?
    var additionalDocumentsExist: String?
    var jsonObject: JSONObject

    var prescriptionJSONArray = JSONArray()
    var medicalDevicesJSONArray = JSONArray()
    var medicationAdministrationsJSONArray = JSONArray()
    var questionnaireResponsesJSONArray = JSONArray()
    var userPreferenceSettingsJSONArray = JSONArray()
    var profilesJSONArray = JSONArray()

    private fun decodeReturnObjects() {
        parseJSONToJSONArrays()
        val gson = Gson()
        prescriptionMedicationOrders = gson.fromJson(prescriptionJSONArray.toString(), (Array<DHPPrescriptionMedicationOrder>::class.java)).toList()
        medicalDevices = gson.fromJson(medicalDevicesJSONArray.toString(), (Array<DHPMedicalDeviceInfo>::class.java)).toList()
        medicationAdministrations = gson.fromJson(medicationAdministrationsJSONArray.toString(), (Array<DHPMedicationAdministration>::class.java)).toList()
        questionnaireResponses =  gson.fromJson(questionnaireResponsesJSONArray.toString(), (Array<DHPQuestionnaireResponse>::class.java)).toList()
        userPreferenceSettings = gson.fromJson(userPreferenceSettingsJSONArray.toString(), (Array<DHPUserPreferenceSettings>::class.java)).toList()
        profiles = gson.fromJson(profilesJSONArray.toString(), (Array<DHPProfileInfo>::class.java)).toList()
    }

    private fun parseJSONToJSONArrays() {
        val returnObjects = jsonObject.getJSONArray("returnObjects")
        for(i in 0 until returnObjects.length()) {
            try {
                val returnObject = returnObjects.getJSONObject(i)
                val objectName = returnObject.getString("objectName")

                when(objectName) {
                    "prescription_medication_order" -> prescriptionJSONArray.put(returnObject)
                    "medical_device_info" -> medicalDevicesJSONArray.put(returnObject)
                    "medication_administration" -> medicationAdministrationsJSONArray.put(returnObject)
                    "questionnaire_response" -> questionnaireResponsesJSONArray.put(returnObject)
                    "user_preference_settings" -> userPreferenceSettingsJSONArray.put(returnObject)
                    "profile_info" -> profilesJSONArray.put(returnObject)
                }
            } catch(exception: Exception) {
                // the entry was not a JSONObject or did not have an object name. do nothing.
            }
        }
    }

    init {
        jsonObject = JSONObject(json)
        inhalerSynchTime_GMT = jsonObject.getString("inhalerSynchTime_GMT")
        nonInhalerSynchTime_GMT = jsonObject.getString("nonInhalerSynchTime_GMT")
        additionalDocumentsExist = jsonObject.getString("additionalDocumentsExist")
        decodeReturnObjects()
    }
}