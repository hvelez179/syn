//
// LocalizationService.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.common.utilities

/**
 * Provides access to localized text
 */
interface LocalizationService {

    /**
     * Gets a localized string resource for the specified string id.
     *
     * @param stringId The resource id of the string.
     * @return The localized string resource for the specified string id.
     */
    fun getString(stringId: Int): String

    /**
     * Gets a localized string resource for the specified string id name.
     *
     * @param stringId The resource id name of the string.
     * @return The localized string resource for the specified string id.
     */
    fun getString(stringId: String): String

    /**
     * Gets a localized string resource for the specified string id and replaces embedded
     * variables with data from a string replacement map.
     *
     * @param stringId           The resource id of the string.
     * @param stringReplacements The map of string replacements.
     * @return The localized string resource for the specified string id.
     */
    fun getString(stringId: Int, stringReplacements: Map<String, Any>?): String

    /**
     * Gets a localized string resource for the specified string id name and replaces embedded
     * variables with data from a string replacement map.
     *
     * @param stringId           The resource id name of the string.
     * @param stringReplacements The map of string replacements.
     * @return The localized string resource for the specified string id.
     */
    fun getString(stringId: String, stringReplacements: Map<String, Any>?): String
}
