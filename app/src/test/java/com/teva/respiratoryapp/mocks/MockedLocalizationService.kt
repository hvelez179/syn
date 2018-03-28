///
// MockedLocalizationService.java
// app
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.respiratoryapp.mocks

import com.teva.common.utilities.LocalizationService

import java.util.HashMap

class MockedLocalizationService : LocalizationService {

    internal var map: MutableMap<Any, String> = HashMap()

    fun add(key: Any, value: String) {
        map.put(key, value)
    }

    /**
     * Gets a localized string resource for the specified string id.

     * @param stringId The resource id of the string.
     * *
     * @return The localized string resource for the specified string id.
     */
    override fun getString(stringId: Int): String {
        var str: String? = map[stringId]
        if (str == null) {
            str = "Localized String"
        }
        return str
    }

    /**
     * Gets a localized string resource for the specified string id name.

     * @param stringId The resource id name of the string.
     * *
     * @return The localized string resource for the specified string id.
     */
    override fun getString(stringId: String): String {
        var str: String? = map[stringId]
        if (str == null) {
            str = "Localized String"
        }
        return str
    }

    /**
     * Gets a localized string resource for the specified string id and replaces embedded
     * variables with data from a string replacement map.

     * @param stringId           The resource id of the string.
     * *
     * @param stringReplacements The map of string replacements.
     * *
     * @return The localized string resource for the specified string id.
     */
    override fun getString(stringId: Int, stringReplacements: Map<String, Any>?): String {
        var localized = getString(stringId)

        if (stringReplacements != null) {
            for ((key1, value1) in stringReplacements) {
                val key = "\\$$key1\\$"
                val value = value1.toString()
                localized = localized.replace(key.toRegex(), value)
            }
        }

        return localized
    }

    /**
     * Gets a localized string resource for the specified string id name and replaces embedded
     * variables with data from a string replacement map.

     * @param stringId           The resource id name of the string.
     * *
     * @param stringReplacements The map of string replacements.
     * *
     * @return The localized string resource for the specified string id.
     */
    override fun getString(stringId: String, stringReplacements: Map<String, Any>?): String {
        var localized = getString(stringId)

        if (stringReplacements != null) {
            for ((key1, value1) in stringReplacements) {
                val key = "\\$$key1\\$"
                val value = value1.toString()
                localized = localized.replace(key.toRegex(), value)
            }
        }

        return localized
    }
}
