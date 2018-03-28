//
// DHPTypes.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPResponseBody
import org.json.JSONObject
import kotlin.reflect.KClass

class DHPTypes {
    companion object {
        fun unwrap(any: Any) : Any {
            var gson = Gson()
            var json = gson.toJson(any)
            var jsonMap: Map<String, Any> = gson.fromJson(json, object : TypeToken<Map<String, Any>>() {}.type)
            return jsonMap
        }

        fun getJSON(fromData: DHPDataType) : Map<String, Any> {
            var gson = Gson()
            var json = gson.toJson(fromData)
            var jsonObj = JSONObject(json)
            json = jsonObj.toString()

            var jsonMap: Map<String, Any> = gson.fromJson(json, object : TypeToken<Map<String, Any>>() {}.type)
            return jsonMap
        }

        fun <T : DHPResponseBody> getResponseBody(json: String?, clas: KClass<T>?) : T? {
            if (json == null || clas == null) {
                return null
            }

            return try {
                var constructor = clas.constructors.first()

                constructor.call(json)
            } catch(exception: Exception) {
                null
            }
        }
    }
}
