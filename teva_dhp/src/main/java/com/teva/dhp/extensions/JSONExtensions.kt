//
// JSONExtensions.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.dhp.extensions

import org.json.JSONArray
import org.json.JSONObject

fun <T> JSONArray.convertToList() : List<T> {
    val list = ArrayList<T>()
    (0 until this.length()).mapTo(list) { this.get(it) as T }
    return list
}

fun JSONObject.convertToMap(excludeKeys: Array<String>): Map<String, Any> {
    val map = HashMap<String, Any>()
    for(key in this.keys()) {

        if(!excludeKeys.contains(key)) {
            val value = this[key]
            if (value is JSONObject) {
                map.put(key, value.convertToMap(excludeKeys))
            } else if (value is JSONArray) {
                map.put(key, value.convertToList<Any>())
            } else {
                map.put(key, value)
            }
        }
    }
    return map
}
