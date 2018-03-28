//
// LocalizationServiceImpl.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.common.utilities

import android.content.Context

/**
 * Provides access to localized text
 */
class LocalizationServiceImpl(private val context: Context) : LocalizationService {
    private val tradeName: String
    private val tradeNameWithCarriageReturn: String
    private val appName: String

    init {

        val tradeNameId = context.resources.getIdentifier(TRADE_NAME_STRING_ID, "string", context.packageName)
        tradeName = context.getString(tradeNameId)

        val tradeNameWithCarriageReturnId = context.resources.getIdentifier(TRADE_NAME_WITH_CARRIAGE_RETURN_STRING_ID, "string", context.packageName)
        tradeNameWithCarriageReturn = context.getString(tradeNameWithCarriageReturnId)

        val appNameId = context.resources.getIdentifier(APP_NAME_STRING_ID, "string", context.packageName)
        appName = context.getString(appNameId)
    }

    /**
     * Gets a localized string resource for the specified string id.
     *
     * @param stringId The resource id of the string.
     * @return The localized string resource for the specified string id.
     */
    override fun getString(stringId: Int): String {
        return getString(stringId, null)
    }

    /**
     * Gets a localized string resource for the specified string id name.
     *
     * @param stringId The resource id name of the string.
     * @return The localized string resource for the specified string id.
     */
    override fun getString(stringId: String): String {
        val id = context.resources.getIdentifier(stringId, "string", context.packageName)
        return getString(id, null)
    }

    /**
     * Gets a localized string resource for the specified string id and replaces embedded
     * variables with data from a string replacement map.
     *
     * @param stringId           The resource id of the string.
     * @param stringReplacements The map of string replacements.
     * @return The localized string resource for the specified string id.
     */
    override fun getString(stringId: Int, stringReplacements: Map<String, Any>?): String {
        if (stringId == 0) {
            return ""
        }

        var localized = context.getString(stringId)

        if (stringReplacements != null) {
            for ((key1, value1) in stringReplacements) {
                val key = "\\$$key1\\$"
                val value = value1.toString()
                localized = localized.replace(key.toRegex(), value)
            }
        }

        localized = localized.replace(TRADE_NAME_TAG.toRegex(), tradeName)
        localized = localized.replace(TRADE_NAME_WITH_CARRIAGE_RETURN_TAG.toRegex(), tradeNameWithCarriageReturn)
        localized = localized.replace(APP_NAME_TAG.toRegex(), appName)

        return localized
    }

    /**
     * Gets a localized string resource for the specified string id name and replaces embedded
     * variables with data from a string replacement map.
     *
     * @param stringId           The resource id name of the string.
     * @param stringReplacements The map of string replacements.
     * @return The localized string resource for the specified string id.
     */
    override fun getString(stringId: String, stringReplacements: Map<String, Any>?): String {
        val id = context.resources.getIdentifier(stringId, "string", context.packageName)
        return getString(id, stringReplacements)
    }

    companion object {
        private val TRADE_NAME_TAG = "\\\$TradeName\\$"
        private val TRADE_NAME_STRING_ID = "trade_name_text"

        private val TRADE_NAME_WITH_CARRIAGE_RETURN_TAG = "\\\$TradeNameWithCarriageReturn\\$"
        private val TRADE_NAME_WITH_CARRIAGE_RETURN_STRING_ID = "trade_name_multiline_text"

        private val APP_NAME_TAG = "\\\$AppName\\$"
        private val APP_NAME_STRING_ID = "app_name"
    }
}
